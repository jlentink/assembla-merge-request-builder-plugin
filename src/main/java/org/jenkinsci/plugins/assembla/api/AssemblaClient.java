package org.jenkinsci.plugins.assembla.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jenkinsci.plugins.assembla.api.models.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        this.apiEndpoint = getApiEndpoint(assemblaHost);
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

    public AssemblaClient setConfig(String apiKey, String apiSecret, String assemblaHost, boolean ignoreSSLErrors) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.assemblaHost = assemblaHost;
        this.ignoreSSLErrors = ignoreSSLErrors;
        this.apiEndpoint = getApiEndpoint(assemblaHost);
        return this;
    }

    private CloseableHttpClient getClient() {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        // We will create a custom SSL context which will accept everything. Otherwise we'll use the default one.
        if (ignoreSSLErrors) {
            try {
                SSLContextBuilder builder = SSLContexts.custom();
                builder.loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        return true;
                    }
                });

                SSLContext sslContext = builder.build();

                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        sslContext, new X509HostnameVerifier() {
                    @Override
                    public void verify(String host, SSLSocket ssl)
                            throws IOException {
                    }

                    @Override
                    public void verify(String host, X509Certificate cert)
                            throws SSLException {
                    }

                    @Override
                    public void verify(String host, String[] cns,
                                       String[] subjectAlts) throws SSLException {
                    }

                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });

                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                        .<ConnectionSocketFactory> create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslsf)
                        .build();

                PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                clientBuilder.setConnectionManager(cm);

            } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize HttpClient", e);
            }
        }


        return clientBuilder.build();
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
                LOGGER.info("Sending payload: " + body.toString());
                postMethod.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));
            }
            method = postMethod;
        }

        method.setHeader("Content-type", "application/json");
        method.setHeader("X-Api-Key", apiKey);
        method.setHeader("X-Api-Secret", apiSecret);

        try (CloseableHttpClient client = getClient()) {
            LOGGER.info("Starting " + method.getMethod() + " " + url + " request to Assembla API");


            HttpResponse response = client.execute(method);
            int statusCode = response.getStatusLine().getStatusCode();

            if (!(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_NO_CONTENT)) {
                LOGGER.severe("Request for " + url + " failed, server returned: " + response.getStatusLine());
            }

            HttpEntity httpEntity = response.getEntity();

            if (httpEntity != null) {
                responseBody = IOUtils.toString(httpEntity.getContent());
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

    private String getApiEndpoint(String assemblaHost) {
        String endpoint;
        if (assemblaHost == null || DEFAULT_ASSEMBLA_URL.equals(assemblaHost)) {
            endpoint = DEFAULT_API_ENDPOINT;
        } else {
            endpoint = assemblaHost;
        }
        return mergeUrl(endpoint, "/v1/");
    }

    private String mergeUrl(String baseUrl, String relativeUrl) {
        try {
            return new URL(new URL(baseUrl), relativeUrl).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AssemblaApiException extends RuntimeException {}

    public static class NotFoundError extends AssemblaApiException {
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

    public static class UnauthorizedError extends AssemblaApiException {}

    public static class ForbiddenError extends AssemblaApiException {}
}
