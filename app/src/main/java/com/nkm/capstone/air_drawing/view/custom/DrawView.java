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
 * DrawView: 센서로부터 전달받은 방향 정보(pitch, yaw)를 기반으로
 * 화면 중앙을 기준으로 포인터를 이동시키고, 터치 상태에 따라 경로를 그리는 사용자 정의 뷰입니다.
 *
 * <br>
 * - 센서 데이터를 통해 curPoint 위치를 실시간으로 갱신합니다.
 * - 사용자가 화면을 터치하고 있는 동안만 포인터의 이동 경로를 저장하고 점을 연결합니다.
 * - 각 포인터 위치는 PointF로 저장되며, 나중에 선을 그리는 데 사용될 수 있습니다.
 * - 포인터는 터치 중일 경우 빨간색, 아닐 경우 파란색으로 표시됩니다.
 *
 * @author 남경민
 * @since 2025.06.15
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
        curPoint.x = viewCenter.x +  (float) Math.tan(yaw) * scale;
        curPoint.y = viewCenter.y + (float) Math.tan(pitch) * scale;
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