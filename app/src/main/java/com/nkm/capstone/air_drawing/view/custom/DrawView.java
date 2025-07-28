package com.nkm.capstone.air_drawing.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

public class DrawView extends View {
    private Paint pointer;
    private Paint lineDrawer;

    private PointF viewCenter;
    private PointF curPoint;

    private Vector<PointF> curStroke;
    private Vector<Vector<PointF>> strokes;

    private boolean isTouching = false;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pointer = new Paint();
        pointer.setColor(Color.RED);
        pointer.setStyle(Paint.Style.FILL);

        lineDrawer = new Paint();
        lineDrawer.setColor(Color.BLACK);
        lineDrawer.setStrokeWidth(8f);
        lineDrawer.setAntiAlias(true);

        viewCenter = new PointF();
        curPoint = new PointF();

        curStroke = new Vector<>();
        strokes = new Vector<>();
    }

    public void updateDirection(float pitch, float yaw) {
        float scale = 1000f;
        curPoint.x = viewCenter.x + (float) Math.sin(yaw) * scale;
        curPoint.y = viewCenter.y + (float) Math.sin(pitch) * scale;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewCenter.x = w / 2f;
        viewCenter.y = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (isTouching) {
            pointer.setColor(Color.RED);
            curStroke.add(new PointF(curPoint.x, curPoint.y));
        } else {
            pointer.setColor(Color.BLUE);
        }

        for (var stroke : strokes) {
            for (int i = 1; i < stroke.size(); i++) {
                PointF from = stroke.get(i - 1);
                PointF to = stroke.get(i);
                canvas.drawLine(from.x, from.y, to.x, to.y, lineDrawer);
            }
        }

        if (isTouching && curStroke.size() >= 2) {
            for (int i = 1; i < curStroke.size(); i++) {
                PointF from = curStroke.get(i - 1);
                PointF to = curStroke.get(i);
                canvas.drawLine(from.x, from.y, to.x, to.y, lineDrawer);
            }
        }

        canvas.drawCircle(curPoint.x, curPoint.y, 10f, pointer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isTouching = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (curStroke.size() >= 2) {
                    strokes.add(new Vector<>(curStroke));
                }
                curStroke.clear();

                isTouching = false;
                break;
        }

        return true;
    }

    public void EraseCanvas() {
        strokes.clear();
        curStroke.clear();
        invalidate();
    }

    public void UndoStroke() {
        if (!strokes.isEmpty())
            strokes.removeElementAt(strokes.size() - 1);
    }
}