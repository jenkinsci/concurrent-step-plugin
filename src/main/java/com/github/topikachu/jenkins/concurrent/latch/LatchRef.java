package com.github.topikachu.jenkins.concurrent.latch;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

@Data
@Builder
public class LatchRef implements Serializable {
    private static final long serialVersionUID = 7845685135950671019L;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient CountDownLatch countDownLatch;
}
