# Verification & Test Evidence — Air Drawing

> 문서 기준일: 2026-11-07. 각 링크는 **검증 당시 GitHub Actions 실행 기록**이며, 이미 완료한 테스트의 증거입니다. 미래의 모든 빌드나 환경에서 동일하게 동작한다는 보증은 아닙니다.

## 1. 검증 범위 요약

| 구분 | 사용한 입력 | 확인한 내용 | 증거 |
| --- | --- | --- | --- |
| Flask API 계약 테스트 | 모의 OCR 모델 | 정상·비정상 JSON, 누락 이미지, HTTP 오류 및 민감정보 로그 | [Flask API CI](https://github.com/scr08212/air-drawing/actions/runs/42347472557) |
| 실제 PaddleOCR CPU 추론 | 생성한 `HELLO 123` PNG | PaddlePaddle/PaddleOCR 실모델 설치·초기화·문자 인식 | [실제 OCR CI](https://github.com/scr08212/air-drawing/actions/runs/42271598029) |
| 논문의 실제 투영 이미지 추론 | 논문 6페이지 그림 6 `hello` 이미지 | PDF의 그림을 추출한 뒤 실제 PaddleOCR 추론 → `hello` | [논문 샘플 CI](https://github.com/scr08212/air-drawing/actions/runs/42346382543) |
| Android ↔ Flask ↔ PaddleOCR E2E | 합성 `HELLO 123`, 논문 `hello` | Android 에뮬레이터에서 실제 API 호출 및 `ResultActivity` 표시·미리보기 | [E2E CI](https://github.com/scr08212/air-drawing/actions/runs/42346550527) |
| Android 데이터 보호 | Android 계측 테스트 | 이전 OCR 설정 제거, 임시 이미지 삭제, 앱 내부 파일 경로 제한 | [보안 개선 CI](https://github.com/scr08212/air-drawing/actions/runs/42348091913) |
| Android 빌드 타입별 주소 | HTTP·FTP 부적절 주소와 로컬 테스트 주소 | Release HTTP 차단, Debug FTP 차단, **Android 계측 테스트 6개 성공** | [설정 검증 CI](https://github.com/scr08212/air-drawing/actions/runs/42351533788) |

**주의:** 마지막 검증의 '6개 성공'은 6종의 데이터셋이나 6명의 실제 사용자를 뜻하지 않습니다. Android 계측 테스트가 6개라는 의미이며, 실모델 OCR 이미지 인식 검증에는 합성 이미지 1개와 논문 이미지 1개가 사용됐습니다.

GitHub Actions가 업로드한 테스트 아티팩트는 보존 기간 종료 후 없어질 수 있습니다. 테스트 설정은 [`.github/workflows/`](../.github/workflows)에 남아 있습니다.

## 2. 실제 OCR 결과

| 사례 | 원문 | 인식 결과 | 설명 |
| --- | --- | --- | --- |
| 합성 이미지 | `HELLO 123` | `HELLO 123` | 자동 생성한 고대비 영문 이미지 |
| 논문 그림 6 | `hello` | `hello` | 캡스톤 논문 PDF에서 추출한 실제 직교 투영 필기 그림 |

위 사례의 **문자열 일치**는 확인됐습니다. 그러나 샘플이 두 개뿐이므로 문자 인식률·단어 인식률·실사용 정확도 등의 통계값으로 환산할 수 없습니다.

특히 논문 그림은 PDF에 포함된 **복제·압축된 그림**이므로, 원본 Android 비트맵과 완전히 같은 입력은 아닐 수 있습니다.

## 3. 자동화된 E2E 경로

```text
Android emulator (API 34)
  → Retrofit: Base64 PNG를 JSON으로 전송
  → Flask: POST /predict_image
  → 실제 PaddleOCR CPU 모델
  → JSON 문자열 응답
  → Android ResultActivity의 텍스트 + 이미지 미리보기 확인
```

- Android E2E 스위트: [`OcrPipelineE2eTest.java`](../android-app/app/src/androidTest/java/com/nkm/ar/core/java/network/OcrPipelineE2eTest.java)
- Android 개인정보 보호 스위트: [`OcrDataPrivacyTest.java`](../android-app/app/src/androidTest/java/com/nkm/ar/core/java/network/OcrDataPrivacyTest.java)
- Flask 및 OCR 경계 테스트: [`tests/`](../python-backend/tests/)
- 실제 서버 연속 요청 검증: [`warmup_live_backend.py`](../python-backend/tests/warmup_live_backend.py)
- CI 구성: [`android-flask-e2e.yml`](../.github/workflows/android-flask-e2e.yml)

단독 모델 테스트와 E2E는 서로 다른 검증입니다. 모델 자체를 모의 객체로 대체한 API 단위 테스트만으로 실제 PaddleOCR 동작을 주장하지 않습니다.

## 4. 초기 설계 검토와 최종 설계 결정 (캡스톤 당시)

**다음은 개발자가 보고한 과거 설계·시도 및 문제 해결 경험입니다.** 최신 CI에서 직접 재현한 장애나 성능 벤치마크가 아닙니다.

| 당시 접근 | 보고된 문제 | 채택한 설계 | 현재 확인 가능한 근거 |
| --- | --- | --- | --- |
| IMU 가속도·자이로 기반 위치 추정 | 적분에 따른 위치 오차 누적 | ARCore 비전·관성 추적의 카메라 Pose 사용 | [HelloArActivity.java](../android-app/app/src/main/java/com/nkm/ar/core/java/views/HelloArActivity.java) |
| PCA의 PC1·PC2 기반 2D 투영 | 필기 형태에 따른 좌우 반전·상하 뒤집힘 | 기준 위치·방향으로 좌표 정렬 후 XY 직교 투영 | [TestProjector.java](../android-app/app/src/main/java/com/nkm/ar/core/java/utils/projectors/TestProjector.java) |

초기 PCA/IMU 프로토타입의 코드 및 실측 비교 결과는 현재 자료로 재검증되지 않았습니다. 문제의 수치적 심각도나 최종 방식의 개선율은 계산하지 않습니다. 설계 차이와 기술적 이유는 [아키텍처 문서](architecture.md#24-개발-중-설계-변경의-근거-개발자-회고)에 설명합니다.

## 5. 이후 안정성·보안 문제 분석 및 수정 이력


| 관찰한 문제 | 확인·수정 내용 | 검증 범위 |
| --- | --- | --- |
| OCR 모델 시작 오류 | PaddleOCR 3.1.0과 자동 설치된 최신 PaddleX의 호환성 문제를 발견하여 PaddleX 3.1.1, 관련 LangChain 0.3.x 계열로 고정 | 실제 CPU 모델 초기화·추론 성공 |
| 동일 서버 재요청 실패 | 첫 인식은 성공했으나 후속 요청에서 내부 오류 발생. Flask 개발 서버의 요청 처리 방식을 `threaded=False`로 조정 | 직렬 연속 HTTP 요청 성공. 동시성의 모든 근본 원인을 입증한 것은 아님 |
| ngrok 인증정보 노출 위험 | 코드에서 인증정보를 제거하고 기존 토큰을 교체. 현재 터널은 환경변수 사용 시 선택적으로 시작 | 현재 소스에서의 하드코딩 제거. Git 이력은 재작성하지 않음 |
| 이미지·문자열 잔존 | Android OCR 결과 저장 제거, 앱 내부 임시 이미지 1회 사용 뒤 삭제, 이전 저장 값 정리 | 계측 테스트로 확인. 강제 종료 직후 완전 소거는 보증하지 않음 |
| 연결 주소가 만료되거나 잘못됨 | 하드코딩된 ngrok 주소 제거. Debug와 Release 서버 주소 분리, Release HTTPS 검증 | 잘못된 URL 거부 및 Android E2E CI 통과 |

## 6. 당시 실행에 사용한 환경

| 항목 | 검증 환경 |
| --- | --- |
| Python | 3.11 |
| PaddlePaddle | 3.1.0 |
| PaddleOCR | 3.1.0 |
| PaddleX | 3.1.1 |
| OCR 장치 | CPU (`OCR_DEVICE=cpu`) |
| Android E2E | GitHub-hosted Ubuntu, Android 에뮬레이터 API 34 |
| Flask | 개발 서버, 포트 5000, OCR 요청 직렬 처리 |
| 네트워크 | 에뮬레이터 `10.0.2.2:5000` → CI 호스트 Flask |

이는 **CI에서 검증된 조합**을 기록한 것입니다. GPU, 다양한 실기기, 외부 배포 환경까지 검증한 것은 아닙니다. 정확한 설치 패키지 버전은 [CI 워크플로](../.github/workflows/android-flask-e2e.yml)에 기록되어 있습니다.

## 7. 재현 방법

1. 저장소를 체크아웃하고 [Android 서버 설정 문서](android-ocr-server-config.md)에 따라 로컬 OCR 서버 주소를 지정합니다.
2. CI에서 고정해 사용한 Python/PaddleOCR 의존성 조합을 설치합니다. 저장소의 기존 `python-backend/requirements.txt`를 그대로 사용할 경우 GPU 및 과거 종속성 때문에 환경별 추가 조정이 필요할 수 있습니다.
3. Flask 서버를 시작합니다: `python python-backend/server/app.py`.
4. Android 에뮬레이터에서 다음을 실행합니다.

```bash
cd android-app
./gradlew connectedDebugAndroidTest -PocrDebugBaseUrl=http://10.0.2.2:5000/
```

CI는 테스트 이미지들을 자동 생성·추출하여 Android 테스트 애셋으로 사용합니다. 로컬에서 같은 계측 테스트를 실행하려면 CI의 이미지 생성 단계도 선행해야 합니다. **가장 재현성이 높은 절차는 GitHub Actions 워크플로 자체를 실행하는 것**입니다.

## 8. 검증하지 못한 항목 및 주장하지 않는 성과

- **논문 원본 실험 이미지 데이터가 없음.** 논문에서 보고한 대규모 정확도나 처리 시간은 현재 코드로 재현·검증하지 못했습니다.
- **ARCore 실기기 필기 전체 경로:** Android 에뮬레이터 E2E는 미리 준비한 이미지를 전송합니다. 실제 공중 필기 제스처·추적 정확도는 이 테스트의 대상이 아닙니다. [시연 영상](https://www.youtube.com/shorts/3RjYSgrBuwM) 및 논문은 과거 시연 자료로 구분합니다.
- **생산 환경의 백엔드:** 공개 인증, 레이트 리밋, 다중 워커/동시성, 장애 복구, 클라우드 배포를 검증하지 않았습니다.
- **OCR 엔진 비교:** EasyOCR/TrOCR 코드가 존재한다는 이유로 두 엔진의 현재 성능 비교를 수행했다고 주장하지 않습니다.
- **개인별 기여도:** 원본 저장소와 CI 기록만으로 팀 구성원별 기여를 단정할 수 없으므로 별도 확인 후 정확하게 적어야 합니다.

## 9. 증거와 논문의 관계

논문은 **설계 배경·기존 연구 시점의 구현 및 결과**를 설명합니다. 최근 GitHub Actions 결과는 **현재 리포지토리 코드에서 특정 샘플·환경의 기능이 동작함**을 확인합니다. 논문 보고 수치와 새 CI 성공 수치는 목적과 표본이 달라 직접 비교하지 않습니다.

- [논문 원문](../논문_비전%20및%20관성%20센서%20융합을%20활용한%20직교%20투영%20기반%20딥러닝%20공중%20필기%20인식.pdf)
- [아키텍처 문서](architecture.md)
- [README](../README.md)
