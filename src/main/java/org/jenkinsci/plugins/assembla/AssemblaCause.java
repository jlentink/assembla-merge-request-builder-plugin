package org.jenkinsci.plugins.assembla;

import hudson.model.Cause;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaCause extends Cause {
    private final Integer mergeRequestId;
    private final String sourceRepositoryUrl;
    private final String sourceRepositoryName;
    private final String sourceBranch;
    private final String targetBranch;
    private final String commitId;
    private final String description;
    private final String sourceSpaceId;
    private final String title;

    public Integer getMergeRequestId() {
        return mergeRequestId;
    }

    public String getSourceRepositoryUrl() {
        return sourceRepositoryUrl;
    }

    public String getSourceRepositoryName() {
        return sourceRepositoryName;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public String getTitle() {
        return title;
    }

    public AssemblaCause(Integer mergeRequestId,
                         String sourceRepositoryUrl,
                         String sourceRepositoryName,
                         String sourceBranch,
                         String targetBranch,
                         String commitId,
                         String description,
                         String sourceSpaceId,
                         String title) {

        this.mergeRequestId = mergeRequestId;
        this.sourceRepositoryUrl = sourceRepositoryUrl;
        this.sourceRepositoryName = sourceRepositoryName;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.commitId = commitId;
        this.description = description;
        this.sourceSpaceId = sourceSpaceId;
        this.title = title;
    }

    @Override
    public String getShortDescription() {
        return "Assembla Merge Request #" + mergeRequestId + ": " + title + " - "
                + sourceRepositoryUrl + "/" + sourceBranch + " => " + targetBranch;
    }
}
