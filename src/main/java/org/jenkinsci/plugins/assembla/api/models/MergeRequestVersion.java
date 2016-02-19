package org.jenkinsci.plugins.assembla.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by pavel on 18/2/16.
 */
public class MergeRequestVersion {
    private int id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("merge_request_id")
    private int mergeRequestId;
    @SerializedName("processed_by_user_id")
    private String processedByUserId;
    @SerializedName("source_revision")
    private String sourceRevision;
    @SerializedName("source_symbol")
    private String sourceSymbol;
    @SerializedName("source_symbol_type")
    private String sourceSymbolType;
    @SerializedName("target_revision")
    private String targetRevision;
    @SerializedName("updated_at")
    private Date updatedAt;
    private String url;
    private int version;
    private String latest;

    public int getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProcessedByUserId() {
        return processedByUserId;
    }

    public String getSourceRevision() {
        return sourceRevision;
    }

    public String getSourceSymbol() {
        return sourceSymbol;
    }

    public String getSourceSymbolType() {
        return sourceSymbolType;
    }

    public String getTargetRevision() {
        return targetRevision;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getUrl() {
        return url;
    }

    public int getVersion() {
        return version;
    }

    public int getMergeRequestId() {
        return mergeRequestId;
    }

    public boolean isLatest() {
        return latest.equals("true");
    }
}
