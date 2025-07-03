from PIL import Image
import numpy as np
import os
import uuid

def preprocess(image: Image.Image, size=384, pad_ratio=0.2) -> Image.Image:
    image = image.convert('L')
    np_img = np.array(image)
    mask = np_img < 240  # 밝기 기준(임계값 조정 필요시 변경)
    coords = np.argwhere(mask)
    if coords.shape[0] == 0:
        return Image.new("RGB", (size, size), color=(255, 255, 255))
    y0, x0 = coords.min(axis=0)
    y1, x1 = coords.max(axis=0) + 1

    # 패딩 픽셀 계산
    h = y1 - y0
    w = x1 - x0
    pad_h = int(h * pad_ratio)
    pad_w = int(w * pad_ratio)
    
    # 패딩을 적용한 크롭 박스
    y0 = max(0, y0 - pad_h)
    y1 = min(np_img.shape[0], y1 + pad_h)
    x0 = max(0, x0 - pad_w)
    x1 = min(np_img.shape[1], x1 + pad_w)

    cropped = image.crop((x0, y0, x1, y1)).convert('RGB')
    cropped_w, cropped_h = cropped.size

    # 필요하다면 크롭 결과를 정해진 크기로 리사이즈(비율 유지)
    scale = min(size / cropped_w, size / cropped_h)
    new_w = int(cropped_w * scale)
    new_h = int(cropped_h * scale)
    resized = cropped.resize((new_w, new_h), Image.LANCZOS)

    background = Image.new("RGB", (size, size), color=(255, 255, 255))
    offset_x = (size - new_w) // 2
    offset_y = (size - new_h) // 2
    background.paste(resized, (offset_x, offset_y))

    return background

def saveImages(raw_image: Image.Image, preprocessed_image: Image.Image, recognized_text: str, save_dir: str):
    if save_dir:
        full_path = os.path.join(save_dir, uuid.uuid4().hex)
        os.makedirs(full_path, exist_ok=True)

        raw_image.save(os.path.join(full_path, "raw.png"))
        preprocessed_image.save(os.path.join(full_path, "preprocessed.png"))

        with open(os.path.join(full_path, f"result.txt"), "w", encoding="utf-8") as f:
            f.write(recognized_text)