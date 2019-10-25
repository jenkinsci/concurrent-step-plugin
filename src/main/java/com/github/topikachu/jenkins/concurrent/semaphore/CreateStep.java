package com.github.topikachu.jenkins.concurrent.semaphore;

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

@Getter
@Setter
public class CreateStep extends Step implements Serializable {

    private static final long serialVersionUID = 7880534681744801670L;
    private int permit = 1;

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(stepContext, this);
    }

    @DataBoundConstructor
    public CreateStep(int permit) {
        this.permit = permit;
    }

    @DataBoundSetter
    public void setPermit(int permit) {
        this.permit = permit;
    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {


        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.<Class<?>>singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "createSemaphore";
        }

        @Override
        public String getDisplayName() {
            return "Create a semaphore.";
        }
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<SemaphoreRef> {


        private CreateStep step;

        public Execution(StepContext context, CreateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected SemaphoreRef run() {

            return SemaphoreRef.builder()
                    .permit(step.getPermit())
                    .build();

        }


    }
}
