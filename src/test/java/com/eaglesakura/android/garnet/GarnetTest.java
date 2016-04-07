package com.eaglesakura.android.garnet;

import org.junit.Test;

import static junit.framework.Assert.*;

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
    public void 名前でバインドしたPrivderを通してインスタンス化できる() {
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

    public static class SingletonNamedProvider implements Provider {
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
