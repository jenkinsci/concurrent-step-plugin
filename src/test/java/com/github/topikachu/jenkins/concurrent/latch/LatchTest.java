package com.github.topikachu.jenkins.concurrent.latch;

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

public class LatchTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test(timeout = 30000)
    public void testCountDownLatchWithBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(LatchTest.class.getResourceAsStream("Jenkinsfile.countDownLatchWithBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("var1=true", b);
        r.assertLogContains("var2=true", b);
        r.assertLogNotContains("var1=false", b);
        r.assertLogNotContains("var2=false", b);
    }

    @Test(timeout = 30000)
    public void testCountDownLatchWithoutBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(LatchTest.class.getResourceAsStream("Jenkinsfile.countDownLatchWithoutBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("var1=true", b);
        r.assertLogContains("var2=true", b);
        r.assertLogNotContains("var1=false", b);
        r.assertLogNotContains("var2=false", b);
    }

    @Test(timeout = 30000)
    public void testCountDownLatchWithExceptionInBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(LatchTest.class.getResourceAsStream("Jenkinsfile.countDownLatchWithExceptionInBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("var1=true", b);
        r.assertLogContains("var2=true", b);
        r.assertLogNotContains("var1=false", b);
        r.assertLogNotContains("var2=false", b);
        r.assertLogContains("has exception", b);

    }

    @Test(timeout = 30000)
    public void testTimeout() throws Exception {
        String jenkinsFileContent = IOUtils.toString(LatchTest.class.getResourceAsStream("Jenkinsfile.awaitTimeout"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("status=TIMEOUT", b);
    }
}
