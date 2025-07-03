import easyocr
import base64
import io
import numpy as np

from PIL import Image
from .utils import preprocess, saveImages

reader = easyocr.Reader(['en'])
path = "D:/Projects/Capstone_Design/data/results"

def predict_image(image_b64):
    try:
        decoded_image = base64.b64decode(image_b64)
        raw_image = Image.open(io.BytesIO(decoded_image)).convert("RGB")
        preprocessed_image = preprocess(raw_image)

        np_image = np.array(preprocessed_image)
        result = reader.readtext(np_image)

        if not result:
            return {'success': False, 'message': "",'error': "Couldn't find any text"}
        
        _, generated_text, _ = result[0]

    except Exception as e:
        return {'success': False, 'message': "",'error': str(e)}
    
    saveImages(raw_image, preprocessed_image, generated_text, path)
    
    return {'success': True, 'message': generated_text, 'error': ""}