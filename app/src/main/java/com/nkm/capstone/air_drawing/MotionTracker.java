package com.nkm.capstone.air_drawing;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.nkm.capstone.air_drawing.util.DeltaTimer;
import com.nkm.capstone.air_drawing.util.MathUtils;

public class MotionTracker implements SensorEventListener {
    public interface OnRotationChangedListener {
        void Invoke(float pitch, float yaw);
    }

    OnRotationChangedListener onRotationChangedListener;

    Context context;

    SensorManager sensorManager;
    Sensor rotationSensor;
    Sensor linearAccSensor;

    DeltaTimer time;
    KalmanFilter yawKalmanFilter, pitchKalmanFilter;

    boolean initialized = false;

    final float[] orientationAngles = new float[3];
    final float[] initialRotationMatrix = new float[9];
    final float[] invertedInitialRotationMatrix = new float[9];
    final float[] currentRotationMatrix = new float[9];
    final float[] relativeRotationMatrix = new float[9];


    public MotionTracker(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        time = new DeltaTimer();
        yawKalmanFilter = new KalmanFilter(0.001f, 0.05f, 0f);
        pitchKalmanFilter = new KalmanFilter(0.001f, 0.05f, 0f);
    }

    public void Resume() {
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, linearAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void Pause() {
        sensorManager.unregisterListener(this);
    }

    public void ReCalibrate() {
        initialized = false;
    }

    public void SetOnSensorChangedListener(OnRotationChangedListener l) {
        onRotationChangedListener = l;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            time.update();
            SensorManager.getRotationMatrixFromVector(currentRotationMatrix, event.values);

            if (!initialized) {
                System.arraycopy(currentRotationMatrix, 0, initialRotationMatrix, 0, 9);
                MathUtils.invert3x3Matrix(initialRotationMatrix, invertedInitialRotationMatrix);
                initialized = true;
                return;
            }

            MathUtils.multiplyMatrices(invertedInitialRotationMatrix, currentRotationMatrix, relativeRotationMatrix);
            SensorManager.getOrientation(relativeRotationMatrix, orientationAngles);

            float relativeYaw = orientationAngles[0]; // azimuth
            float relativePitch = orientationAngles[1]; // pitch

            float filteredYaw = yawKalmanFilter.update(relativeYaw);
            float filteredPitch = pitchKalmanFilter.update(relativePitch);

            onRotationChangedListener.Invoke(filteredPitch, filteredYaw);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
