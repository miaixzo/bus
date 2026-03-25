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

import org.miaixz.bus.cortex.Assets;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Tracer;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.multipart.Part;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents the request context, a stateful object that is created at the beginning of a request and enriched as it
 * passes through the strategy chain.
 * <p>
 * This class acts as a central data carrier for a single request, holding everything from request parameters and
 * headers to authorization details and matched API asset information. It is created once per request in the
 * {@link org.miaixz.bus.vortex.filter.PrimaryFilter} and shared across all components via the Reactor context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Context extends Tracer {

    /**
     * The key used to store and retrieve this Context object from the attributes of a {@code ServerWebExchange}.
     * <p>
     * This provides a fallback mechanism for accessing the context, especially in components like
     * {@link org.miaixz.bus.vortex.handler.ErrorsHandler} where the Reactor context may no longer be available.
     */
    public static final String $ = "X.CONTEXT";

    /**
     * A map of the HTTP request headers. This map is a direct, single-value representation of the incoming request's
     * headers.
     */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * A map of the request's business parameters. This map aggregates parameters from the URL query, the request body
     * (form or JSON), and any additional parameters derived during processing.
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();

    /**
     * A map of the original URL query parameters. This map stores only the query parameters from the original request
     * URL (e.g., ?task_id=123456), separate from request body parameters. This allows POST requests to preserve URL
     * query parameters while keeping body parameters in the request body.
     * <p>
     * These parameters can be modified during processing and will be used when building the target URI.
     */
    @Builder.Default
    private Map<String, String> query = new HashMap<>();

    /**
     * A map of uploaded files for multipart/form-data requests. The key is the form field name, and the value is the
     * {@link Part} object representing the uploaded file.
     */
    @Builder.Default
    private Map<String, Part> fileParts = new HashMap<>();

    /**
     * The requested data format for the response, such as JSON or XML. This is determined from the request parameters.
     */
    @Builder.Default
    private Formats format = Formats.JSON;

    /**
     * The channel through which the request was made, e.g., WEB, APP, etc. This is determined from the request
     * parameters or headers.
     */
    @Builder.Default
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

}
