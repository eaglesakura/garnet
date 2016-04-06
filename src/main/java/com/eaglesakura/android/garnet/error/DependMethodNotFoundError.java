package com.eaglesakura.android.garnet.error;

public class DependMethodNotFoundError extends Error {
    public DependMethodNotFoundError() {
    }

    public DependMethodNotFoundError(String detailMessage) {
        super(detailMessage);
    }

    public DependMethodNotFoundError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DependMethodNotFoundError(Throwable throwable) {
        super(throwable);
    }
}
