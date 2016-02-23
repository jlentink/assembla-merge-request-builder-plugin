package org.jenkinsci.plugins.assembla;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.assembla.api.AssemblaClient;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.jenkinsci.plugins.assembla.api.models.MergeRequestVersion;
import org.jenkinsci.plugins.assembla.api.models.Ticket;
import org.jenkinsci.plugins.assembla.cause.AssemblaMergeRequestCause;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pavel on 18/2/16.
 */
public class AssemblaBuildReporter {
    private AssemblaBuildTrigger trigger;
    private static final Logger LOGGER = Logger.getLogger(AssemblaBuildReporter.class.getName());

    public AssemblaBuildReporter(AssemblaBuildTrigger trigger) {
        this.trigger = trigger;
    }

    public void onStarted(AbstractBuild build) {
        AssemblaMergeRequestCause cause = getCause(build);

        if (cause != null) {
            AssemblaClient client = AssemblaBuildTrigger.getAssembla();
            MergeRequest mr = client.getMergeRequest(
                cause.getSourceSpaceId(),
                cause.getSourceRepositoryName(),
                cause.getMergeRequestId()
            );

            if (trigger.isNotifyOnStart()) {
                String message = "Build started, monitor at " + getBuildUrl(build);

                if (trigger.isTicketComments()) {
                    for (Ticket ticket : client.getMergeRequestTickets(mr)) {
                        client.createTicketComment(ticket, message);
                    }
                }

                if (trigger.isMergeRequestComments()) {
                    client.commentMergeRequest(mr, client.getLatestVersion(mr), message);
                }
            }

            try {
                build.setDescription(getOnStartedMessage(mr));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to set build description", e);
            }
        }
    }

    public void onCompleted(AbstractBuild build) {
        AssemblaMergeRequestCause cause = getCause(build);
        if (cause == null) {
            return;
        }

        AssemblaClient client = AssemblaBuildTrigger.getAssembla();

        MergeRequest mr = client.getMergeRequest(
                cause.getSourceSpaceId(),
                cause.getSourceRepositoryName(),
                cause.getMergeRequestId()
        );

        if (mr == null) {
            LOGGER.info("Could not find Merge Request");
            return;
        }

        Result result = build.getResult();


        String message = new StringBuilder()
            .append(build.getProject().getDisplayName()).append(" finished with status: ")
            .append(build.getResult().toString()).append("\n")
            .append("Source revision: ").append(cause.getCommitId()).append("\n")
            .append("Build results available at: ").append(getBuildUrl(build)).append("\n")
            .toString();

        if (trigger.isMergeRequestComments()) {
            MergeRequestVersion mrVersion = client.getLatestVersion(mr);

            client.commentMergeRequest(mr, mrVersion, message);

            if (result == Result.SUCCESS) {
                client.upVoteMergeRequest(mr, mrVersion);
            } else if (result == Result.FAILURE || result == Result.UNSTABLE) {
                client.downVoteMergeRequest(mr, mrVersion);
            }
        }

        if (trigger.isTicketComments()) {
          for (Ticket ticket : client.getMergeRequestTickets(mr)) {
              client.createTicketComment(ticket, message);
          }
        }

        LOGGER.info("Build result: " + result);
    }

    private AssemblaMergeRequestCause getCause(AbstractBuild build) {
        Cause cause = build.getCause(AssemblaMergeRequestCause.class);

        if (cause == null || !(cause instanceof AssemblaMergeRequestCause)) {
            return null;
        }

        return (AssemblaMergeRequestCause) cause;
    }

    private String getBuildUrl(AbstractBuild build) {
        return Jenkins.getInstance().getRootUrl() + build.getUrl();
    }

    private String getOnStartedMessage(MergeRequest mr) {
        String mrUrl = AssemblaBuildTrigger.getAssembla().getMergeRequestWebUrl(mr);
        return "MR <a href=\"" + mrUrl + "\">#" + mr.getId()
                + "</a> " + " (" + mr.getSourceSymbol() + " => " + mr.getTargetSymbol() + ")";
    }
}
