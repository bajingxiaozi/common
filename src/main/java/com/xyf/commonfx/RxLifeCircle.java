package com.xyf.commonfx;

import com.xyf.common.annotation.UiThread;
import io.reactivex.disposables.Disposable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RxLifeCircle {

    @UiThread
    protected void addDisposable(@Nonnull String tag, @Nonnull Disposable disposable) {
        disposableMap.put(tag, disposable);
    }

    @UiThread
    protected void addDisposable(@Nonnull Disposable disposable) {
        disposables.add(disposable);
    }

    @UiThread
    protected void removeDisposable(@Nonnull String tag) {
        if (disposableMap.containsKey(tag)) {
            disposableMap.get(tag).dispose();
        }
        disposableMap.remove(tag);
    }

    @UiThread
    protected void clearDisposable() {
        for (Disposable disposable : disposableMap.values()) {
            disposable.dispose();
        }
        disposableMap.clear();
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
    }

    private final Map<String, Disposable> disposableMap = new HashMap<>();
    private final List<Disposable> disposables = new ArrayList<>();

}
