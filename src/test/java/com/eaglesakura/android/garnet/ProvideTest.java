package com.eaglesakura.android.garnet;


import org.junit.Test;

import android.content.Context;

import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;

public class ProvideTest extends UnitTestCase {


    @Test
    public void Providerに必要なメソッドを取得できる() throws Throwable {

        ProviderClassHolder testProvider = new ProviderClassHolder(TestProvider.class);
        ProviderClassHolder extendsTestProvider = new ProviderClassHolder(ExtendsTestProvider.class);

        assertEquals(testProvider.mDependSetters.size(), 1);
        assertEquals(testProvider.mProvideGetters.size(), 1);

        assertEquals(extendsTestProvider.mDependSetters.size(), 2);
        assertEquals(extendsTestProvider.mProvideGetters.size(), 2);
    }

    @Test
    public void DependGetterが取得できる() {
        InjectionClassHolder injectTarget = new InjectionClassHolder(TestInjectTarget.class);
        InjectionClassHolder extendsInjectTarget = new InjectionClassHolder(ExtendsTestInjectTarget.class);

        assertEquals(injectTarget.mDependGetters.size(), 1);
        assertEquals(extendsInjectTarget.mDependGetters.size(), 2);
    }

    public static class TestInjectTarget {

        @Inject(TestProvider.class)
        SayObject mSayObject;

        Context mContext;

        public TestInjectTarget(Context context) {
            mContext = context;
        }

        @Depend
        public Context getContext() {
            return mContext;
        }
    }

    public static class SayObject {
        public String hello() {
            return "hello";
        }
    }

    public static class ExtendsTestInjectTarget extends TestInjectTarget {

        TestCase mTestCase;

        public ExtendsTestInjectTarget(Context context, TestCase testCase) {
            super(context);
            mTestCase = testCase;
        }

        @Depend
        public TestCase getTestCase() {
            return mTestCase;
        }
    }


    public static class TestProvider implements Provider {
        Context mContext;

        @Depend
        public void setContext(Context context) {
            mContext = context;
        }

        @Provide
        public SayObject provideSay() {
            return new SayObject();
        }

        @Override
        public void onDependsCompleted(Object src) {

        }

        @Override
        public void onInjectCompleted(Object src) {

        }
    }

    public static class ExtendsTestProvider extends TestProvider {
        UnitTestCase mTestCase;

        @Depend
        public void setTestCase(UnitTestCase testCase) {
            mTestCase = testCase;
        }

        @Provide
        public ExtendsTestProvider provideSelf() {
            return this;
        }
    }
}
