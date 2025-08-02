package com.nkm.ar.core.java.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Vector;

public class ProjectionCanvasView extends View {
    private Paint pointer;
    private Paint lineDrawer;

    private Vector<PointF> points;
    private Vector<Integer> breakPoints;

    private PointF cursorPos = null;

    private boolean showCursor = false;

    public ProjectionCanvasView(Context context) {
        super(context);
        Init();
    }

    public ProjectionCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    private void Init(){
        pointer = new Paint();
        pointer.setColor(Color.RED);
        pointer.setStyle(Paint.Style.FILL);

        lineDrawer = new Paint();
        lineDrawer.setColor(Color.BLACK);
        lineDrawer.setStrokeWidth(8f);
        lineDrawer.setAntiAlias(true);

        cursorPos = new PointF(0,0);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (points == null || points.isEmpty()) return;

        // 1. 데이터의 최소/최대값 계산
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
        for (PointF p : points) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        float dataWidth = maxX - minX;
        float dataHeight = maxY - minY;
        if (dataWidth == 0 || dataHeight == 0) return;

        int canvasWidth = getWidth();
        int canvasHeight = getHeight();

        float centerX = canvasWidth / 2f;
        float centerY = canvasHeight / 2f;

        float dataCenterX = (minX + maxX) / 2f;
        float dataCenterY = (minY + maxY) / 2f;

        // 2. X/Y 비율 고정된 스케일
        float scaleX = canvasWidth / dataWidth;
        float scaleY = canvasHeight / dataHeight;
        float scale = 0.6f * Math.min(scaleX, scaleY); // 여백 확보

        // 3. 그리기
        int breakIndexPointer = 0;
        int nextBreakIndex = (breakPoints != null && breakPoints.size() > 0) ? breakPoints.get(0) : -1;

        for (int i = 1; i < points.size(); i++) {
            if (i == nextBreakIndex) {
                breakIndexPointer++;
                nextBreakIndex = (breakIndexPointer < breakPoints.size()) ? breakPoints.get(breakIndexPointer) : -1;
                continue;
            }

            PointF from = transformToCanvas(points.get(i - 1), scale, centerX, centerY, dataCenterX, dataCenterY);
            PointF to = transformToCanvas(points.get(i), scale, centerX, centerY, dataCenterX, dataCenterY);
            canvas.drawLine(from.x, from.y, to.x, to.y, lineDrawer);
        }

        if(cursorPos != null && showCursor) {
            PointF canvasCursor = transformToCanvas(cursorPos, scale, centerX, centerY, dataCenterX, dataCenterY);
            canvas.drawCircle(canvasCursor.x, canvasCursor.y, 8f, pointer);
        }
    }

    private PointF transformToCanvas(PointF p, float scale, float centerX, float centerY, float dataCenterX, float dataCenterY) {
        float x = (p.x - dataCenterX) * scale + centerX;
        float y = centerY + (p.y - dataCenterY) * scale; // Y축 반전
        return new PointF(x, y);
    }

    public void updateCanvas(Vector<PointF> points, Vector<Integer> breakPoints) {
        this.points = new Vector<>(points);
        this.breakPoints = new Vector<>(breakPoints);
        invalidate();
    }

    public Bitmap getBitmap(){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public void setCursorPos(PointF pos){
        cursorPos.x = pos.x;
        cursorPos.y = pos.y;
        invalidate();
    }

    public void setShowCursor(boolean value){
        showCursor = value;
        invalidate();
    }
}
