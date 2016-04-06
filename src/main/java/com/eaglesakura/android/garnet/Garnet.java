package com.eaglesakura.android.garnet;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 簡易的な依存性注入を行う
 */
public class Garnet {
    public static Context getContext(@NonNull Object obj) {
        return InternalUtils.getImpl(obj).getContext(obj);
    }

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
