package org.jenkinsci.plugins.assembla;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by pavel on 28/2/16.
 */
public class AssemblaCrumbExclusionTest {

    @Test
    public void testGetExclusionPath() throws Exception {
        assertEquals("/assembla-webhook/", new AssemblaCrumbExclusion().getExclusionPath());
    }
}