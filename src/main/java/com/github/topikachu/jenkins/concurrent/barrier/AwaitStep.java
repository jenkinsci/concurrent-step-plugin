package com.github.topikachu.jenkins.concurrent.barrier;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

@Getter
@Setter
public class AwaitStep extends Step implements Serializable {
    private static final long serialVersionUID = 3637188256729702059L;
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

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

    public static class Execution extends StepExecution {
        private AwaitStep step;

        public Execution(StepContext context, AwaitStep step) {
            super(context);
            this.step = step;
        }


        private ExitStatus await() {
            return Optional.ofNullable(step.getBarrier().getCyclicBarrier())
                    .map(
                            barrier -> {
                                try {
                                    if (step.getTimeout() > 0) {
                                        barrier.await(step.getTimeout(), step.getUnit());
                                    } else {
                                        barrier.await();
                                    }
                                    return ExitStatus.COMPLETED;
                                } catch (TimeoutException e) {
                                    return ExitStatus.TIMEOUT;

                                } catch (BrokenBarrierException e) {
                                    return ExitStatus.BROKEN;
                                } catch (InterruptedException e) {
                                    throw new ConcurrentInterruptedException(e);
                                }
                            }
                    ).orElseThrow(NotAValidLockRefException::new);
        }

        @Override
        public boolean start() throws Exception {
            CompletableFuture
                    .runAsync(() -> {
                        if (getContext().hasBody()) {
                            try {
                                getContext().newBodyInvoker().start().get();
                            } catch (InterruptedException e) {
                                throw new ConcurrentInterruptedException(e);
                            } catch (ExecutionException e) {
                                throw new ConcurrentException(e);
                            }
                        }
                    })
                    .handleAsync((none, throwable) -> {
                        ExitStatus status = await();
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
        public void onResume() {
            getContext().onFailure(new Exception("Resume after a restart not supported"));
        }
    }


}
