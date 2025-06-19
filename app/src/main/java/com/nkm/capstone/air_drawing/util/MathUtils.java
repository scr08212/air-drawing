package com.nkm.capstone.air_drawing.util;

/**
 * MathUtils: 선형대수 및 기타 수학 계산과 관련된 유틸리티 함수들을 모아둔 클래스입니다.
 *
 * <br>
 * - 모든 메서드는 static으로 선언되어 객체 생성 없이 바로 접근 가능합니다.
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * float[] result = new float[9];
 * MathUtils.multiplyMatrices(matA, matB, result);
 * }</pre>
 *
 * @author 남경민
 * @since 2025.06.15
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

        float det =
                a11 * (a22 * a33 - a23 * a32) -
                        a12 * (a21 * a33 - a23 * a31) +
                        a13 * (a21 * a32 - a22 * a31);

        if (Math.abs(det) < 1e-6f) {
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
