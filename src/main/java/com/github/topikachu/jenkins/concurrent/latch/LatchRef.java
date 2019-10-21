package com.github.topikachu.jenkins.concurrent.latch;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

@Data
@Builder
public class LatchRef implements Serializable {
    private transient CountDownLatch countDownLatch;
}
