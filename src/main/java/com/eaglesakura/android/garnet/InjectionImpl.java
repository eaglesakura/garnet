package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.InjectTargetError;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class InjectionImpl {
    /**
     * 対象のClassファイル
     */
    @NonNull
    final Class mClass;

    List<Pair<Field, InstanceCreator>> mCreators = new ArrayList<>();

    public InjectionImpl(@NonNull Class aClass) {
        mClass = aClass;
        initInjectors();
    }

    void initInjectors() {
        try {
            for (Field field : InternalUtils.listAnnotationFields(mClass, Inject.class)) {

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                Inject inject = field.getAnnotation(Inject.class);
                InstanceCreator creator = InternalUtils.getCreator(inject);

                // 遅延実行する
                if (field.getType().equals(Lazy.class)) {
                    mCreators.add(new Pair<>(field, new LazyCreatorImpl(creator)));
                } else {
                    mCreators.add(new Pair<>(field, creator));
                }
            }
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new InjectTargetError(e);
        }
    }

    public void inject(Object dst) {
        try {
            for (Pair<Field, InstanceCreator> creator : mCreators) {
                Object instance = creator.second.newInstance(creator.first, dst);
                creator.first.set(dst, instance);
            }

            for (Pair<Field, InstanceCreator> creator : mCreators) {
                Object instance = creator.first.get(dst);
                creator.second.initialize(creator.first, instance, dst);
            }
        } catch (Exception e) {
            throw new Error(e);
        }

    }
}
