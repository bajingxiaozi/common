package com.xyf.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
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
            Lg.w(TAG, file, "not exist");
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
    public static List<File> listImages(@Nonnull File directory) {
        return new ArrayList<>(FileUtils.listFiles(directory, new SuffixFileFilter(ImageUtils.IMAGE_SUFFIX, IOCase.INSENSITIVE), null));
    }

    @Nonnull
    public static List<File> listDirectories(@Nonnull File directory) {
        return CollectionUtils.asList(directory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE));
    }

    @Nonnull
    public static String randomName() {
        return UUID.randomUUID().toString();
    }

    @Nonnull
    public static File randomFile(@Nonnull File directory) {
        return new File(directory, randomName());
    }

}
