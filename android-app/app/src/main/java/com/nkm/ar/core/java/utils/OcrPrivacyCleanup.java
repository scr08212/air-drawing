package com.nkm.ar.core.java.utils;

import android.content.Context;

/** Remove OCR data saved by older builds without touching unrelated preferences. */
public final class OcrPrivacyCleanup {
    private OcrPrivacyCleanup() {}

    public static void clearLegacyResults(Context context) {
        context.getSharedPreferences("RecognizedTextPrefs", Context.MODE_PRIVATE)
                .edit().clear().apply();
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .edit().remove("last_recognized_text").apply();
        OcrPreviewCache.clearLegacyPreview(context);
    }
}
