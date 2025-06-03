import sys, os
import base64
from flask import Flask, request, jsonify
from pyngrok import ngrok

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import model

app = Flask(__name__)
print(f"name: {__name__}")
ngrok.set_auth_token("2xK4s84bkpQ1Bk3p5LrWsvTzHcv_5rcNXXqBSUPVzrB3t5Nzs")
public_url = ngrok.connect(5000)
print(f" * ngrok URL: {public_url}")

@app.route('/predict_image', methods=['POST'])
def predict_image():
    data = request.get_json()
    image_b64 = data.get('image_base64')

    if not image_b64:
        return jsonify({'success': False, 'error': 'No image data provided'}), 400
    
    result = model.predict_image(image_b64)

    if not result['success']:
        return jsonify(result), 400

    return jsonify(result)
    
@app.route('/upload_image', methods=['POST'])
def upload_image():
    data = request.get_json()
    image_b64 = data.get('image_base64')

    if not image_b64:
        return jsonify({'success': False, 'error': 'No image data provided'}), 400

    try:
        image_data = base64.b64decode(image_b64)
        with open('uploaded_image.png', 'wb') as f:
            f.write(image_data)
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

    return jsonify({'success': True, 'message': 'Image uploaded successfully'})

@app.route('/get_image', methods=['GET'])
def get_image():
    try:
        with open('uploaded_image.png', 'rb') as img_file:
            encoded_image = base64.b64encode(img_file.read()).decode('utf-8')
        
        return jsonify({'success': True, 'image_base64': encoded_image})

    except FileNotFoundError:
        return jsonify({'success': False, 'error': 'Image file not found'}), 404

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)