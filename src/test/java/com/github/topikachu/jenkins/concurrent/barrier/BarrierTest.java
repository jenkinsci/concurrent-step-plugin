package com.github.topikachu.jenkins.concurrent.barrier;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class BarrierTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test(timeout = 30000)
    public void testAwaitWithoutBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(BarrierTest.class.getResourceAsStream("Jenkinsfile.awaitWithoutBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out=true", b);
        r.assertLogNotContains("out=false", b);

    }


    @Test(timeout = 30000)
    public void testAwaitWithBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(BarrierTest.class.getResourceAsStream("Jenkinsfile.awaitWithBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out=true", b);
        r.assertLogNotContains("out=false", b);

    }

    @Test(timeout = 30000)
    public void testAwaitWithExceptionInBody() throws Exception {
        String jenkinsFileContent = IOUtils.toString(BarrierTest.class.getResourceAsStream("Jenkinsfile.awaitWithExceptionInBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out=true", b);
        r.assertLogContains("has exception", b);
        r.assertLogNotContains("out=false", b);

    }

    @Test(timeout = 30000)
    public void testTimeout() throws Exception {
        String jenkinsFileContent = IOUtils.toString(BarrierTest.class.getResourceAsStream("Jenkinsfile.awaitTimeout"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("status=TIMEOUT", b);

    }


}
