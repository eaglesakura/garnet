package com.eaglesakura.android.garnet.error;

public class ProvideMethodNotFoundError extends ProvideMethodError {
    public ProvideMethodNotFoundError() {
    }

    public ProvideMethodNotFoundError(String detailMessage) {
        super(detailMessage);
    }

    public ProvideMethodNotFoundError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProvideMethodNotFoundError(Throwable throwable) {
        super(throwable);
    }
}
