package com.github.topikachu.jenkins.concurrent.all;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class AllTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test(timeout = 30000)
    public void testPlugin() throws Exception {

        BufferedReader jenkinsFileReader = new BufferedReader(new InputStreamReader(AllTest.class.getResourceAsStream("Jenkinsfile")));
        String jenkinsFileContent = jenkinsFileReader.lines().collect(Collectors.joining("\n"));
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(jenkinsFileContent, true));
        r.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }


}
