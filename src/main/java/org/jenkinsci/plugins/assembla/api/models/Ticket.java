package org.jenkinsci.plugins.assembla.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by pavel on 19/2/16.
 */
public class Ticket {
    private String id;
    private int number;
    private String summary;
    private String description;
    @SerializedName("space_id")
    private String spaceId;
    private List<String> tags;

    public String getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public List<String> getTags() {
        return tags;
    }
}
