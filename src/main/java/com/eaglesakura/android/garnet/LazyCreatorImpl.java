package com.eaglesakura.android.garnet;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;

class LazyCreatorImpl implements InstanceCreator {

    @NonNull
    final InstanceCreator mCreator;

    public LazyCreatorImpl(@NonNull InstanceCreator injector) {
        mCreator = injector;
    }

    @Override
    public Object newInstance(Field field, Object dst) {
        return new LazyImpl(dst, field);
    }

    @Override
    public void initialize(Field field, Object instance, Object dst) {

    }

    class LazyImpl implements Lazy {
        Object mItem;

        final Field mField;

        boolean mInitialized;

        final Object mDst;

        public LazyImpl(Object dst, Field field) {
            mDst = dst;
            mField = field;
        }

        @Override
        public Object get() {
            if (!mInitialized) {
                synchronized (this) {
                    if (!mInitialized) {
                        mItem = mCreator.newInstance(mField, mDst);
                        mCreator.initialize(mField, mItem, mDst);
                        mInitialized = true;
                    }
                }
            }
            return mItem;
        }

        @Override
        public void clear() {
            synchronized (this) {
                mItem = null;
                mInitialized = false;
            }
        }
    }
}
