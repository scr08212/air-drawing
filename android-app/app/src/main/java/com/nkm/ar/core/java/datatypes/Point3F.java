package com.nkm.ar.core.java.datatypes;

public class Point3F{
    public float x, y, z;

    public Point3F(){
        x = y = z = 0f;
    }

    public Point3F(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3F add(Point3F other) {
        return new Point3F(x + other.x, y + other.y, z + other.z);
    }

    public Point3F subtract(Point3F other) {
        return new Point3F(x - other.x, y - other.y, z - other.z);
    }

    public Point3F scale(float s) {
        return new Point3F(x * s, y * s, z * s);
    }

    public float dot(Point3F other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Point3F cross(Point3F b) {
        return new Point3F(
                y * b.z - z * b.y,
                z * b.x - x * b.z,
                x * b.y - y * b.x
        );
    }

    public float norm() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Point3F normalize() {
        float n = norm();
        if (n == 0) return new Point3F(0, 0, 0);
        return scale(1.0f / n);
    }

    public Point3F negate() {
        return new Point3F(-x, -y, -z);
    }
}