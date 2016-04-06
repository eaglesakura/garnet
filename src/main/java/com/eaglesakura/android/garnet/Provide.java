package com.eaglesakura.android.garnet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * オブジェクトの生成を行う
 *
 * このアノテーションが付いたメソッドは必ず引数なしである必要がある。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Provide {
    String name() default "";
}
