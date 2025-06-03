from transformers import TrOCRProcessor, VisionEncoderDecoderModel
from PIL import Image
import base64
import io

processor = TrOCRProcessor.from_pretrained("microsoft/trocr-base-handwritten")
model = VisionEncoderDecoderModel.from_pretrained("microsoft/trocr-base-handwritten")

def predict_image(image_b64):
    try:
        decoded_image = base64.b64decode(image_b64)
        image = Image.open(io.BytesIO(decoded_image)).convert("RGB")
        pixel_values = processor(images=image, return_tensors="pt").pixel_values
        generated_ids = model.generate(pixel_values)
        generated_text = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
    except Exception as e:
        return {'success': False, 'message': "",'error': str(e)}
    
    return {'success': True, 'message': generated_text, 'error': ""}