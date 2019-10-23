package com.github.topikachu.jenkins.concurrent.semaphore;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SemaphoreTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test(timeout = 5000)
    public void testSemaphoreWithBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(SemaphoreTest.class.getResourceAsStream("Jenkinsfile.acquireWithBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out1 1", b);
        r.assertLogContains("out2 2", b);
        r.assertLogNotContains("out2 0", b);
    }

    @Test(timeout = 5000)
    public void testSemaphoreWithoutBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(SemaphoreTest.class.getResourceAsStream("Jenkinsfile.acquireWithoutBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out1 1", b);
        r.assertLogContains("out2 2", b);
        r.assertLogNotContains("out2 0", b);
    }

    @Test(timeout = 5000)
    public void testSemaphoreWithExceptionInBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(SemaphoreTest.class.getResourceAsStream("Jenkinsfile.acquireWithExceptionInBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out1 1", b);
        r.assertLogContains("has exception", b);

        r.assertLogContains("out2 2", b);
        r.assertLogNotContains("out2 0", b);
    }

    @Test(timeout = 10000)
    public void testTimeout() throws Exception {
        String jenkinsFileContent = IOUtils.toString(SemaphoreTest.class.getResourceAsStream("Jenkinsfile.acquireTimeout"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("status=TIMEOUT", b);
    }
}
