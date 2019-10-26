package com.github.topikachu.jenkins.concurrent.condition;

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

public class ConditionTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();


    @Test(timeout = 30000)
    public void testSignalAllWithoutBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(ConditionTest.class.getResourceAsStream("Jenkinsfile.signalAllWithoutBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out=true", b);
        r.assertLogNotContains("out=false", b);
    }

    @Test(timeout = 30000)
    public void testSignalAllWithBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(ConditionTest.class.getResourceAsStream("Jenkinsfile.signalAllWithBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out=true", b);
        r.assertLogNotContains("out=false", b);
    }

    @Test(timeout = 30000)
    public void testSignalAllWithExceptionInBody() throws Exception {

        String jenkinsFileContent = IOUtils.toString(ConditionTest.class.getResourceAsStream("Jenkinsfile.signalAllWithExceptionInBody"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("out=true", b);
        r.assertLogNotContains("out=false", b);
        r.assertLogContains("has exception", b);

    }

    @Test(timeout = 30000)
    public void testTimeout() throws Exception {
        String jenkinsFileContent = IOUtils.toString(ConditionTest.class.getResourceAsStream("Jenkinsfile.awaitTimeout"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogContains("status=TIMEOUT", b);

    }
}
