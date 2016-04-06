package com.eaglesakura.android.garnet;

import android.support.annotation.NonNull;

/**
 * 簡易的な依存性注入を行う
 */
public class Garnet {
    /**
     * 依存注入を行う
     */
    public static <T> T inject(@NonNull T obj) {
        InternalUtils.getImpl(obj).inject(obj);
        return obj;
    }

    /**
     * 動作クラスを上書きする
     *
     * @param origin 元のClass
     * @param stead  今後利用するClass
     */
    public static void override(Class origin, Class stead) {
        InternalUtils.override(origin, stead);
    }
}
