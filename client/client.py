import requests
import base64

server_url = "https://f50c-116-125-219-214.ngrok-free.app" # 서버 url로 교체
endpoint = "/predict_image"

def test_prediction(image):
    url = server_url + endpoint
    encoded_image = base64.b64encode(image.read()).decode('utf-8')
    payload = {'image_base64': encoded_image}
    response = requests.post(url, json=payload)

    print("Status Code:", response.status_code)
    print("Response JSON:", response.json())

if __name__ == "__main__":
    image_path = 'D:/Projects/Capstone_Design/data/raw/images/the_quick_brown.jpg'
    with open(image_path, 'rb') as image:
        test_prediction(image)