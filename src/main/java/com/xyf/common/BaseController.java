package com.xyf.common;

import com.xyf.common.annotation.UiThread;

public abstract class BaseController extends RefreshableRxLifeCircle {

    @UiThread
    public void onStop() {
        clearDisposable();
    }

}
