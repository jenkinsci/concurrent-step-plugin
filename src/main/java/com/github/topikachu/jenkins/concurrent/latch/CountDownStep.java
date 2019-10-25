package com.github.topikachu.jenkins.concurrent.latch;

import com.github.topikachu.jenkins.concurrent.exception.ConcurrentException;
import com.github.topikachu.jenkins.concurrent.exception.ConcurrentInterruptedException;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Getter
@Setter
public class CountDownStep extends Step implements Serializable {
    private static final long serialVersionUID = 8351999924255758163L;
    private LatchRef latch;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public CountDownStep(LatchRef latch) {
        this.latch = latch;
    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "countDownLatch";
        }

        @Override
        public String getDisplayName() {
            return "Decrements the count of the latch.";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

    public static class Execution extends StepExecution {
        private CountDownStep step;


        public Execution(StepContext context, CountDownStep step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() {
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
                            }

                    )
                    .handleAsync((none, throwable) -> {
                                countDown();
                                if (throwable == null) {
                                    getContext().onSuccess(null);

                                } else {
                                    getContext().onFailure(throwable);
                                }
                                return null;
                            }
                    );

            return false;
        }


        @Override
        public void stop(@Nonnull Throwable throwable) throws Exception {
            countDown();
            super.stop(throwable);
        }

        private void countDown() {
            Optional.ofNullable(step.getLatch().getCountDownLatch())
                    .ifPresent(CountDownLatch::countDown);
        }

        @Override
        public void onResume() {
            getContext().onFailure(new Exception("Resume after a restart not supported"));
        }
    }
}
