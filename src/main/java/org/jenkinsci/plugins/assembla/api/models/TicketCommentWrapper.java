package org.jenkinsci.plugins.assembla.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pavel on 19/2/16.
 */
public class TicketCommentWrapper {
    @SerializedName("ticket_comment")
    private TicketComment ticketComment;

    public TicketCommentWrapper(TicketComment ticketComment) {
        this.ticketComment = ticketComment;
    }

    public TicketComment getTicketComment() {
        return ticketComment;
    }

    public void setTicketComment(TicketComment ticketComment) {
        this.ticketComment = ticketComment;
    }
}
