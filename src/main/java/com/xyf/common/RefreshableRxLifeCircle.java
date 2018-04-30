package com.xyf.common;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public abstract class RefreshableRxLifeCircle extends RxLifeCircle implements Refreshable {

    @Override
    public final void refresh() {
        final String methodTag = getMethodTag();
        removeDisposable(methodTag);
        Disposable disposable = Observable.timer(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(aLong -> realRefresh());
        addDisposable(methodTag, disposable);
    }

    protected abstract void realRefresh();

}
