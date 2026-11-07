package com.nkm.ar.core.java.network;

import android.app.Activity;
import android.content.Intent;

import com.nkm.ar.core.java.utils.OcrPreviewCache;
import com.nkm.ar.core.java.views.ResultActivity;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shared Android OCR request and result-screen flow, used by the AR app and E2E test.
 * Never log or persist the Base64 payload or recognized text in this class.
 */
public final class OcrRequestFlow {
    private OcrRequestFlow() {}

    public static void recognizeAndShow(Activity activity, String imageBase64, String imagePath) {
        if (!RetrofitClient.isConfigured()) {
            openResult(activity, "OCR 서버 주소가 설정되지 않았습니다", imagePath);
            return;
        }
        final Call<OcrResponse> pending;
        try {
            pending = RetrofitClient.getInstance()
                    .predictImage(Collections.singletonMap("image_base64", imageBase64));
        } catch (IllegalStateException | IllegalArgumentException error) {
            // Do not expose internal URLs or exception contents to the user or logs.
            openResult(activity, "OCR 서버 설정을 확인해 주세요", imagePath);
            return;
        }
        pending
                .enqueue(new Callback<OcrResponse>() {
                    @Override
                    public void onResponse(Call<OcrResponse> call, Response<OcrResponse> response) {
                        String displayed = "인식 실패";
                        if (response.isSuccessful() && response.body() != null
                                && response.body().success && response.body().message != null) {
                            displayed = response.body().message;
                        }
                        openResult(activity, displayed, imagePath);
                    }

                    @Override
                    public void onFailure(Call<OcrResponse> call, Throwable error) {
                        openResult(activity, "OCR 서버에 연결할 수 없습니다", imagePath);
                    }
                });
    }

    private static void openResult(Activity activity, String result, String imagePath) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            OcrPreviewCache.deleteIfOwned(activity, imagePath);
            return;
        }
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.putExtra("result_char", result);
        intent.putExtra("image_path", imagePath);
        activity.startActivity(intent);
    }
}
