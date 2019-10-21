package com.github.topikachu.jenkins.concurrent.latch;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Data;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Data
public class AwaitStep extends Step implements Serializable {
    private LatchRef latch;
    private long timeout;
    private TimeUnit unit = TimeUnit.SECONDS;

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public AwaitStep(LatchRef latch) {
        this.latch = latch;
    }


    @DataBoundSetter
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @DataBoundSetter
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "awaitLatch";
        }

        @Override
        public String getDisplayName() {
            return "Wait until the latch has counted down to zero.";
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution {
        private AwaitStep step;

        public Execution(StepContext context, AwaitStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Object run() throws Exception {
            Optional.ofNullable(step.getLatch().getCountDownLatch())
                    .ifPresent(
                            latch -> {
                                try {
                                    if (step.getTimeout() > 0) {
                                        latch.await(step.getTimeout(), step.getUnit());
                                    } else {
                                        latch.await();
                                    }
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );


            return null;
        }


        @Override
        public void stop(@Nonnull Throwable throwable) throws Exception {
            Optional.ofNullable(step.getLatch().getCountDownLatch())
                    .ifPresent(latch -> {
                        while (latch.getCount() > 0) {
                            latch.countDown();
                        }
                    });
            super.stop(throwable);
        }
    }
}
