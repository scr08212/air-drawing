"""PaddleOCR image inference with validated inputs and opt-in local artifacts."""
import base64
import binascii
import io
import logging
import os
import threading

import cv2
import numpy as np
from PIL import Image, UnidentifiedImageError

from .errors import InvalidImageError, ImageTooLargeError, OCRError, OCRUnavailableError
from .utils import preprocess, saveImages

logger = logging.getLogger(__name__)
MAX_IMAGE_BYTES = 5 * 1024 * 1024
MAX_IMAGE_PIXELS = 12_000_000
_ocr = None
_ocr_lock = threading.Lock()


def _get_ocr():
    global _ocr
    if _ocr is None:
        with _ocr_lock:
            if _ocr is None:
                try:
                    from paddleocr import PaddleOCR
                    _ocr = PaddleOCR(
                        lang='en',
                        device=os.getenv('OCR_DEVICE', 'cpu'),
                        use_doc_orientation_classify=False,
                        use_doc_unwarping=False,
                        use_textline_orientation=False,
                    )
                except Exception:
                    logger.error("OCR model initialization failed")
                    raise OCRUnavailableError() from None
    return _ocr


def _decode_image(image_b64):
    if not isinstance(image_b64, str) or not image_b64:
        raise InvalidImageError()
    # A bounded encoded payload prevents unexpectedly large allocations.
    if len(image_b64) > ((MAX_IMAGE_BYTES + 2) // 3 * 4 + 4):
        raise ImageTooLargeError()
    try:
        payload = base64.b64decode(image_b64, validate=True)
    except (ValueError, binascii.Error):
        raise InvalidImageError() from None
    if not payload:
        raise InvalidImageError()
    if len(payload) > MAX_IMAGE_BYTES:
        raise ImageTooLargeError()
    try:
        with Image.open(io.BytesIO(payload)) as candidate:
            if candidate.format not in {'PNG', 'JPEG', 'WEBP'}:
                raise InvalidImageError()
            if candidate.width * candidate.height > MAX_IMAGE_PIXELS:
                raise ImageTooLargeError()
            candidate.verify()
        with Image.open(io.BytesIO(payload)) as candidate:
            return candidate.convert('RGB')
    except (UnidentifiedImageError, OSError, ValueError, SyntaxError, Image.DecompressionBombError):
        raise InvalidImageError() from None


def predict_image(image_b64):
    raw_image = _decode_image(image_b64)
    try:
        preprocessed_image = preprocess(raw_image)
        np_image = cv2.cvtColor(np.asarray(preprocessed_image), cv2.COLOR_RGB2BGR)
        predictions = _get_ocr().predict(np_image)
        if not predictions:
            return {'success': False, 'message': '', 'error': "Couldn't find any text"}
        texts = predictions[0].get('rec_texts', [])
        if not isinstance(texts, list):
            raise OCRError()
        recognized_text = ' '.join(text for text in texts if isinstance(text, str)).strip()
        if not recognized_text:
            return {'success': False, 'message': '', 'error': "Couldn't find any text"}
    except OCRError:
        raise
    except Exception:
        logger.error("OCR inference failed")
        raise OCRError() from None

    # Never persist user handwriting unless explicitly configured by the operator.
    output_dir = os.getenv('OCR_SAVE_RESULTS_DIR')
    if output_dir:
        try:
            saveImages(raw_image, preprocessed_image, recognized_text, output_dir)
        except Exception:
            logger.error("Optional OCR artifact save failed")
            # A storage failure must not discard a valid prediction.
    return {'success': True, 'message': recognized_text, 'error': ''}
