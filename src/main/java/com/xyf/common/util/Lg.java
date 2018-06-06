package com.xyf.common.util;

import com.google.common.base.Strings;
import io.reactivex.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Lg {

    private static final Map<String, Logger> LOGGER_MAP = new HashMap<>();

    private static Logger getLogger(@Nonnull String tag) {
        if (!LOGGER_MAP.containsKey(tag)) {
            LOGGER_MAP.put(tag, LoggerFactory.getLogger(tag));
        }

        return LOGGER_MAP.get(tag);
    }

    private static final int METHOD_BORDER_LENGTH = 100;
    private static final String METHOD_HEAD;
    private static final String METHOD_SEPARATE;
    private static final String MESSAGE_TAIL;

    static {
        METHOD_HEAD = "┌" + Strings.repeat("─", METHOD_BORDER_LENGTH) + "┐";
        METHOD_SEPARATE = "│" + Strings.repeat("-", METHOD_BORDER_LENGTH) + "│";
        MESSAGE_TAIL = "└" + Strings.repeat("─", METHOD_BORDER_LENGTH) + "┘";
    }

    private static void logMethodHead(@Nonnull TYPE type, @Nonnull String tag) {
        logBorder(type, tag, METHOD_HEAD);
        final StackTraceElement element = new Throwable().getStackTrace()[3];
        logLine(type, tag, String.format("%s(%s:%d)", element.getMethodName(), element.getFileName(), element.getLineNumber()));
        logBorder(type, tag, METHOD_SEPARATE);
    }

    private enum TYPE {
        INFO, ERROR
    }

    private static void logMethodTail(@Nonnull TYPE type, @Nonnull String tag) {
        switch (type) {
            case ERROR:
                getLogger(tag).error(MESSAGE_TAIL);
                break;
            default:
                getLogger(tag).info(MESSAGE_TAIL);
                break;
        }
    }

    public static void i(@Nonnull String tag, @Nullable Object... messages) {
        log(TYPE.INFO, tag, messages);
    }

    private static void log(@NonNull TYPE type, @Nonnull String tag, @Nullable Object... objects) {
        logMethodHead(type, tag);
        if (objects != null) {
            for (Object obj : objects) {
                if (obj instanceof Throwable) {
                    Throwable throwable = (Throwable) obj;
                    logLine(type, tag, throwable);
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        logLine(type, tag, element);
                    }
                } else if (obj instanceof Collection) {
                    Collection collection = (Collection) obj;
                    for (Object item : collection) {
                        logLine(type, tag, item);
                    }
                } else {
                    for (String s : obj.toString().split("\n")) {
                        logLine(type, tag, s);
                    }
                }
            }
        }
        logMethodTail(type, tag);
    }

    private static void logLine(@NonNull TYPE type, @Nonnull String tag, @Nonnull Object message) {
        final String line = String.format("│%-" + METHOD_BORDER_LENGTH + "s│", message.toString());
        switch (type) {
            case ERROR:
                getLogger(tag).error(line);
                break;
            default:
                getLogger(tag).info(line);
                break;
        }
    }

    private static void logBorder(@NonNull TYPE type, @Nonnull String tag, @Nonnull String border) {
        switch (type) {
            case ERROR:
                getLogger(tag).error(border);
                break;
            default:
                getLogger(tag).info(border);
                break;
        }
    }

    public static void e(@Nonnull String tag, @Nullable Object... messages) {
        log(TYPE.ERROR, tag, messages);
    }

}
