package com.nkm.ar.core.java.network;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Base64;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nkm.ar.core.java.views.ResultActivity;
import com.nkm.ar.core.java.views.helloar.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Live emulator -> host Flask -> real PaddleOCR -> ResultActivity UI.
 * Input comes from a generated synthetic PNG: ARCore camera tracking is not tested.
 */
@RunWith(AndroidJUnit4.class)
public class OcrPipelineE2eTest {
    private static final String EXPECTED = "HELLO 123";

    @Test
    public void sampleImageAppearsAsRecognizedTextOnResultScreen() throws Exception {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream input = testContext.getAssets().open("ocr_sample.png")) {
            byte[] chunk = new byte[4096];
            int count;
            while ((count = input.read(chunk)) != -1) {
                buffer.write(chunk, 0, count);
            }
        }
        byte[] pngBytes = buffer.toByteArray();
        String base64Image = Base64.encodeToString(pngBytes, Base64.NO_WRAP);

        File cacheImage = new File(appContext.getCacheDir(), "e2e-ocr-sample.png");
        try (FileOutputStream file = new FileOutputStream(cacheImage)) {
            file.write(pngBytes);
        }

        Intent hostIntent = new Intent(appContext, ResultActivity.class);
        hostIntent.putExtra("result_char", "PENDING");
        hostIntent.putExtra("image_path", cacheImage.getAbsolutePath());

        try (ActivityScenario<ResultActivity> scenario = ActivityScenario.launch(hostIntent)) {
            // This is the same production network+navigation code used in HelloArActivity.
            scenario.onActivity(activity ->
                    OcrRequestFlow.recognizeAndShow(
                            activity, base64Image, cacheImage.getAbsolutePath()));

            long deadline = SystemClock.elapsedRealtime() + 120_000;
            boolean recognized = false;
            while (SystemClock.elapsedRealtime() < deadline) {
                try {
                    onView(withId(R.id.textResult)).check(matches(withText(EXPECTED)));
                    recognized = true;
                    break;
                } catch (AssertionError | RuntimeException notYetReady) {
                    SystemClock.sleep(750);
                }
            }
            if (!recognized) {
                fail("Android did not display real Flask/PaddleOCR result: " + EXPECTED);
            }
            onView(withId(R.id.imagePreview)).check((view, error) -> {
                if (error != null) throw error;
                assertNotNull("The submitted image should be visible", ((ImageView) view).getDrawable());
            });
        } finally {
            // Synthetic-only test fixture; avoid leaving the image cached on the device.
            cacheImage.delete();
        }
    }
}
