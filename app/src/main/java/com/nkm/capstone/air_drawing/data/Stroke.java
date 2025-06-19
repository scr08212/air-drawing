package com.nkm.capstone.air_drawing.data;

import android.graphics.PointF;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import android.util.Pair;

/**
 * Stroke: 사용자의 한 번의 터치 또는 드로잉 동작으로 생성된
 * 연속적인 선 궤적을 저장하는 데이터 클래스입니다.
 *
 * <br>
 * <b>기능 설명:</b>
 * <ul>
 *     <li>각 Stroke는 PointF 객체의 리스트로 구성되며, 그 순서에 따라 연속적인 궤적을 나타냅니다.</li>
 *     <li>깊은 복사 생성자(복사 생성자)를 제공하여 기존 Stroke를 안전하게 복제할 수 있습니다.</li>
 *     <li>{@code lineSegments()}를 통해 선분 쌍(Pair<PointF, PointF>)을 iterable 형태로 반환하여
 *     Canvas에 선을 쉽게 그릴 수 있도록 지원합니다.</li>
 *     <li>DrawView 등에서 사용자가 그린 경로를 분리, 저장, 렌더링하는 데 사용됩니다.</li>
 * </ul>
 *
 * @author 남경민
 * @since 2025.06.20
 */

public class Stroke
{
    public Vector<PointF> points = new Vector<>();

    public Stroke()
    {

    }

    public Stroke(Stroke other)
    {
        for(PointF point: other.points)
        {
            points.add(new PointF(point.x, point.y));
        }
    }

    public Iterable<Pair<PointF, PointF>> lineSegments()
    {
        return () -> new Iterator<Pair<PointF, PointF>>()
        {
            int index = 1;
            @Override
            public boolean hasNext()
            {
                return index < points.size();
            }

            @Override
            public Pair<PointF, PointF> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return new Pair<>(points.get(index - 1), points.get(index++));
            }
        };
    }
}
