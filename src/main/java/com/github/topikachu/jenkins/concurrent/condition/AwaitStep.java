package com.github.topikachu.jenkins.concurrent.condition;

import com.github.topikachu.jenkins.concurrent.exception.ConcurrentInterruptedException;
import com.github.topikachu.jenkins.concurrent.exception.NotAValidLockRefException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class AwaitStep extends Step implements Serializable {
    private static final long serialVersionUID = -3666076914428236514L;
    private LockAndCondition condition;
    private long timeout;
    private TimeUnit unit = TimeUnit.SECONDS;

    @Override
    public StepExecution start(StepContext stepContext) {
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

    @SuppressFBWarnings("WA_AWAIT_NOT_IN_LOOP")
    public static class Execution extends SynchronousNonBlockingStepExecution<ExitStatus> {
        private AwaitStep step;

        public Execution(StepContext context, AwaitStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected ExitStatus run() {
            LockAndCondition lockAndCondition = step.getCondition();
            return Optional.ofNullable(lockAndCondition.getLock())
                    .map(
                            lock -> {
                                lock.lock();
                                try {
                                    if (step.getTimeout() > 0) {
                                        boolean causedBySignal = lockAndCondition.getCondition().await(step.getTimeout(), step.getUnit());
                                        if (causedBySignal) {
                                            return ExitStatus.COMPLETED;
                                        } else {
                                            return ExitStatus.TIMEOUT;
                                        }
                                    } else {
                                        lockAndCondition.getCondition().await();
                                        return ExitStatus.COMPLETED;
                                    }
                                } catch (InterruptedException e) {
                                    throw new ConcurrentInterruptedException();
                                } finally {
                                    lock.unlock();
                                }
                            }
                    )
                    .orElseThrow(NotAValidLockRefException::new);

        }

        @Override
        public void onResume() {
            getContext().onFailure(new Exception("Resume after a restart not supported"));
        }
    }
}