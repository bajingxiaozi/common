package com.xyf.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

public class FileUtils2 {

    private static final String TAG = FileUtils2.class.getSimpleName();

    public static boolean isFile(@Nullable File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isDirectory(@Nullable File directory) {
        return directory != null && directory.exists() && directory.isDirectory();
    }

    public static void deleteFile(@Nonnull File file) {
        if (!file.exists()) {
            Lg.e(TAG, file, "not exist");
            return;
        }

        if (file.isFile()) {
            boolean result = file.delete();
            Lg.i(TAG, file, result);
        } else {
            Lg.e(TAG, "not a file", file);
        }
    }

    @Nonnull
    public static String randomName() {
        return UUID.randomUUID().toString();
    }

}
