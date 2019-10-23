package com.github.topikachu.jenkins.concurrent.exception;


public class AfterAwaitException extends RuntimeException {
    public AfterAwaitException() {
    }

    public AfterAwaitException(String message) {
        super(message);
    }

    public AfterAwaitException(String message, Throwable cause) {
        super(message, cause);
    }

    public AfterAwaitException(Throwable cause) {
        super(cause);
    }

    public AfterAwaitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
