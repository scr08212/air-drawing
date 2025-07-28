package com.nkm.capstone.air_drawing;

public class KalmanFilter {
    private float estimate;  // 현재 추정값
    private float errorEstimate; // 현재 추정 오차
    private final float q;   // 프로세스 노이즈 (작게)
    private final float r;   // 측정 노이즈 (크게)

    public KalmanFilter(float processNoise, float measurementNoise, float initialEstimate) {
        this.q = processNoise;
        this.r = measurementNoise;
        this.estimate = initialEstimate;
        this.errorEstimate = 1;
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
}
