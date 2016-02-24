package org.jenkinsci.plugins.assembla.cause;

import hudson.model.Cause;

/**
 * Created by pavel on 23/2/16.
 */
public abstract class AssemblaCause extends Cause {
    private final String sourceRepositoryUrl;
    private final String sourceRepositoryName;
    private final String sourceBranch;
    private final String commitId;
    private final String description;
    private final String sourceSpaceId;
    private final String title;

    public String getSourceRepositoryUrl() {
        return sourceRepositoryUrl;
    }

    public String getSourceRepositoryName() {
        return sourceRepositoryName;
    }

    public String getSourceBranch() {
        return sourceBranch;
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

    public AssemblaCause(String sourceRepositoryUrl,
                             String sourceRepositoryName,
                             String sourceBranch,
                             String commitId,
                             String title,
                             String description,
                             String sourceSpaceId) {

        this.sourceRepositoryUrl = sourceRepositoryUrl;
        this.sourceRepositoryName = sourceRepositoryName;
        this.sourceBranch = sourceBranch;
        this.commitId = commitId;
        this.description = description;
        this.sourceSpaceId = sourceSpaceId;
        this.title = title;
    }

    @Override
    public String toString() {
        return "AssemblaCause{" +
                "sourceRepositoryUrl='" + sourceRepositoryUrl + '\'' +
                ", sourceRepositoryName='" + sourceRepositoryName + '\'' +
                ", sourceBranch='" + sourceBranch + '\'' +
                ", commitId='" + commitId + '\'' +
                ", description='" + description + '\'' +
                ", sourceSpaceId='" + sourceSpaceId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
