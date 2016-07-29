package com.eaglesakura.android.garnet;

import android.support.annotation.NonNull;

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

        public <T> Builder<DstType> depend(Class<T> clazz, T value) {
            mDependValues.put(ProviderClassHolder.makeName(clazz), value);
            return this;
        }

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
}
