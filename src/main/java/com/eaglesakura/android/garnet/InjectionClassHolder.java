package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.InjectTargetError;
import com.eaglesakura.android.garnet.error.InstanceCreateError;
import com.eaglesakura.android.garnet.error.ProvideMethodError;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
class InjectionClassHolder {
    /**
     * 対象のClassファイル
     */
    @NonNull
    final Class mClass;

    /**
     * 注入対象のフィールド
     */
    List<FieldHolder> mTargetFields = new ArrayList<>();

    Map<String, Method> mDependGetters = new HashMap<>();

    /**
     * 使用されるProvider一覧
     */
    List<Class<? extends Provider>> mProviders = new ArrayList<>();

    public InjectionClassHolder(@NonNull Class aClass) {
        mClass = aClass;
        initFields();
        initDependGetters();
    }

    /**
     * 依存関係のGetterを収集する
     */
    void initDependGetters() {
        Class clazz = mClass;
        while (!clazz.equals(Object.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getReturnType().equals(void.class)) {
                    // 戻り値voidのメソッドはチェックを行わない
                    continue;
                }

                Depend annotation = method.getAnnotation(Depend.class);
                if (annotation != null) {
                    String name = ProviderClassHolder.makeName(method.getReturnType(), annotation);
                    if (!mDependGetters.containsKey(name)) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 0) {
                            throw new ProvideMethodError();
                        }
                        mDependGetters.put(name, method);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    private void addProvider(Class<? extends Provider> clazz) {
        if (mProviders.contains(clazz)) {
            // 既にリストに含まれているため何もしない
            return;
        }
        // 末尾に追加する
        mProviders.add(clazz);
    }

    void initFields() {
        try {
            Class srcClass = mClass;
            while (!srcClass.equals(Object.class)) {
                for (Field field : srcClass.getDeclaredFields()) {
                    Inject inject = field.getAnnotation(Inject.class);

                    if (inject == null) {
                        continue;
                    }

                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    FieldHolder holder = new FieldHolder();
                    holder.field = field;
                    holder.provider = inject.value();
                    holder.name = ProviderClassHolder.makeName(getInstanceType(field), inject);
                    mTargetFields.add(holder);
                    addProvider(inject.value());
                }

                srcClass = srcClass.getSuperclass();
            }
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new InjectTargetError(e);
        }
    }

    private static Class getInstanceType(Field field) {
        if (field.getType().equals(Lazy.class)) {
            Type genericType = field.getGenericType();
            return (Class) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else {
            return field.getType();
        }
    }


    /**
     * 提供可能な依存オブジェクトを返す
     */
    Map<String, Object> getDependValues(Object inject) {
        Map<String, Object> result = new HashMap<>();
        Iterator<Map.Entry<String, Method>> iterator = mDependGetters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Method> entry = iterator.next();
            try {
                result.put(entry.getKey(), entry.getValue().invoke(inject));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 必要なプロバイダを生成する
     */
    List<Provider> newProviders() {
        List<Provider> result = new ArrayList<>();
        for (Class clazz : mProviders) {
            try {

                Provider provider;
                if (InternalUtils.isSingleton(clazz)) {
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

                result.add(provider);
            } catch (Exception e) {
                throw new InstanceCreateError(e);
            }
        }
        return result;
    }

    void apply(Object inject, ProviderClassHolder providerClassHolder, Provider provider) {
        for (FieldHolder fieldHolder : mTargetFields) {
            if (!InternalUtils.getClass(fieldHolder.provider).equals(provider.getClass())) {
                continue;
            }

            try {
                // invoke対象のProviderを見つけた
                if (fieldHolder.field.getType().equals(Lazy.class)) {
                    // 遅延注入を行う
                    ProviderInstance instance = () -> providerClassHolder.getProvideObject(provider, fieldHolder.name);
                    fieldHolder.field.set(inject, new LazyImpl(instance));
                } else {
                    Object provideObject = providerClassHolder.getProvideObject(provider, fieldHolder.name);
                    fieldHolder.field.set(inject, provideObject);
                }
            } catch (Exception e) {
                throw new ProvideMethodError(e);
            }
        }
    }

    static class FieldHolder {
        Class<? extends Provider> provider;

        String name;

        Field field;
    }

    private static Map<Class, InjectionClassHolder> sHolders = new HashMap<>();

    static synchronized InjectionClassHolder get(Class clazz) {
        InjectionClassHolder holder = sHolders.get(clazz);
        if (holder == null) {
            holder = new InjectionClassHolder(clazz);
            sHolders.put(clazz, holder);
        }
        return holder;
    }

    interface ProviderInstance {
        Object get();
    }

}
