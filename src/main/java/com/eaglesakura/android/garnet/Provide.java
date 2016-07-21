package com.eaglesakura.android.garnet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * オブジェクトの生成を行う
 *
 * 必須：
 * * public属性メソッド
 * * 引数なしでオブジェクトを返却する
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Provide {
    String name() default "";
}
