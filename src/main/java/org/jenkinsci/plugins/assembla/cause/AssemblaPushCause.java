package org.jenkinsci.plugins.assembla.cause;

import hudson.model.Cause;
import org.jenkinsci.plugins.assembla.AssemblaBuildTrigger;
import org.jenkinsci.plugins.assembla.WebhookPayload;
import org.jenkinsci.plugins.assembla.api.models.SpaceTool;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaPushCause extends AssemblaCause {
    public AssemblaPushCause(String sourceRepositoryUrl, String sourceRepositoryName, String sourceBranch, String commitId, String title, String description, String sourceSpaceId) {
        super(sourceRepositoryUrl, sourceRepositoryName, sourceBranch, commitId, title, description, sourceSpaceId);
    }

    @Override
    public String getShortDescription() {
        return "Assembla " + getTitle() + " - " + getDescription();
    }

    public static AssemblaPushCause fromChangeset(SpaceTool tool, WebhookPayload payload) {
        return new AssemblaPushCause(
                payload.getRepositoryUrl(),
                tool.getName(),
                payload.getBranch(),
                payload.getCommitId(),
                payload.getTitle(),
                payload.getBody(),
                tool.getSpaceId()
        );
    }


}
