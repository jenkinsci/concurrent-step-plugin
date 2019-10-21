package com.github.topikachu.jenkins.concurrent.condition;

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

    @Test(timeout = 5000)
    public void testPlugin() throws Exception {

        BufferedReader jenkinsFileReader = new BufferedReader(new InputStreamReader(ConditionTest.class.getResourceAsStream("Jenkinsfile")));
        String jenkinsFileContent = jenkinsFileReader.lines().collect(Collectors.joining("\n"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
        r.assertLogNotContains("out=false", b);
    }

    @Test(timeout = 5000)
    public void testTimeout() throws Exception {
        BufferedReader jenkinsFileReader = new BufferedReader(new InputStreamReader(ConditionTest.class.getResourceAsStream("Jenkinsfile2")));
        String jenkinsFileContent = jenkinsFileReader.lines().collect(Collectors.joining("\n"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        r.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }
}
