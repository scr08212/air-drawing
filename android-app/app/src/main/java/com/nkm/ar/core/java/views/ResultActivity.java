package com.nkm.ar.core.java.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nkm.ar.core.java.utils.OcrPreviewCache;
import com.nkm.ar.core.java.utils.OcrPrivacyCleanup;
import com.nkm.ar.core.java.views.helloar.R;

/**
 * Displays the OCR result for this session only.
 * Recognized text is never stored in SharedPreferences.
 */
public class ResultActivity extends AppCompatActivity {

    private TextView textResult;
    private ImageView imagePreview;
    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OcrPrivacyCleanup.clearLegacyResults(this);

        setContentView(R.layout.activity_result);
        textResult = findViewById(R.id.textResult);
        imagePreview = findViewById(R.id.imagePreview);
        btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(view -> finish());

        Intent intent = getIntent();
        String recognizedText = intent == null ? null : intent.getStringExtra("result_char");
        String imagePath = intent == null ? null : intent.getStringExtra("image_path");

        // Display only the result passed for this request. Never load a prior result.
        textResult.setText(recognizedText == null ? "인식 결과 없음" : recognizedText);

        if (imagePath != null) {
            Bitmap preview = OcrPreviewCache.consume(this, imagePath);
            if (preview != null) {
                imagePreview.setImageBitmap(preview);
            } else {
                Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
