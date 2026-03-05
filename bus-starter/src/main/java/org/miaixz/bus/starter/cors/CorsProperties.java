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
