package com.nkm.ar.core.java.utils.projectors;

import android.graphics.PointF;
import com.nkm.ar.core.java.datatypes.Point3F;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import java.util.Vector;

public class TestProjector {
    public static Vector<PointF> project(Vector<Point3F> points3D, Point3F origin, Point3F originDir, Point3F originRight) {
        Vector3D originVec = new Vector3D(origin.x, origin.y, origin.z);
        Vector3D originDirVec = new Vector3D(originDir.x, originDir.y, originDir.z);
        Vector3D originRightVec = new Vector3D(originRight.x, originRight.y, originRight.z);
        Vector3D normalizedDir = originDirVec.normalize();

        // 축 이동
        Rotation rotation = new Rotation(Vector3D.PLUS_J, Vector3D.PLUS_K, normalizedDir, originRightVec);

        Vector<PointF> projectedPoints = new Vector<>();

        // 평행이동 및 투영
        for (Point3F point3D : points3D) {
            Vector3D transformedVec = new Vector3D(point3D.x, point3D.y, point3D.z).subtract(originVec);

            Vector3D rotatedVec = rotation.applyTo(transformedVec);

            float projectedX = (float) rotatedVec.getX();
            float projectedY = (float) rotatedVec.getY();

            projectedPoints.add(new PointF(projectedX, projectedY));
        }

        return projectedPoints;
    }
}