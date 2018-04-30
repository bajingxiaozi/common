package com.xyf.common.util;

import org.apache.commons.io.IOCase;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ImageUtils {

    public static final String PNG_SUFFIX = ".png";
    public static final List<String> IMAGE_SUFFIX = Arrays.asList(PNG_SUFFIX, ".jpg");

    public static boolean isImage(@Nonnull File file) {
        return FileUtils2.isFile(file) && isImage(file.getName());
    }

    public static boolean isImage(@Nonnull String name) {
        for (String suffix : IMAGE_SUFFIX) {
            if (IOCase.INSENSITIVE.checkEndsWith(name, suffix)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPng(@Nonnull String name) {
        return IOCase.INSENSITIVE.checkEndsWith(name, PNG_SUFFIX);
    }

    public static boolean isPng(@Nonnull File name) {
        return isPng(name.getName());
    }

}
