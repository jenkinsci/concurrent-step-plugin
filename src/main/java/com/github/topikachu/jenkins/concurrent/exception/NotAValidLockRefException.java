package com.github.topikachu.jenkins.concurrent.exception;

public class NotAValidLockRefException extends ConcurrentException {
    public NotAValidLockRefException() {
    }

    public NotAValidLockRefException(String message) {
        super(message);
    }

    public NotAValidLockRefException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAValidLockRefException(Throwable cause) {
        super(cause);
    }

    public NotAValidLockRefException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
