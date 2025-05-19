from flask import Flask, request, jsonify
from pyngrok import ngrok
import base64

app = Flask(__name__)
print(f"name: {__name__}")
ngrok.set_auth_token("본인 ngrok Authtoken") # 아마 토큰 없어도 8시간은 세션 유지되는걸로 암, https://ngrok.com/ 회원가입 후 토큰 입력시 무제한 세션 유지됨.
public_url = ngrok.connect(5000)
print(f" * ngrok URL: {public_url}")

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
        
        return jsonify({
            'success': True,
            'image_base64': encoded_image
        })

    except FileNotFoundError:
        return jsonify({'success': False, 'error': 'Image file not found'}), 404

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)