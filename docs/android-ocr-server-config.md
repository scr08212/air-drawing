# Android OCR server address configuration

The Android app has **no default remote OCR endpoint**. The expired ngrok address
previously committed in `app/build.gradle` is no longer used.

The OCR server URL is **not a secret**: it is embedded in the APK as
`BuildConfig.OCR_BASE_URL` and can be discovered by an APK user. Do not embed
ngrok authtokens, API keys, usernames, passwords, or other secrets in this URL.

## Development: Android emulator + local Flask

Start the backend on the development computer:

```bash
python python-backend/server/app.py
```

In the Android emulator, `10.0.2.2` is the host computer's loopback address.
From the `android-app/` directory, build or install the debug app with:

```bash
./gradlew :app:installDebug -PocrDebugBaseUrl=http://10.0.2.2:5000/
```

Windows PowerShell:

```powershell
.\gradlew.bat :app:installDebug -PocrDebugBaseUrl=http://10.0.2.2:5000/
```

This URL is **only valid inside the Android emulator**. It is not the URL for
a physical phone.

For an ARCore-compatible physical phone on the same trusted network, explicitly
pass your computer's LAN address instead (replace the example address):

```bash
./gradlew :app:installDebug -PocrDebugBaseUrl=http://192.168.1.20:5000/
```

Make sure the phone can reach the Flask host and that its firewall permits the
required connection. Debug builds allow cleartext HTTP for local testing only,
so do not send personal handwriting or other sensitive input over untrusted
networks. Avoid exposing the Flask development server directly on the internet.

## Production / release APK

A release endpoint must be a real, reachable **HTTPS** server under your
control. Set it explicitly when building a release (example URL only):

```bash
./gradlew :app:assembleRelease -PocrReleaseBaseUrl=https://ocr.example.com/
```

Use the actual HTTPS origin at which the Flask `/predict_image` API is served.
An HTTP release URL, malformed URL, URL containing credentials, or URL without
a trailing `/` is rejected during Gradle configuration.

**Release builds without a configured URL do not contact an unknown server.**
The app instead displays `OCR 서버 주소가 설정되지 않았습니다` when OCR is
requested. Configure and validate a real HTTPS URL before distributing the APK.

If a public tunnel (e.g. ngrok) is required, configure the current HTTPS
tunnel URL for that build. Rotating ngrok authtokens does not update the app's
server address automatically. A public tunnel alone is **not API authentication**:
add appropriate authentication, abuse prevention/rate limiting and network
protections before publicly exposing the inference API.

## Environment variables

Equivalent environment variables can be set instead of command-line Gradle
properties:

| Build type | Gradle project property | Environment variable |
| --- | --- | --- |
| Debug | `ocrDebugBaseUrl` | `OCR_DEBUG_BASE_URL` |
| Release | `ocrReleaseBaseUrl` | `OCR_RELEASE_BASE_URL` |

An explicitly supplied Gradle property takes precedence over its environment
variable. Both default to an empty string. No remote endpoint or token is
stored in the repository.

### URL policy

- Debug: an explicit HTTP or HTTPS **origin root** with trailing `/`.
- Release: an explicit HTTPS **origin root** with trailing `/`.
- No URL credentials, query string, fragment, or non-root path.
- The client posts JSON to `/predict_image`; do not append that path to the
  configured base URL.
- A missing URL results in a clear error in the Android result screen instead
  of a Retrofit initialization crash.
- A connection failure displays `OCR 서버에 연결할 수 없습니다`, without
  exposing the URL or internal exception in Android logs.

## Automated verification

The workflow `.github/workflows/android-flask-e2e.yml` provides
`-PocrDebugBaseUrl=http://10.0.2.2:5000/` during Android instrumentation
testing. It also verifies that an HTTP release URL and an FTP debug URL are
rejected during Gradle configuration. The existing synthetic and thesis-derived
handwriting end-to-end tests continue to exercise the real Flask/PaddleOCR API.

This configuration change does not test the physical ARCore gesture capture
path. That step still requires an ARCore-compatible Android phone.
