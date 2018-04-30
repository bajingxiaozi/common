package com.xyf.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.prefs.Preferences;

public class Sp {

    @Nonnull
    private final Preferences sp;

    public Sp(@Nonnull Class clazz) {
        sp = Preferences.userNodeForPackage(clazz);
    }

    public void set(@Nonnull String key, @Nonnull String value) {
        sp.put(key, value);
    }

    @Nullable
    public String get(@Nonnull String key, @Nullable String def) {
        return sp.get(key, def);
    }

    @Nullable
    public String get(@Nonnull String key) {
        return get(key, null);
    }

}
