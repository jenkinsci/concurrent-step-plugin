package com.github.topikachu.jenkins.concurrent.latch;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Data;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Data
public class CountDownStep extends Step implements Serializable {
    private LatchRef latch;

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
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
    }

    public static class Execution extends SynchronousStepExecution {
        private CountDownStep step;


        public Execution(StepContext context, CountDownStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Object run() throws Exception {
            countDown();
            return null;
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
    }
}
