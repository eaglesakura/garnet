package com.eaglesakura.android.garnet;

import android.support.annotation.Keep;

@Keep
public interface Provider<SourceType> {

    /**
     * 依存関係の解決が完了したタイミングで呼び出される
     */
    void onDependsCompleted(SourceType inject);

    /**
     * 依存注入が完了したら呼び出される
     */
    void onInjectCompleted(SourceType inject);
}
