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
package org.miaixz.bus.vortex;

import java.io.Serial;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import org.springframework.http.codec.multipart.Part;

import org.miaixz.bus.auth.guard.CallerValidator;
import org.miaixz.bus.auth.guard.GuardConfig;
import org.miaixz.bus.core.basic.entity.Tracer;
import org.miaixz.bus.core.center.function.BiFunctionX;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.vortex.filter.PrimaryFilter;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.magic.Parameter;

/**
 * Represents the request context, a stateful object that is created at the beginning of a request and enriched as it
 * passes through the strategy chain.
 * <p>
 * This class acts as a central data carrier for a single request, holding everything from request parameters and
 * headers to authorization details and matched API asset information. It is created once per request in the
 * {@link PrimaryFilter} and shared across all components via the Reactor context.
 *
 * @author Kimi Liu
 */
@Getter
public class Context extends Tracer {

    /**
     * Serialization version identifier.
     */
    @Serial
    private static final long serialVersionUID = 2560609205001L;

    /**
     * The key used to store and retrieve this Context object from the attributes of a {@code ServerWebExchange}.
     * <p>
     * This provides a fallback mechanism for accessing the context, especially in components like {@link ErrorsHandler}
     * where the Reactor context may no longer be available.
     */
    public static final String $ = "X.CONTEXT";

    /**
     * The exchange attribute key that marks a request whose JSON body uses an array as its root.
     * <p>
     * This marker is internal request state only. It is not added to request parameters, signatures, or downstream
     * payloads.
     */
    public static final String JSON_ARRAY_BODY_ATTRIBUTE = "X.JSON_ARRAY_BODY";

    /**
     * Marks a request body that must be forwarded without parsing or replay caching.
     */
    public static final String RAW_BODY_PASSTHROUGH_ATTRIBUTE = "X.RAW_BODY_PASSTHROUGH";

    /**
     * A map of the HTTP request headers. This map is a direct, single-value representation of the incoming request's
     * headers.
     */
    private Parameter headers = new Parameter(true);

    /**
     * Controlled business-parameter storage. External callers only see read-only views and sanitized snapshots.
     */
    private Parameter parameters = new Parameter();

    /**
     * Controlled query-parameter storage. External callers only see read-only sanitized views.
     */
    private Parameter query = new Parameter(true);

    /**
     * A map of uploaded files for multipart/form-data requests. The key is the form field name, and the value is the
     * {@link Part} object representing the uploaded file.
     */
    private Map<String, Part> fileParts = new HashMap<>();

    /**
     * The requested data format for the response, such as JSON or XML. This is determined from the request parameters.
     */
    private Formats format = Formats.JSON;

    /**
     * The channel through which the request was made, e.g., WEB, APP, etc. This is determined from the request
     * parameters or headers.
     */
    private Channel channel = Channel.WEB;

    /**
     * The access token / api key extracted from the request headers, used for authentication and authorization.
     */
    private String bearer;

    /**
     * The resolved route asset that matches the incoming request dimensions. This object contains the runtime
     * configuration for the selected route.
     */
    private Assets assets;

    /**
     * Signs the selected downstream URI and the exact forwarded body.
     */
    private transient BiFunctionX<URI, byte[], String> forwardProof;

    /**
     * Installs the signer after successful authentication for the current protected REST call.
     *
     * @param signer signer bound to this request's verified authorization
     */
    public void setForwardProof(BiFunctionX<URI, byte[], String> signer) {
        this.forwardProof = signer;
    }

    /**
     * The remaining request path produced by route resolution. Prefix-routed protocols can use this value for
     * downstream path rewriting without repeating route matching in executors.
     */
    private String remainingPath;

    /**
     * The timestamp in milliseconds when the request processing started. This is used for calculating total execution
     * time.
     */
    private long timestamp;

    /**
     * The HTTP method of the incoming request (e.g., GET, POST).
     */
    private Http.Method httpMethod;

    /**
     * Whether direct client Bearer authentication is enabled for this listener.
     */
    private final boolean bearerEnabled;

    /**
     * Whether client API key authentication is enabled for this listener.
     */
    private final boolean apiKeyEnabled;

    /**
     * Maximum Bearer credential length for V1 request signing.
     */
    private final int bearerMaxLength;

    /**
     * Maximum API key length for V1 request signing.
     */
    private final int apiKeyMaxLength;

    /**
     * Whether the client's request signature is verified when required by the asset.
     */
    private final boolean signingEnabled;

    /**
     * Creates an empty request context.
     */
    public Context() {
        this(true, true, true);
    }

    /**
     * Creates an empty request context using the client's effective Guard switches.
     *
     * @param bearerEnabled  whether direct client Bearer authentication is enabled
     * @param apiKeyEnabled  whether client API key authentication is enabled
     * @param signingEnabled whether client request-signature verification is enabled
     */
    public Context(boolean bearerEnabled, boolean apiKeyEnabled, boolean signingEnabled) {
        this(new GuardConfig.Bearer(bearerEnabled), new GuardConfig.ApiKey(apiKeyEnabled), signingEnabled);
    }

    /**
     * Creates a request context with the listener's client credential and signing settings.
     *
     * @param bearerGuard    direct Bearer authentication settings
     * @param apiKeyGuard    API key authentication settings
     * @param signingEnabled whether client request-signature verification is enabled
     */
    public Context(GuardConfig.Bearer bearerGuard, GuardConfig.ApiKey apiKeyGuard, boolean signingEnabled) {
        this.bearerEnabled = bearerGuard.enabled();
        this.apiKeyEnabled = apiKeyGuard.enabled();
        this.bearerMaxLength = bearerGuard.maxLength();
        this.apiKeyMaxLength = apiKeyGuard.maxLength();
        this.signingEnabled = signingEnabled;
    }

    /**
     * Selects the first presented, enabled client authentication method.
     *
     * @return the enabled Bearer or API key credential, or {@code null} if neither is available
     */
    public Http.Auth.Credential clientCredential() {
        return CallerValidator.credential(getHeaders(), bearerEnabled, apiKeyEnabled);
    }

    /**
     * Returns a read-only live view of the business parameters.
     *
     * @return An unmodifiable parameter map view.
     */
    public Map<String, String> getHeaders() {
        return this.headers.asStringMap();
    }

    /**
     * Replaces the current request headers using the shared sanitization rules.
     *
     * @param values The source header map.
     */
    public void setHeaders(Map<?, ?> values) {
        this.headers.replaceAll(values);
    }

    /**
     * Adds a single request header after applying the shared sanitization rules.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Removes a single request header from the controlled header view.
     *
     * @param key header name
     */
    public void removeHeader(String key) {
        this.headers.remove(key);
    }

    /**
     * Returns a read-only live view of the business parameters.
     *
     * @return The controlled parameter map.
     */
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    /**
     * Returns a read-only sanitized view of the query parameters.
     *
     * @return A sanitized query-parameter map.
     */
    public Map<String, String> getQuery() {
        return this.query.asStringMap();
    }

    /**
     * Replaces the current query parameters using the shared sanitization rules.
     *
     * @param values The source query-parameter map.
     */
    public void setQuery(Map<?, ?> values) {
        this.query.replaceAll(values);
    }

    /**
     * Replaces uploaded multipart parts for the current request.
     *
     * @param fileParts multipart parts keyed by field name
     */
    public void setFileParts(Map<String, Part> fileParts) {
        this.fileParts = fileParts;
    }

    /**
     * Sets the preferred response format resolved for the request.
     *
     * @param format negotiated response format
     */
    public void setFormat(Formats format) {
        this.format = format;
    }

    /**
     * Sets the access channel resolved from the request metadata.
     *
     * @param channel request channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Stores the bearer token or API key extracted from the request.
     *
     * @param bearer bearer credential value
     */
    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    /**
     * Stores the matched route asset for downstream processing.
     *
     * @param assets resolved route asset
     */
    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    /**
     * Stores the remaining request path produced by route resolution.
     *
     * @param remainingPath remaining path, or an empty string for an exact route match
     */
    public void setRemainingPath(String remainingPath) {
        this.remainingPath = remainingPath;
    }

    /**
     * Stores the request start timestamp in milliseconds.
     *
     * @param timestamp request start time
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Stores the incoming HTTP method.
     *
     * @param httpMethod request HTTP method
     */
    public void setHttpMethod(Http.Method httpMethod) {
        this.httpMethod = httpMethod;
    }

}
