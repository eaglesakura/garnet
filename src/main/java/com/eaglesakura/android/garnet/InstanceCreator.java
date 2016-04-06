package com.eaglesakura.android.garnet;

import java.lang.reflect.Field;

interface InstanceCreator {
    Object newInstance(Field field, Object dst);

    void initialize(Field field, Object instance, Object dst);
}
