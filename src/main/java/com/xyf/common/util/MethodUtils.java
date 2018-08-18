package com.xyf.common.util;

import javax.annotation.Nonnull;

public class MethodUtils {

    @Nonnull
    public static String getTag() {
        final StackTraceElement element = new Throwable().getStackTrace()[1];
        return String.format("%s.%s(%d)", element.getFileName(), element.getMethodName(), element.getLineNumber());
    }

    @Nonnull
    public static String getTag(@Nonnull Object object) {
        return getTag() + object;
    }

}
