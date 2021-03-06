package com.xyf.common;

import com.google.common.base.Preconditions;
import com.xyf.common.annotation.UiThread;
import com.xyf.common.annotation.WorkThread;
import com.xyf.common.util.FileUtils2;
import com.xyf.common.util.Lg;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FileObserverHelper {

    private static final String TAG = FileObserverHelper.class.getSimpleName();

    @Nullable
    private static WatchService watchService;

    @UiThread
    public static void addFile(@Nonnull String tag, @Nonnull Collection<File> files, @Nonnull Refreshable refreshable) {
        for (File file : files) {
            FileObserverHelper.addFile(tag, file, refreshable);
        }
    }

    @UiThread
    public static void addFile(@Nonnull String tag, @Nonnull File file, @Nonnull Refreshable refreshable) {
        Lg.d(TAG, tag, file, refreshable);

        for (WatchHolder holder : fileWatchHolders) {
            if (holder.file.equals(file) && holder.refreshable == refreshable) {
                return;
            }
        }

        Disposable disposable = Observable.fromCallable(() -> {
            Preconditions.checkArgument(FileUtils2.isFile(file), file + " not a file");

            ensureInit();
            WatchKey watchKey = file.getParentFile().toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Lg.d(TAG, "watching file", file, watchKey);
            return watchKey;
        }).subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(watchKey -> fileWatchHolders.add(new WatchHolder(tag, file, watchKey, refreshable)), throwable -> Lg.e(TAG, throwable));
    }

    @UiThread
    public static void removeFile(@Nonnull String tag) {
        fileWatchHolders.removeIf(holder -> Objects.equals(holder.tag, tag));
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
        directoryWatchHolders.removeIf(holder -> Objects.equals(holder.tag, tag));
    }

    @UiThread
    public static void addDirectories(@Nonnull String tag, @Nonnull List<File> directories, @Nonnull Refreshable refreshable) {
        for (File dir : directories) {
            FileObserverHelper.addDirectory(tag, dir, refreshable);
        }
    }

    @UiThread
    public static void addDirectory(@Nonnull String tag, @Nonnull File directory, @Nonnull Refreshable refreshable) {
        Lg.d(TAG, tag, directory, refreshable);

        for (WatchHolder holder : directoryWatchHolders) {
            if (holder.file.equals(directory) && holder.refreshable == refreshable) {
                return;
            }
        }

        Disposable disposable = Observable.fromCallable(() -> {
            Preconditions.checkArgument(FileUtils2.isDirectory(directory), directory + " is not a directory");

            ensureInit();
            WatchKey watchKey = directory.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Lg.d(TAG, "watching directory", directory, watchKey);
            return watchKey;
        }).subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(watchKey -> directoryWatchHolders.add(new WatchHolder(tag, directory, watchKey, refreshable)), throwable -> Lg.e(TAG, throwable));
    }

    /**
     * 在UI线程操作此变量
     */
    private static final List<WatchHolder> fileWatchHolders = new ArrayList<>();
    /**
     * 在UI线程操作此变量
     */
    private static final List<WatchHolder> directoryWatchHolders = new ArrayList<>();

    private static final Object WAIT_INIT_LOCK = new Object();

    private static boolean hasStartInit = false;
    private static boolean hasInitSuccess = false;

    @WorkThread
    private static void ensureInit() throws InterruptedException {
        if (hasInitSuccess) {
            return;
        }

        synchronized (WAIT_INIT_LOCK) {
            while (!hasInitSuccess) {
                if (!hasStartInit) {
                    hasStartInit = true;
                    init();
                }

                WAIT_INIT_LOCK.wait();
            }
        }
    }

    @WorkThread
    private static void handleEvent() throws InterruptedException {
        Preconditions.checkNotNull(watchService);

        final WatchKey watchKey = watchService.take();
        for (WatchEvent watchEvent : watchKey.pollEvents()) {
            final WatchEvent.Kind kind = watchEvent.kind();
            Lg.d(TAG, watchKey, watchEvent, kind, watchEvent.context());
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }

            if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_DELETE) {
                Disposable disposable = Observable.just(new Object())
                        .subscribeOn(Schedulers.io())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe(o -> {
                            final Path path = (Path) watchEvent.context();
                            for (WatchHolder holder : fileWatchHolders) {
                                if (holder.watchKey == watchKey) {
                                    final boolean isThisFileChange;
                                    if (path.isAbsolute()) {
                                        isThisFileChange = path.toAbsolutePath().toFile().equals(holder.file);
                                    } else {
                                        isThisFileChange = path.toString().equals(holder.file.getName());
                                    }
                                    if (isThisFileChange) {
                                        holder.refreshable.refresh();
                                    }
                                }
                            }

                            for (WatchHolder holder : directoryWatchHolders) {
                                if (holder.watchKey == watchKey) {
                                    holder.refreshable.refresh();
                                }
                            }
                        });
            }
        }

        boolean valid = watchKey.reset();
    }

    @WorkThread
    private static void init() {
        Thread thread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Preconditions.checkState(FileObserverHelper.watchService == null, "file watch service init twice");

                synchronized (WAIT_INIT_LOCK) {
                    FileObserverHelper.watchService = watchService;
                    hasInitSuccess = true;
                    Lg.d(TAG, "file watch service init success, start watch file change", watchService);
                    WAIT_INIT_LOCK.notifyAll();
                }

                while (true) {
                    handleEvent();
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
