package org.jenkinsci.plugins.assembla;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by pavel on 28/2/16.
 */
public class AssemblaBuildListenerTest {

    AbstractProject project;
    AbstractBuild build;
    AssemblaBuildTrigger trigger;
    AssemblaBuildReporter buildReporter;
    AssemblaBuildListener listener;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        project = mock(AbstractProject.class);
        build = mock(AbstractBuild.class);
        trigger = spy(AssemblaTestUtil.getTrigger());
        buildReporter = spy(new AssemblaBuildReporter(trigger));

        listener = new AssemblaBuildListener();

        given(build.getProject()).willReturn(project);
        given(project.getTrigger(any(Class.class))).willReturn(trigger);
        trigger.setBuildReporter(buildReporter);

    }

    @Test
    public void testOnStarted() throws Exception {
        listener.onStarted(build, mock(TaskListener.class));
        verify(buildReporter, times(1)).onStarted(eq(build), any(TaskListener.class));
    }

    @Test
    public void testOnCompleted() throws Exception {
        listener.onCompleted(build, mock(TaskListener.class));
        verify(buildReporter, times(1)).onCompleted(eq(build), any(TaskListener.class));
    }
}