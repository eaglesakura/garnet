package com.eaglesakura.android.garnet;

class LazyImpl implements Lazy {
    Object mValue;
    InjectionClassHolder.ProviderInstance mProviderInstance;

    public LazyImpl(InjectionClassHolder.ProviderInstance providerInstance) {
        mProviderInstance = providerInstance;
    }

    @Override
    public Object get() {
        if (mProviderInstance != null) {
            synchronized (this) {
                if (mProviderInstance != null) {
                    mValue = mProviderInstance.get();
                    mProviderInstance = null;
                }
            }
        }
        return mValue;
    }
}
