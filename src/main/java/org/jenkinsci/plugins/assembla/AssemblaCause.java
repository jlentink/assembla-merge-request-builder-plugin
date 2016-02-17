package org.jenkinsci.plugins.assembla;

import hudson.model.Cause;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaCause extends Cause {
    private final Integer mergeRequestId;
    private final String sourceRepository;
    private final String sourceBranch;
    private final String targetBranch;
    private final String description;
    private final String sourceSpaceId;
    private final String title;

    public Integer getMergeRequestId() {
        return mergeRequestId;
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

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public String getTitle() {
        return title;
    }

    public AssemblaCause(Integer mergeRequestId,
                         String sourceRepository,
                         String sourceBranch,
                         String targetBranch,
                         String description,
                         String sourceSpaceId,
                         String title) {

        this.mergeRequestId = mergeRequestId;
        this.sourceRepository = sourceRepository;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.description = description;
        this.sourceSpaceId = sourceSpaceId;
        this.title = title;
    }

    @Override
    public String getShortDescription() {
        return "Assembla Merge Request #" + mergeRequestId + " : " + sourceRepository + "/" + sourceBranch + " => " + targetBranch;
    }
}
