# Testing & Evidence | Air Drawing

> **목적:** 실제 실행·테스트로 **확인한 사실**을 기록합니다. 캡스톤 당시의 IMU/PCA 설계 변경 이유는 [아키텍처 문서](architecture.md#7-engineering-decisions)에, 개인 기여는 [README](../README.md#project-overview--my-role)에 정리했습니다.

## 1. 검증 범위 및 결과

| 수준 | 테스트 입력·방법 | 확인한 결과 | CI 실행 기록 |
| --- | --- | --- | --- |
| 3D 투영·2D 렌더링 | Robolectric/JUnit으로 기준 원점·회전축·평행이동 및 음수·수평·수직·단일 점·획 구분 검증 | [투영 테스트](../android-app/app/src/test/java/com/nkm/ar/core/java/utils/projectors/ReferencePoseProjectorTest.java) · [렌더링 테스트](../android-app/app/src/test/java/com/nkm/ar/core/java/views/ProjectionCanvasViewTest.java) | [단위 테스트 워크플로](../.github/workflows/android-projection-unit-tests.yml) |
| Flask API 계약 | OCR 모델을 모의 객체로 교체하여 정상·비정상 요청 확인 | JSON·상태코드·예외 처리 | [Flask API](https://github.com/scr08212/air-drawing/actions/runs/42347472557) |
| 실제 OCR 모델 | 자동 생성한 `HELLO 123` PNG → CPU PaddleOCR | `HELLO 123` 인식 | [실모델 추론](https://github.com/scr08212/air-drawing/actions/runs/42271598029) |
| 논문 이미지 단독 추론 | 논문 PDF 그림 6에서 추출한 `hello` 이미지 | `hello` 인식 결과 기록 | [논문 샘플 분석](https://github.com/scr08212/air-drawing/actions/runs/42346382543) |
| Android↔Flask E2E | 에뮬레이터에서 합성·논문 이미지 전송, 실제 Flask/PaddleOCR 추론 | 결과 화면에 기대 문자열·이미지 표시 확인 | [E2E](https://github.com/scr08212/air-drawing/actions/runs/42346550527) |
| 데이터 보호 | Android 계측 테스트 | OCR 결과 이전 저장 값 정리, 일시적 이미지 삭제, 허용되지 않은 경로 차단 | [개인정보 보호](https://github.com/scr08212/air-drawing/actions/runs/42348091913) |
| 서버 주소 설정 | Release HTTP·Debug FTP 거부 테스트 + E2E | 부적절한 URL 거부, **Android 계측 테스트 6개 성공** | [최신 설정 검증](https://github.com/scr08212/air-drawing/actions/runs/42351533788) |

- 마지막 행의 **6개**는 **Android 계측 테스트 수**입니다. 사용자 6명이나 서로 다른 OCR 필기 샘플 6개가 아닙니다.
- 실제 OCR 이미지로는 생성한 `HELLO 123`와 논문에서 추출한 `hello`를 각각 사용했습니다. **두 개의 성공 샘플로 전체 문자 인식 정확도를 추정할 수 없습니다.**
- 논문 PDF에 들어간 그림은 원본 Android Bitmap과 픽셀 단위로 동일하다고 보장할 수 없습니다.
- 독립 스크립트 [`validate_paper_hello.py`](../python-backend/tests/validate_paper_hello.py)는 추론 결과를 기록하며 **문자열 불일치 자체로 실패하도록 만들지 않았습니다.** 반면 Android [`OcrPipelineE2eTest`](../android-app/app/src/androidTest/java/com/nkm/ar/core/java/network/OcrPipelineE2eTest.java)는 기대 문자열과 UI를 검사합니다. 진단과 회귀 테스트를 혼동하지 않습니다.

## 2. 무엇을 End-to-End로 검증했는가

```text
합성 또는 논문 이미지 준비
  → Android 에뮬레이터 앱
  → Retrofit JSON / image_base64
  → Flask /predict_image
  → 실제 PaddleOCR CPU 모델
  → JSON 응답
  → Android ResultActivity에 문자열·미리보기 표시
```

이 테스트는 **네트워크 전송·실모델 추론·화면 표시**를 연결해 검증합니다. 반면 에뮬레이터에 이미지를 주입하므로, **실제 스마트폰의 ARCore 위치 추적 → 터치 획 수집 → 직교 투영으로 이미지를 생성하는 전체 경로**를 검증하지는 않습니다.

사용한 테스트 코드:
- [`OcrPipelineE2eTest.java`](../android-app/app/src/androidTest/java/com/nkm/ar/core/java/network/OcrPipelineE2eTest.java)
- [`OcrDataPrivacyTest.java`](../android-app/app/src/androidTest/java/com/nkm/ar/core/java/network/OcrDataPrivacyTest.java)
- [`test_flask_api.py`](../python-backend/tests/test_flask_api.py)
- [`warmup_live_backend.py`](../python-backend/tests/warmup_live_backend.py)
- [Android E2E GitHub Actions 워크플로](../.github/workflows/android-flask-e2e.yml)
- [투영·렌더링 JVM 단위 테스트 워크플로](../.github/workflows/android-projection-unit-tests.yml)

## 3. 후속 유지보수·장애 대응 내역

| 증상·위험 | 조치 | 결과·한계 |
| --- | --- | --- |
| PaddleOCR 모델 초기화 시 의존성 충돌 | PaddlePaddle 3.1.0, PaddleOCR 3.1.0, PaddleX 3.1.1과 관련 LangChain 버전을 CI에서 고정 | Ubuntu/Python 3.11/CPU 조합에서 실모델 추론 확인 |
| 최초 요청 성공 후 후속 OCR 요청에서 HTTP 500 | 공유 모델의 후속 요청 오류를 조사하고 Flask 개발 서버에 `threaded=False` 적용 | 실제 연속 요청 성공. 오류의 모든 근본 원인이나 운영 서버 동시성은 검증하지 못함 |
| 코드·앱 설정의 민감정보 잔존 | ngrok 토큰 교체 및 코드에서 제거, OCR 문자열 영구 저장 제거, 이미지 캐시 일시 보존, 주소를 빌드 타입별로 분리 | 코드·계측 테스트 확인. Git 이력 재작성이나 저장매체 보안 삭제를 수행한 것은 아님 |
| 만료된 서버 URL 또는 잘못된 연결 설정 | 기본 ngrok 주소 제거, Debug/Release 엔드포인트 명시적 지정, Release HTTPS 검증 | 부적절한 URL 거부 및 Android E2E 통과 |

위 내용은 **캡스톤 당시의 IMU/PCA 설계 연구와 별개**인, 이후 실행 신뢰성·데이터 보호 개선 과정입니다. 설계 전환에 대한 설명은 [Architecture 7절](architecture.md#7-engineering-decisions)를 참고하세요.

## 4. 검증된 환경에서 재실행

### CI에서 사용한 환경

| 항목 | 검증된 구성 |
| --- | --- |
| Python / OS | Python **3.11** / GitHub Actions **Ubuntu 22.04** |
| 실제 OCR 엔진 | PaddlePaddle **3.1.0**, PaddleOCR **3.1.0**, PaddleX **3.1.1** |
| 추론 장치 | `OCR_DEVICE=cpu`, `OMP_NUM_THREADS=1` |
| Android E2E | Android 에뮬레이터 **API 34**, Java **17**, Android compile SDK **36** |
| Flask | 개발 서버 포트 **5000**, `threaded=False` |
| 테스트 연결 | 에뮬레이터 `10.0.2.2:5000` → 호스트 Flask |

### 로컬 CPU 백엔드 개발 환경

저장소 루트에서 **Python 3.11** 가상환경을 만들고, 실제 CPU 모델 실행에 사용된 의존성 조합을 [requirements-cpu.txt](../python-backend/requirements-cpu.txt)로 설치합니다. 이 조합은 Ubuntu 22.04에서 검증했으며 다른 운영체제의 호환성은 별도로 확인해야 합니다.

```bash
python3.11 -m venv python-backend/.venv
source python-backend/.venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -r python-backend/requirements-cpu.txt
python -m pip check
export OCR_DEVICE=cpu OMP_NUM_THREADS=1
python python-backend/server/app.py
```

논문 이미지 추출을 포함한 CI 검증은 테스트용 **PyMuPDF 1.26.6** 등을 추가 설치합니다. GPU 실험용 패키지를 포함한 기존 `python-backend/requirements.txt`는 CPU 서버 설치에 사용하지 않습니다. 자동화 테스트의 자세한 설치·실행 단계는 [E2E 워크플로](../.github/workflows/android-flask-e2e.yml)에서 확인할 수 있습니다.

### Android 연결

다른 터미널에서 Android SDK·에뮬레이터를 준비한 뒤 다음을 실행합니다.

```bash
cd android-app
./gradlew :app:installDebug -PocrDebugBaseUrl=http://10.0.2.2:5000/
```

**주의:** `10.0.2.2`는 Android 에뮬레이터에서 호스트 개발 머신을 가리키는 특수 주소로, 실기기의 일반적인 서버 주소가 아닙니다. Windows에서는 `gradlew.bat`를 사용합니다.

CI의 `connectedDebugAndroidTest`는 **합성·논문 샘플을 Android 테스트 애셋으로 먼저 생성**합니다. 로컬에서 계측 테스트까지 동일하게 실행하려면 [CI의 fixture 생성 단계](../.github/workflows/android-flask-e2e.yml)를 재현해야 합니다. 따라서 전체 자동화 검증의 기준 절차는 해당 GitHub Actions 실행입니다.

[Android 개발/배포 URL 설정 상세](android-ocr-server-config.md)

## 5. 남아 있는 검증 공백

- **실기기 ARCore 전 과정:** 카메라 Pose 기반 공중 필기와 기준 자세 설정이 실제 지원 기기에서 어떻게 동작하는지 독립적인 최근 실기기 회귀 테스트가 필요합니다. 과거 시연 영상·논문은 별도 증거입니다.
- **투영·렌더링 단위 테스트의 범위:** 기준 원점 이동·구현상의 회전축 매핑·깊이 성분 제거와 렌더링 경계값은 JVM/Robolectric 테스트로 검사합니다. 이는 독립적으로 검증된 월드↔카메라 좌표계 정의나 실제 손글씨 방향·반전 방지 성능까지 증명하는 것은 아닙니다.
- **기존 PCA/IMU 시도:** [초기 PCA 스크립트](../python-backend/utils/3d_visualizer/pca.py) 및 [약 140개 좌표가 담긴 샘플 CSV](../python-backend/utils/3d_visualizer/ar_camera_poses.csv)는 존재합니다. 다만 스크립트는 현재 독립 실행 검증이 되지 않았고, 초기 IMU/PCA 방식과 최종 방식의 정량적 비교 실험 자료도 부족합니다.
- **논문 원본 OCR 데이터셋:** 논문에 기술된 전체 정확도·GPU 처리시간을 재현할 수 있는 대규모 원본 샘플은 확보되지 않았습니다.
- **배포·운영:** 공개 HTTPS 서버의 인증, 레이트 리밋, 동시성·장애 복구·성능 지표, 클라우드 운영은 현재 검증 범위가 아닙니다.

**샘플 인식 성공, 논문에 보고된 연구 결과, 자동화 테스트 통과, 실제 운영 환경 성능은 서로 다른 증거 수준**으로 취급합니다. CI의 업로드 아티팩트는 보존 기간이 지나면 사라질 수 있지만 워크플로·테스트 소스와 실행 기록 링크는 저장소에서 확인할 수 있습니다.
