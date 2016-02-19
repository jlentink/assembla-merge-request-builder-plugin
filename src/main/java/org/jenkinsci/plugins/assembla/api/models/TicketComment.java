package org.jenkinsci.plugins.assembla.api.models;

/**
 * Created by pavel on 19/2/16.
 */
public class TicketComment {
    private String comment;

    public TicketComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
