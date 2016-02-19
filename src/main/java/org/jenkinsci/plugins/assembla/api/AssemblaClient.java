package org.jenkinsci.plugins.assembla.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.reflect.TypeToken;
import com.sun.scenario.effect.Merge;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jenkinsci.plugins.assembla.api.models.*;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaClient {
    private static final String API_ENDPOINT = "https://api.assembla.com/v1/";
    private static final String ASSEMBLA_URL = "https://assembla.com/";
    private static final Logger LOGGER = Logger.getLogger(AssemblaClient.class.getName());
    private String apiKey;
    private String apiSecret;

    private Gson gson;

    public AssemblaClient(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.gson = new GsonBuilder().create();
    }

    public String getMergeRequestWebUrl(MergeRequest mr) {
        String url = "";
        try {
            url = new URL(
                new URL(ASSEMBLA_URL),
                String.format("/spaces/%s/%s/merge_requests/%s", mr.getTargetSpaceId(), mr.getSpaceToolId(), mr.getId())
            ).toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid URL", e);
        }
        return url;
    }

    public MergeRequest getMergeRequest(String spaceName, String toolId, int id) {
        String requestPath = "spaces/" + spaceName + "/space_tools/" + toolId + "/merge_requests/" + String.valueOf(id);
        return gson.fromJson(apiRequest(requestPath, Method.GET), MergeRequest.class);
    }

    public SpaceTool getTool(String spaceName, String id) {
        String requestPath = "spaces/" + spaceName + "/space_tools/" + id;
        return gson.fromJson(apiRequest(requestPath, Method.GET), SpaceTool.class);
    }

    public SpaceTool getRepoByUrl(String spaceName, String url) {
        for (SpaceTool tool : getRepos(spaceName)) {
            if (tool.getUrl().equals(url)) {
                return tool;
            }
        }
        return null;
    }

    public List<SpaceTool> getRepos(String spaceName) {
        String requestPath = "spaces/" + spaceName + "/space_tools/repo";
        Type listType = new TypeToken<ArrayList<SpaceTool>>() {
        }.getType();
        return gson.fromJson(apiRequest(requestPath, Method.GET), listType);
    }

    public List<MergeRequestVersion> getMergeRequestVersions(MergeRequest mr) {
        String requestPath = String.format(
                "spaces/%s/space_tools/%s/merge_requests/%s/versions",
                mr.getTargetSpaceId(),
                mr.getSpaceToolId(),
                String.valueOf(mr.getId())
        );
        Type listType = new TypeToken<ArrayList<MergeRequestVersion>>() {
        }.getType();
        return gson.fromJson(apiRequest(requestPath, Method.GET), listType);
    }

    public List<Ticket> getMergeRequestTickets(MergeRequest mr) {
        String requestPath = String.format(
                "spaces/%s/space_tools/%s/merge_requests/%s/tickets",
                mr.getTargetSpaceId(),
                mr.getSpaceToolId(),
                String.valueOf(mr.getId())
        );
        Type listType = new TypeToken<ArrayList<Ticket>>() {
        }.getType();

        String response = apiRequest(requestPath, Method.GET);

        List<Ticket> tickets = gson.fromJson(response, listType);

        if (tickets == null) {
            tickets = new ArrayList<>();
        }

        return tickets;
    }

    public void createTicketComment(Ticket ticket, String commentText) {
        TicketCommentWrapper comment = new TicketCommentWrapper(new TicketComment(commentText));

        String requestPath = String.format(
                "spaces/%s/tickets/%s/ticket_comments",
                ticket.getSpaceId(),
                ticket.getNumber()
        );
        apiRequest(requestPath, Method.POST, gson.toJson(comment));
    }

    public MergeRequestVersion getLatestVersion(MergeRequest mr) {
        for (MergeRequestVersion mrVersion : this.getMergeRequestVersions(mr)) {
            if (mrVersion.isLatest()) {
                return mrVersion;
            }
        }

        return null;
    }

    public void upVoteMergeRequest(MergeRequest mr, MergeRequestVersion version) {
        String requestPath = String.format(
                "spaces/%s/space_tools/%s/merge_requests/%s/versions/%s/votes/upvote",
                mr.getTargetSpaceId(),
                mr.getSpaceToolId(),
                String.valueOf(version.getMergeRequestId()),
                version.getVersion()
        );
        apiRequest(requestPath, Method.POST);
    }

    public void downVoteMergeRequest(MergeRequest mr, MergeRequestVersion version) {
        String requestPath = String.format(
            "spaces/%s/space_tools/%s/merge_requests/%s/versions/%s/votes/downvote",
            mr.getTargetSpaceId(),
            mr.getSpaceToolId(),
            String.valueOf(version.getMergeRequestId()),
            version.getVersion()
        );
        apiRequest(requestPath, Method.POST);
    }

    public void commentMergeRequest(MergeRequest mr, MergeRequestVersion version, String commentText) {
        MergeRequestComment comment = new MergeRequestComment(commentText);
        String requestPath = String.format(
                "spaces/%s/space_tools/%s/merge_requests/%s/versions/%s/comments",
                mr.getTargetSpaceId(),
                mr.getSpaceToolId(),
                String.valueOf(version.getMergeRequestId()),
                version.getVersion()
        );
        apiRequest(requestPath, Method.POST, gson.toJson(comment));
    }

    private String apiRequest(String path, Method requestMethod, Object body) {
        String url = getRequestUrl(path);
        HttpClient client = new HttpClient();
        HttpMethodBase method;
        String responseBody = "";

        if (requestMethod == Method.GET) {
            method = new GetMethod(url);
        } else {
            PostMethod postMethod = new PostMethod(url);
            if (body != null) {
                try {
                    StringRequestEntity requestEntity = new StringRequestEntity(
                        body.toString(),
                        "application/json",
                        "UTF-8"
                    );
                    LOGGER.info("Sending payload: " + requestEntity.getContent());
                    postMethod.setRequestEntity(requestEntity);
                } catch (UnsupportedEncodingException e) {
                    LOGGER.severe("Failed to set request body");
                    LOGGER.severe(e.getMessage());
                }
            }
            method = postMethod;
        }

        method.setRequestHeader("Content-type", "application/json");
        method.setRequestHeader("X-Api-Key", apiKey);
        method.setRequestHeader("X-Api-Secret", apiSecret);

        try {
            LOGGER.info("Starting " + method.getName() + " " + url + " request to Assembla API");

            int statusCode = client.executeMethod(method);

            if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_NO_CONTENT)) {
                LOGGER.severe("Request for " + url + " failed, server returned: " + method.getStatusLine());
            }

            byte[] responseBuffer = method.getResponseBody();

            if (responseBuffer != null) {
                responseBody = new String(responseBuffer);
                LOGGER.info("Assembla API response: " + responseBody);
            }

        } catch (IOException e) {
            LOGGER.severe("Net for " + url + " failed, server returned: " + method.getStatusLine());
        } finally {
            method.releaseConnection();
        }

        return responseBody;
    }

    private String apiRequest(String path, Method requestMethod) {
        return apiRequest(path, requestMethod, null);
    }

    private String getRequestUrl(String path) {
        String url = null;
        try {
            url = new URL(new URL(API_ENDPOINT), path).toString();
        } catch (MalformedURLException e) {
            LOGGER.severe("Invalid URL: " + e.toString());
        }

        return url;
    }
}
