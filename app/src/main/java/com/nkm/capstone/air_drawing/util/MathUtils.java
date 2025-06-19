package com.nkm.capstone.air_drawing.util;

/**
 * MathUtils: 선형대수 및 기타 수학 계산에 필요한 정적 유틸리티 메서드들을 제공하는 클래스입니다.
 *
 * <br>
 * - 모든 메서드는 static으로 선언되어 별도의 객체 생성 없이 직접 호출 가능합니다.
 * - 3x3 행렬 연산 등 그래픽 처리나 센서 데이터 보정 등에 유용하게 사용할 수 있습니다.
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * float[] result = new float[9];
 * MathUtils.multiplyMatrices(matA, matB, result);
 *
 * float[] inverse = new float[9];
 * MathUtils.invert3x3Matrix(matA, inverse);
 * }</pre>
 *
 * @author 남경민
 * @since 2025.06.20
 */


public class MathUtils
{
    public static void multiplyMatrices(float[] a, float[] b, float[] out)
    {
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                out[3 * i + j] = 0;
                for (int k = 0; k < 3; ++k)
                {
                    out[3 * i + j] += a[3 * i + k] * b[3 * k + j];
                }
            }
        }
    }

    public static void invert3x3Matrix(float[] a, float[] out)
    {
        float a11 = a[0], a12 = a[1], a13 = a[2];
        float a21 = a[3], a22 = a[4], a23 = a[5];
        float a31 = a[6], a32 = a[7], a33 = a[8];

        float det = a11 * (a22 * a33 - a23 * a32) -
                        a12 * (a21 * a33 - a23 * a31) +
                        a13 * (a21 * a32 - a22 * a31);

        if (Math.abs(det) < 1e-6f)
        {
            throw new IllegalArgumentException("Matrix is singular and cannot be inverted.");
        }

        float invDet = 1.0f / det;

        out[0] =  (a22 * a33 - a23 * a32) * invDet;
        out[1] = -(a12 * a33 - a13 * a32) * invDet;
        out[2] =  (a12 * a23 - a13 * a22) * invDet;

        out[3] = -(a21 * a33 - a23 * a31) * invDet;
        out[4] =  (a11 * a33 - a13 * a31) * invDet;
        out[5] = -(a11 * a23 - a13 * a21) * invDet;

        out[6] =  (a21 * a32 - a22 * a31) * invDet;
        out[7] = -(a11 * a32 - a12 * a31) * invDet;
        out[8] =  (a11 * a22 - a12 * a21) * invDet;
    }
}
