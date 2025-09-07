import sys, os
from flask import Flask, request, jsonify
from pyngrok import ngrok
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import get_predictor

predictor = get_predictor('paddleocr')

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
    
    result = predictor(image_b64)
    print("----------------------------------")
    print("Status Code:", result['success'])
    print("message:", result['message'])
    print("error:", result['error'])
    print("----------------------------------")
    if not result['success']:
        return jsonify(result), 400

    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)