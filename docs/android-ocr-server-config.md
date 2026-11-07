# Android OCR 서버 연결 설정

현재 Android 앱을 Flask/PaddleOCR 서버에 연결하는 가이드입니다. 알고리즘과 데이터 흐름은 [Architecture](architecture.md), 프로젝트 개요는 [README](../README.md)를 참고하세요.

**기본 원격 서버 주소는 없습니다.** OCR 주소는 앱 빌드 시 Gradle 속성 또는 환경변수로 지정하며 `BuildConfig.OCR_BASE_URL`에 반영됩니다.

## 1. 준비 및 Flask 실행

- 개발 PC에 Python 3.11과 Flask/PaddleOCR 실행에 필요한 의존성을 준비합니다. Python 의존성은 CPU/GPU 및 운영체제별로 달라질 수 있습니다.
- Android Studio/SDK와 Java 17이 필요합니다. **실제 공중 필기에는 ARCore 지원 Android 실기기**가 필요합니다.
- 아래 명령은 별도 언급이 없다면 저장소 루트에서 시작합니다.

개발 PC에서 서버를 실행합니다.

```bash
python python-backend/server/app.py
```

서버의 기본 포트는 `5000`입니다. 서버를 실행한 터미널을 유지한 채 Android 앱을 빌드합니다. 의존성 준비의 개요는 [README — Getting Started](../README.md#getting-started)를 참고하세요.

## 2. Debug: Android 에뮬레이터

일반적인 Android 에뮬레이터에서 `10.0.2.2`는 **에뮬레이터가 접근하는 개발 PC의 루프백 주소**입니다. 에뮬레이터 내부 `localhost`는 개발 PC가 아니라 에뮬레이터 자신을 의미합니다.

**macOS / Linux**

```bash
cd android-app
./gradlew :app:installDebug -PocrDebugBaseUrl=http://10.0.2.2:5000/
```

**Windows PowerShell**

```powershell
cd android-app
.\gradlew.bat :app:installDebug -PocrDebugBaseUrl=http://10.0.2.2:5000/
```

`installDebug`는 앱을 설치하는 명령이며, 이 단계만으로 실제 ARCore 필기와 OCR 결과까지 검증되었다는 뜻은 아닙니다.

## 3. Debug: ARCore 실기기

실제 스마트폰에서는 `10.0.2.2`를 사용할 수 없습니다. **스마트폰에서 접근 가능한 PC의 LAN IP 주소**를 지정합니다. 아래 `192.168.1.20`은 예시이며 실제 PC 주소로 바꿔야 합니다.

```bash
cd android-app
./gradlew :app:installDebug -PocrDebugBaseUrl=http://192.168.1.20:5000/
```

Windows에서는 같은 옵션을 `.\gradlew.bat`에 전달합니다.

- 스마트폰과 PC가 서로 통신할 수 있는 신뢰 가능한 네트워크에 있는지 확인합니다.
- PC 방화벽의 **TCP 5000 포트** 허용 여부와 Flask 실행 상태를 확인합니다.
- Flask는 `0.0.0.0:5000`에서 요청을 수신하지만, **Android에 `0.0.0.0`을 서버 주소로 지정하면 안 됩니다.**
- Debug 앱에서는 로컬 HTTP 연결을 허용합니다. 민감한 필기 이미지는 신뢰할 수 없는 네트워크로 전송하지 마세요.

## 4. Release: HTTPS 서버

Release 빌드에는 접근 가능한 **HTTPS 서버의 루트 URL**을 명시합니다. 아래 도메인은 실제 서버가 아닌 예시입니다.

```bash
cd android-app
./gradlew :app:assembleRelease -PocrReleaseBaseUrl=https://ocr.example.com/
```

주소를 지정하지 않았다면 임의의 원격 서버로 접속하지 않으며, 인식 요청 시 `OCR 서버 주소가 설정되지 않았습니다`라고 표시합니다. HTTP Release 주소는 빌드 설정에서 거부됩니다.

`assembleRelease`는 Release APK를 생성하는 명령이며 배포 서명·인증·서버 운영을 자동으로 보장하지 않습니다.

외부 HTTPS 터널(예: ngrok)을 사용한다면 **현재 터널 주소**를 빌드 옵션으로 전달해야 합니다. 터널 주소가 변경되어도 이미 빌드된 APK의 주소는 바뀌지 않습니다. 공개 터널 또는 HTTPS만으로 API 인증과 접근 제어가 해결되는 것은 아닙니다.

## 5. URL 설정 규칙

| 빌드 유형 | Gradle 속성 | 환경변수 | 허용 주소 |
| --- | --- | --- | --- |
| Debug | `ocrDebugBaseUrl` | `OCR_DEBUG_BASE_URL` | HTTP / HTTPS |
| Release | `ocrReleaseBaseUrl` | `OCR_RELEASE_BASE_URL` | HTTPS |

동시에 지정하면 **Gradle 속성이 환경변수보다 우선**합니다. 둘 다 없다면 빈 문자열로 설정됩니다.

- URL에는 스킴과 호스트가 필요하며 반드시 `/`로 끝나야 합니다. 예: `http://10.0.2.2:5000/`
- 서버 **루트 주소만** 입력합니다. `/predict_image`는 붙이지 않습니다. Android가 `POST /predict_image`를 호출합니다.
- URL에 사용자 인증정보, 쿼리, 프래그먼트, 루트 이외 경로를 포함할 수 없습니다.
- Debug에서는 HTTP/HTTPS, Release에서는 HTTPS만 허용합니다.
- 서버 주소는 APK에서 확인할 수 있는 설정값으로 **비밀정보가 아닙니다**. 인증 토큰, API 키, 비밀번호를 URL이나 소스 코드에 포함하지 마세요.

구현 근거: [Android Gradle 설정](../android-app/app/build.gradle) · [RetrofitClient.java](../android-app/app/src/main/java/com/nkm/ar/core/java/network/RetrofitClient.java)

## 6. 연결 오류 점검

| 증상 | 확인할 사항 |
| --- | --- |
| `OCR 서버 주소가 설정되지 않았습니다` | 해당 빌드 유형의 URL을 전달했는지, 주소를 지정한 APK를 다시 설치했는지 확인 |
| `OCR 서버에 연결할 수 없습니다` | Flask 실행, IP 주소, PC 방화벽 TCP 5000, 기기와 PC의 네트워크 연결 확인 |
| Gradle URL 오류 | 스킴, Release HTTPS 제한, 마지막 `/`, 불필요한 경로·쿼리·인증정보 확인 |
| 앱은 설치되지만 공중 필기가 동작하지 않음 | ARCore 지원 실기기, 카메라 권한, ARCore 실행 상태 확인 |

현재 Flask 서버에는 별도의 루트 상태 확인 엔드포인트가 없습니다. 브라우저에서 `http://PC주소:5000/`을 열어 `404`가 표시되는 것만으로 OCR API 연결 실패라고 단정할 수 없습니다.

참고: [Flask API](../python-backend/server/app.py) · [Android OCR 요청 흐름](../android-app/app/src/main/java/com/nkm/ar/core/java/network/OcrRequestFlow.java) · [Architecture](architecture.md)
