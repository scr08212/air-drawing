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
 * - 각 Stroke는 PointF 리스트로 구성되며, 선을 구성하는 순차적인 좌표들을 포함합니다.
 * - DrawView에서 터치 구간마다 하나의 Stroke 인스턴스를 생성하여 분리된 선을 관리합니다.
 * - 이를 통해 터치 중단 구간을 구분하고, 자연스러운 라인 렌더링이 가능합니다.
 *
 * @author 남경민
 * @since 2025.06.15
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
