package com.xyf.common.util;

import com.google.common.base.Preconditions;
import com.xyf.common.annotation.WorkThread;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemUtils {

    private static final String TAG = SystemUtils.class.getSimpleName();

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static void killWindowsProces(@Nonnull String name) throws Exception {
        Preconditions.checkArgument(isWindows());

        execute("taskkill" + "/im" + name + "/f");
    }

    @WorkThread
    public static List<String> execute(@Nonnull List<String> parameters) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(parameters);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> messages = new ArrayList<>();
        while (true) {
            String message = reader.readLine();
            if (message == null) {
                break;
            }

            Lg.i(TAG, parameters, messages);
            messages.add(message);
        }
        final int exitValue = process.waitFor();
        Lg.i(TAG, parameters, exitValue);

        return messages;
    }

    @WorkThread
    public static List<String> execute(@Nonnull String... parameters) throws Exception {
        return execute(Arrays.asList(parameters));
    }

}
