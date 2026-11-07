"""Inspect the original capstone PDF figures without rewriting the paper.

Run:
  python python-backend/tests/extract_paper_figures.py

Outputs renderings and embedded image candidates to the ignored
ci-paper-figures directory for manual quality assessment.
"""
import json
from pathlib import Path

import fitz


ROOT = Path(__file__).resolve().parents[2]
PAPER_FILES = sorted(ROOT.glob("논문_*.pdf"))
OUT = ROOT / "ci-paper-figures"


def main():
    if len(PAPER_FILES) != 1:
        raise RuntimeError(f"Expected exactly one capstone paper PDF, found {len(PAPER_FILES)}")
    OUT.mkdir(exist_ok=True)
    doc = fitz.open(PAPER_FILES[0])
    info = {"paper": PAPER_FILES[0].name, "page_count": len(doc), "pages": []}
    for idx, page in enumerate(doc):
        page_number = idx + 1
        pixmap = page.get_pixmap(matrix=fitz.Matrix(1.6, 1.6), alpha=False)
        pixmap.save(OUT / f"page-{page_number:02d}.png")
        page_info = {
            "page_number": page_number,
            "page_text": page.get_text()[:3500],
            "embedded_images": [],
        }
        for img_idx, image in enumerate(page.get_images(full=True), start=1):
            xref = image[0]
            # Only save image regions embedded as raster objects; vector images
            # remain visible in the page renderings.
            rects = page.get_image_rects(xref)
            for instance_idx, rect in enumerate(rects, start=1):
                if rect.width < 30 or rect.height < 20:
                    continue
                crop = page.get_pixmap(matrix=fitz.Matrix(2.5, 2.5), clip=rect, alpha=False)
                name = f"p{page_number:02d}-image{img_idx:02d}-{instance_idx:02d}.png"
                crop.save(OUT / name)
                page_info["embedded_images"].append({
                    "filename": name, "page_bbox": [round(n, 2) for n in rect],
                    "width": crop.width, "height": crop.height
                })
        info["pages"].append(page_info)
        print(f"Page {page_number}: {len(page_info['embedded_images'])} embedded-image regions", flush=True)
    (OUT / "manifest.json").write_text(
        json.dumps(info, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"Paper pages: {len(doc)}", flush=True)


if __name__ == "__main__":
    main()
