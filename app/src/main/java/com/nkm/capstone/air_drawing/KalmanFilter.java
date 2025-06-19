package com.nkm.capstone.air_drawing;

/**
 * KalmanFilter: 센서 측정값에 포함된 노이즈와 누적 오차를 줄이기 위한 1차원 칼만 필터 클래스입니다.
 *
 * <br>
 * - 본 필터는 단일 스칼라(1D) 값에 대한 예측-보정을 반복하여, 측정값의 신뢰도를 개선합니다.
 * - IMU, 자이로스코프, 가속도계 등에서 발생하는 잡음이나 누적 오차를 보정하는 데 주로 사용됩니다.
 *
 * <p><b>주요 구성:</b></p>
 * <ul>
 *     <li>{@code q} (process noise): 시스템 모델의 불확실성 (작게 설정)</li>
 *     <li>{@code r} (measurement noise): 센서 측정값의 불확실성 (크게 설정)</li>
 *     <li>{@code estimate}: 현재 상태 추정값</li>
 *     <li>{@code errorEstimate}: 추정값의 오차 범위</li>
 * </ul>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * KalmanFilter filter = new KalmanFilter(0.01f, 1.0f, 0f);
 * float filtered = filter.update(sensorReading);
 * }</pre>
 *
 * @author 남경민
 * @since 2025.06.20
 */

public class KalmanFilter
{
    private float estimate;  // 현재 추정값
    private float errorEstimate; // 현재 추정 오차
    private final float q;   // 프로세스 노이즈 (작게)
    private final float r;   // 측정 노이즈 (크게)

    public KalmanFilter(float processNoise, float measurementNoise, float initialEstimate)
    {
        this.q = processNoise;
        this.r = measurementNoise;
        this.estimate = initialEstimate;
        this.errorEstimate = 1;
    }

    public float update(float measurement)
    {
        // 예측 단계
        errorEstimate += q;

        // 보정 단계
        float kalmanGain = errorEstimate / (errorEstimate + r);
        estimate += kalmanGain * (measurement - estimate);
        errorEstimate *= (1 - kalmanGain);

        return estimate;
    }
}
