import base64
import io
import cv2
import numpy as np

from paddleocr import PaddleOCR
from PIL import Image
from .utils import preprocess, saveImages

path = "D:/Projects/Capstone_Design/data/results"
ocr = PaddleOCR(lang='en',
                device='gpu',
                use_doc_orientation_classify=False,
                use_doc_unwarping=False,
                use_textline_orientation=False,
                )


def predict_image(image_b64):
    try:
        decoded_image = base64.b64decode(image_b64)
        raw_image = Image.open(io.BytesIO(decoded_image)).convert("RGB")
        preprocessed_image = preprocess(raw_image)
        np_image = cv2.cvtColor(np.array(preprocessed_image), cv2.COLOR_RGB2BGR)
        result = ocr.predict(np_image)

        if not result:
            return {'success': False, 'message': "",'error': "Couldn't find any text"}
        
        generated_text = " ".join(result[0].get("rec_texts", []))

    except Exception as e:
        return {'success': False, 'message': "",'error': str(e)}
    
    saveImages(raw_image, preprocessed_image, generated_text, path)

    for res in result:
        res.print()
        res.save_to_img("output")
        res.save_to_json("output")
    
    return {'success': True, 'message': generated_text, 'error': ""}