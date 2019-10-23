package com.github.topikachu.jenkins.concurrent.semaphore;

import com.github.topikachu.jenkins.concurrent.exception.ConcurrentException;
import com.github.topikachu.jenkins.concurrent.exception.ConcurrentInterruptedException;
import com.github.topikachu.jenkins.concurrent.exception.NotAValidLockRefException;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class AcquireStep extends Step implements Serializable {
    private static final long serialVersionUID = -6128324288332670699L;
    private SemaphoreRef semaphore;
    private long timeout;
    private TimeUnit unit = TimeUnit.SECONDS;
    private int permit = 1;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public AcquireStep(SemaphoreRef semaphore) {
        this.semaphore = semaphore;
    }


    @DataBoundSetter
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @DataBoundSetter
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    @DataBoundSetter
    public void setPermit(int permit) {
        this.permit = permit;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "acquireSemaphore";
        }

        @Override
        public String getDisplayName() {
            return "Wait until the latch has counted down to zero.";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

    public static class Execution extends StepExecution {
        private AcquireStep step;


        public Execution(StepContext context, AcquireStep step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() {
            Semaphore semaphore = Optional.ofNullable(step)
                    .map(AcquireStep::getSemaphore)
                    .map(SemaphoreRef::getSemaphore)
                    .orElseThrow(NotAValidLockRefException::new);

            CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            if (step.getTimeout() > 0) {
                                boolean acquired = semaphore.tryAcquire(step.getPermit(), step.getTimeout(), step.getUnit());
                                if (acquired) {
                                    return ExitStatus.COMPLETED;
                                } else {
                                    return ExitStatus.TIMEOUT;
                                }
                            } else {
                                semaphore.acquire(step.getPermit());
                                return ExitStatus.COMPLETED;
                            }
                        } catch (InterruptedException e) {
                            throw new ConcurrentInterruptedException(e);
                        }
                    })
                    .thenApplyAsync((status) -> {
                                if (getContext().hasBody()) {
                                    try {
                                        getContext().newBodyInvoker().start().get();
                                    } catch (InterruptedException e) {
                                        throw new ConcurrentInterruptedException(e);
                                    } catch (ExecutionException e) {
                                        throw new ConcurrentException(e);
                                    } finally {
                                        //don't move to next stage
                                        //only release when acquiring with block
                                        semaphore.release(step.getPermit());
                                    }
                                }
                                return status;
                            }
                    )
                    .handleAsync((status, throwable) -> {
                        if (throwable == null) {
                            getContext().onSuccess(status);
                        } else {
                            getContext().onFailure(throwable);
                        }
                        return null;
                    });
            return false;
        }


        @Override
        public void stop(@Nonnull Throwable throwable) throws Exception {
            Optional.ofNullable(step)
                    .map(AcquireStep::getSemaphore)
                    .map(SemaphoreRef::getSemaphore)
                    .ifPresent(semaphore -> semaphore.release(step.getPermit()));
            super.stop(throwable);
        }
    }
}
