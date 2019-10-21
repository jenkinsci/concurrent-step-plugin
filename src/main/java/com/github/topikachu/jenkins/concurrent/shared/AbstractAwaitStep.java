package com.github.topikachu.jenkins.concurrent.shared;

import com.github.topikachu.jenkins.concurrent.barrier.BarrierRef;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


abstract public class  AbstractAwaitStep <T> extends Step implements Serializable  {
    private T barrier;
    private long timeout;
    private TimeUnit unit = TimeUnit.SECONDS;
//    @DataBoundConstructor
//    public AwaitStep(BarrierRef barrier) {
//        this.barrier = barrier;
//    }
}
