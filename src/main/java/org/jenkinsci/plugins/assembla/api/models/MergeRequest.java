package org.jenkinsci.plugins.assembla.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by pavel on 16/2/16.
 */
public class MergeRequest {
    private int id;
    private String title;
    private String description;
    @SerializedName("commit_id")
    private String commitId;
    @SerializedName("user_id")
    private String userId;

    @SerializedName("processed_by_user_id")
    private String processedByUserId;
    @SerializedName("source_cleanup")
    private int sourceCleanup;

    @SerializedName("source_symbol")
    private String sourceSymbol;
    @SerializedName("source_symbol_type")
    private String sourceSymbolType;
    @SerializedName("space_tool_id")
    private String spaceToolId;
    private int status;
    @SerializedName("target_space_tool_id")
    private String targetSpaceToolId;
    @SerializedName("target_space_id")
    private String targetSpaceId;
    @SerializedName("target_symbol")
    private String targetSymbol;

    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;

    private String url;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProcessedByUserId() {
        return processedByUserId;
    }

    public int getSourceCleanup() {
        return sourceCleanup;
    }

    public String getSourceSymbol() {
        return sourceSymbol;
    }

    public String getSourceSymbolType() {
        return sourceSymbolType;
    }

    public String getSpaceToolId() {
        return spaceToolId;
    }

    public int getStatus() {
        return status;
    }

    public String getTargetSpaceToolId() {
        return targetSpaceToolId;
    }

    public String getTargetSpaceId() {
        return targetSpaceId;
    }

    public String getTargetSymbol() {
        return targetSymbol;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "MergeRequest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", commitId='" + commitId + '\'' +
                ", userId='" + userId + '\'' +
                ", processedByUserId='" + processedByUserId + '\'' +
                ", sourceCleanup=" + sourceCleanup +
                ", sourceSymbol='" + sourceSymbol + '\'' +
                ", sourceSymbolType='" + sourceSymbolType + '\'' +
                ", spaceToolId='" + spaceToolId + '\'' +
                ", status=" + status +
                ", targetSpaceToolId='" + targetSpaceToolId + '\'' +
                ", targetSpaceId='" + targetSpaceId + '\'' +
                ", targetSymbol='" + targetSymbol + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", url='" + url + '\'' +
                '}';
    }

    public String getUrl() {
        return url;
    }
}
