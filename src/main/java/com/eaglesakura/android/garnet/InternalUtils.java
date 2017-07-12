package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.InstanceCreateError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InternalUtils {

    /**
     * シングルトンオブジェクトをキャッシュする
     */
    private static final Map<Class, SingletonHolder> sSingletonStore = new HashMap<>();

    /**
     * 動的に実装を切り替える場合のファクトリ
     */
    private static final Map<Class, Class> sOverrideClasses = new HashMap<>();

    /**
     * Provide後の処理を行うためのキャッシュ
     */
    private static final Map<Class, ProvideTargetClassHolder> sProvideTargetClasses = new HashMap<>();

    /**
     * シングルトン属性である場合はtrue
     */
    static boolean isSingleton(Class clazz) {
        for (Class ifs : clazz.getInterfaces()) {
            if (ifs.getAnnotation(Singleton.class) != null) {
                return true;
            }
        }
        return clazz.getAnnotation(Singleton.class) != null;
    }

    /**
     * 保持しているシングルトンインスタンスを開放する
     */
    static void clearSingletonCache() {
        synchronized (sSingletonStore) {
            for (SingletonHolder holder : sSingletonStore.values()) {
                holder.instance = null;
            }
        }
    }

    /**
     * 保持しているProviderオーバーライドマッピングを開放する
     */
    static void clearOverrideMapping() {
        synchronized (sOverrideClasses) {
            sOverrideClasses.clear();
        }
    }

    static SingletonHolder getSingleton(Class clazz) {
        synchronized (sSingletonStore) {
            SingletonHolder result = sSingletonStore.get(clazz);
            if (result == null) {
                result = new SingletonHolder();
                sSingletonStore.put(clazz, result);
            }
            return result;
        }
    }

    static void override(Class origin, Class stead) {
        synchronized (sOverrideClasses) {
            sOverrideClasses.put(origin, stead);
        }
    }

    static Class getClass(Class origin) {
        synchronized (sOverrideClasses) {
            Class aClass = sOverrideClasses.get(origin);
            if (aClass != null) {
                return aClass;
            } else {
                return origin;
            }
        }
    }

    /**
     * 指定したAnnotationが含まれたメソッド(public以外を含む)一覧を返す
     *
     * AnnotationにはRuntime属性が付与されてなければならない
     * オーバーライドされたメソッドは1つにまとめて扱う
     */
    static <T extends Annotation> List<Method> listAnnotationMethods(Class srcClass, Class<T> annotationClass) {
        Map<String, Method> result = new HashMap<>();

        while (!srcClass.equals(Object.class)) {
            for (Method method : srcClass.getDeclaredMethods()) {
                T annotation = method.getAnnotation(annotationClass);
                if (annotation != null && !result.containsKey(method.getName())) {
                    result.put(method.getName(), method);
                }
            }
            for (Method method : srcClass.getMethods()) {
                T annotation = method.getAnnotation(annotationClass);
                if (annotation != null && !result.containsKey(method.getName())) {
                    result.put(method.getName(), method);
                }
            }
            srcClass = srcClass.getSuperclass();
        }

        return new ArrayList<>(result.values());
    }

    /**
     * ProviderによってProvideされたオブジェクトを初期化する
     */
    static Object requestProvideInitialize(Object provided, Object injectTarget) {
        if (provided == null) {
            // 初期化不可能
            return null;
        }

        ProvideTargetClassHolder holder;
        synchronized (sProvideTargetClasses) {
            holder = sProvideTargetClasses.get(provided.getClass());
            if (holder == null) {
                holder = new ProvideTargetClassHolder(provided.getClass());
                sProvideTargetClasses.put(provided.getClass(), holder);
            }
        }

        holder.initialize(provided, injectTarget);
        return provided;
    }

    static Provider newProvider(Class clazz) {
        try {
            Provider provider;
            if (isSingleton(clazz)) {
                // Provider自体にSingleton属性がある場合、Providerをキャッシュする
                SingletonHolder singleton = InternalUtils.getSingleton(clazz);
                synchronized (singleton) {
                    if (singleton.instance == null) {
                        singleton.instance = InternalUtils.getClass(clazz).newInstance();
                    }
                }
                provider = (Provider) singleton.instance;
            } else {
                // 毎度Providerオブジェクトを生成する
                provider = (Provider) InternalUtils.getClass(clazz).newInstance();
            }

            return provider;
        } catch (Exception e) {
            throw new InstanceCreateError(e);
        }
    }
}
