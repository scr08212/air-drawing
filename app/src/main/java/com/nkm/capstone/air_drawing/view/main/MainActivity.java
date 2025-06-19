package com.nkm.capstone.air_drawing.view.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nkm.capstone.air_drawing.KalmanFilter;
import com.nkm.capstone.air_drawing.R;
import com.nkm.capstone.air_drawing.util.MathUtils;
import com.nkm.capstone.air_drawing.view.custom.DrawView;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    SensorManager sensorManager;
    Sensor rotationSensor;

    final float[] orientationAngles = new float[3];
    final float[] initialRotationMatrix = new float[9];
    final float[] invertedInitialRotationMatrix = new float[9];
    final float[] currentRotationMatrix = new float[9];
    final float[] relativeRotationMatrix = new float[9];
    boolean initialized = false;
    private final KalmanFilter yawFilter = new KalmanFilter(0.001f, 0.05f, 0f);
    private final KalmanFilter pitchFilter = new KalmanFilter(0.001f, 0.05f, 0f);

    DrawView drawView;
    Button btnReCalibrate;
    Button btnErase;
    Button btnUndoStroke;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawView = findViewById(R.id.drawView);
        btnReCalibrate = findViewById(R.id.btnReCalibrate);
        btnErase = findViewById(R.id.btnErase);
        btnUndoStroke = findViewById(R.id.btnUndoStroke);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        btnReCalibrate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                initialized = false;
            }
        });

        btnErase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawView.EraseCanvas();
            }
        });

        btnUndoStroke.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawView.UndoStroke();
            }
        });


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(rotationSensor != null)
        {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            SensorManager.getRotationMatrixFromVector(currentRotationMatrix, event.values);

            if (!initialized)
            {
                System.arraycopy(currentRotationMatrix, 0, initialRotationMatrix, 0, 9);
                MathUtils.invert3x3Matrix(initialRotationMatrix, invertedInitialRotationMatrix);
                initialized = true;
                return;
            }

            MathUtils.multiplyMatrices(invertedInitialRotationMatrix, currentRotationMatrix, relativeRotationMatrix);
            SensorManager.getOrientation(relativeRotationMatrix, orientationAngles);

            float relativeYaw = orientationAngles[0]; // azimuth
            float relativePitch = orientationAngles[1]; // pitch

            float filteredYaw = yawFilter.update(relativeYaw);
            float filteredPitch = pitchFilter.update(relativePitch);

            drawView.updateDirection(filteredPitch, filteredYaw);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}