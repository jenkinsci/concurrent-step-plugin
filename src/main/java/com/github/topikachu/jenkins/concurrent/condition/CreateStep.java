package com.github.topikachu.jenkins.concurrent.condition;

import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
public class CreateStep extends Step implements Serializable {


    private static final long serialVersionUID = -2236165192828626451L;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public CreateStep() {

    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "createCondition";
        }

        @Override
        public String getDisplayName() {
            return "Create a lock.";
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<LockAndCondition> {


        private CreateStep step;

        public Execution(StepContext context, CreateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected LockAndCondition run() {

            return LockAndCondition.builder().build();


        }


    }
}
