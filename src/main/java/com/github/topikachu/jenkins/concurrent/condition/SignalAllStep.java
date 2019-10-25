package com.github.topikachu.jenkins.concurrent.condition;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
@Setter
public class SignalAllStep extends Step implements Serializable {
    private static final long serialVersionUID = -1356972213491598903L;
    private LockAndCondition condition;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public SignalAllStep(LockAndCondition condition) {
        this.condition = condition;
    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "signalAll";
        }

        @Override
        public String getDisplayName() {
            return "Notify all wa.";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

    public static class Execution extends StepExecution {
        private SignalAllStep step;


        public Execution(StepContext context, SignalAllStep step) {
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
                                throw new ConcurrentInterruptedException(e);
                            }
                        }
                    })
                    .handleAsync((none, throwable) -> {
                        signalAll();
                        if (throwable == null) {
                            getContext().onSuccess(null);
                        } else {
                            getContext().onFailure(throwable);
                        }
                        return null;
                    });
            return false;
        }

        private void signalAll() {
            LockAndCondition lockAndCondition = step.getCondition();
            Optional.ofNullable(lockAndCondition.getLock())
                    .ifPresent(
                            lock -> {
                                lock.lock();
                                try {
                                    lockAndCondition.getCondition().signalAll();

                                } finally {
                                    lock.unlock();
                                }
                            }
                    );
        }

        @Override
        public void stop(Throwable cause) throws Exception {
            signalAll();
            super.stop(cause);
        }

        @Override
        public void onResume() {
            getContext().onFailure(new Exception("Resume after a restart not supported"));
        }
    }


}
