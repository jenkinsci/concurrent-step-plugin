package com.github.topikachu.jenkins.concurrent.condition;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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

    public static class Execution extends SynchronousNonBlockingStepExecution {
        private SignalAllStep step;


        public Execution(StepContext context, SignalAllStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Object run() {

            if (getContext().hasBody()) {
                getContext().newBodyInvoker().withCallback(new BodyExecutionCallback() {
                    @Override
                    public void onSuccess(StepContext context, Object result) {
                        signalAll();
                    }

                    @Override
                    public void onFailure(StepContext context, Throwable t) {
                        signalAll();
                        context.onFailure(t);
                    }
                }).start();
            } else {
                signalAll();
            }
            return null;
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
    }


}
