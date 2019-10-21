package com.github.topikachu.jenkins.concurrent.condition;

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

@Data
public class AwaitStep extends Step implements Serializable {
    private LockAndCondition condition;
    private long timeout;
    private TimeUnit unit = TimeUnit.SECONDS;

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public AwaitStep(LockAndCondition condition) {
        this.condition = condition;
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
            return "awaitCondition";
        }

        @Override
        public String getDisplayName() {
            return "Causes the current thread to wait until it is signalled or interrupted.";
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
            LockAndCondition lockAndCondition = step.getCondition();
            Optional.ofNullable(lockAndCondition.getLock())
                    .ifPresent(
                            lock -> {
                                lock.lock();
                                try {
                                    if (step.getTimeout() > 0) {
                                        lockAndCondition.getCondition().await(step.getTimeout(), step.getUnit());
                                    } else {
                                        lockAndCondition.getCondition().await();
                                    }
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    lock.unlock();
                                }
                            }
                    );


            return null;
        }

    }
}
