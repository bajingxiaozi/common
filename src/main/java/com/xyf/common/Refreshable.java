package com.xyf.common;

import com.xyf.common.annotation.UiThread;

public interface Refreshable {

    @UiThread
    void refresh();

}
