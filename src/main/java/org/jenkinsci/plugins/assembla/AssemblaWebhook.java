package org.jenkinsci.plugins.assembla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.scenario.effect.Merge;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.assembla.api.AssemblaClient;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.jenkinsci.plugins.assembla.api.models.SpaceTool;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pavel on 16/2/16.
 */

@Extension
public class AssemblaWebhook implements UnprotectedRootAction {
    private static final Logger LOGGER = Logger.getLogger(AssemblaWebhook.class.getName());
    private static AssemblaBuildTrigger trigger;

    public static void setTrigger(AssemblaBuildTrigger trigger) {
        AssemblaWebhook.trigger = trigger;
    }

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
        LOGGER.info("Merge request ID: " + String.valueOf(payload.getMergeRequestId()));

        if (payload.isMergeRequestEvent()) {
            LOGGER.info("Event is merge request action");
            for (AbstractProject<?, ?> project : getTriggers(payload)) {
                AssemblaBuildTrigger trigger = project.getTrigger(AssemblaBuildTrigger.class);

                MergeRequest mr = AssemblaBuildTrigger.getAssembla()
                                    .findMergeRequest(
                                        payload.getSpace(),
                                        trigger.getRepoName(),
                                        payload.getMergeRequestId()
                                    );
                LOGGER.info("MR: " + mr.toString());

                if (trigger != null) {
                    LOGGER.info("Trigger is present!");
                    trigger.handleMergeRequest(new AssemblaCause(
                        mr.getId(),
                        payload.getRepositoryUrl(),
                        mr.getSourceSymbol(),
                        mr.getTargetSymbol(),
                        mr.getDescription(),
                        mr.getTargetSpaceId(),
                        mr.getTitle()
                    ));
                } else {
                    LOGGER.info("Trigger is null");
                }
            }
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

    private Set<AbstractProject<?, ?>> getTriggers(WebhookPayload payload) {
        // TODO: Implement me
//        MergeRequest mergeRequest = AssemblaBuildTrigger
//                                        .getAssembla()
//                                        .findMergeRequest(payload.getSpace(), payload.getMergeRequestId());
        Set<AbstractProject<?, ?>> triggers = AssemblaBuildTrigger.getDesc().getRepoTriggers("git-5");
        LOGGER.info("Triggers count: " + triggers.size());
        return triggers;
    }
}
