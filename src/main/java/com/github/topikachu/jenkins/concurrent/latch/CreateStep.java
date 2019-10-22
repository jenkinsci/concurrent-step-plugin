package com.github.topikachu.jenkins.concurrent.latch;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Getter
@Setter
public class CreateStep extends Step implements Serializable {

    private static final long serialVersionUID = 4494454093632960334L;
    private int count = 1;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public CreateStep() {

    }

    @DataBoundSetter
    public void setCount(int count) {
        this.count = count;
    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "createLatch";
        }

        @Override
        public String getDisplayName() {
            return "Create a count down latch.";
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<LatchRef> {


        private CreateStep step;

        public Execution(StepContext context, CreateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected LatchRef run() {

            return LatchRef.builder()
                    .countDownLatch(new CountDownLatch(step.getCount()))
                    .build();

        }


    }
}
