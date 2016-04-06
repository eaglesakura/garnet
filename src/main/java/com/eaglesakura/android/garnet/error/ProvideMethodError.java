package com.eaglesakura.android.garnet.error;

public class ProvideMethodError extends Error {
    public ProvideMethodError() {
    }

    public ProvideMethodError(String detailMessage) {
        super(detailMessage);
    }

    public ProvideMethodError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProvideMethodError(Throwable throwable) {
        super(throwable);
    }
}
