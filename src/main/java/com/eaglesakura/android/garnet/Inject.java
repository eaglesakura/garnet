package com.eaglesakura.android.garnet;

import android.support.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Inject {
    /**
     * ファクトリを使用する場合はClassを指定する
     */
    Class<? extends Provider> value();

    String name() default "";
}
