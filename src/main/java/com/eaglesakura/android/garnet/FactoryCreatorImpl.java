package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.InstanceCreateError;
import com.eaglesakura.android.garnet.error.InstanceInitializeException;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;

class FactoryCreatorImpl implements InstanceCreator {
    @NonNull
    final Class<? extends ComponentFactory> mFactoryClass;

    @NonNull
    ComponentFactory mFactory;

    /**
     * ファクトリにシングルトン属性がある場合はtrue
     */
    final boolean mIsSingleton;

    public FactoryCreatorImpl(@NonNull Class<? extends ComponentFactory> factory) {
        mFactoryClass = factory;
        mIsSingleton = InternalUtils.isSingleton(mFactoryClass);
    }

    private synchronized ComponentFactory getFactory() throws Exception {
        if (mFactory == null) {
            mFactory = mFactoryClass.newInstance();
        }
        return mFactory;
    }

    @Override
    public Object newInstance(Field field, Object dst) {
        try {
            // シングルトンならば、Storeを調べる
            if (mIsSingleton) {
                SingletonHolder holder = InternalUtils.getSingleton(mFactoryClass);
                synchronized (holder) {
                    if (holder.instance != null) {
                        return holder.instance;
                    }
                    holder.instance = getFactory().newInstance(field, dst);
                    if (holder.instance == null) {
                        // シングルトン要求なのにインスタンスが無いので、エラーである
                        throw new InstanceCreateError();
                    }

                    return holder.instance;
                }
            } else {
                return getFactory().newInstance(field, dst);
            }
        } catch (Exception e) {
            throw new InstanceInitializeException(e);
        }
    }

    @Override
    public void initialize(Field field, Object instance, Object dst) {
        try {
            if (mIsSingleton) {
                SingletonHolder holder = InternalUtils.getSingleton(mFactoryClass);
                synchronized (holder) {
                    if (holder.initialized) {
                        return;
                    } else {
                        // 初期化処理を行う
                        holder.initialized = true;
                        getFactory().initialize(field, dst, instance);
                    }
                }
            } else {
                // 初期化処理を行う
                getFactory().initialize(field, dst, instance);
            }
        } catch (Throwable e) {
            throw new InstanceInitializeException(e);
        }
    }
}
