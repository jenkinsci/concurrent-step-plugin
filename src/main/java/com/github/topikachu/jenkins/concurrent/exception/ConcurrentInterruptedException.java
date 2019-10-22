package com.github.topikachu.jenkins.concurrent.exception;


public class ConcurrentInterruptedException extends ConcurrentException {
    public ConcurrentInterruptedException() {
    }

    public ConcurrentInterruptedException(String message) {
        super(message);
    }

    public ConcurrentInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentInterruptedException(Throwable cause) {
        super(cause);
    }

    public ConcurrentInterruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
