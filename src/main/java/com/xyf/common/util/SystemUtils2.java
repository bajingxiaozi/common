package com.xyf.common.util;

import com.google.common.base.Preconditions;
import com.xyf.common.annotation.WorkThread;
import io.reactivex.ObservableEmitter;
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemUtils2 {

    private static final String TAG = SystemUtils2.class.getSimpleName();

    public static void killWindowsProcess(@Nonnull String name) throws Exception {
        Preconditions.checkArgument(SystemUtils.IS_OS_WINDOWS);

        execute("taskkill", "/im", name, "/f");
    }

    @WorkThread
    public static List<String> execute(@Nonnull List<String> parameters) throws Exception {
        Lg.d(TAG, parameters);
        ProcessBuilder processBuilder = new ProcessBuilder(parameters).redirectErrorStream(true);
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> messages = new ArrayList<>();
            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                messages.add(message);
            }

            final int exitValue = process.waitFor();
            Lg.d(TAG, parameters, messages, exitValue);

            return messages;
        }
    }

    @WorkThread
    public static List<String> execute(@Nonnull String... parameters) throws Exception {
        return execute(Arrays.asList(parameters));
    }

    @WorkThread
    public static boolean execute(@Nullable ObservableEmitter<String> emitter, @Nonnull List<String> parameters) throws Exception {
        Lg.d(TAG, parameters);
        ProcessBuilder processBuilder = new ProcessBuilder(parameters).redirectErrorStream(true);
        Process executor = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(executor.getInputStream(), StandardCharsets.UTF_8))) {
            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                Lg.d(TAG, message);
                if (emitter != null) {
                    emitter.onNext(message);
                }
            }
        }

        final int NORMAL_EXIT = 0;
        final int exitValue = executor.waitFor();
        Lg.d(TAG, parameters, exitValue);
        return exitValue == NORMAL_EXIT;
    }

    @WorkThread
    public static boolean execute(@Nullable ObservableEmitter<String> emitter, @Nonnull String... parameters) throws Exception {
        return execute(emitter, Arrays.asList(parameters));
    }

}
