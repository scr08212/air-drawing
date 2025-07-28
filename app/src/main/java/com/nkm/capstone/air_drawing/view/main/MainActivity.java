package com.nkm.capstone.air_drawing.view.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nkm.capstone.air_drawing.MotionTracker;
import com.nkm.capstone.air_drawing.R;
import com.nkm.capstone.air_drawing.view.custom.DrawView;

public class MainActivity extends AppCompatActivity {
    MotionTracker motionTracker;

    DrawView drawView;
    Button btnReCalibrate;
    Button btnErase;
    Button btnUndoStroke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        motionTracker = new MotionTracker(this);
        drawView = findViewById(R.id.drawView);
        btnReCalibrate = findViewById(R.id.btnReCalibrate);
        btnErase = findViewById(R.id.btnErase);
        btnUndoStroke = findViewById(R.id.btnUndoStroke);

        motionTracker.SetOnSensorChangedListener(new MotionTracker.OnRotationChangedListener() {
            @Override
            public void Invoke(float pitch, float yaw) {
                drawView.updateDirection(pitch, yaw);
            }
        });

        btnReCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motionTracker.ReCalibrate();
            }
        });

        btnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.EraseCanvas();
            }
        });

        btnUndoStroke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.UndoStroke();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        motionTracker.Resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionTracker.Pause();
    }
}