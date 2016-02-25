package org.jenkinsci.plugins.assembla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;
import org.apache.commons.io.IOUtils;
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
import java.util.logging.Logger;

/**
 * Created by pavel on 16/2/16.
 */

@Extension
public class AssemblaWebhook implements UnprotectedRootAction {
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
        return "assembla-webhook";
    }


    public void doIndex(StaplerRequest req, StaplerResponse resp) {
        String body = extractRequestBody(req);
        LOGGER.info("Webhook payload: " + body);
        Gson gson = new GsonBuilder().create();
        WebhookPayload payload = gson.fromJson(body, WebhookPayload.class);

        if (payload != null && payload.shouldTriggerBuild()) {
            SpaceTool tool = AssemblaBuildTrigger
                    .getAssembla()
                    .getRepoByUrl(payload.getSpace(), payload.getRepositoryUrl());

            if (tool == null) {
                LOGGER.info(
                    "Can not find tool with url: " + payload.getRepositoryUrl() + ", space: " + payload.getSpace()
                );
                return;
            }

            if (payload.isMergeRequestEvent()) {
                processMergeRequestEvent(payload, tool);
            } else if (payload.isChangesetEvent()) {
                processChangesetEvent(payload, tool);
            }
        }

    }

    private void processChangesetEvent(WebhookPayload payload, SpaceTool tool) {
        LOGGER.info("Processing changeset event");
        AssemblaPushCause cause = AssemblaPushCause.fromChangeset(tool, payload);

        for (AssemblaBuildTrigger trigger : getTriggers(payload.getSpace(), tool.getName())) {
            LOGGER.info("Triggering " + trigger.toString());
            trigger.handlePush(cause);
        }
    }

    private void processMergeRequestEvent(WebhookPayload payload, SpaceTool tool) {
        MergeRequest mr = AssemblaBuildTrigger.getAssembla()
                .getMergeRequest(
                        payload.getSpace(),
                        tool.getName(),
                        payload.getMergeRequestId()
                );

        if (mr == null) {
            LOGGER.info(
                "Can not find MR with ID: " + payload.getMergeRequestId() + ", tool: " + tool.getName() + ", space: " + payload.getSpace()
            );
            return;
        }

        AssemblaMergeRequestCause cause = AssemblaMergeRequestCause.fromMergeRequest(mr, tool, payload);

        for (AssemblaBuildTrigger trigger : getTriggers(payload.getSpace(), tool.getName())) {
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

    private List<AssemblaBuildTrigger> getTriggers(String spaceName, String repoName) {
        List<AssemblaBuildTrigger> triggers = new ArrayList<>();

        for (AbstractProject project : AssemblaBuildTrigger.getDesc().getRepoJobs(spaceName, repoName)) {
            AssemblaBuildTrigger trigger = (AssemblaBuildTrigger) project.getTrigger(AssemblaBuildTrigger.class);

            triggers.add(trigger);
        }
        return triggers;
    }
}
