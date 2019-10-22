package com.github.topikachu.jenkins.concurrent.condition;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Data

public class LockAndCondition implements Serializable {
    private static final long serialVersionUID = 4871938278796007364L;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Lock lock;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Condition condition;

    @Builder
    public LockAndCondition(Lock lock) {
        this.lock = lock;
        this.condition = lock.newCondition();
    }


}
