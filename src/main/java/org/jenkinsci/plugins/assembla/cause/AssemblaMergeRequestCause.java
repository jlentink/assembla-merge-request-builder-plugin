package org.jenkinsci.plugins.assembla.cause;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.assembla.WebhookPayload;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.jenkinsci.plugins.assembla.api.models.SpaceTool;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaMergeRequestCause extends AssemblaCause {
    private final Integer mergeRequestId;
    private final String targetBranch;

    public Integer getMergeRequestId() {
        return mergeRequestId;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public AssemblaMergeRequestCause(Integer mergeRequestId,
                                     String sourceRepositoryUrl,
                                     String sourceRepositoryName,
                                     String sourceBranch,
                                     String targetBranch,
                                     String commitId,
                                     String description,
                                     String sourceSpaceId,
                                     String title,
                                     String author) {
        super(
            sourceRepositoryUrl,
            sourceRepositoryName,
            sourceBranch,
            commitId,
            title,
            description,
            sourceSpaceId,
            author
        );

        this.targetBranch = targetBranch;
        this.mergeRequestId = mergeRequestId;

    }

    public String getAbbreviatedTitle() {
        return StringUtils.abbreviate(getTitle(), 30);
    }

    @Override
    public String getShortDescription() {
        return "Assembla Merge Request #" + getMergeRequestId() + ": " + getTitle() + " - "
                + getSourceRepositoryUrl() + "/" + getSourceBranch() + " => " + getTargetBranch();
    }

    public static AssemblaMergeRequestCause fromMergeRequest(MergeRequest mr, SpaceTool tool, WebhookPayload payload) {
        return new AssemblaMergeRequestCause(
                mr.getId(),
                payload.getRepositoryUrl(),
                tool.getName(),
                mr.getSourceSymbol(),
                mr.getTargetSymbol(),
                payload.getCommitId(),
                mr.getDescription(),
                mr.getTargetSpaceId(),
                mr.getTitle(),
                payload.getAuthor()
        );
    }
}
