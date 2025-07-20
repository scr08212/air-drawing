package com.nkm.capstone.air_drawing.data;

public class Point3F
{
     public double x, y, z;

     public Point3F(double x, double y, double z)
     {
         this.x = x;
         this.y = y;
         this.z = z;
     }

     public Point3F(Point3F other)
     {
         this.x = other.x;
         this.y = other.y;
         this.z = other.z;
     }

     public void add(Point3F r)
     {
         this.x += r.x;
         this.y += r.y;
         this.z += r.z;
     }

     public void add(double x, double y, double z)
     {
         this.x += x;
         this.y += y;
         this.z += z;
     }

     public void multiply(double scale)
     {
         this.x *= scale;
         this.y *= scale;
         this.z *= scale;
     }

     public void divide(double scalar)
     {
         if(scalar == 0)
             throw new ArithmeticException("0으로 나눌 수 없습니다.");

         this.x /= scalar;
         this.y /= scalar;
         this.z /= scalar;
     }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Point3F other = (Point3F) obj;
        return Double.compare(x, other.x) == 0 &&
                Double.compare(y, other.y) == 0 &&
                Double.compare(z, other.z) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }
}