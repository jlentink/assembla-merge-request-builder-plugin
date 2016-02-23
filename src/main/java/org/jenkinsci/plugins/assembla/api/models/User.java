package org.jenkinsci.plugins.assembla.api.models;

/**
 * Created by pavel on 23/2/16.
 */
public class User {
    private String id;
    private String login;
    private String email;
    private String name;

    public String getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
