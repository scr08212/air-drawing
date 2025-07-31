package com.nkm.ar.core.java.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OcrService {
    @POST("/predict_image")
    Call<OcrResponse> predictImage(@Body Map<String, String> payload);
}
