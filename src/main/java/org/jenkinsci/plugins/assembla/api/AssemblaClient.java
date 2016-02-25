package org.jenkinsci.plugins.assembla.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jenkinsci.plugins.assembla.api.models.*;

import javax.net.ssl.X509TrustManager;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaClient {
    private static final Logger LOGGER = Logger.getLogger(AssemblaClient.class.getName());
    private static final String DEFAULT_API_ENDPOINT = "https://api.assembla.com/";
    private static final String DEFAULT_ASSEMBLA_URL = "https://www.assembla.com/";
    private String assemblaHost;
    private String apiKey;
    private String apiSecret;
    private String apiEndpoint;
    private boolean ignoreSSLErrors;

    private Gson gson;

    public AssemblaClient(String apiKey, String apiSecret, String assemblaHost, boolean ignoreSSLErrors) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.assemblaHost = assemblaHost;
        this.gson = new GsonBuilder().create();
        this.ignoreSSLErrors = ignoreSSLErrors;
        this.apiEndpoint = getApiEndpoint();
    }

    public User getUser() {
        return gson.fromJson(apiRequest("user", Method.GET), User.class);
    }

    public Space getSpace(String spaceName) {
        String requestPath = String.format("spaces/%s", spaceName);

        return gson.fromJson(apiRequest(requestPath, Method.GET), Space.class);
    }

    public SpaceTool getTool(String spaceName, String id) {
        String requestPath = String.format("spaces/%s/space_tools/%s", spaceName, id);

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

    public MergeRequest getMergeRequest(String spaceName, String toolId, int id) {
        String requestPath = "spaces/" + spaceName + "/space_tools/" + toolId + "/merge_requests/" + String.valueOf(id);
        return gson.fromJson(apiRequest(requestPath, Method.GET), MergeRequest.class);
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

    public String getMergeRequestWebUrl(MergeRequest mr) {
        String url = "";
        try {
            url = new URL(
                    new URL(assemblaHost),
                    String.format("/spaces/%s/%s/merge_requests/%s", mr.getTargetSpaceId(), mr.getSpaceToolId(), mr.getId())
            ).toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Invalid URL", e);
        }
        return url;
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

    public HttpClient getClient() {
        if (ignoreSSLErrors) {
            try {

                SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                }, new AllowAllHostnameVerifier());

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", 443, sf));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(registry);
                return new DefaultHttpClient(ccm);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize HttpClient", e);
                return new DefaultHttpClient();
            }
        }

        return new DefaultHttpClient();
    }

    private String apiRequest(String path, Method requestMethod, Object body) {
        String url = getRequestUrl(path);
        HttpUriRequest method;
        String responseBody = "";

        if (requestMethod == Method.GET) {
            method = new HttpGet(url);
        } else {
            HttpPost postMethod = new HttpPost(url);
            if (body != null) {
                try {
                    LOGGER.info("Sending payload: " + body.toString());
                    postMethod.setEntity(new StringEntity(body.toString(), "application/json", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.severe("Failed to set request body");
                    LOGGER.severe(e.getMessage());
                }
            }
            method = postMethod;
        }

        method.setHeader("Content-type", "application/json");
        method.setHeader("X-Api-Key", apiKey);
        method.setHeader("X-Api-Secret", apiSecret);

        try {
            LOGGER.info("Starting " + method.getMethod() + " " + url + " request to Assembla API");

            HttpResponse response = getClient().execute(method);
            int statusCode = response.getStatusLine().getStatusCode();

            if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_NO_CONTENT)) {
                LOGGER.severe("Request for " + url + " failed, server returned: " + response.getStatusLine());
            }

            responseBody = IOUtils.toString(response.getEntity().getContent());

            if (responseBody != null) {
                LOGGER.info("Assembla API response: " + responseBody);
            }

            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new NotFoundError(url, responseBody);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedError();
            } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
                throw new ForbiddenError();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Network failure", e);
        }

        return responseBody;
    }

    private String apiRequest(String path, Method requestMethod) {
        return apiRequest(path, requestMethod, null);
    }

    private String getRequestUrl(String path) {
        return mergeUrl(apiEndpoint, path);
    }

    private String getApiEndpoint() {
        String endpoint;
        if (assemblaHost == null || assemblaHost.contains(DEFAULT_ASSEMBLA_URL)) {
            endpoint = DEFAULT_API_ENDPOINT;
        } else {
            endpoint = assemblaHost;
        }
        return mergeUrl(endpoint, "/v1/");
    }

    public static class NotFoundError extends RuntimeException {
        private String requestUrl;
        private String response;

        public NotFoundError(String requestUrl, String response) {
            this.requestUrl = requestUrl;
            this.response = response;
        }

        @Override
        public String toString() {
            return "NotFoundError{" +
                    "requestUrl='" + requestUrl + '\'' +
                    ", response='" + response + '\'' +
                    '}';
        }
    }

    private String mergeUrl(String baseUrl, String relativeUrl) {
        try {
            return new URL(new URL(baseUrl), relativeUrl).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class UnauthorizedError extends RuntimeException {}

    public static class ForbiddenError extends RuntimeException {}

    private static class AllowEverythingTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
