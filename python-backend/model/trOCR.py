import base64
import io

from transformers import TrOCRProcessor, VisionEncoderDecoderModel
from PIL import Image
from .utils import preprocess, saveImages

processor = TrOCRProcessor.from_pretrained("microsoft/trocr-base-handwritten")
model = VisionEncoderDecoderModel.from_pretrained("microsoft/trocr-base-handwritten")
path = "D:/Projects/Capstone_Design/data/results"

def predict_image(image_b64):
    try:
        decoded_image = base64.b64decode(image_b64)
        raw_image = Image.open(io.BytesIO(decoded_image)).convert("RGB")
        preprocessed_image = preprocess(raw_image)
        
        pixel_values = processor(images=preprocessed_image, return_tensors="pt").pixel_values
        generated_ids = model.generate(pixel_values)
        generated_text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]

    except Exception as e:
        return {'success': False, 'message': "",'error': str(e)}
    
    saveImages(raw_image, preprocessed_image, generated_text, path)
    
    return {'success': True, 'message': generated_text, 'error': ""}