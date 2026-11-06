import sys, os
from flask import Flask, request, jsonify
from pyngrok import ngrok
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import get_predictor

predictor = get_predictor('paddleocr')

app = Flask(__name__)


@app.route('/predict_image', methods=['POST'])
def predict_image():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({'success': False, 'error': 'Invalid JSON payload'}), 400
    image_b64 = data.get('image_base64')

    if not image_b64:
        return jsonify({'success': False, 'error': 'No image data provided'}), 400
    
    result = predictor(image_b64)
    app.logger.info("OCR request completed: success=%s", result["success"])
    if not result['success']:
        return jsonify(result), 400

    return jsonify(result)

if __name__ == '__main__':
    if os.environ.get('ENABLE_NGROK') == '1':
        authtoken = os.environ.get('NGROK_AUTHTOKEN')
        if authtoken:
            ngrok.set_auth_token(authtoken)
        ngrok.connect(5000)
        app.logger.info('ngrok tunnel started')
    app.run(host='0.0.0.0', port=5000)