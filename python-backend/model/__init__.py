"""Lazy-load the selected OCR backend without importing unused models."""

def get_predictor(engine: str):
    engine = engine.lower()
    if engine == 'paddleocr':
        from .paddleOCR import predict_image
    elif engine == 'easyocr':
        from .easyOCR import predict_image
    elif engine == 'trocr':
        from .trOCR import predict_image
    else:
        raise ValueError(f"Unknown OCR engine: {engine}")
    return predict_image
