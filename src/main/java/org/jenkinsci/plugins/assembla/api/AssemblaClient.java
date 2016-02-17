package org.jenkinsci.plugins.assembla.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jenkinsci.plugins.assembla.api.models.MergeRequest;
import org.jenkinsci.plugins.assembla.api.models.SpaceTool;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaClient {
    private static final String API_ENDPOINT = "https://api.assembla.com/v1/";
    private static final Logger LOGGER = Logger.getLogger(AssemblaClient.class.getName());
    private String apiKey;
    private String apiSecret;

    private Gson gson;

    public AssemblaClient(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.gson = new GsonBuilder().create();
    }

    public MergeRequest findMergeRequest(String spaceName, String toolId, int id) {
        String requestPath = "spaces/" + spaceName + "/space_tools/" + toolId + "/merge_requests/" + String.valueOf(id);
        return gson.fromJson(apiRequest(requestPath, Method.GET), MergeRequest.class);
    }

    public SpaceTool findSpaceTool(String spaceName, String id) {
        String requestPath = "spaces/" + spaceName + "/space_tools/" + id;
        return gson.fromJson(apiRequest(requestPath, Method.GET), SpaceTool.class);
    }

    public SpaceTool findSpaceToolByName(String spaceName, String name) {
        for (SpaceTool tool : findSpaceTools(spaceName)) {
            if (tool.getName().equals(name)) {
                return tool;
            }
        }
        return null;
    }

    public List<SpaceTool> findSpaceTools(String spaceName) {
        String requestPath = "spaces/" + spaceName + "/space_tools/";
        Type listType = new TypeToken<ArrayList<SpaceTool>>() {
        }.getType();
        return gson.fromJson(apiRequest(requestPath, Method.GET), listType);
    }

    private String apiRequest(String path, Method requestMethod) {
        String url = getRequestUrl(path);
        HttpClient client = new HttpClient();
        HttpMethodBase method;
        byte[] responseBody = new byte[0];

        if (requestMethod == Method.GET) {
            method = new GetMethod(url);
        } else {
            method = new PostMethod(url);
        }

        method.setRequestHeader("X-Api-Key", apiKey);
        method.setRequestHeader("X-Api-Secret", apiSecret);

        try {
            LOGGER.info("Starting request to Assembla API...");

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.severe("Request for " + url + " failed, server returned: " + method.getStatusLine());
            }
            responseBody = method.getResponseBody();

        } catch (IOException e) {
            LOGGER.severe("Net for " + url + " failed, server returned: " + method.getStatusLine());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return new String(responseBody);
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
