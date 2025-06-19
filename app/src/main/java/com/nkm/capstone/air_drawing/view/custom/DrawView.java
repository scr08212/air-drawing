package com.nkm.capstone.air_drawing.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.nkm.capstone.air_drawing.data.Stroke;

import java.util.Vector;

/**
 * DrawView: 센서 방향(pitch, yaw)에 따라 화면상 포인터를 이동시키고,
 * 터치 입력과 연동하여 자유 곡선을 그릴 수 있는 사용자 정의 캔버스 뷰입니다.
 *
 * <br>
 * <b>주요 기능:</b>
 * <ul>
 *     <li>센서 방향에 따라 포인터 위치(curPoint)를 실시간 업데이트</li>
 *     <li>터치 중일 때만 현재 포인터 위치를 Stroke에 저장</li>
 *     <li>각 Stroke는 사용자의 한 번의 드로잉 궤적이며, 선으로 구성됨</li>
 *     <li>완성된 Stroke 리스트는 Vector로 저장되어 Undo 및 전체 지우기 가능</li>
 *     <li>터치 상태에 따라 포인터 색상(빨간색/파란색) 변화</li>
 * </ul>
 *
 * <br>
 * <b>공개 메서드:</b>
 * <ul>
 *     <li>{@code updateDirection(pitch, yaw)}: 센서 방향 반영</li>
 *     <li>{@code EraseCanvas()}: 모든 Stroke 초기화</li>
 *     <li>{@code UndoStroke()}: 마지막 Stroke 한 개 제거</li>
 * </ul>
 *
 * @author 남경민
 * @since 2025.06.20
 */

public class DrawView extends View
{
    private Paint pointer;
    private Paint lineDrawer;

    private PointF viewCenter;
    private PointF curPoint;

    private Stroke curStroke;
    private Vector<Stroke> strokes;

    private boolean isTouching = false;


    public DrawView(Context context)
    {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        pointer = new Paint();
        pointer.setColor(Color.RED);
        pointer.setStyle(Paint.Style.FILL);

        lineDrawer = new Paint();
        lineDrawer.setColor(Color.BLACK);
        lineDrawer.setStrokeWidth(8f);
        lineDrawer.setAntiAlias(true);

        viewCenter = new PointF();
        curPoint = new PointF();

        curStroke = new Stroke();
        strokes = new Vector<>();
    }

    public void updateDirection(float pitch, float yaw)
    {
        float scale = 1000f;
        curPoint.x = viewCenter.x +  (float) Math.sin(yaw) * scale;
        curPoint.y = viewCenter.y + (float) Math.sin(pitch) * scale;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        viewCenter.x = w / 2f;
        viewCenter.y = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if(isTouching)
        {
            pointer.setColor(Color.RED);
            curStroke.points.add(new PointF(curPoint.x, curPoint.y));
        }
        else
        {
            pointer.setColor(Color.BLUE);
        }

        for(Stroke stroke: strokes)
        {
            for(Pair<PointF, PointF> line: stroke.lineSegments())
            {
                canvas.drawLine(line.first.x, line.first.y, line.second.x, line.second.y, lineDrawer);
            }
        }

        if (isTouching && curStroke.points.size() >= 2)
        {
            for (Pair<PointF, PointF> line : curStroke.lineSegments())
            {
                canvas.drawLine(line.first.x, line.first.y, line.second.x, line.second.y, lineDrawer);
            }
        }

        canvas.drawCircle(curPoint.x, curPoint.y, 10f, pointer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isTouching = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(curStroke.points.size() >= 2)
                {
                    strokes.add(new Stroke(curStroke));
                }
                curStroke.points.clear();

                isTouching = false;
                break;
        }

        return true;
    }

    public void EraseCanvas()
    {
        strokes.clear();
        curStroke.points.clear();
        invalidate();
    }

    public void UndoStroke()
    {
        if(!strokes.isEmpty())
            strokes.removeElementAt(strokes.size() - 1);
    }
}