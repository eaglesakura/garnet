package com.eaglesakura.android.garnet.error;

public class ProvidedObjectInitializeError extends Error {
    public ProvidedObjectInitializeError() {
    }

    public ProvidedObjectInitializeError(String detailMessage) {
        super(detailMessage);
    }

    public ProvidedObjectInitializeError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProvidedObjectInitializeError(Throwable throwable) {
        super(throwable);
    }
}
