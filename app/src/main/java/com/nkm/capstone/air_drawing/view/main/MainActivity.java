package com.nkm.capstone.air_drawing.view.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nkm.capstone.air_drawing.R;
import com.nkm.capstone.air_drawing.data.Stroke;
import com.nkm.capstone.air_drawing.util.MathUtils;
import com.nkm.capstone.air_drawing.view.custom.DrawView;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    SensorManager sensorManager;
    Sensor rotationSensor;

    final float[] orientationAngles = new float[3];

    final float[] initialRotationMatrix = new float[9];
    final float[] currentRotationMatrix = new float[9];
    final float[] relativeRotationMatrix = new float[9];
    boolean initialized = false;

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
                initialized = true;
                return;
            }
            /*
            float[] invInitial = new float[9];
            SensorManager.remapCoordinateSystem(initialRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, invInitial);
            */

            float[] invInitial = new float[9];
            MathUtils.invert3x3Matrix(initialRotationMatrix, invInitial);

            MathUtils.multiplyMatrices(invInitial, currentRotationMatrix, relativeRotationMatrix);

            SensorManager.getOrientation(relativeRotationMatrix, orientationAngles);

            float relativeYaw = orientationAngles[0]; // azimuth
            float relativePitch = orientationAngles[1]; // pitch

            drawView.updateDirection(relativePitch, relativeYaw);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}