package com.nkm.ar.core.java.utils.projectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.PointF;

import com.nkm.ar.core.java.datatypes.Point3F;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Vector;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class TestProjectorTest {
    private static final float EPSILON = 0.0001f;

    private static Point3F p(float x, float y, float z) {
        return new Point3F(x, y, z);
    }

    private static Vector<Point3F> points(Point3F... values) {
        return new Vector<>(Arrays.asList(values));
    }

    private static void assertPoint(PointF actual, float x, float y) {
        assertEquals(x, actual.x, EPSILON);
        assertEquals(y, actual.y, EPSILON);
    }

    @Test
    public void alignedBasisSubtractsOriginAndDiscardsDepth() {
        // Constructor maps +Y -> +Y and +Z -> +Z, so R is identity.
        Vector<PointF> result = TestProjector.project(
                points(p(3, 4, 5), p(5, 1, 100), p(5, 1, -50)),
                p(3, 4, 5), p(0, 1, 0), p(0, 0, 1));

        assertEquals(3, result.size());
        assertPoint(result.get(0), 0, 0);
        assertPoint(result.get(1), 2, -3);
        assertPoint(result.get(2), 2, -3);
    }

    @Test
    public void nontrivialBasisRotatesAxesBeforeXYProjection() {
        // Rotation(+Y, +Z, +X, +Y) maps +Y to +X and +Z to +Y.
        Vector<PointF> result = TestProjector.project(
                points(p(0, 1, 0), p(0, 0, 1), p(0, -1, 0), p(1, 0, 0)),
                p(0, 0, 0), p(1, 0, 0), p(0, 1, 0));

        assertPoint(result.get(0), 1, 0);
        assertPoint(result.get(1), 0, 1);
        assertPoint(result.get(2), -1, 0);
        assertPoint(result.get(3), 0, 0); // depth axis is discarded
    }

    @Test
    public void translatingOriginAndTrajectoryTogetherDoesNotChangeOutput() {
        Vector<PointF> original = TestProjector.project(
                points(p(0, 1, 0), p(0, 0, 1)),
                p(0, 0, 0), p(1, 0, 0), p(0, 1, 0));
        Vector<PointF> translated = TestProjector.project(
                points(p(10, -3, 7), p(10, -4, 8)),
                p(10, -4, 7), p(1, 0, 0), p(0, 1, 0));

        assertEquals(original.size(), translated.size());
        for (int i = 0; i < original.size(); i++) {
            assertPoint(translated.get(i), original.get(i).x, original.get(i).y);
        }
    }

    @Test
    public void negativeCoordinatesDoNotReverseSignsWithAlignedBasis() {
        Vector<PointF> result = TestProjector.project(
                points(p(-2, -3, -4)), p(0, 0, 0), p(0, 1, 0), p(0, 0, 1));
        assertPoint(result.get(0), -2, -3);
    }

    @Test
    public void emptyTrajectoryReturnsNoProjectedPoints() {
        Vector<PointF> result = TestProjector.project(
                points(), p(0, 0, 0), p(0, 1, 0), p(0, 0, 1));
        assertTrue(result.isEmpty());
    }
}
