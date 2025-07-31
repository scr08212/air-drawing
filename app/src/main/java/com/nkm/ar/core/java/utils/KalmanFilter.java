package com.nkm.ar.core.java.utils;

public class KalmanFilter {
    private float estimate;  // 현재 추정값
    private float errorEstimate; // 현재 추정 오차
    private final float q;   // 프로세스 노이즈 (작게)
    private final float r;   // 측정 노이즈 (크게)

    private float initialEstimate;
    private float initialErrorEstimate;

    public KalmanFilter(float processNoise, float measurementNoise, float initialEstimate) {
        this.q = processNoise;
        this.r = measurementNoise;
        this.estimate = initialEstimate;
        this.errorEstimate = 1;

        this.initialEstimate = initialEstimate;
        this.initialErrorEstimate = 1;
    }

    public float update(float measurement) {
        // 예측 단계
        errorEstimate += q;

        // 보정 단계
        float kalmanGain = errorEstimate / (errorEstimate + r);
        estimate += kalmanGain * (measurement - estimate);
        errorEstimate *= (1 - kalmanGain);

        return estimate;
    }

    public void clear(){
        estimate = initialEstimate;
        errorEstimate = initialErrorEstimate;
    }
}
