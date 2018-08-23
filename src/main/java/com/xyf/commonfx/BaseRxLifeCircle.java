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
    public void refresh() {
        Lg.d(TAG, this);
        final String methodTag = MethodUtils.getTag();
        removeDisposable(methodTag);
        Disposable disposable = Observable.timer(waitMilliSeconds, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(aLong -> realRefresh());
        addDisposable(methodTag, disposable);
    }

    private long waitMilliSeconds = 300;

    public void setWaitMilliSeconds(long waitMilliSeconds) {
        this.waitMilliSeconds = waitMilliSeconds;
    }

    protected void realRefresh() {

    }

}
