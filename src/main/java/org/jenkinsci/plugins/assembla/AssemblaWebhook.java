package org.jenkinsci.plugins.assembla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.assembla.api.AssemblaClient;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.jenkinsci.plugins.assembla.api.models.SpaceTool;
import org.jenkinsci.plugins.assembla.cause.AssemblaMergeRequestCause;
import org.jenkinsci.plugins.assembla.cause.AssemblaPushCause;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pavel on 16/2/16.
 */

@Extension
public class AssemblaWebhook implements UnprotectedRootAction {
    static final String URL =  "assembla-webhook";
    private static final Logger LOGGER = Logger.getLogger(AssemblaWebhook.class.getName());

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL;
    }


    public void doIndex(StaplerRequest req, StaplerResponse resp) {
        String body = extractRequestBody(req);
        LOGGER.info("Webhook payload: " + body);
        Gson gson = new GsonBuilder().create();
        WebhookPayload payload = gson.fromJson(body, WebhookPayload.class);

        if (payload != null && payload.shouldTriggerBuild()) {
            try {
                if (payload.isMergeRequestEvent()) {
                    processMergeRequestEvent(payload);
                } else if (payload.isChangesetEvent()) {
                    processChangesetEvent(payload);
                }
            } catch (AssemblaClient.AssemblaApiException ex) {
                LOGGER.log(Level.SEVERE, "Assembla API request failed", ex);
            }

        }

    }

    public void processChangesetEvent(WebhookPayload payload) {
        SpaceTool sourceRepo = getSpaceTool(payload);
        if (sourceRepo == null) {
            return;
        }

        LOGGER.info("Processing changeset event");
        AssemblaPushCause cause = AssemblaPushCause.fromChangeset(sourceRepo, payload);

        for (AssemblaBuildTrigger trigger : getTriggers(payload.getSpaceWikiName(), sourceRepo.getName())) {
            trigger.handlePush(cause);
        }
    }

    public void processMergeRequestEvent(WebhookPayload payload) {
        SpaceTool sourceRepo = getSpaceTool(payload);
        if (sourceRepo == null) {
            LOGGER.info("Can not find source tool with url: " + payload.getRepositoryUrl());
            return;
        }

        MergeRequest mr = AssemblaBuildTrigger.getAssembla()
                .getMergeRequest(
                        payload.getSpaceWikiName(),
                        sourceRepo.getName(),
                        payload.getMergeRequestId()
                );

        if (mr == null) {
            LOGGER.info("Can not find MR with ID: " + payload.getMergeRequestId() + ", tool: " + sourceRepo.getName());
            return;
        }

        SpaceTool targetRepo;

        // Merge request is not from forked repo
        if (mr.getSpaceToolId().equals(mr.getTargetSpaceToolId())) {
            targetRepo = sourceRepo;
        } else {
            targetRepo = AssemblaBuildTrigger.getAssembla().getTool(payload.getSpaceWikiName(), mr.getTargetSpaceToolId());
        }

        if (targetRepo == null) {
            LOGGER.info("Can not find target tool with ID: " + mr.getTargetSpaceToolId());
            return;
        }

        AssemblaMergeRequestCause cause = AssemblaMergeRequestCause.fromMergeRequest(mr, sourceRepo, targetRepo, payload);

        for (AssemblaBuildTrigger trigger : getTriggers(payload.getSpaceWikiName(), targetRepo.getName())) {
            trigger.handleMergeRequest(cause);
        }
    }

    private String extractRequestBody(StaplerRequest req) {
        String body = null;
        BufferedReader br = null;
        try {
            br = req.getReader();
            body = IOUtils.toString(br);
        } catch (IOException e) {
            body = null;
        } finally {
            IOUtils.closeQuietly(br);
        }
        return body;
    }

    public List<AssemblaBuildTrigger> getTriggers(String spaceName, String repoName) {

        List<AssemblaBuildTrigger> triggers = new ArrayList<>();

        for (AbstractProject project : AssemblaBuildTrigger.getDesc().getRepoJobs(spaceName, repoName)) {
            AssemblaBuildTrigger trigger = (AssemblaBuildTrigger) project.getTrigger(AssemblaBuildTrigger.class);

            triggers.add(trigger);
        }

        return triggers;
    }

    private SpaceTool getSpaceTool(WebhookPayload payload) {
        return AssemblaBuildTrigger
                .getAssembla()
                .getRepoByUrl(payload.getSpaceWikiName(), payload.getRepositoryUrl());
    }
}
