package org.jenkinsci.plugins.assembla;

import hudson.model.Cause;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaCause extends Cause {
    private final Integer mergeRequestId;
    private final String sourceName;
    private final String sourceRepository;
    private final String sourceBranch;
    private final String targetBranch;
    private final String description;
    private final String sourceProjectName;
    private final String lastCommitId;

    public Integer getMergeRequestId() {
        return mergeRequestId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceRepository() {
        return sourceRepository;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceProjectName() {
        return sourceProjectName;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public AssemblaCause(Integer mergeRequestId, String sourceName, String sourceRepository, String sourceBranch, String targetBranch, String description, String sourceProjectName, String lastCommitId) {
        this.mergeRequestId = mergeRequestId;
        this.sourceName = sourceName;
        this.sourceRepository = sourceRepository;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.description = description;
        this.sourceProjectName = sourceProjectName;
        this.lastCommitId = lastCommitId;
    }

    @Override
    public String getShortDescription() {
        return "Assembla Merge Request #" + mergeRequestId + " : " + sourceName + "/" + sourceBranch + " => " + targetBranch;
    }
}
