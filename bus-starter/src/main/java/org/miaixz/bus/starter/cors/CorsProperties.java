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
package org.miaixz.bus.starter.cors;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS (Cross-Origin Resource Sharing) configuration properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(GeniusBuilder.CORS)
public class CorsProperties {

    /**
     * The path pattern to which this CORS configuration applies. Default is {@code /**}.
     */
    private String path = "/**";

    /**
     * Allowed origins. A value of "*" allows all origins. Default is {@code ["*"]}.
     */
    private String[] allowedOrigins = new String[] { Symbol.STAR };

    /**
     * Allowed request headers. A value of "*" allows all headers. Default is {@code ["*"]}.
     */
    private String[] allowedHeaders = new String[] { Symbol.STAR };

    /**
     * Allowed HTTP methods. Default includes GET, POST, PUT, OPTIONS, DELETE.
     */
    private String[] allowedMethods = new String[] { HTTP.GET, HTTP.POST, HTTP.PUT, HTTP.OPTIONS, HTTP.DELETE };

    /**
     * Headers to be exposed in the response.
     */
    private String[] exposedHeaders;

    /**
     * Whether the browser should include any cookies associated with the domain of the request. Default is
     * {@code true}.
     */
    private Boolean allowCredentials = true;

    /**
     * The validity period of the pre-flight request, in seconds. During this period, the pre-flight request will not be
     * sent again. Default is 1800 seconds (30 minutes).
     */
    private Long maxAge = 1800L;

}
