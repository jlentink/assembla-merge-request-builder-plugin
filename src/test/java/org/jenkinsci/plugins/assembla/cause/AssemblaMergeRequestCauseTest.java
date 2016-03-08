package org.jenkinsci.plugins.assembla.cause;

import com.google.gson.GsonBuilder;
import org.jenkinsci.plugins.assembla.AssemblaTestUtil;
import org.jenkinsci.plugins.assembla.WebhookPayload;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by pavel on 28/2/16.
 */
public class AssemblaMergeRequestCauseTest {

    AssemblaMergeRequestCause cause;

    @Before
    public void setUp() throws Exception {
        cause = AssemblaTestUtil.getMergeRequestCause();
    }


    @Test
    public void testGetAbbreviatedTitle() throws Exception {
        assertEquals("Redirect all old catalog pa...", cause.getAbbreviatedTitle());
    }

    @Test
    public void testGetShortDescription() throws Exception {
        assertEquals(
            "Assembla Merge Request #12345: Redirect all old catalog pa... - git@git.assembla.com:pavel-fork.git/develop => git@git.assembla.com:pavel-test.git/master",
            cause.getShortDescription()
        );
    }

    @Test
    public void testIsFromFork() throws Exception {
        assertTrue(cause.isFromFork());
    }
}