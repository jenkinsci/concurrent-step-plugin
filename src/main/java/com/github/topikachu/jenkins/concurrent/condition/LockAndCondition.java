package com.github.topikachu.jenkins.concurrent.condition;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Data

public class LockAndCondition implements Serializable {
    private transient Lock lock;
    private transient Condition condition;

    @Builder
    public LockAndCondition(Lock lock) {
        this.lock = lock;
        this.condition = lock.newCondition();
    }


}
