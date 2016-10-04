package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.ProvidedObjectInitializeError;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Provideの戻り値となるオブジェクトのClassを管理する
 */
class ProvideTargetClassHolder {
    Class mClazz;

    Set<Method> mInitializers = new HashSet<>();

    ProvideTargetClassHolder(Class clazz) {
        mClazz = clazz;

        for (Method method : mClazz.getMethods()) {
            Initializer annotation = method.getAnnotation(Initializer.class);
            if (annotation == null) {
                continue;
            }

            mInitializers.add(method);
        }
    }

    /**
     * 初期化を行わせる
     */
    synchronized void initialize(Object provideObject, Object injectTarget) {
        if (mInitializers.isEmpty()) {
            return;
        }

        try {
            for (Method method : mInitializers) {
                if (method.getParameterTypes().length == 0) {
                    method.invoke(provideObject);
                } else {
                    method.invoke(provideObject, injectTarget);
                }
            }
        } catch (Exception e) {
            throw new ProvidedObjectInitializeError(e);
        }
    }
}
