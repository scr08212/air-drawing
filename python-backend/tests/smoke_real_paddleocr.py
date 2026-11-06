"""Real PaddleOCR CPU smoke test on a synthetic image (no mocks).

Run after installing paddlepaddle, paddleocr and this repository's lightweight
image-processing dependencies:
    python python-backend/tests/smoke_real_paddleocr.py

Only synthetic data is used. The test writes its own evidence to ci-artifacts/.
"""
import base64
import io
import json
import os
import re
import sys
import time
from pathlib import Path
from difflib import SequenceMatcher

from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve()
BACKEND_DIR = HERE.parents[1]
sys.path.insert(0, str(BACKEND_DIR))
os.environ["OCR_DEVICE"] = "cpu"
os.environ.pop("OCR_SAVE_RESULTS_DIR", None)

OUTPUT_DIR = HERE.parents[2] / "ci-artifacts"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
EXPECTED = "HELLO 123"


def sample_png():
    canvas = Image.new("RGB", (1000, 230), "white")
    draw = ImageDraw.Draw(canvas)
    candidates = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf",
    ]
    path = next((p for p in candidates if Path(p).exists()), None)
    if path is None:
        raise RuntimeError("CI runner needs a TrueType font for sample generation")
    font = ImageFont.truetype(path, 105)
    bbox = draw.textbbox((0, 0), EXPECTED, font=font)
    x = (canvas.width - (bbox[2] - bbox[0])) // 2
    y = (canvas.height - (bbox[3] - bbox[1])) // 2 - bbox[1]
    draw.text((x, y), EXPECTED, font=font, fill="black")
    path = OUTPUT_DIR / "sample.png"
    canvas.save(path)
    with io.BytesIO() as buffer:
        canvas.save(buffer, format="PNG")
        return base64.b64encode(buffer.getvalue()).decode("ascii")


def main():
    import paddle
    import paddleocr
    from model import get_predictor

    print("PaddlePaddle version:", paddle.__version__, flush=True)
    print("PaddleOCR version:", getattr(paddleocr, "__version__", "unknown"), flush=True)
    payload = sample_png()
    started = time.monotonic()
    try:
        result = get_predictor("paddleocr")(payload)
    except Exception:
        # Diagnostics only: this test processes synthetic image data and has
        # no credentials, so show the underlying PaddleOCR initialization error.
        from paddleocr import PaddleOCR
        print("DIAGNOSTIC: initializing PaddleOCR directly...", flush=True)
        PaddleOCR(
            lang="en", device="cpu",
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            use_textline_orientation=False,
        )
        raise
    duration = round(time.monotonic() - started, 2)
    recognized = result.get("message", "")
    normalized = re.sub(r"[^A-Z0-9]", "", recognized.upper())
    reference = re.sub(r"[^A-Z0-9]", "", EXPECTED)
    similarity = round(SequenceMatcher(None, reference, normalized).ratio(), 3)
    evidence = {
        "sample_text": EXPECTED,
        "recognized_text": recognized,
        "success": result.get("success"),
        "similarity": similarity,
        "cpu_inference_elapsed_seconds": duration,
        "paddle_version": paddle.__version__,
        "paddleocr_version": getattr(paddleocr, "__version__", "unknown"),
    }
    (OUTPUT_DIR / "result.json").write_text(
        json.dumps(evidence, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    print("OCR synthetic result:", json.dumps(evidence, ensure_ascii=False), flush=True)
    assert result.get("success") is True, "Real OCR reported failure"
    assert similarity >= 0.6, f"Recognition quality below threshold: {similarity}"
    print("REAL PADDLEOCR CPU SMOKE: PASS", flush=True)


if __name__ == "__main__":
    main()
