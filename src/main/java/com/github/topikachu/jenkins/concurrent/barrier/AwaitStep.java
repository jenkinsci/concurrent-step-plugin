package com.github.topikachu.jenkins.concurrent.barrier;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Data;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
public class AwaitStep extends Step implements Serializable {
    private BarrierRef barrier;
    private long timeout;
    private TimeUnit unit = TimeUnit.SECONDS;

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public AwaitStep(BarrierRef barrier) {
        this.barrier = barrier;
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
            return "awaitBarrier";
        }

        @Override
        public String getDisplayName() {
            return "Waits until all parties have invoked await on this barrier.";
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<Boolean> {
        private AwaitStep step;

        public Execution(StepContext context, AwaitStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Boolean run() throws Exception {
            Optional.ofNullable(step.getBarrier().getCyclicBarrier())
                    .ifPresent(
                            barrier -> {
                                try {
                                    if (step.getTimeout() > 0) {
                                        barrier.await(step.getTimeout(), step.getUnit());
                                    } else {
                                        barrier.await();
                                    }
                                } catch (TimeoutException e) {

                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
            return true;
        }


    }
}
