package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.DependMethodNotFoundError;
import com.eaglesakura.android.garnet.error.InstanceInitializeException;
import com.eaglesakura.android.garnet.error.ProvideMethodError;
import com.eaglesakura.android.garnet.error.ProvideMethodNotFoundError;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ProviderClassHolder {
    final Map<String, DependMethodHolder> mDependSetters = new HashMap<>();

    final Map<String, ProvideMethodHolder> mProvideGetters = new HashMap<>();

    Class mClass;

    ProviderClassHolder(Class clazz) {
        mClass = clazz;
        initProvideGetters();
        initDependSetters();
    }

    /**
     * 依存実体を返却するメソッドを収集する
     */
    void initProvideGetters() {
        Class clazz = mClass;
        while (!clazz.equals(Object.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                Provide provide = method.getAnnotation(Provide.class);

                if (provide != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Class<?> returnType = method.getReturnType();
                    if (parameterTypes.length != 0 || method.getReturnType().equals(void.class)) {
                        throw new ProvideMethodError();
                    }

                    String name = makeName(returnType, provide);
                    if (!mProvideGetters.containsKey(name)) {
                        ProvideMethodHolder methodHolder = new ProvideMethodHolder();
                        methodHolder.method = method;
                        if (InternalUtils.isSingleton(returnType)) {
                            methodHolder.singleton = InternalUtils.getSingleton(returnType);
                        }
                        mProvideGetters.put(name, methodHolder);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    void initDependSetters() {
        Class clazz = mClass;
        while (!clazz.equals(Object.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                Depend annotation = method.getAnnotation(Depend.class);

                if (annotation != null) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1 || !method.getReturnType().equals(void.class)) {
                        throw new ProvideMethodError();
                    }

                    String name = makeName(parameterTypes[0], annotation);
                    if (!mDependSetters.containsKey(name)) {
                        DependMethodHolder methodHolder = new DependMethodHolder();
                        methodHolder.method = method;
                        methodHolder.required = annotation.require();
                        mDependSetters.put(name, methodHolder);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 必要な依存オブジェクトをリンクする
     */
    void setDepends(Object inject, Provider provider, Map<String, Object> depends) {
        Iterator<Map.Entry<String, DependMethodHolder>> iterator = mDependSetters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DependMethodHolder> entry = iterator.next();
            String key = entry.getKey();
            DependMethodHolder methodHolder = entry.getValue();

            if (!depends.containsKey(key)) {
                if (methodHolder.required) {
                    // 必須であるが、キーが存在しないのはエラーである
                    throw new DependMethodNotFoundError(key);
                } else {
                    // キーが存在しないならこのキーは無視する
                    continue;
                }
            }

            Object obj = depends.get(key);
            if (obj == null && methodHolder.required) {
                // 必須であるが、オブジェクトがnullの場合はエラーである
                throw new InstanceInitializeException();
            }

            // setterを呼び出す
            try {
                methodHolder.method.invoke(provider, obj);
            } catch (Exception e) {
                throw new InstanceInitializeException(e);
            }
        }

        // 依存処理が終わったので通知
        provider.onDependsCompleted(inject);
    }

    /**
     * 依存オブジェクトを取得する
     */
    Object getProvideObject(Object injectTarget, Provider provider, String name) {
        ProvideMethodHolder method = mProvideGetters.get(name);
        if (method == null) {
            // 指定されたProvideメソッドが見つからない
            throw new ProvideMethodNotFoundError("Inject[" + name + "] Provider[" + provider.getClass().getName() + "]");
        }

        try {
            boolean initialize = false;
            Object result = null;
            if (method.singleton != null) {
                // Classにシングルトンが指定されている
                synchronized (method.singleton) {
                    if (method.singleton.instance == null) {
                        method.singleton.instance = method.method.invoke(provider);
                        initialize = true;

                        // シングルトンの生成に失敗した
                        if (method.singleton.instance == null) {
                            throw new ProvideMethodError();
                        }
                    }
                    result = method.singleton.instance;
                }
            } else {
                // 通常のProvide
                initialize = true;
                result = method.method.invoke(provider);
            }

            if (initialize) {
                // 初期化を行わせる
                InternalUtils.requestProvideInitialize(result, injectTarget);
            }

            return result;
        } catch (InvocationTargetException e) {
            throw new ProvideMethodError(e.getTargetException());
        } catch (Exception e) {
            throw new ProvideMethodError(e);
        }
    }

    private static class DependMethodHolder {
        boolean required;
        Method method;
    }

    private static class ProvideMethodHolder {
        SingletonHolder singleton;
        Method method;
    }


    static String makeName(@NonNull Class clazz, @NonNull Provide provide) {
        return makeName(clazz, provide.name());
    }

    static String makeName(@NonNull Class clazz, @NonNull Inject inject) {
        return makeName(clazz, inject.name());
    }

    static String makeName(@NonNull Class clazz, @NonNull Depend depend) {
        return makeName(clazz, depend.name());
    }

    static String makeName(@NonNull Class clazz) {
        return makeName(clazz, "");
    }

    static String makeName(@NonNull Class clazz, @NonNull String name) {
        if (name == null) {
            return clazz.getName() + "|";
        } else {
            return clazz.getName() + "|" + name;
        }
    }

    private static Map<Class, ProviderClassHolder> sHolders = new HashMap<>();

    static synchronized ProviderClassHolder get(Class<? extends Provider> clazz) {
        ProviderClassHolder holder = sHolders.get(clazz);
        if (holder == null) {
            holder = new ProviderClassHolder(clazz);
            sHolders.put(clazz, holder);
        }
        return holder;
    }
}
