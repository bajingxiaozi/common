package com.xyf.common.util;

import com.xyf.common.annotation.UiThread;

public class ClickHelper {

    private static final long CLICK_INTERVAL = 100;
    private static long lastClickTime;

    @UiThread
    public static boolean canClick() {
        if (Math.abs(System.currentTimeMillis() - lastClickTime) > CLICK_INTERVAL) {
            lastClickTime = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

}
