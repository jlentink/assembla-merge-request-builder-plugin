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
    private final String targetRepositoryUrl;

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
                                     String targetRepositoryUrl,
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
            targetBranch,
            commitId,
            title,
            description,
            sourceSpaceId,
            author
        );

        this.targetBranch = targetBranch;
        this.mergeRequestId = mergeRequestId;
        this.targetRepositoryUrl = targetRepositoryUrl;

    }

    public String getAbbreviatedTitle() {
        return StringUtils.abbreviate(getTitle(), 30);
    }

    public String getTargetRepositoryUrl() {
        return targetRepositoryUrl;
    }

    @Override
    public String getShortDescription() {
        if (isFromFork()) {
            return String.format("Assembla Merge Request #%s: %s - %s/%s => %s/%s",
                    getMergeRequestId(), getAbbreviatedTitle(),
                    getSourceRepositoryUrl(), getSourceBranch(),
                    getTargetRepositoryUrl(), getTargetBranch()
            );
        }
        return String.format("Assembla Merge Request #%s: %s - %s/%s => %s",
                getMergeRequestId(), getAbbreviatedTitle(),
                getSourceRepositoryUrl(), getSourceBranch(),
                getTargetBranch()
        );
    }

    public static AssemblaMergeRequestCause fromMergeRequest(MergeRequest mr,
                                                             SpaceTool sourceTool,
                                                             SpaceTool targetTool,
                                                             WebhookPayload payload) {
        return new AssemblaMergeRequestCause(
                mr.getId(),
                sourceTool.getUrl(),
                sourceTool.getName(),
                mr.getSourceSymbol(),
                targetTool.getUrl(),
                mr.getTargetSymbol(),
                payload.getCommitId(),
                mr.getDescription(),
                mr.getTargetSpaceId(),
                mr.getTitle(),
                payload.getAuthor()
        );
    }

    public boolean isFromFork() {
        return !getSourceRepositoryUrl().equals(getTargetRepositoryUrl());
    }
}
