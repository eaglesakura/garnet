package com.eaglesakura.android.garnet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Depend {
    /**
     * 型が衝突した場合にチェックされる名前
     */
    String name() default "";

    /**
     * バインドを強要する
     */
    boolean require() default false;
}
