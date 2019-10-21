package com.github.topikachu.jenkins.concurrent.barrier;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Data;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;

@Data
public class CreateStep extends Step implements Serializable {

    private int count = 1;

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
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
            return "createBarrier";
        }

        @Override
        public String getDisplayName() {
            return "Create a Cyclic Barrier.";
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<BarrierRef> {


        private CreateStep step;

        public Execution(StepContext context, CreateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected BarrierRef run() throws Exception {

            return BarrierRef.builder()
                    .cyclicBarrier(new CyclicBarrier(step.getCount()))
                    .build();
        }


    }
}
