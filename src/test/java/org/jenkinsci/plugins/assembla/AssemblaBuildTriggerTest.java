package org.jenkinsci.plugins.assembla;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParameterValue;
import org.jenkinsci.plugins.assembla.api.AssemblaClient;
import org.jenkinsci.plugins.assembla.api.models.SpaceTool;
import org.jenkinsci.plugins.assembla.cause.AssemblaMergeRequestCause;
import org.jenkinsci.plugins.assembla.cause.AssemblaPushCause;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by pavel on 28/2/16.
 */
public class AssemblaBuildTriggerTest {

    AssemblaClient client = mock(AssemblaClient.class);

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    AssemblaWebhook webhook;
    AssemblaBuildTrigger trigger;
    FreeStyleProject project;

    @Before
    public void setUp() throws Exception {
        project = jenkinsRule.createFreeStyleProject("testJob");
        AssemblaTestUtil.setupAssemblaTriggerDescriptor();
        trigger = spy(AssemblaTestUtil.getTrigger());

        webhook = spy(new AssemblaWebhook());
        AssemblaBuildTrigger.setAssembla(client);

        given(client.setConfig(anyString(), anyString(), anyString(), anyBoolean())).willCallRealMethod();
        given(client.getRepoByUrl(anyString(), anyString())).willReturn(mock(SpaceTool.class));
    }


    @Test
    public void testHandlePush() throws Exception {
        project = spy(project);
        trigger.start(project, true);
        AssemblaPushCause pushCause = AssemblaTestUtil.getPushCause();
        trigger.handlePush(pushCause);
        verify(project, times(1)).scheduleBuild2(eq(0), eq(pushCause), any(ParametersAction.class));
    }

    @Test
    public void testHandleMergeRequest() throws Exception {
        project = spy(project);
        trigger.start(project, true);
        AssemblaMergeRequestCause mrCause = AssemblaTestUtil.getMergeRequestCause();
        trigger.handleMergeRequest(mrCause);
        verify(project, times(1)).scheduleBuild2(eq(0), eq(mrCause), any(ParametersAction.class));
    }

    @Test
    public void testHandleMergeRequestWithoutDescription() throws Exception {
        project = spy(project);
        trigger.start(project, true);
        AssemblaMergeRequestCause mrCause = AssemblaTestUtil.getMergeRequestCauseWithoutDescription();
        trigger.handleMergeRequest(mrCause);

        ArgumentCaptor<ParametersAction> argument = ArgumentCaptor.forClass(ParametersAction.class);
        verify(project, times(1)).scheduleBuild2(eq(0), eq(mrCause), argument.capture());
        ParameterValue descriptionValue = argument.getValue().getParameter("assemblaDescription");
        assertEquals("", descriptionValue.getValue());
    }

    @Test
    public void testGetDescriptor() throws Exception {
        assertTrue(trigger.getDescriptor() != null);
    }


    @Test
    public void testAddsRepoTriggerOnStart() throws Exception {
        trigger.start(project, true);
        Set<AbstractProject<?, ?>> projects = trigger.getDescriptor()
                .getRepoJobs(trigger.getSpaceName(), trigger.getRepoName());

        assertFalse("Projects set is empty", projects.isEmpty());
    }
}