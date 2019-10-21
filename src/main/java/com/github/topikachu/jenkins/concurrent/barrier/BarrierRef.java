package com.github.topikachu.jenkins.concurrent.barrier;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.CyclicBarrier;

@Data
@Builder
public class BarrierRef implements Serializable {
    private transient CyclicBarrier cyclicBarrier;
}
