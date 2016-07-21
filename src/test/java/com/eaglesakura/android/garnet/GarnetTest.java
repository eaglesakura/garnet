package com.eaglesakura.android.garnet;

import com.eaglesakura.android.garnet.error.DependMethodNotFoundError;

import org.junit.Test;

import android.content.Context;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class GarnetTest extends UnitTestCase {

    @Test
    public void シンプルなProviderを通してインスタンス化できる() {
        SimpleInjectionTarget target = new SimpleInjectionTarget();
        assertNull(target.mSay);

        Garnet.inject(target);

        assertNotNull(target.mSay);
        assertEquals(target.mSay.hello(), "hello");
    }

    @Test
    public void Providerの上書きに対応する() {
        SimpleInjectionTarget target = new SimpleInjectionTarget();
        assertNull(target.mSay);

        Garnet.override(SayProvider.class, SayJapaneseProvider.class);
        Garnet.inject(target);
        Garnet.override(SayProvider.class, SayProvider.class);

        assertNotNull(target.mSay);
        assertEquals(target.mSay.hello(), "こんにちは");
    }

    @Test
    public void 名前でバインドしたProviderを通してインスタンス化できる() {
        NamedInjectionTarget target = new NamedInjectionTarget();
        assertNull(target.mSayEng);
        assertNull(target.mSayJpn);

        Garnet.inject(target);

        assertNotNull(target.mSayEng);
        assertEquals(target.mSayEng.hello(), "hello");

        assertNotNull(target.mSayJpn);
        assertEquals(target.mSayJpn.hello(), "こんにちは");

    }

    @Test
    public void シングルトン設定されたインスタンスが正常に取得される() {
        SingletonInjectionTarget target0 = new SingletonInjectionTarget();
        SingletonInjectionTarget target1 = new SingletonInjectionTarget();

        assertTrue(InternalUtils.isSingleton(SingletonNamedProvider.SingletonSay.class));

        assertNull(target0.mSay0);
        assertNull(target0.mSay1);

        Garnet.inject(target0);
        Garnet.inject(target1);

        assertNotNull(target0.mSay0);
        assertEquals(target0.mSay0.hello(), "singleton");

        assertNotNull(target0.mSay1);
        assertEquals(target0.mSay1.hello(), "singleton");

        assertEquals(target0.mSay0, target0.mSay1);
        assertTrue(target0.mSay0 == target0.mSay1);
        assertTrue(target0.mSay0 == target1.mSay1);
    }

    @Test
    public void 継承された複数のProvider設定が反映される() {
        ExtendsInjectionTarget2 target0 = new ExtendsInjectionTarget2();
        ExtendsInjectionTarget2 target1 = new ExtendsInjectionTarget2();

        assertNotNull(target0.mSay0);
        assertNotNull(target0.mSay1);
        assertNotNull(target0.mSayEng);
        assertNotNull(target0.mLazySayJpn);
        assertNotNull(target0.mLazySayJpn.get());
        assertEquals(target0.mLazySayJpn.get(), target0.mLazySayJpn.get());
        assertTrue(target0.mLazySayJpn.get() == target0.mLazySayJpn.get());
        assertTrue(target0.mLazySaySingleton.get() == target0.mLazySaySingleton.get());

        assertNotNull(target1.mSay0);
        assertNotNull(target1.mSay1);
        assertNotNull(target1.mSayEng);
        assertNotNull(target1.mLazySayJpn);
        assertNotNull(target1.mLazySayJpn.get());
        assertEquals(target1.mLazySayJpn.get(), target1.mLazySayJpn.get());
        assertTrue(target1.mLazySayJpn.get() == target1.mLazySayJpn.get());
        assertTrue(target1.mLazySaySingleton.get() == target1.mLazySaySingleton.get());


        // シングルトンチェック
        assertEquals(target0.mSay1, target0.mLazySaySingleton.get());
        assertTrue(target0.mSay1 == target0.mLazySaySingleton.get());
        assertTrue(target1.mSay1 == target0.mLazySaySingleton.get());
        assertTrue(target1.mSay1 == target1.mLazySaySingleton.get());
    }

    @Test(expected = DependMethodNotFoundError.class)
    public void Dependが足りない場合はエラーとなる() {
        DependRequireInjectionTarget target = new DependRequireInjectionTarget();
        Garnet.inject(target);

        fail();
    }

    @Depend(name = "context2")
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Test
    public void Dependは必須ではない() {
        {
            DependInjectionTarget target = new DependInjectionTarget();
            Garnet.inject(target);
            assertEquals(target.mSay.hello(), "null");
        }
        // depend()メソッドを通じて与える
        {
            DependInjectionTarget target = new DependInjectionTarget();
            Garnet.create(target).depend(Context.class, getContext()).inject();
            assertFalse(target.mSay.hello().equals("null"));
        }
        // @Dependを通じて与える
        {
            DependInjectionTargetWithDependGet target = new DependInjectionTargetWithDependGet(getContext());
            Garnet.inject(target);
            assertFalse(target.mSay.hello().equals("null"));
        }
    }

    public interface Say {
        String hello();
    }

    @Singleton
    public interface SaySingleton extends Say {
    }

    public static class SimpleInjectionTarget {
        @Inject(SayProvider.class)
        Say mSay;
    }


    public static class DependRequireInjectionTarget {
        @Inject(DependRequireSayProvider.class)
        Say mSay;
    }

    public static class DependInjectionTarget {
        @Inject(DependSayProvider.class)
        Say mSay;
    }

    public static class DependInjectionTargetWithDependGet {
        @Inject(DependSayProvider.class)
        Say mSay;

        Context mContext;

        public DependInjectionTargetWithDependGet(Context context) {
            mContext = context;
        }

        @Depend
        public Context getContext() {
            return mContext;
        }
    }

    public static class SayProvider implements Provider {

        @Override
        public void onDependsCompleted(Object inject) {

        }

        @Override
        public void onInjectCompleted(Object inject) {

        }

        @Provide
        Say provideSay() {
            return () -> "hello";
        }
    }


    public static class DependSayProvider implements Provider {
        Context mContext;

        @Depend
        public void setContext(Context context) {
            mContext = context;
        }

        @Depend(name = "context2")
        public void setContext2(Context context) {
            mContext = context;
        }

        @Override
        public void onDependsCompleted(Object inject) {

        }

        @Override
        public void onInjectCompleted(Object inject) {

        }

        @Provide
        Say provideSay() {
            return () -> "" + mContext;
        }
    }


    public static class DependRequireSayProvider implements Provider {
        Context mContext;

        @Depend(require = true)
        public void setContext(Context context) {
            mContext = context;
        }

        @Override
        public void onDependsCompleted(Object inject) {

        }

        @Override
        public void onInjectCompleted(Object inject) {

        }

        @Provide
        Say provideSay() {
            return () -> "hello";
        }
    }

    public static class SayJapaneseProvider implements Provider {

        @Override
        public void onDependsCompleted(Object inject) {

        }

        @Override
        public void onInjectCompleted(Object inject) {

        }

        @Provide
        Say provideSay() {
            return () -> "こんにちは";
        }
    }


    public static class NamedInjectionTarget {
        @Inject(value = NamedProvider.class, name = "eng")
        Say mSayEng;

        @Inject(value = NamedProvider.class, name = "jpn")
        Say mSayJpn;
    }

    public static class NamedProvider implements Provider {
        @Override
        public void onDependsCompleted(Object inject) {

        }

        @Provide(name = "eng")
        Say provideSayEnglish() {
            return () -> "hello";
        }

        @Provide(name = "jpn")
        Say provideSayJapanese() {
            return () -> "こんにちは";
        }

        @Override
        public void onInjectCompleted(Object inject) {

        }
    }

    public static class SingletonInjectionTarget {
        @Inject(value = SingletonNamedProvider.class, name = "eng")
        SaySingleton mSay0;

        @Inject(value = SingletonNamedProvider.class)
        SaySingleton mSay1;
    }

    @Singleton
    public static class SingletonNamedProvider implements Provider {

        static int sCreatedCount;

        public SingletonNamedProvider() {
            synchronized (SingletonNamedProvider.class) {
                // 一度しか生成されない
                assertEquals(++sCreatedCount, 1);
            }
        }

        @Override
        public void onDependsCompleted(Object inject) {

        }

        @Provide(name = "eng")
        SaySingleton provideSayEnglish() {
            return new SingletonSay();
        }

        @Provide
        SaySingleton provideSayJapanese() {
            return new SingletonSay();
        }

        static class SingletonSay implements SaySingleton {
            @Override
            public String hello() {
                return "singleton";
            }
        }

        @Override
        public void onInjectCompleted(Object inject) {

        }
    }

    public static class ExtendsInjectionTarget extends SingletonInjectionTarget {
        @Inject(value = NamedProvider.class, name = "eng")
        Say mSayEng;

        @Inject(value = SingletonNamedProvider.class, name = "eng")
        Lazy<SaySingleton> mLazySaySingleton;

        public ExtendsInjectionTarget() {
            Garnet.inject(this);
        }
    }

    public static class ExtendsInjectionTarget2 extends ExtendsInjectionTarget {
        @Inject(value = NamedProvider.class, name = "jpn")
        Lazy<Say> mLazySayJpn;
    }
}
