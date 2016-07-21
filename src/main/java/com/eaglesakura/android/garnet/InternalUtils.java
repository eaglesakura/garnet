package com.eaglesakura.android.garnet;

import java.util.HashMap;
import java.util.Map;

class InternalUtils {

    private static final Map<Class, SingletonHolder> sSingletonStore = new HashMap<>();

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

    /**
     * 動的に実装を切り替える場合のファクトリ
     */
    private static final Map<Class, Class> sOverrideClasses = new HashMap<>();

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
}
