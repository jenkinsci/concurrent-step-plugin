package com.github.topikachu.jenkins.concurrent.semaphore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Data
public class SemaphoreRef implements Serializable {
    private static final long serialVersionUID = 7845685135950671019L;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient Semaphore semaphore;

    @Builder
    private SemaphoreRef(int count) {
        semaphore = new Semaphore(count, true);
    }
}
