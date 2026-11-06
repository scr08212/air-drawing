"""Warm up the real Flask+PaddleOCR service with the same synthetic CI image."""
import base64
import json
import sys
import time
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

SAMPLE = Path("ci-artifacts/sample.png")
EXPECTED = "HELLO123"
ENDPOINT = "http://127.0.0.1:5000/predict_image"


def main():
    if not SAMPLE.is_file():
        raise FileNotFoundError(f"Missing generated test image: {SAMPLE}")
    data = json.dumps({
        "image_base64": base64.b64encode(SAMPLE.read_bytes()).decode("ascii")
    }).encode("utf-8")
    deadline = time.monotonic() + 240
    while time.monotonic() < deadline:
        try:
            request = Request(
                ENDPOINT, data=data, headers={"Content-Type": "application/json"}, method="POST"
            )
            with urlopen(request, timeout=150) as response:
                payload = json.loads(response.read())
                if payload.get("success") is not True:
                    raise RuntimeError("Flask OCR response did not report success")
                recognized = "".join(c for c in payload.get("message", "").upper() if c.isalnum())
                if recognized != EXPECTED:
                    raise RuntimeError("Flask OCR did not recognize the synthetic input")
                print("Live Flask+PaddleOCR warmup: PASS", flush=True)
                return
        except URLError as error:
            if isinstance(error, HTTPError):
                raise RuntimeError(f"Flask returned HTTP {error.code}") from error
            time.sleep(2)
    raise RuntimeError("Flask server did not become ready")


if __name__ == "__main__":
    main()
