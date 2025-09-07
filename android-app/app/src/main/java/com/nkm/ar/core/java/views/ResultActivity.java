package com.nkm.ar.core.java.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nkm.ar.core.java.views.helloar.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ResultActivity extends AppCompatActivity {

    private String imagePath;
    private String currentImageName;

    private TextView textResult;
    private ImageView imagePreview;
    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        textResult = findViewById(R.id.textResult);
        imagePreview = findViewById(R.id.imagePreview);
        btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(view -> finish());

        Intent intent = getIntent();
        String resultChar = intent.getStringExtra("result_char");
        imagePath = intent.getStringExtra("image_path");

        if (imagePath != null) {
            currentImageName = new File(imagePath).getName();
            loadImageFromPath(imagePath);
            if (resultChar != null) {
                textResult.setText(resultChar);
                saveRecognizedTextForImage(currentImageName, resultChar);
            } else {
                String savedText = loadRecognizedTextForImage(currentImageName);
                textResult.setText(savedText);
            }
        } else {
            String savedText = loadRecognizedText();
            textResult.setText(savedText);
            currentImageName = null;
        }
    }

    private void saveRecognizedTextForImage(String filename, String result) {
        getSharedPreferences("RecognizedTextPrefs", MODE_PRIVATE)
                .edit()
                .putString("result_" + filename, result)
                .apply();
    }

    private String loadRecognizedTextForImage(String filename) {
        return getSharedPreferences("RecognizedTextPrefs", MODE_PRIVATE)
                .getString("result_" + filename, "저장된 결과 없음");
    }

    private String loadRecognizedText() {
        return getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("last_recognized_text", "저장된 결과 없음");
    }

    private void loadImageFromPath(String path) {
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            imagePreview.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
