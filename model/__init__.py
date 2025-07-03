from .trOCR import predict_image as trocr_predict_image
from .easyOCR import predict_image as easyocr_predict_image
from .paddleOCR import predict_image as paddleocr_predict_image

def get_predictor(engine: str):
    if engine.lower() == 'trocr':
        return trocr_predict_image
    elif engine.lower() == 'easyocr':
        return easyocr_predict_image
    elif engine.lower() == 'paddleocr':
        return paddleocr_predict_image
    else:
        raise ValueError(f"Unknown OCR engine: {engine}")
    
__all__ = ['trocr_predict_image', 'easyocr_predict_image', 'paddleocr_predict_image', 'get_predictor']