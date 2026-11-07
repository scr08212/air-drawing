package com.nkm.ar.core.java.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.util.Arrays;
import java.util.Vector;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ProjectionCanvasViewTest {
    private static final int CANVAS_SIZE = 200;

    private static PointF p(float x, float y) {
        return new PointF(x, y);
    }

    private static Vector<PointF> points(PointF... values) {
        return new Vector<>(Arrays.asList(values));
    }

    private static Vector<Integer> breaks(Integer... values) {
        return new Vector<>(Arrays.asList(values));
    }

    private static Bitmap render(Vector<PointF> input, Vector<Integer> strokeBreaks) {
        ProjectionCanvasView view = new ProjectionCanvasView(RuntimeEnvironment.getApplication());
        view.layout(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        view.updateCanvas(input, strokeBreaks);
        return view.getBitmap();
    }

    private static void assertInk(Bitmap image, int x, int y) {
        assertTrue("Expected a black stroke at (" + x + "," + y + ")",
                Color.red(image.getPixel(x, y)) < 128);
    }

    private static void assertBackground(Bitmap image, int x, int y) {
        assertEquals("Expected white background at (" + x + "," + y + ")",
                Color.WHITE, image.getPixel(x, y));
    }

    @Test
    public void negativeOnlyCoordinatesUseTheirActualMaximum() {
        Bitmap image = render(points(p(-3, -3), p(-2, -2), p(-1, -1)), breaks());
        try {
            // Correct bounds [-3,-1] center the diagonal from (40,40) to (160,160).
            assertInk(image, 150, 150);
            assertBackground(image, 150, 30);
        } finally {
            image.recycle();
        }
    }

    @Test
    public void horizontalStrokeIsRenderedWithZeroHeight() {
        Bitmap image = render(points(p(-1, 5), p(0, 5), p(1, 5)), breaks());
        try {
            assertInk(image, 150, 100);
            assertBackground(image, 150, 80);
        } finally {
            image.recycle();
        }
    }

    @Test
    public void verticalStrokeIsRenderedWithZeroWidth() {
        Bitmap image = render(points(p(5, -1), p(5, 0), p(5, 1)), breaks());
        try {
            assertInk(image, 100, 150);
            assertBackground(image, 80, 150);
        } finally {
            image.recycle();
        }
    }

    @Test
    public void singleOrCoincidentPointRendersCenteredDot() {
        for (Vector<PointF> input : Arrays.asList(
                points(p(7, -2)),
                points(p(7, -2), p(7, -2)))) {
            Bitmap image = render(input, breaks());
            try {
                assertInk(image, 100, 100);
                assertBackground(image, 120, 100);
            } finally {
                image.recycle();
            }
        }
    }

    @Test
    public void strokeBreakDoesNotConnectSeparateStrokes() {
        Bitmap image = render(
                points(p(-1, -1), p(-1, 1), p(1, -1), p(1, 1)),
                breaks(2));
        try {
            assertInk(image, 40, 100);
            assertInk(image, 160, 100);
            assertBackground(image, 100, 100);
        } finally {
            image.recycle();
        }
    }
}
