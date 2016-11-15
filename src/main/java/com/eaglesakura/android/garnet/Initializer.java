package com.eaglesakura.android.garnet;

import android.support.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Providerで生成された後、呼び出しを行わせるメソッドを定義する.
 * このメソッドは引数なし、もしくはObject型を一つとる（Inject対象のオブジェクトが渡される）必要がある。
 */
@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Initializer {
}
