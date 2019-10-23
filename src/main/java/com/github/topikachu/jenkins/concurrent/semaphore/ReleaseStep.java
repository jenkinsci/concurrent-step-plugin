package com.github.topikachu.jenkins.concurrent.semaphore;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;

@Getter
@Setter
public class ReleaseStep extends Step implements Serializable {
    private static final long serialVersionUID = 8351999924255758163L;
    private SemaphoreRef semaphore;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public ReleaseStep(SemaphoreRef semaphore) {
        this.semaphore = semaphore;
    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "releaseSemaphore";
        }

        @Override
        public String getDisplayName() {
            return "Release the semaphore.";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution {
        private ReleaseStep step;


        public Execution(StepContext context, ReleaseStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Object run() {
            if (getContext().hasBody()) {
                getContext().newBodyInvoker().withCallback(new BodyExecutionCallback() {
                    @Override
                    public void onSuccess(StepContext context, Object result) {
                        release();
                    }

                    @Override
                    public void onFailure(StepContext context, Throwable t) {
                        release();
                        context.onFailure(t);
                    }
                }).start();
            } else {
                release();
            }
            return null;
        }


        @Override
        public void stop(@Nonnull Throwable throwable) throws Exception {
            release();
            super.stop(throwable);
        }

        private void release() {
            Optional.ofNullable(step.getSemaphore().getSemaphore())
                    .ifPresent(Semaphore::release);
        }
    }
}
