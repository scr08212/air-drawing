package com.nkm.ar.core.java.network;

import okhttp3.OkHttpClient;
import com.nkm.ar.core.java.views.helloar.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.OCR_BASE_URL;
    private static Retrofit retrofitInstance;

    public static OcrService getInstance() {
        if (retrofitInstance == null) {
            OkHttpClient client = new OkHttpClient.Builder().build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance.create(OcrService.class);
    }
}