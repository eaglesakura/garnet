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


    public interface Say {
        String hello();
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


    public static class NamedInjectionTarget {
        @Inject(value = SayProvider.class, name = "eng")
        Say mSayEng;

        @Inject(value = SayProvider.class, name = "jpn")
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
}
