"""Contract tests for the Flask API without loading heavyweight OCR models.

The OCR predictor and ngrok module are replaced with local fakes so these tests
verify request/response behavior and sensitive logging, not OCR inference.
"""
import importlib.util
import logging
import sys
import types
from pathlib import Path
from unittest.mock import Mock

import pytest


APP_PATH = Path(__file__).resolve().parents[1] / "server" / "app.py"


@pytest.fixture
def backend(monkeypatch):
    predictor = Mock(return_value={"success": True, "message": "DEMO", "error": ""})

    model_stub = types.ModuleType("model")
    model_stub.get_predictor = Mock(return_value=predictor)
    monkeypatch.setitem(sys.modules, "model", model_stub)

    ngrok_stub = types.ModuleType("pyngrok")
    ngrok_stub.ngrok = Mock()
    monkeypatch.setitem(sys.modules, "pyngrok", ngrok_stub)

    spec = importlib.util.spec_from_file_location("flask_api_under_test", APP_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    module.app.config["TESTING"] = True
    return module.app.test_client(), predictor, module.app


@pytest.mark.parametrize(
    "body,content_type",
    [
        ("not-json", "application/json"),
        ("[]", "application/json"),
        ("null", "application/json"),
        ('{"image_base64": "x"}', "text/plain"),
    ],
)
def test_invalid_json_payload_returns_400(backend, body, content_type):
    client, predictor, _ = backend
    response = client.post("/predict_image", data=body, content_type=content_type)
    assert response.status_code == 400
    assert response.get_json()["success"] is False
    predictor.assert_not_called()


@pytest.mark.parametrize("payload", [{}, {"image_base64": ""}, {"image_base64": None}])
def test_missing_image_returns_400(backend, payload):
    client, predictor, _ = backend
    response = client.post("/predict_image", json=payload)
    assert response.status_code == 400
    assert response.get_json()["error"] == "No image data provided"
    predictor.assert_not_called()


def test_successful_prediction_returns_200(backend):
    client, predictor, _ = backend
    response = client.post("/predict_image", json={"image_base64": "base64-placeholder"})
    assert response.status_code == 200
    assert response.get_json() == {"success": True, "message": "DEMO", "error": ""}
    predictor.assert_called_once_with("base64-placeholder")


def test_predictor_failure_returns_400(backend):
    client, predictor, _ = backend
    predictor.return_value = {
        "success": False, "message": "", "error": "No text detected"
    }
    response = client.post("/predict_image", json={"image_base64": "base64-placeholder"})
    assert response.status_code == 400
    assert response.get_json()["error"] == "No text detected"


def test_sensitive_ocr_text_and_image_are_not_logged(backend, caplog):
    client, predictor, app = backend
    sensitive_text = "PRIVATE_OCR_RESULT_DO_NOT_LOG"
    sensitive_image = "PRIVATE_BASE64_IMAGE_DO_NOT_LOG"
    predictor.return_value = {
        "success": True, "message": sensitive_text, "error": ""
    }
    with caplog.at_level(logging.INFO, logger=app.logger.name):
        response = client.post("/predict_image", json={"image_base64": sensitive_image})
    assert response.status_code == 200
    assert sensitive_text not in caplog.text
    assert sensitive_image not in caplog.text
    assert "OCR request completed: success=True" in caplog.text
