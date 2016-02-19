package org.jenkinsci.plugins.assembla;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.assembla.api.AssemblaClient;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.jenkinsci.plugins.assembla.api.models.MergeRequestVersion;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pavel on 18/2/16.
 */
public class AssemblaBuilder {
    private AssemblaBuildTrigger trigger;
    private static final Logger LOGGER = Logger.getLogger(AssemblaBuilder.class.getName());

    public AssemblaBuilder(AssemblaBuildTrigger trigger) {
        this.trigger = trigger;
    }

    public void onStarted(AbstractBuild build) {
        AssemblaCause cause = getCause(build);

        if (cause != null) {
            MergeRequest mr = AssemblaBuildTrigger.getAssembla().getMergeRequest(
                cause.getSourceSpaceId(),
                cause.getSourceRepositoryName(),
                cause.getMergeRequestId()
            );
            try {
                build.setDescription(getOnStartedMessage(mr));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to set build description", e);
            }
        }
    }

    public void onCompleted(AbstractBuild build) {
        AssemblaCause cause = getCause(build);
        if (cause == null) {
            return;
        }

        AssemblaClient client = AssemblaBuildTrigger.getAssembla();

        MergeRequest mr = client.getMergeRequest(
                cause.getSourceSpaceId(),
                cause.getSourceRepositoryName(),
                cause.getMergeRequestId()
        );

        Result result = build.getResult();
        StringBuilder messageBuilder = new StringBuilder();

        if (mr != null) {
            MergeRequestVersion mrVersion = client.getLatestVersion(mr);


            if (result == Result.SUCCESS) {
                client.upVoteMergeRequest(mr, mrVersion);
            } else if (result == Result.FAILURE || result == Result.UNSTABLE) {
                client.downVoteMergeRequest(mr, mrVersion);
            }

            String buildUrl = Jenkins.getInstance().getRootUrl() + build.getUrl();

            messageBuilder
                .append(build.getResult().toString()).append("\n")
                .append("Build results available at: ").append(buildUrl).append("\n");

            client.commentMergeRequest(mr, mrVersion, messageBuilder.toString());
        }

        LOGGER.info("Build result: " + result.toString());
    }

    private AssemblaCause getCause(AbstractBuild build) {
        Cause cause = build.getCause(AssemblaCause.class);

        if (cause == null || !(cause instanceof AssemblaCause)) {
            return null;
        }

        return (AssemblaCause) cause;
    }

    private String getOnStartedMessage(MergeRequest mr) {
        String mrUrl = AssemblaBuildTrigger.getAssembla().getMergeRequestWebUrl(mr);
        return "Merge Request <a href=\"" + mrUrl + "\" target=\"_blank\">#" + mr.getId()
                + "</a> " + " (" + mr.getSourceSymbol() + " => " + mr.getTargetSymbol() + ")";
    }
}
