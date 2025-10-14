/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex;

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
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Context extends Tracer {

    /**
     * The key used to store and retrieve this Context object from the attributes of a {@code ServerWebExchange}.
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
    private Map<String, String> parameters = new HashMap<>();

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
     * The HTTP method of the incoming request (e.g., GET, POST).
     */
    private HttpMethod httpMethod;

    /**
     * The resolved API asset that matches the incoming request's method and version. This object contains all
     * configuration for the requested API endpoint.
     */
    private Assets assets;

    /**
     * The access token extracted from the request headers, used for authentication and authorization.
     */
    private String token;

    /**
     * A flag indicating the security mode of the request, typically used to enable or disable features like
     * encryption/decryption.
     */
    private Integer sign;

    /**
     * The timestamp in milliseconds when the request processing started. This is used for calculating total execution
     * time.
     */
    private long timestamp;

}
