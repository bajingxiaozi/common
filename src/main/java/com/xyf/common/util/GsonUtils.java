package com.xyf.common.util;

import com.google.gson.GsonBuilder;
import com.xyf.common.annotation.WorkThread;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class GsonUtils {

    private static final String TAG = GsonUtils.class.getSimpleName();

    @WorkThread
    public static void save(@Nonnull Object bean, @Nonnull File file) throws IOException {
        final File tempFile = new File(FileUtils.getTempDirectory(), FileUtils2.randomName());
        try {
            FileUtils.forceMkdirParent(tempFile);
            try (Writer reader = FileUtils2.toWriter(tempFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(bean, reader);
            }

            FileUtils2.deleteFile(file);
            FileUtils.moveFile(tempFile, file);
            Lg.d(TAG, "save", bean, file);
        } finally {
            FileUtils2.deleteFile(tempFile);
        }
    }

}
