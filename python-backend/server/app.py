import sys, os
from flask import Flask, request, jsonify
from pyngrok import ngrok
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import get_predictor
from model.errors import OCRError

predictor = get_predictor('paddleocr')

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 8 * 1024 * 1024


@app.route('/predict_image', methods=['POST'])
def predict_image():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({'success': False, 'error': 'Invalid JSON payload'}), 400
    image_b64 = data.get('image_base64')

    if not image_b64:
        return jsonify({'success': False, 'error': 'No image data provided'}), 400
    if not isinstance(image_b64, str):
        return jsonify({'success': False, 'message': '', 'error': 'Invalid image data'}), 400
    
    try:
        result = predictor(image_b64)
    except OCRError as exc:
        app.logger.warning("OCR request rejected: status=%d", exc.status_code)
        return jsonify({'success': False, 'message': '', 'error': exc.public_message}), exc.status_code
    except Exception:
        app.logger.error("Unhandled OCR request failure")
        return jsonify({'success': False, 'message': '', 'error': 'Internal server error'}), 500
    app.logger.info("OCR request completed: success=%s", result["success"])
    if not result['success']:
        return jsonify(result), 400

    return jsonify(result)

@app.errorhandler(413)
def payload_too_large(_error):
    return jsonify({'success': False, 'message': '', 'error': 'Request body too large'}), 413


if __name__ == '__main__':
    if os.environ.get('ENABLE_NGROK') == '1':
        authtoken = os.environ.get('NGROK_AUTHTOKEN')
        if authtoken:
            ngrok.set_auth_token(authtoken)
        ngrok.connect(5000)
        app.logger.info('ngrok tunnel started')
    # The shared PaddleOCR predictor is not used concurrently in this demo server.
    app.run(host='0.0.0.0', port=5000, threaded=False)

