package com.eaglesakura.android.garnet.error;

public class InstanceInitializeException extends RuntimeException {
    public InstanceInitializeException() {
    }

    public InstanceInitializeException(String detailMessage) {
        super(detailMessage);
    }

    public InstanceInitializeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InstanceInitializeException(Throwable throwable) {
        super(throwable);
    }
}
