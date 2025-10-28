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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A central repository for constants defining the gateway's public API contract and for binding configuration
 * properties.
 * <p>
 * This class holds constants for request parameter names, HTTP header names, and URI path prefixes that form the
 * "language" of the gateway. It also contains nested static classes that serve as models for type-safe configuration
 * binding from application properties (e.g., YAML or .properties files) using {@code @ConfigurationProperties}.
 *
 * @author Kimi Liu
 * @see org.miaixz.bus.vortex.strategy.AuthorizeStrategy
 * @see org.miaixz.bus.vortex.strategy.RequestStrategy
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Args {

    /**
     * The mandatory parameter name for the logical API method to be invoked (e.g., "user.getProfile").
     * 
     * @see org.miaixz.bus.vortex.strategy.AuthorizeStrategy
     */
    public static final String METHOD = "method";
    /**
     * The parameter name for specifying the desired response format (e.g., "json", "xml").
     * 
     * @see org.miaixz.bus.vortex.strategy.AuthorizeStrategy
     */
    public static final String FORMAT = "format";
    /**
     * The parameter name for specifying the version of the requested API method (e.g., "v1", "1.0.0").
     * 
     * @see org.miaixz.bus.vortex.strategy.AuthorizeStrategy
     */
    public static final String VERSION = "v";
    /**
     * The parameter name for the request signature, used for validation and to indicate encryption status.
     *
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final String SIGN = "sign";
    /**
     * The parameter name for the request signature, used for validation and to indicate encryption status.
     *
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final String TIMESTAMP = "timestamp";
    /**
     * The parameter name for the request signature, used for validation and to indicate encryption status.
     *
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final String APIKEY = "apiKey";
    /**
     * The HTTP header name for the bearer access token (e.g., JWT).
     * 
     * @see org.miaixz.bus.vortex.strategy.AuthorizeStrategy
     */
    public static final String X_ACCESS_TOKEN = "X-Access-Token";
    /**
     * The HTTP header name for identifying the client channel (e.g., "web", "app", "mobile").
     * 
     * @see org.miaixz.bus.vortex.strategy.AuthorizeStrategy
     */
    public static final String X_REMOTE_CHANNEL = "x_remote_channel";

    /**
     * The base URI path for standard RESTful API requests.
     * 
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final String REST_PATH_PREFIX = "/router/rest";

    /**
     * The base URI path for requests to the MCP (Miaixz Communication Protocol) hub.
     * 
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final String MCP_PATH_PREFIX = "/router/mcp";

    /**
     * The base URI path for requests to be forwarded to a Message Queue.
     * 
     * @see org.miaixz.bus.vortex.strategy.RequestStrategy
     */
    public static final String MQ_PATH_PREFIX = "/router/mq";

    /**
     * A configuration properties model for rate limiting settings, typically bound from application properties under a
     * prefix like {@code vortex.limit}.
     */
    @Getter
    @Setter
    public static class Limit {

        /**
         * Whether rate limiting is globally enabled.
         */
        private boolean enabled;
    }

    /**
     * A configuration properties model for general security settings, typically bound from application properties under
     * a prefix like {@code vortex.security}.
     */
    @Getter
    @Setter
    public static class Mock {

        /**
         * Whether general security features are enabled.
         */
        private boolean enabled;

    }

}
