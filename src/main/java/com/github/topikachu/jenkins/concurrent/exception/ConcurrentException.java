package com.github.topikachu.jenkins.concurrent.exception;

public class ConcurrentException extends RuntimeException {
    public ConcurrentException() {
    }

    public ConcurrentException(String message) {
        super(message);
    }

    public ConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentException(Throwable cause) {
        super(cause);
    }

    public ConcurrentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
