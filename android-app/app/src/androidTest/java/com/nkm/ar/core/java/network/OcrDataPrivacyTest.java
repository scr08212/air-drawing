package com.nkm.ar.core.java.network;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.nkm.ar.core.java.utils.OcrPreviewCache;
import com.nkm.ar.core.java.views.ResultActivity;
import com.nkm.ar.core.java.views.helloar.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;

@RunWith(AndroidJUnit4.class)
public class OcrDataPrivacyTest {

    @Test
    public void legacyOcrPreferencesArePurgedWithoutRemovingOtherSettings() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences results = context.getSharedPreferences(
                "RecognizedTextPrefs", Context.MODE_PRIVATE);
        SharedPreferences general = context.getSharedPreferences(
                "MyAppPrefs", Context.MODE_PRIVATE);
        results.edit().putString("result_old-image.png", "PRIVATE_OLD_TEXT").commit();
        general.edit().putString("last_recognized_text", "PRIVATE_RECENT_TEXT")
                .putString("unrelated_setting", "retain-this").commit();

        try (ActivityScenario<ResultActivity> ignored =
                ActivityScenario.launch(new Intent(context, ResultActivity.class))) {
            onView(withId(R.id.textResult)).check(matches(withText("인식 결과 없음")));
            assertTrue("All legacy recognized text should be cleared", results.getAll().isEmpty());
            assertFalse("Legacy last result should be removed",
                    general.contains("last_recognized_text"));
            assertEquals("retain-this", general.getString("unrelated_setting", null));
        } finally {
            general.edit().remove("unrelated_setting").apply();
        }
    }

    @Test
    public void privatePreviewIsDecodedAndImmediatelyDeleted() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap image = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        String path = null;
        try {
            path = OcrPreviewCache.save(context, image);
            assertNotNull("Preview should have a cache path", path);
            assertTrue(new File(path).exists());
            Bitmap decoded = OcrPreviewCache.consume(context, path);
            assertNotNull("Preview should be decoded", decoded);
            decoded.recycle();
            assertFalse("Preview must not persist after it is read", new File(path).exists());
        } finally {
            image.recycle();
            OcrPreviewCache.deleteIfOwned(context, path);
        }
    }

    @Test
    public void filesOutsidePrivateCacheCannotBeReadOrDeleted() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File unrelated = new File(context.getFilesDir(), "ocr-preview-not-owned.png");
        try {
            try (FileOutputStream output = new FileOutputStream(unrelated)) {
                output.write(new byte[] {1, 2, 3, 4});
            }
            assertNull(OcrPreviewCache.consume(context, unrelated.getAbsolutePath()));
            OcrPreviewCache.deleteIfOwned(context, unrelated.getAbsolutePath());
            assertTrue("An external path must not be deleted", unrelated.exists());
        } finally {
            unrelated.delete();
        }
    }

    @Test
    public void legacyFixedFilenamePreviewIsRemoved() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File old = new File(context.getCacheDir(), "temp_drawing.png");
        try {
            try (FileOutputStream output = new FileOutputStream(old)) {
                output.write(new byte[] {1, 2, 3, 4});
            }
            OcrPreviewCache.clearLegacyPreview(context);
            assertFalse("Old fixed-name preview should be removed", old.exists());
        } finally {
            old.delete();
        }
    }
}
