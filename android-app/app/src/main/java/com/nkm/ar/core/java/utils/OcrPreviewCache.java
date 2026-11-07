package com.nkm.ar.core.java.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Short-lived image previews for OCR requests.
 *
 * These files are stored only in the app's private cache. Only files matching
 * this class's generated naming convention are eligible for deletion or read.
 * No image bytes, recognized text, or exception details are logged.
 */
public final class OcrPreviewCache {
    private static final String PREFIX = "ocr-preview-";
    private static final String SUFFIX = ".png";

    private OcrPreviewCache() {}

    public static String save(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        File file = null;
        try {
            file = File.createTempFile(PREFIX, SUFFIX, context.getCacheDir());
            try (FileOutputStream output = new FileOutputStream(file)) {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw new IOException("Could not encode preview");
                }
            }
            return file.getAbsolutePath();
        } catch (IOException | SecurityException ignored) {
            if (file != null) {
                file.delete();
            }
            return null;
        }
    }

    /** Decode one owned preview and delete it immediately, even on decode failure. */
    public static Bitmap consume(Context context, String path) {
        File file = resolve(context, path);
        if (file == null) {
            return null;
        }
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } finally {
            file.delete();
        }
    }

    /** Drop the preview if the request finishes after its originating Activity exits. */
    public static void deleteIfOwned(Context context, String path) {
        File file = resolve(context, path);
        if (file != null) {
            file.delete();
        }
    }

    /** Remove the fixed-name preview left by versions prior to this change. */
    public static void clearLegacyPreview(Context context) {
        File oldFile = new File(context.getCacheDir(), "temp_drawing.png");
        if (oldFile.isFile()) {
            oldFile.delete();
        }
    }

    private static File resolve(Context context, String path) {
        if (path == null) {
            return null;
        }
        try {
            File cacheRoot = context.getCacheDir().getCanonicalFile();
            File file = new File(path).getCanonicalFile();
            String name = file.getName();
            if (!name.startsWith(PREFIX) || !name.endsWith(SUFFIX)) {
                return null;
            }
            if (!cacheRoot.equals(file.getParentFile())) {
                return null;
            }
            return file;
        } catch (IOException | SecurityException ignored) {
            return null;
        }
    }
}
