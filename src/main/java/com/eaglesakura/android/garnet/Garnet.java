package com.eaglesakura.android.garnet;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 簡易的な依存性注入を行う
 */
public class Garnet {

    /**
     * 依存注入用のBuilderを生成する
     */
    public static <T> Builder<T> create(@NonNull T obj) {
        return new Builder<>(obj);
    }

    /**
     * 依存注入を行う
     */
    public static <T> T inject(@NonNull T obj) {
        create(obj).inject();
        return obj;
    }

    /**
     * １オブジェクトのみを扱うInstance Factoryを生成する
     */
    public static <T extends Provider> FactoryBuilder<T> factory(@NonNull Class<T> provider) {
        return new FactoryBuilder<>(provider);
    }

    /**
     * インスタンスを直接生成する
     *
     * @param providerClass 生成を移譲するProvider
     * @param instanceClass 生成するclass
     */
    public static <T> T instance(@NonNull Class<? extends Provider> providerClass, @NonNull Class<T> instanceClass) {
        return factory(providerClass).instance(instanceClass);
    }

    /**
     * Providerを強制的に切り替える
     *
     * @param origin 元のClass
     * @param stead  今後利用するClass
     */
    public static void override(Class<? extends Provider> origin, Class<? extends Provider> stead) {
        InternalUtils.override(origin, stead);
    }

    /**
     * Singleton属性が付いているインスタンスのキャッシュを廃棄する
     */
    public static void clearSingletonCache() {
        InternalUtils.clearSingletonCache();
    }

    public static class Builder<DstType> {
        final InjectionClassHolder mInjectionClassHolder;

        final DstType mInject;

        /**
         * Inject対象から取得した依存オブジェクト
         */
        final Map<String, Object> mDependValues;
        /**
         * 依存注入クラス一覧
         */
        final List<Provider> mProviders;

        Builder(DstType inject) {
            mInject = inject;
            if (inject instanceof Class) {
                mInjectionClassHolder = InjectionClassHolder.get((Class) inject);
            } else {
                mInjectionClassHolder = InjectionClassHolder.get(inject.getClass());
            }
            mProviders = mInjectionClassHolder.newProviders();
            mDependValues = mInjectionClassHolder.getDependValues(inject);
        }

        /**
         * 依存引数を直接解決する
         */
        public <T> Builder<DstType> depend(Class<T> clazz, T value) {
            mDependValues.put(ProviderClassHolder.makeName(clazz), value);
            return this;
        }

        /**
         * 依存引数を直接解決する
         */
        public <T> Builder<DstType> depend(Class<T> clazz, String name, T value) {
            mDependValues.put(ProviderClassHolder.makeName(clazz, name), value);
            return this;
        }

        public DstType inject() {
            // Providerに依存オブジェクトを設定する
            for (Provider provider : mProviders) {
                ProviderClassHolder holder = ProviderClassHolder.get(provider.getClass());
                holder.setDepends(mInject, provider, mDependValues);
                mInjectionClassHolder.apply(mInject, holder, provider);
            }

            // 無事に注入を終えたので通知
            for (Provider provider : mProviders) {
                provider.onInjectCompleted(mInject);
            }

            return mInject;
        }
    }

    public static class FactoryBuilder<ProviderType extends Provider> {
        final ProviderType mProvider;

        /**
         * Inject対象から取得した依存オブジェクト
         */
        final Map<String, Object> mDependValues = new HashMap<>();

        Object mInject;

        FactoryBuilder(Class<ProviderType> provider) {
            mProvider = (ProviderType) InternalUtils.newProvider(provider);
        }

        /**
         * Objectに付与されたDepend属性から取り出す
         */
        public FactoryBuilder<ProviderType> depend(Object inject) {
            if (mInject != null) {
                throw new IllegalStateException();
            }

            InjectionClassHolder injectionClassHolder = InjectionClassHolder.get(inject.getClass());
            mDependValues.putAll(injectionClassHolder.getDependValues(inject));
            mInject = inject;
            return this;
        }

        /**
         * 依存引数を直接解決する
         */
        public <T> FactoryBuilder<ProviderType> depend(Class<T> clazz, T value) {
            mDependValues.put(ProviderClassHolder.makeName(clazz), value);
            return this;
        }

        /**
         * 依存引数を直接解決する
         */
        public <T> FactoryBuilder<ProviderType> depend(Class<T> clazz, String name, T value) {
            mDependValues.put(ProviderClassHolder.makeName(clazz, name), value);
            return this;
        }

        /**
         * Providerに指定したClassのオブジェクトを生成させる
         */
        public <T> T instance(Class<T> clazz) {
            return instance(clazz, null);
        }

        /**
         * Providerに指定したClassのオブジェクトを生成させる
         */
        public <T> T instance(Class<T> clazz, String name) {
            ProviderClassHolder holder = ProviderClassHolder.get(mProvider.getClass());
            holder.setDepends(mInject, mProvider, mDependValues);
            return (T) holder.getProvideObject(mInject, mProvider, ProviderClassHolder.makeName(clazz, name));
        }

    }
}
