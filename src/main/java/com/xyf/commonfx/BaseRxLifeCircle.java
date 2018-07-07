package com.xyf.commonfx;

import com.xyf.common.Refreshable;
import com.xyf.common.util.Lg;
import com.xyf.common.util.MethodUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public abstract class BaseRxLifeCircle extends RxLifeCircle implements Refreshable {

    private static final String TAG = BaseRxLifeCircle.class.getSimpleName();

    @Override
    public final void refresh() {
        Lg.d(TAG, this);
        final String methodTag = MethodUtils.getMethodTag();
        removeDisposable(methodTag);
        Disposable disposable = Observable.timer(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(aLong -> realRefresh());
        addDisposable(methodTag, disposable);
    }

    protected abstract void realRefresh();

}
