"""Public, sanitized OCR errors used at the HTTP boundary."""

class OCRError(Exception):
    status_code = 500
    public_message = "OCR processing failed"


class InvalidImageError(OCRError):
    status_code = 400
    public_message = "Invalid image data"


class ImageTooLargeError(OCRError):
    status_code = 413
    public_message = "Image exceeds size or dimension limits"


class OCRUnavailableError(OCRError):
    status_code = 503
    public_message = "OCR service unavailable"
