package org.jenkinsci.plugins.assembla;

import com.google.gson.annotations.SerializedName;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pavel on 16/2/16.
 */
public class WebhookPayload {
    private static final Pattern mergeRequestIdPattern = Pattern.compile("(\\d+)");
    private static final Logger LOGGER = Logger.getLogger(AssemblaWebhook.class.getName());

    private String space;
    private String action;
    private String object;
    private String title;
    private String body;
    private String author;
    private String branch;
    @SerializedName("repository_url")
    private String repositoryUrl;
    @SerializedName("commit_id")
    private String commitId;

    //  Payload example:
    //  {
    //    "space": "name",
    //    "action": "created",
    //    "object": "Merge request",
    //    "title": "Merge Request 2883693: Fix Helpdesk session issue",
    //    "body": "Pavel Dotsulenko (pavel.d) created Merge Request 2883693 (1): Fix Helpdesk session issue [+0] [-0]\n\n    some desc\n",
    //    "author": "pavel.d",
    //    "repository_suffix": "5",
    //    "repository_url": "git@git.assembla.com:232213.5.git",
    //    "branch": "assembla-2d6a0afde0",
    //    "commit_id": "2d6a0afde055f27fd0764b5fe525f926b4090360"
    //  }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public WebhookPayload(String space, String action, String object, String title, String body, String author, String branch, String repositoryUrl, String commitId) {
        this.space = space;
        this.action = action;
        this.object = object;
        this.title = title;
        this.body = body;
        this.author = author;
        this.branch = branch;
        this.repositoryUrl = repositoryUrl;
        this.commitId = commitId;
    }

    public String getSpace() {
        return space;
    }

    public String getAction() {
        return action;
    }

    public String getObject() {
        return object;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getAuthor() {
        return author;
    }

    public String getBranch() {
        return branch;
    }

    public String getCommitId() {
        return commitId;
    }

    public Integer getMergeRequestId() {
        Matcher m = mergeRequestIdPattern.matcher(title);
        Integer mergeRequestId = null;

        try {
            if (m.find()) {
                mergeRequestId = Integer.parseInt(m.group());
            }

        } catch (IllegalStateException | NumberFormatException ex) {
            LOGGER.info("Payload title: " + title);
            LOGGER.severe("Failed to parse merge request ID");
            LOGGER.severe(ex.toString());
        }

        return mergeRequestId;
    }

    public boolean isMergeRequestEvent() {
        return object.equals("Merge request");
    }

    public boolean shouldTriggerBuild() {
        return isMergeRequestEvent() &&  (action.equals("updated") || action.equals("created"));
    }

    @Override
    public String toString() {
        return "WebhookPayload{" +
                "space='" + space + '\'' +
                ", action='" + action + '\'' +
                ", object='" + object + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", author='" + author + '\'' +
                ", branch='" + branch + '\'' +
                ", commitId='" + commitId + '\'' +
                '}';
    }
}
