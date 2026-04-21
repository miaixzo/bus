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

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.core.basic.entity.Tracer;
import org.miaixz.bus.vortex.filter.PrimaryFilter;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.magic.Parameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.multipart.Part;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the request context, a stateful object that is created at the beginning of a request and enriched as it
 * passes through the strategy chain.
 * <p>
 * This class acts as a central data carrier for a single request, holding everything from request parameters and
 * headers to authorization details and matched API asset information. It is created once per request in the
 * {@link PrimaryFilter} and shared across all components via the Reactor context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@NoArgsConstructor
public class Context extends Tracer {

    /**
     * The key used to store and retrieve this Context object from the attributes of a {@code ServerWebExchange}.
     * <p>
     * This provides a fallback mechanism for accessing the context, especially in components like {@link ErrorsHandler}
     * where the Reactor context may no longer be available.
     */
    public static final String $ = "X.CONTEXT";

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
     * The resolved API asset that matches the incoming request's method and version. This object contains all
     * configuration for the requested API endpoint.
     */
    private Assets assets;

    /**
     * The timestamp in milliseconds when the request processing started. This is used for calculating total execution
     * time.
     */
    private long timestamp;

    /**
     * The HTTP method of the incoming request (e.g., GET, POST).
     */
    private HttpMethod httpMethod;

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
    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

}
