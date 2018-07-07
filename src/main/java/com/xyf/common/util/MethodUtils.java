package com.xyf.common.util;

public class MethodUtils {

    public static String getMethodTag() {
        final StackTraceElement element = new Throwable().getStackTrace()[1];
        return String.format("%s.%s(%d)", element.getFileName(), element.getMethodName(), element.getLineNumber());
    }

}
