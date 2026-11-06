"""PaddleOCR boundary tests using fake inference (no model download or GPU)."""
import base64
import io
import logging
import sys
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import Mock

import pytest
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from model import paddleOCR as module
from model.errors import InvalidImageError, ImageTooLargeError, OCRError, OCRUnavailableError


def encoded_image(size=(24, 24), fmt='PNG'):
    buffer = io.BytesIO()
    Image.new('RGB', size, (255, 255, 255)).save(buffer, format=fmt)
    return base64.b64encode(buffer.getvalue()).decode()


@pytest.fixture
def engine(monkeypatch):
    fake = SimpleNamespace(predict=Mock(return_value=[{'rec_texts': ['Hello', 'World']}]))
    monkeypatch.setattr(module, '_get_ocr', lambda: fake)
    saved = Mock()
    monkeypatch.setattr(module, 'saveImages', saved)
    monkeypatch.delenv('OCR_SAVE_RESULTS_DIR', raising=False)
    return fake, saved


@pytest.mark.parametrize('payload', ['', 'bad%%%base64', 42, base64.b64encode(b'not-an-image').decode()])
def test_invalid_inputs(engine, payload):
    with pytest.raises(InvalidImageError):
        module.predict_image(payload)


def test_rejects_large_encoded_input(engine):
    with pytest.raises(ImageTooLargeError):
        module.predict_image('A' * (module.MAX_IMAGE_BYTES * 2))


def test_rejects_excessive_pixel_dimensions(engine):
    with pytest.raises(ImageTooLargeError):
        module.predict_image(encoded_image(size=(4000, 4000)))


def test_normal_result_and_no_default_artifacts(engine):
    fake, saved = engine
    result = module.predict_image(encoded_image())
    assert result == {'success': True, 'message': 'Hello World', 'error': ''}
    fake.predict.assert_called_once()
    saved.assert_not_called()


def test_no_text_detected(engine):
    fake, _ = engine
    fake.predict.return_value = [{'rec_texts': []}]
    result = module.predict_image(encoded_image())
    assert result['success'] is False
    assert result['error'] == "Couldn't find any text"


def test_inference_error_is_sanitized(engine, caplog):
    fake, _ = engine
    fake.predict.side_effect = RuntimeError('PRIVATE_PATH_AND_USER_DATA')
    with caplog.at_level(logging.ERROR, logger=module.logger.name):
        with pytest.raises(OCRError):
            module.predict_image(encoded_image())
    assert 'PRIVATE_PATH_AND_USER_DATA' not in caplog.text


def test_opt_in_storage_failure_does_not_disclose_data(engine, monkeypatch, caplog):
    _, saved = engine
    monkeypatch.setenv('OCR_SAVE_RESULTS_DIR', '/private/ocr-results')
    saved.side_effect = OSError('SECRET_STORAGE_PATH')
    with caplog.at_level(logging.ERROR, logger=module.logger.name):
        result = module.predict_image(encoded_image())
    assert result['success'] is True
    saved.assert_called_once()
    assert 'SECRET_STORAGE_PATH' not in caplog.text


def test_optional_storage_can_be_enabled(engine, monkeypatch):
    _, saved = engine
    monkeypatch.setenv('OCR_SAVE_RESULTS_DIR', '/tmp/ocr-output')
    module.predict_image(encoded_image())
    assert saved.call_args.args[3] == '/tmp/ocr-output'


def test_model_initialization_error_is_sanitized(monkeypatch, caplog):
    monkeypatch.setattr(module, '_ocr', None)
    fake_paddle = SimpleNamespace(PaddleOCR=Mock(side_effect=RuntimeError('TOKEN_OR_MODEL_PATH')))
    monkeypatch.setitem(sys.modules, 'paddleocr', fake_paddle)
    with caplog.at_level(logging.ERROR, logger=module.logger.name):
        with pytest.raises(OCRUnavailableError):
            module._get_ocr()
    assert 'TOKEN_OR_MODEL_PATH' not in caplog.text
