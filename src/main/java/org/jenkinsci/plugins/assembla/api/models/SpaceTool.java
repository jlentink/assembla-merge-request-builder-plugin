package org.jenkinsci.plugins.assembla.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by pavel on 16/2/16.
 */
public class SpaceTool {
    private String id;
    @SerializedName("space_id")
    private String spaceId;
    private boolean active;
    private String url;
    private int number;
    @SerializedName("tool_id")
    private int toolId;
    private String type;
    @SerializedName("parent_id")
    private String parentId;
    @SerializedName("menu_name")
    private String menuName;
    private String name;

    private Date createdAt;

    public int getPublicPermissions() {
        return publicPermissions;
    }

    public int getTeamPermissions() {
        return teamPermissions;
    }

    public int getWatcherPermissions() {
        return watcherPermissions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getMenuName() {
        return menuName;
    }

    public String getParentId() {
        return parentId;
    }

    public String getType() {
        return type;
    }

    public int getToolId() {
        return toolId;
    }

    public int getNumber() {
        return number;
    }

    public String getUrl() {
        return url;
    }

    public boolean isActive() {
        return active;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getId() {
        return id;
    }

    private int watcherPermissions;
    private int teamPermissions;
    private int publicPermissions;
}
