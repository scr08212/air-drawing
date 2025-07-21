package com.nkm.capstone.air_drawing.data;

public class DeltaTimer
{
    long lastTime = 0;
    public double deltaTime;

    public DeltaTimer()
    {
        lastTime = System.nanoTime();
        deltaTime = 0;
    }

    public void update()
    {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
        lastTime = currentTime;
    }
}