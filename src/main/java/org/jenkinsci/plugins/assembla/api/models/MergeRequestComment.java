package org.jenkinsci.plugins.assembla.api.models;

/**
 * Created by pavel on 18/2/16.
 */
public class MergeRequestComment {
    private String content;

    public MergeRequestComment(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
