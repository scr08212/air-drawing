package com.nkm.ar.core.java.network;

import com.nkm.ar.core.java.views.helloar.BuildConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** OCR endpoint settings are injected into BuildConfig per Android build type. */
public final class RetrofitClient {
    private static volatile Retrofit retrofitInstance;

    private RetrofitClient() {}

    public static boolean isConfigured() {
        String url = BuildConfig.OCR_BASE_URL;
        return url != null && !url.trim().isEmpty();
    }

    public static OcrService getInstance() {
        if (!isConfigured()) {
            throw new IllegalStateException("OCR server URL not configured");
        }
        if (!BuildConfig.DEBUG && !BuildConfig.OCR_BASE_URL.toLowerCase(java.util.Locale.ROOT).startsWith("https://")) {
            throw new IllegalStateException("Release OCR endpoint must use HTTPS");
        }

        Retrofit instance = retrofitInstance;
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                instance = retrofitInstance;
                if (instance == null) {
                    OkHttpClient client = new OkHttpClient.Builder().build();
                    instance = new Retrofit.Builder()
                            .baseUrl(BuildConfig.OCR_BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    retrofitInstance = instance;
                }
            }
        }
        return instance.create(OcrService.class);
    }
}
