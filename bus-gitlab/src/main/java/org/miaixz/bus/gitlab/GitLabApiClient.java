/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.net.ssl.*;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.miaixz.bus.gitlab.GitLabApi.ApiVersion;
import org.miaixz.bus.gitlab.models.Constants.TokenType;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.MaskingLoggingFilter;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;
import org.miaixz.bus.logger.Logger;

/**
 * This class utilizes the Jersey client package to communicate with a GitLab API endpoint.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GitLabApiClient implements AutoCloseable {

    protected static final String PRIVATE_TOKEN_HEADER = "PRIVATE-TOKEN";
    protected static final String JOB_TOKEN_HEADER = "JOB-TOKEN";
    protected static final String SUDO_HEADER = "Sudo";
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String X_GITLAB_TOKEN_HEADER = "X-Gitlab-Token";

    private ClientConfig clientConfig;
    private Client apiClient;
    private String baseUrl;
    private String hostUrl;
    private TokenType tokenType = TokenType.PRIVATE;
    private Supplier<String> authToken;
    private String secretToken;
    private boolean ignoreCertificateErrors;
    private SSLContext openSslContext;
    private HostnameVerifier openHostnameVerifier;
    private Long sudoAsId;
    private Integer connectTimeout;
    private Integer readTimeout;
    private String userAgentHeader;

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version, server URL,
     * private token, and secret token.
     *
     * @param apiVersion   the ApiVersion specifying which version of the API to use
     * @param hostUrl      the URL to the GitLab API server
     * @param privateToken the private token to authenticate with
     */
    public GitLabApiClient(ApiVersion apiVersion, String hostUrl, String privateToken) {
        this(apiVersion, hostUrl, TokenType.PRIVATE, privateToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version, server URL,
     * auth token type, private or access token, and secret token.
     *
     * @param apiVersion the ApiVersion specifying which version of the API to use
     * @param hostUrl    the URL to the GitLab API server
     * @param tokenType  the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken  the token to authenticate with
     */
    public GitLabApiClient(ApiVersion apiVersion, String hostUrl, TokenType tokenType, String authToken) {
        this(apiVersion, hostUrl, tokenType, authToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using GitLab API version 4, and the specified
     * server URL, private token, and secret token.
     *
     * @param hostUrl      the URL to the GitLab API server
     * @param privateToken the private token to authenticate with
     */
    public GitLabApiClient(String hostUrl, String privateToken) {
        this(ApiVersion.V4, hostUrl, TokenType.PRIVATE, privateToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using GitLab API version 4, and the specified
     * server URL, private token, and secret token.
     *
     * @param hostUrl   the URL to the GitLab API server
     * @param tokenType the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken the token to authenticate with
     */
    public GitLabApiClient(String hostUrl, TokenType tokenType, String authToken) {
        this(ApiVersion.V4, hostUrl, tokenType, authToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version, server URL,
     * private token, and secret token.
     *
     * @param apiVersion   the ApiVersion specifying which version of the API to use
     * @param hostUrl      the URL to the GitLab API server
     * @param privateToken the private token to authenticate with
     * @param secretToken  use this token to validate received payloads
     */
    public GitLabApiClient(ApiVersion apiVersion, String hostUrl, String privateToken, String secretToken) {
        this(apiVersion, hostUrl, TokenType.PRIVATE, privateToken, secretToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version, server URL,
     * private token, and secret token.
     *
     * @param apiVersion  the ApiVersion specifying which version of the API to use
     * @param hostUrl     the URL to the GitLab API server
     * @param tokenType   the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken   the token to authenticate with
     * @param secretToken use this token to validate received payloads
     */
    public GitLabApiClient(ApiVersion apiVersion, String hostUrl, TokenType tokenType, String authToken,
            String secretToken) {
        this(apiVersion, hostUrl, tokenType, authToken, secretToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using GitLab API version 4, and the specified
     * server URL, private token, and secret token.
     *
     * @param hostUrl      the URL to the GitLab API server
     * @param privateToken the private token to authenticate with
     * @param secretToken  use this token to validate received payloads
     */
    public GitLabApiClient(String hostUrl, String privateToken, String secretToken) {
        this(ApiVersion.V4, hostUrl, TokenType.PRIVATE, privateToken, secretToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using GitLab API version 4, and the specified
     * server URL, private token, and secret token.
     *
     * @param hostUrl     the URL to the GitLab API server
     * @param tokenType   the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken   the token to authenticate with
     * @param secretToken use this token to validate received payloads
     */
    public GitLabApiClient(String hostUrl, TokenType tokenType, String authToken, String secretToken) {
        this(ApiVersion.V4, hostUrl, tokenType, authToken, secretToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using GitLab API version 4, and the specified
     * server URL and private token.
     *
     * @param hostUrl                the URL to the GitLab API server
     * @param privateToken           the private token to authenticate with
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties the properties given to Jersey's clientconfig
     */
    public GitLabApiClient(String hostUrl, String privateToken, String secretToken,
            Map<String, Object> clientConfigProperties) {
        this(ApiVersion.V4, hostUrl, TokenType.PRIVATE, privateToken, secretToken, clientConfigProperties);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version, server URL
     * and private token.
     *
     * @param apiVersion             the ApiVersion specifying which version of the API to use
     * @param hostUrl                the URL to the GitLab API server
     * @param privateToken           the private token to authenticate with
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties the properties given to Jersey's clientconfig
     */
    public GitLabApiClient(ApiVersion apiVersion, String hostUrl, String privateToken, String secretToken,
            Map<String, Object> clientConfigProperties) {
        this(apiVersion, hostUrl, TokenType.PRIVATE, privateToken, secretToken, clientConfigProperties);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version, server URL
     * and private token.
     *
     * @param apiVersion             the ApiVersion specifying which version of the API to use
     * @param hostUrl                the URL to the GitLab API server
     * @param tokenType              the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken              the private token to authenticate with
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties the properties given to Jersey's clientconfig
     */
    public GitLabApiClient(ApiVersion apiVersion, String hostUrl, TokenType tokenType, String authToken,
            String secretToken, Map<String, Object> clientConfigProperties) {

        // Remove the trailing "/" from the hostUrl if present
        this.hostUrl = (hostUrl.endsWith("/") ? hostUrl.replaceAll("/$", "") : hostUrl);
        this.baseUrl = this.hostUrl;
        this.hostUrl += apiVersion.getApiNamespace();

        this.tokenType = tokenType;
        this.authToken = () -> authToken;

        if (secretToken != null) {
            secretToken = secretToken.trim();
            secretToken = (secretToken.length() > 0 ? secretToken : null);
        }

        this.secretToken = secretToken;

        // Disable auto-discovery of feature and services lookup, this will force Jersey
        // to use the features and services explicitly configured by gitlab4j
        clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        clientConfig.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
        clientConfig.property(ClientProperties.FOLLOW_REDIRECTS, true);

        if (clientConfigProperties != null) {
            for (Map.Entry<String, Object> propertyEntry : clientConfigProperties.entrySet()) {
                clientConfig.property(propertyEntry.getKey(), propertyEntry.getValue());
            }
        }

        clientConfig.register(JacksonJson.class);
        clientConfig.register(MultiPartFeature.class);
        Logger.info(
                false,
                "GitLab",
                "GitLab API client initialized: apiVersion={}, hostUrl={}, tokenType={}, secretTokenPresent={}, customPropertyCount={}",
                apiVersion,
                this.hostUrl,
                tokenType,
                this.secretToken != null,
                clientConfigProperties == null ? 0 : clientConfigProperties.size());
    }

    /**
     * Close the underlying {@link Client} and its associated resources.
     */
    @Override
    public void close() {
        if (apiClient != null) {
            Logger.info(true, "GitLab", "GitLab Jersey client close started: hostUrl={}", hostUrl);
            apiClient.close();
            Logger.info(false, "GitLab", "GitLab Jersey client close completed: hostUrl={}", hostUrl);
        }
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API.
     *
     * @param logger            the Logger instance to log to
     * @param level             the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntityLength   maximum number of entity bytes to be logged. When logging if the maxEntitySize is
     *                          reached, the entity logging will be truncated at maxEntitySize and "...more..." will be
     *                          added at the end of the log entry. If maxEntitySize is <= 0, entity logging will be
     *                          disabled
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    void enableRequestResponseLogging(
            java.util.logging.Logger logger,
            Level level,
            int maxEntityLength,
            List<String> maskedHeaderNames) {

        Logger.info(
                true,
                "GitLab",
                "GitLab request response logging enabled: level={}, maxEntityLength={}, maskedHeaderCount={}",
                level,
                maxEntityLength,
                maskedHeaderNames == null ? 0 : maskedHeaderNames.size());
        MaskingLoggingFilter loggingFilter = new MaskingLoggingFilter(logger, level, maxEntityLength,
                maskedHeaderNames);
        clientConfig.register(loggingFilter);

        // Recreate the Client instance if already created.
        if (apiClient != null) {
            createApiClient();
        }
        Logger.info(
                false,
                "GitLab",
                "GitLab request response logging configured: level={}, maxEntityLength={}",
                level,
                maxEntityLength);
    }

    /**
     * Sets the per request connect and read timeout.
     *
     * @param connectTimeout the per request connect timeout in milliseconds, can be null to use default
     * @param readTimeout    the per request read timeout in milliseconds, can be null to use default
     */
    void setRequestTimeout(Integer connectTimeout, Integer readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
        clientConfig.property(ClientProperties.READ_TIMEOUT, readTimeout);

        // Recreate the Client instance if already created.
        if (apiClient != null) {
            createApiClient();
        }
        Logger.info(
                false,
                "GitLab",
                "GitLab request timeout configured: connectTimeoutMs={}, readTimeoutMs={}",
                connectTimeout,
                readTimeout);
    }

    /**
     * Get the auth token being used by this client.
     *
     * @return the auth token being used by this client
     */
    String getAuthToken() {
        return (authToken.get());
    }

    /**
     * Get the secret token.
     *
     * @return the secret token
     */
    String getSecretToken() {
        return (secretToken);
    }

    /**
     * Get the TokenType this client is using.
     *
     * @return the TokenType this client is using
     */
    TokenType getTokenType() {
        return (tokenType);
    }

    /**
     * Set the ID of the user to sudo as.
     */
    Long getSudoAsId() {
        return (sudoAsId);
    }

    /**
     * Set the ID of the user to sudo as.
     *
     * @param sudoAsId the ID of the user to sudo as
     */
    void setSudoAsId(Long sudoAsId) {
        this.sudoAsId = sudoAsId;
        Logger.info(false, "GitLab", "GitLab sudo user configured: sudoAsId={}", sudoAsId);
    }

    /**
     * Construct a REST URL with the specified path arguments.
     *
     * @param pathArgs variable list of arguments used to build the URI
     * @return a REST URL with the specified path arguments
     * @throws IOException if an error occurs while constructing the URL
     */
    protected URL getApiUrl(Object... pathArgs) throws IOException {
        String url = appendPathArgs(this.hostUrl, pathArgs);
        return (new URL(url));
    }

    /**
     * Construct a REST URL with the specified path arguments using Gitlab base url.
     *
     * @param pathArgs variable list of arguments used to build the URI
     * @return a REST URL with the specified path arguments
     * @throws IOException if an error occurs while constructing the URL
     */
    protected URL getUrlWithBase(Object... pathArgs) throws IOException {
        String url = appendPathArgs(this.baseUrl, pathArgs);
        return (new URL(url));
    }

    private String appendPathArgs(String url, Object... pathArgs) {
        StringBuilder urlBuilder = new StringBuilder(url);
        for (Object pathArg : pathArgs) {
            if (pathArg != null) {
                urlBuilder.append("/");
                urlBuilder.append(pathArg.toString());
            }
        }
        return urlBuilder.toString();
    }

    /**
     * Validates the secret token (X-GitLab-Token) header against the expected secret token, returns true if valid,
     * otherwise returns false.
     *
     * @param response the Response instance sent from the GitLab server
     * @return true if the response's secret token is valid, otherwise returns false
     */
    protected boolean validateSecretToken(Response response) {

        if (this.secretToken == null)
            return (true);

        String secretToken = response.getHeaderString(X_GITLAB_TOKEN_HEADER);
        if (secretToken == null) {
            Logger.warn(
                    false,
                    "GitLab",
                    "GitLab response secret token validation failed: reason=missingHeader, status={}",
                    response.getStatus());
            return (false);
        }

        boolean valid = this.secretToken.equals(secretToken);
        Logger.debug(
                false,
                "GitLab",
                "GitLab response secret token validation completed: status={}, valid={}",
                response.getStatus(),
                valid);
        return valid;
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and path objects, returning a ClientResponse
     * instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response get(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (get(queryParams, url));
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and URL, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url         the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response get(MultivaluedMap<String, String> queryParams, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=GET, urlPath={}, queryParamCount={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size());
        Response response = invocation(url, queryParams).get();
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=GET, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and path objects, returning a ClientResponse
     * instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param accepts     if non-empty will set the Accepts header to this value
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response getWithAccepts(MultivaluedMap<String, String> queryParams, String accepts, Object... pathArgs)
            throws IOException {
        URL url = getApiUrl(pathArgs);
        return (getWithAccepts(queryParams, url, accepts));
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and URL, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url         the fully formed path to the GitLab API endpoint
     * @param accepts     if non-empty will set the Accepts header to this value
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response getWithAccepts(MultivaluedMap<String, String> queryParams, URL url, String accepts) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=GET, urlPath={}, queryParamCount={}, accept={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size(),
                accepts);
        Response response = invocation(url, queryParams, accepts).get();
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=GET, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP HEAD call with the specified query parameters and path objects, returning a ClientResponse
     * instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response head(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (head(queryParams, url));
    }

    /**
     * Perform an HTTP HEAD call with the specified query parameters and URL, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url         the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response head(MultivaluedMap<String, String> queryParams, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=HEAD, urlPath={}, queryParamCount={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size());
        Response response = invocation(url, queryParams).head();
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=HEAD, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP PATCH call with the specified query parameters and path objects, returning a ClientResponse
     * instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response patch(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (patch(queryParams, url));
    }

    /**
     * Perform an HTTP PATCH call with the specified query parameters and URL, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url         the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response patch(MultivaluedMap<String, String> queryParams, URL url) {
        Entity<?> empty = Entity.text("");
        // use "X-HTTP-Method-Override" header on POST to override to unsupported PATCH
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=PATCH, urlPath={}, queryParamCount={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size());
        Response response = invocation(url, queryParams).header("X-HTTP-Method-Override", "PATCH").post(empty);
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=PATCH, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP POST call with the specified form data and path objects, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(Form formData, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return post(formData, url);
    }

    /**
     * Perform an HTTP POST call with the specified form data and path objects, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a Response instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return post(queryParams, url);
    }

    /**
     * Perform an HTTP POST call with the specified form data and URL, returning a ClientResponse instance with the data
     * returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param url      the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response post(Form formData, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=POST, urlPath={}, formParamCount={}, formType={}",
                url == null ? null : url.getPath(),
                formData == null ? 0 : formData.asMap().size(),
                formData == null ? "null" : formData.getClass().getSimpleName());
        Response response;
        if (formData instanceof GitLabApiForm) {
            response = invocation(url, null)
                    .post(Entity.entity(formData.asMap(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        } else if (formData != null) {
            response = invocation(url, null).post(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        } else {
            response = invocation(url, null)
                    .post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        }
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=POST, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP POST call with the specified form data and URL, returning a ClientResponse instance with the data
     * returned from the endpoint.
     *
     * @param queryParams multivalue map of request parametersformData the Form containing the name/value pairs
     * @param url         the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response post(MultivaluedMap<String, String> queryParams, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=POST, urlPath={}, queryParamCount={}, payloadType={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size(),
                "emptyForm");
        Response response = invocation(url, queryParams)
                .post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=POST, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP POST call with the specified payload object and URL, returning a ClientResponse instance with the
     * data returned from the endpoint.
     *
     * @param payload  the object instance that will be serialized to JSON and used as the POST data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(Object payload, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        Entity<?> entity = Entity.entity(payload, MediaType.APPLICATION_JSON);
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=POST, urlPath={}, payloadType={}, pathArgCount={}",
                url == null ? null : url.getPath(),
                payload == null ? "null" : payload.getClass().getSimpleName(),
                pathArgs == null ? 0 : pathArgs.length);
        Response response = invocation(url, null).post(entity);
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=POST, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP POST call with the specified StreamingOutput, MediaType, and path objects, returning a
     * ClientResponse instance with the data returned from the endpoint.
     *
     * @param stream    the StreamingOutput instance that contains the POST data
     * @param mediaType the content-type of the POST data
     * @param pathArgs  variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(StreamingOutput stream, String mediaType, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=POST, urlPath={}, payloadType={}, mediaType={}, pathArgCount={}",
                url == null ? null : url.getPath(),
                stream == null ? "null" : stream.getClass().getSimpleName(),
                mediaType,
                pathArgs == null ? 0 : pathArgs.length);
        Response response = invocation(url, null).post(Entity.entity(stream, mediaType));
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=POST, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform a file upload using the specified media type, returning a ClientResponse instance with the data returned
     * from the endpoint.
     *
     * @param name            the name for the form field that contains the file name
     * @param fileToUpload    a File instance pointing to the file to upload
     * @param mediaTypeString unused; will be removed in the next major version
     * @param pathArgs        variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response upload(String name, File fileToUpload, String mediaTypeString, Object... pathArgs)
            throws IOException {
        return upload(name, fileToUpload, mediaTypeString, null, pathArgs);
    }

    /**
     * Perform a file upload using the specified media type, returning a ClientResponse instance with the data returned
     * from the endpoint.
     *
     * @param name            the name for the form field that contains the file name
     * @param fileToUpload    a File instance pointing to the file to upload
     * @param mediaTypeString unused; will be removed in the next major version
     * @param formData        the Form containing the name/value pairs
     * @param pathArgs        variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response upload(String name, File fileToUpload, String mediaTypeString, Form formData, Object... pathArgs)
            throws IOException {
        URL url = getApiUrl(pathArgs);
        return (upload(name, fileToUpload, mediaTypeString, formData, url));
    }

    /**
     * Perform a file upload using multipart/form-data, returning a ClientResponse instance with the data returned from
     * the endpoint.
     *
     * @param name            the name for the form field that contains the file name
     * @param fileToUpload    a File instance pointing to the file to upload
     * @param mediaTypeString unused; will be removed in the next major version
     * @param formData        the Form containing the name/value pairs
     * @param url             the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response upload(String name, File fileToUpload, String mediaTypeString, Form formData, URL url)
            throws IOException {
        Logger.info(
                true,
                "GitLab",
                "GitLab file upload request started: method=POST, urlPath={}, fieldName={}, fileName={}, fileSize={}, formParamCount={}",
                url == null ? null : url.getPath(),
                name,
                fileToUpload == null ? null : fileToUpload.getName(),
                fileToUpload == null ? -1 : fileToUpload.length(),
                formData == null ? 0 : formData.asMap().size());
        FileDataBodyPart filePart = new FileDataBodyPart(name, fileToUpload);
        return upload(filePart, formData, url);
    }

    protected Response upload(
            String name,
            InputStream inputStream,
            String filename,
            String mediaTypeString,
            Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (upload(name, inputStream, filename, mediaTypeString, null, url));
    }

    protected Response upload(
            String name,
            InputStream inputStream,
            String filename,
            String mediaTypeString,
            Form formData,
            URL url) throws IOException {
        Logger.info(
                true,
                "GitLab",
                "GitLab stream upload request started: method=POST, urlPath={}, fieldName={}, fileName={}, mediaType={}, formParamCount={}",
                url == null ? null : url.getPath(),
                name,
                filename,
                mediaTypeString,
                formData == null ? 0 : formData.asMap().size());
        StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart(name, inputStream, filename);
        return upload(streamDataBodyPart, formData, url);
    }

    protected Response upload(BodyPart bodyPart, Form formData, URL url) throws IOException {
        try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
            if (formData != null) {
                formData.asMap().forEach((key, values) -> {
                    if (values != null) {
                        values.forEach(value -> multiPart.field(key, value));
                    }
                });
            }

            multiPart.bodyPart(bodyPart);
            final Entity<?> entity = Entity.entity(multiPart, Boundary.addBoundary(multiPart.getMediaType()));
            Response response = invocation(url, null).post(entity);
            Logger.info(
                    false,
                    "GitLab",
                    "GitLab multipart upload response received: method=POST, urlPath={}, status={}, bodyPartType={}, rateLimitRemaining={}",
                    url == null ? null : url.getPath(),
                    response.getStatus(),
                    bodyPart == null ? "null" : bodyPart.getClass().getSimpleName(),
                    response.getHeaderString("RateLimit-Remaining"));
            return response;
        }
    }

    /**
     * Perform a file upload using multipart/form-data using the HTTP PUT method, returning a ClientResponse instance
     * with the data returned from the endpoint.
     *
     * @param name         the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param pathArgs     variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response putUpload(String name, File fileToUpload, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (putUpload(name, fileToUpload, url));
    }

    /**
     * Perform a file upload using multipart/form-data using the HTTP PUT method, returning a ClientResponse instance
     * with the data returned from the endpoint.
     *
     * @param name         the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param url          the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response putUpload(String name, File fileToUpload, URL url) throws IOException {

        try (MultiPart multiPart = new FormDataMultiPart()) {
            Logger.info(
                    true,
                    "GitLab",
                    "GitLab file upload request started: method=PUT, urlPath={}, fieldName={}, fileName={}, fileSize={}",
                    url == null ? null : url.getPath(),
                    name,
                    fileToUpload == null ? null : fileToUpload.getName(),
                    fileToUpload == null ? -1 : fileToUpload.length());
            if (fileToUpload == null) {
                multiPart.bodyPart(new FormDataBodyPart(name, "", MediaType.APPLICATION_OCTET_STREAM_TYPE));
            } else {
                multiPart.bodyPart(new FileDataBodyPart(name, fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            }
            final Entity<?> entity = Entity.entity(multiPart, Boundary.addBoundary(multiPart.getMediaType()));
            Response response = invocation(url, null).put(entity);
            Logger.info(
                    false,
                    "GitLab",
                    "GitLab multipart upload response received: method=PUT, urlPath={}, status={}, rateLimitRemaining={}",
                    url == null ? null : url.getPath(),
                    response.getStatus(),
                    response.getHeaderString("RateLimit-Remaining"));
            return response;
        }
    }

    /**
     * Perform an HTTP PUT call with the specified form data and path objects, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response put(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (put(queryParams, url));
    }

    /**
     * Perform an HTTP PUT call with the specified form data and URL, returning a ClientResponse instance with the data
     * returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url         the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response put(MultivaluedMap<String, String> queryParams, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=PUT, urlPath={}, formParamCount={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size());
        Response response;
        if (queryParams == null || queryParams.isEmpty()) {
            Entity<?> empty = Entity.text("");
            response = invocation(url, null).put(empty);
        } else {
            response = invocation(url, null)
                    .put(Entity.entity(queryParams, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        }
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=PUT, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP PUT call with the specified form data and path objects, returning a ClientResponse instance with
     * the data returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response put(Form formData, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return put(formData, url);
    }

    /**
     * Perform an HTTP PUT call with the specified form data and URL, returning a ClientResponse instance with the data
     * returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param url      the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response put(Form formData, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=PUT, urlPath={}, formParamCount={}, formType={}",
                url == null ? null : url.getPath(),
                formData == null ? 0 : formData.asMap().size(),
                formData == null ? "null" : formData.getClass().getSimpleName());
        Response response;
        if (formData instanceof GitLabApiForm)
            response = invocation(url, null)
                    .put(Entity.entity(formData.asMap(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        else
            response = invocation(url, null).put(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=PUT, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP PUT call with the specified payload object and URL, returning a ClientResponse instance with the
     * data returned from the endpoint.
     *
     * @param payload  the object instance that will be serialized to JSON and used as the PUT data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response put(Object payload, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        Entity<?> entity = Entity.entity(payload, MediaType.APPLICATION_JSON);
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=PUT, urlPath={}, payloadType={}, pathArgCount={}",
                url == null ? null : url.getPath(),
                payload == null ? "null" : payload.getClass().getSimpleName(),
                pathArgs == null ? 0 : pathArgs.length);
        Response response = invocation(url, null).put(entity);
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=PUT, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    /**
     * Perform an HTTP DELETE call with the specified form data and path objects, returning a Response instance with the
     * data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs    variable list of arguments used to build the URI
     * @return a Response instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response delete(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        return (delete(queryParams, getApiUrl(pathArgs)));
    }

    /**
     * Perform an HTTP DELETE call with the specified form data and URL, returning a Response instance with the data
     * returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url         the fully formed path to the GitLab API endpoint
     * @return a Response instance with the data returned from the endpoint
     */
    protected Response delete(MultivaluedMap<String, String> queryParams, URL url) {
        Logger.debug(
                true,
                "GitLab",
                "GitLab HTTP request started: method=DELETE, urlPath={}, queryParamCount={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size());
        Response response = invocation(url, queryParams).delete();
        Logger.debug(
                false,
                "GitLab",
                "GitLab HTTP response received: method=DELETE, urlPath={}, status={}, rateLimitRemaining={}",
                url == null ? null : url.getPath(),
                response.getStatus(),
                response.getHeaderString("RateLimit-Remaining"));
        return response;
    }

    protected Invocation.Builder invocation(URL url, MultivaluedMap<String, String> queryParams) {
        return (invocation(url, queryParams, MediaType.APPLICATION_JSON));
    }

    protected Client createApiClient() {

        Logger.info(
                true,
                "GitLab",
                "GitLab Jersey client creation started: hostUrl={}, ignoreCertificateErrors={}, connectTimeoutMs={}, readTimeoutMs={}",
                hostUrl,
                ignoreCertificateErrors,
                connectTimeout,
                readTimeout);
        // Explicitly use an instance of the JerseyClientBuilder, this allows this
        // library to work when both Jersey and Resteasy are present
        ClientBuilder clientBuilder = new JerseyClientBuilder().withConfig(clientConfig);

        // Register JacksonJson as the ObjectMapper provider.
        clientBuilder.register(JacksonJson.class);

        if (ignoreCertificateErrors) {
            clientBuilder.sslContext(openSslContext).hostnameVerifier(openHostnameVerifier);
        }

        apiClient = clientBuilder.build();
        Logger.info(
                false,
                "GitLab",
                "GitLab Jersey client creation completed: hostUrl={}, ignoreCertificateErrors={}, userAgentPresent={}",
                hostUrl,
                ignoreCertificateErrors,
                userAgentHeader != null);
        return (apiClient);
    }

    protected Invocation.Builder invocation(URL url, MultivaluedMap<String, String> queryParams, String accept) {

        if (apiClient == null) {
            Logger.debug(
                    true,
                    "GitLab",
                    "GitLab Jersey client lazy initialization requested: urlPath={}, tokenType={}",
                    url == null ? null : url.getPath(),
                    tokenType);
            createApiClient();
        }

        Logger.debug(
                true,
                "GitLab",
                "GitLab invocation preparation started: urlPath={}, queryParamCount={}, accept={}, tokenType={}, sudoPresent={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size(),
                accept,
                tokenType,
                sudoAsId != null && sudoAsId.intValue() > 0);
        WebTarget target = apiClient.target(url.toExternalForm());
        if (queryParams != null) {
            for (Map.Entry<String, List<String>> param : queryParams.entrySet()) {
                target = target.queryParam(param.getKey(), param.getValue().toArray());
            }
        }

        String authHeader = getAuthHeader();
        String authValue = getAuthValue();
        Invocation.Builder builder = target.request();
        if (accept == null || accept.trim().length() == 0) {
            builder = builder.header(authHeader, authValue);
        } else {
            builder = builder.header(authHeader, authValue).accept(accept);
        }
        if (userAgentHeader != null) {
            builder.header("User-Agent", userAgentHeader);
        }
        // If sudo as ID is set add the Sudo header
        if (sudoAsId != null && sudoAsId.intValue() > 0)
            builder = builder.header(SUDO_HEADER, sudoAsId);

        Logger.debug(
                false,
                "GitLab",
                "GitLab invocation preparation completed: urlPath={}, queryParamCount={}, accept={}, sudoPresent={}, connectTimeoutMs={}, readTimeoutMs={}",
                url == null ? null : url.getPath(),
                queryParams == null ? 0 : queryParams.size(),
                accept,
                sudoAsId != null && sudoAsId.intValue() > 0,
                connectTimeout,
                readTimeout);
        return (builder);
    }

    private String getAuthValue() {
        switch (tokenType) {
            case OAUTH2_ACCESS:
                return "Bearer " + authToken.get();

            default:
                return authToken.get();
        }
    }

    private String getAuthHeader() {
        switch (tokenType) {
            case OAUTH2_ACCESS:
                return AUTHORIZATION_HEADER;

            case JOB_TOKEN:
                return JOB_TOKEN_HEADER;

            default:
                return PRIVATE_TOKEN_HEADER;
        }
    }

    /**
     * Used to set the host URL to be used by OAUTH2 login in GitLabApi.
     */
    void setHostUrlToBaseUrl() {
        this.hostUrl = this.baseUrl;
        Logger.info(false, "GitLab", "GitLab host URL switched to base URL: hostUrl={}", hostUrl);
    }

    /**
     * Returns true if the API is setup to ignore SSL certificate errors, otherwise returns false.
     *
     * @return true if the API is setup to ignore SSL certificate errors, otherwise returns false
     */
    public boolean getIgnoreCertificateErrors() {
        return (ignoreCertificateErrors);
    }

    /**
     * Sets up the Jersey system ignore SSL certificate errors or not.
     *
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     */
    public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {

        if (this.ignoreCertificateErrors == ignoreCertificateErrors) {
            Logger.debug(
                    false,
                    "GitLab",
                    "GitLab ignore certificate setting unchanged: ignoreCertificateErrors={}",
                    ignoreCertificateErrors);
            return;
        }

        Logger.info(
                true,
                "GitLab",
                "GitLab ignore certificate setting update started: requested={}",
                ignoreCertificateErrors);
        if (!ignoreCertificateErrors) {

            this.ignoreCertificateErrors = false;
            openSslContext = null;
            openHostnameVerifier = null;
            apiClient = null;
            Logger.info(false, "GitLab", "GitLab ignore certificate setting disabled: apiClientReset={}", true);

        } else {

            if (setupIgnoreCertificateErrors()) {
                this.ignoreCertificateErrors = true;
                apiClient = null;
                Logger.info(false, "GitLab", "GitLab ignore certificate setting enabled: apiClientReset={}", true);
            } else {
                this.ignoreCertificateErrors = false;
                apiClient = null;
                Logger.error(
                        false,
                        "GitLab",
                        "GitLab ignore certificate setting failed: requested={}, apiClientReset={}",
                        ignoreCertificateErrors,
                        true);
                throw new RuntimeException("Unable to ignore certificate errors.");
            }
        }
    }

    /**
     * Returns the configured custom 'User-Agent' header value
     *
     * @return User-Agent header value
     */
    public String getUserAgentHeader() {
        return userAgentHeader;
    }

    /**
     * Configures a custom value for the 'User-Agent' header
     *
     * @param userAgentHeader User-Agent header value
     */
    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
        Logger.info(
                false,
                "GitLab",
                "GitLab user agent header configured: userAgentPresent={}",
                userAgentHeader != null);
    }

    /**
     * Sets up Jersey client to ignore certificate errors.
     *
     * @return true if successful at setting up to ignore certificate errors, otherwise returns false.
     */
    private boolean setupIgnoreCertificateErrors() {

        Logger.debug(true, "GitLab", "GitLab insecure TLS context setup started: hostUrl={}", hostUrl);
        // Create a TrustManager that trusts all certificates
        TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
            }
        } };

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hostnameVerifier = (hostname, session) -> true;

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            openSslContext = sslContext;
            openHostnameVerifier = hostnameVerifier;
        } catch (GeneralSecurityException ex) {
            openSslContext = null;
            openHostnameVerifier = null;
            Logger.error(
                    false,
                    "GitLab",
                    ex,
                    "GitLab insecure TLS context setup failed: exception={}",
                    ex.getClass().getSimpleName());
            return (false);
        }

        Logger.debug(false, "GitLab", "GitLab insecure TLS context setup completed: hostUrl={}", hostUrl);
        return (true);
    }

    /**
     * Set auth token supplier for gitlab api client.
     *
     * @param authTokenSupplier - supplier which provide actual auth token
     */
    public void setAuthTokenSupplier(Supplier<String> authTokenSupplier) {
        this.authToken = authTokenSupplier;
        Logger.info(
                false,
                "GitLab",
                "GitLab auth token supplier configured: tokenType={}, supplierPresent={}",
                tokenType,
                authTokenSupplier != null);
    }

}
