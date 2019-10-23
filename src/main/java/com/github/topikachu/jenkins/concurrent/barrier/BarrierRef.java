package com.github.topikachu.jenkins.concurrent.barrier;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.CyclicBarrier;

@Data

public class BarrierRef implements Serializable {
    private static final long serialVersionUID = 5796547287075859603L;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient CyclicBarrier cyclicBarrier;

    @Builder
    private BarrierRef(int count) {
        cyclicBarrier = new CyclicBarrier(count);
    }
}
