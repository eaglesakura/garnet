package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.InjectTargetError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InternalUtils {

    private static Map<Class, InstanceCreator> sInstanceCreators = new HashMap<>();

    static synchronized InstanceCreator getCreator(Inject inject) {
        synchronized (sInstanceCreators) {
            Class clazz;
            if (!inject.instance().equals(Object.class)) {
                // インスタンスを直接生成する
                clazz = getClass(inject.instance());
                InstanceCreator creator = sInstanceCreators.get(clazz);
                if (creator == null) {
                    creator = new InstanceCreatorImpl(clazz);
                    sInstanceCreators.put(clazz, creator);
                }

                return creator;
            } else if (!inject.factory().equals(ComponentFactory.class)) {
                // ファクトリを経由して生成する
                clazz = getClass(inject.factory());
                InstanceCreator creator = sInstanceCreators.get(clazz);
                if (creator == null) {
                    creator = new FactoryCreatorImpl(clazz);
                    sInstanceCreators.put(clazz, creator);
                }

                return creator;
            } else {
                throw new InjectTargetError();
            }
        }
    }

    static synchronized InjectionImpl getImpl(Object obj) {
        Class clazz = obj.getClass();
//        InjectionImpl impl;
//        impl = sImplCache.get(clazz);
//        if (impl == null) {
//            impl = new InjectionImpl(clazz);
//            sImplCache.put(clazz, impl);
//        }

        return new InjectionImpl(clazz);
    }

    /**
     * 指定したAnnotationが含まれたフィールド(public以外を含む)一覧を返す
     *
     * AnnotationにはRuntime属性が付与されてなければならない
     */
    static <T extends Annotation> List<Field> listAnnotationFields(Class srcClass, Class<T> annotationClass) {
        List<Field> result = new ArrayList<>();

        while (!srcClass.equals(Object.class)) {
            for (Field field : srcClass.getDeclaredFields()) {
                T annotation = field.getAnnotation(annotationClass);
                if (annotation != null) {
                    result.add(field);
                }
            }

            srcClass = srcClass.getSuperclass();
        }

        return result;
    }

    private static final Map<Class, SingletonHolder> sSingletonStore = new HashMap<>();

    /**
     * シングルトン属性である場合はtrue
     */
    static boolean isSingleton(Class clazz) {
        return clazz.getAnnotation(Singleton.class) != null;
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
