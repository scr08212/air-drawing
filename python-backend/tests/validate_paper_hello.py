"""Run real PaddleOCR on the original orthographically projected word in Figure 6."""
import base64
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "python-backend"))
from model import get_predictor

os.environ["OCR_DEVICE"] = "cpu"
os.environ.pop("OCR_SAVE_RESULTS_DIR", None)

source = ROOT / "ci-paper-figures" / "p06-image03-01.png"
if not source.exists():
    raise SystemExit(f"missing figure crop: {source}")
result = get_predictor("paddleocr")(base64.b64encode(source.read_bytes()).decode("ascii"))
normalized = "".join(c for c in str(result.get("message", "")).casefold() if c.isalnum())
record = {
    "source": "capstone PDF, page 6, Figure 6: projected handwritten word",
    "expected_text": "hello",
    "recognized_text": result.get("message", ""),
    "normalized_result": normalized,
    "success": result.get("success"),
    "exact_match": normalized == "hello",
}
(ROOT / "ci-paper-figures" / "paper-ocr-result.json").write_text(
    json.dumps(record, ensure_ascii=False, indent=2), encoding="utf-8"
)
print("REAL PAPER FIGURE OCR:", json.dumps(record, ensure_ascii=False), flush=True)
# Intentionally do not fail on a recognition mismatch: inspect real-world
# accuracy before deciding whether an exact-match test is appropriate.
