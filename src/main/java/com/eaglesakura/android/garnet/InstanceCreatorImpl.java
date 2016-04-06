package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.DefaultConstructorNotFoundError;
import com.eaglesakura.android.garnet.error.InstanceCreateError;

import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

class InstanceCreatorImpl implements InstanceCreator {
    @NonNull
    final Class mClass;

    @NonNull
    final Constructor mConstructor;

    /**
     * ファクトリにシングルトン属性がある場合はtrue
     */
    final boolean mIsSingleton;

    public InstanceCreatorImpl(@NonNull Class aClass) throws Error {
        mClass = aClass;
        mIsSingleton = InternalUtils.isSingleton(aClass);
        try {
            mConstructor = mClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new DefaultConstructorNotFoundError(e);
        }
    }

    @Override
    public synchronized Object newInstance(Field field, Object dst) {
        try {
            if (mIsSingleton) {
                SingletonHolder holder = InternalUtils.getSingleton(mClass);
                synchronized (holder) {
                    if (holder.instance == null) {
                        holder.instance = mConstructor.newInstance();
                        holder.initialized = true;
                    }
                    return holder.instance;
                }
            } else {
                return mConstructor.newInstance();
            }
        } catch (Exception e) {
            throw new InstanceCreateError(e);
        }
    }

    @Override
    public void initialize(Field field, Object instance, Object dst) {

    }
}
