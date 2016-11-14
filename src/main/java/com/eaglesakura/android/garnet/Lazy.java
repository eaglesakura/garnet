package com.eaglesakura.android.garnet;

import android.support.annotation.Keep;

/**
 * 値を遅延取得する
 *
 * @param <T> データ型
 */
@Keep
public interface Lazy<T> {
    /**
     * 値を取得する
     */
    T get();
}
