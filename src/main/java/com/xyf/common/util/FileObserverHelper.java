package com.xyf.common.util;

import com.google.common.base.Preconditions;
import com.xyf.common.Refreshable;
import com.xyf.common.annotation.UiThread;
import com.xyf.common.annotation.WorkThread;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileObserverHelper {

    private static final String TAG = FileObserverHelper.class.getSimpleName();

    @Nullable
    private static WatchService watchService;

    @UiThread
    public static void addFile(@Nonnull File file, @Nonnull Refreshable refreshable) {
        for (WatchHolder holder : fileCallbacks) {
            if (holder.file.equals(file) && holder.refreshable == refreshable) {
                return;
            }
        }

        Disposable disposable = Observable.fromCallable(() -> {
            Preconditions.checkArgument(FileUtils2.isFile(file));

            ensureInit();
            WatchKey watchKey = file.getParentFile().toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Lg.i(TAG, "start watch file", file);
            return watchKey;
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(watchKey -> fileCallbacks.add(new WatchHolder(null, file, watchKey, refreshable)), throwable -> Lg.e(TAG, throwable));
    }

    static class WatchHolder {

        @Nullable
        final String tag;
        @Nonnull
        final File file;
        @Nonnull
        final WatchKey watchKey;
        @Nonnull
        final Refreshable refreshable;

        WatchHolder(@Nullable String tag, @Nonnull File file, @Nonnull WatchKey watchKey, @Nonnull Refreshable refreshable) {
            this.tag = tag;
            this.file = file;
            this.watchKey = watchKey;
            this.refreshable = refreshable;
        }

    }

    @UiThread
    public static void removeDirectory(@Nonnull String tag) {
        directoryCallbacks.removeIf(holder -> Objects.equals(holder.tag, tag));
    }

    @UiThread
    public static void addDirectory(@Nonnull String tag, @Nonnull File directory, @Nonnull Refreshable refreshable) {
        for (WatchHolder holder : directoryCallbacks) {
            if (holder.file.equals(directory) && holder.refreshable == refreshable && Objects.equals(holder.tag, tag)) {
                return;
            }
        }

        Disposable disposable = Observable.fromCallable(() -> {
            ensureInit();
            Preconditions.checkArgument(FileUtils2.isDirectory(directory), directory);

            WatchKey watchKey = directory.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Lg.i(TAG, "start watch directory", directory);
            return watchKey;
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(watchKey -> directoryCallbacks.add(new WatchHolder(tag, directory, watchKey, refreshable)), throwable -> Lg.e(TAG, throwable));
    }

    private static final List<WatchHolder> fileCallbacks = new ArrayList<>();
    private static final List<WatchHolder> directoryCallbacks = new ArrayList<>();

    private static final Object WAIT_INIT_LOCK = new Object();

    @WorkThread
    private static void ensureInit() throws InterruptedException {
        synchronized (WAIT_INIT_LOCK) {
            while (watchService == null) {
                init();
                WAIT_INIT_LOCK.wait();
            }
        }
    }

    @WorkThread
    private static void init() {
        Thread thread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                synchronized (WAIT_INIT_LOCK) {
                    FileObserverHelper.watchService = watchService;
                    WAIT_INIT_LOCK.notifyAll();
                }

                Lg.i(TAG, "start watch file change");

                while (true) {
                    final WatchKey watchKey = watchService.take();
                    for (WatchEvent watchEvent : watchKey.pollEvents()) {
                        final WatchEvent.Kind kind = watchEvent.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            final Path path = (Path) watchEvent.context();
                            Lg.i(TAG, path, kind);
                            for (WatchHolder holder : fileCallbacks) {
                                if (holder.watchKey == watchKey) {
                                    final boolean isThisFileChange;
                                    if (path.isAbsolute()) {
                                        isThisFileChange = path.toAbsolutePath().toFile().equals(holder.file);
                                    } else {
                                        isThisFileChange = path.toString().equals(holder.file.getName());
                                    }
                                    if (isThisFileChange) {
                                        Disposable disposable = Observable.just(new Object())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(JavaFxScheduler.platform())
                                                .subscribe(o -> holder.refreshable.refresh());
                                    }
                                }
                            }

                            for (WatchHolder holder : directoryCallbacks) {
                                if (holder.watchKey == watchKey) {
                                    Disposable disposable = Observable.just(new Object())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(JavaFxScheduler.platform())
                                            .subscribe(o -> holder.refreshable.refresh());
                                }
                            }
                        }
                    }

                    boolean valid = watchKey.reset();
                }

            } catch (IOException | InterruptedException e) {
                Lg.e(TAG, e);
            }
        });
        thread.setName("Watch File Change Thread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

}
