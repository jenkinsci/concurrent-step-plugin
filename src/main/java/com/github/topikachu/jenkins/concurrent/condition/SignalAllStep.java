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
    }

    public static class Execution extends SynchronousStepExecution {
        private SignalAllStep step;


        public Execution(StepContext context, SignalAllStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Object run() {
            signAll();
            return null;
        }

        private void signAll() {
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
            signAll();
            super.stop(cause);
        }
    }
}
