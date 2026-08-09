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

import java.time.Duration;
import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable CORS configuration properties.
 *
 * @author Kimi Liu
 */
@Validated
@ConfigurationProperties(GeniusBuilder.CORS)
public class CorsProperties {

    /**
     * HTTP DELETE method used by the default CORS policy.
     */
    private static final String METHOD_DELETE = "DELETE";

    /**
     * HTTP GET method used by the default CORS policy.
     */
    private static final String METHOD_GET = "GET";

    /**
     * HTTP OPTIONS method used by the default CORS policy.
     */
    private static final String METHOD_OPTIONS = "OPTIONS";

    /**
     * HTTP POST method used by the default CORS policy.
     */
    private static final String METHOD_POST = "POST";

    /**
     * HTTP PUT method used by the default CORS policy.
     */
    private static final String METHOD_PUT = "PUT";

    /**
     * Whether the cors integration is enabled.
     */
    private final boolean enabled;
    /**
     * Servlet path pattern to which the CORS policy applies.
     */
    private final String path;
    /**
     * Origins permitted to issue cross-origin requests.
     */
    private final String[] allowedOrigins;
    /**
     * Request headers accepted during cross-origin requests.
     */
    private final String[] allowedHeaders;
    /**
     * HTTP methods accepted during cross-origin requests.
     */
    private final String[] allowedMethods;
    /**
     * Response headers made visible to browser clients.
     */
    private final String[] exposedHeaders;
    /**
     * Whether browsers may include credentials in cross-origin requests.
     */
    private final boolean allowCredentials;
    /**
     * Duration for which browsers may cache a preflight response.
     */
    private final Duration maxAge;

    /**
     * Validates credential, origin, and preflight settings and stores defensive copies of all header arrays.
     *
     * @param enabled          whether CORS support is enabled
     * @param path             mapped request path
     * @param allowedOrigins   permitted origins
     * @param allowedHeaders   permitted request headers
     * @param allowedMethods   permitted HTTP methods
     * @param exposedHeaders   response headers exposed to browsers
     * @param allowCredentials whether credentialed requests are permitted
     * @param maxAge           preflight cache duration
     */
    public CorsProperties(@DefaultValue(Normal.FALSE) boolean enabled,
            @DefaultValue(Symbol.SLASH + Symbol.STAR + Symbol.STAR) String path, @DefaultValue String[] allowedOrigins,
            @DefaultValue(Symbol.STAR) String[] allowedHeaders,
            @DefaultValue({ METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_OPTIONS,
                    METHOD_DELETE }) String[] allowedMethods,
            @DefaultValue String[] exposedHeaders, @DefaultValue(Normal.FALSE) boolean allowCredentials,
            @DefaultValue("30m") Duration maxAge) {
        String[] origins = allowedOrigins == null ? Normal.EMPTY_STRING_ARRAY : allowedOrigins.clone();
        if (allowCredentials && Arrays.asList(origins).contains(Symbol.STAR)) {
            throw new IllegalArgumentException("bus.cors cannot combine wildcard origins with credentials");
        }
        if (maxAge == null || maxAge.isNegative()) {
            throw new IllegalArgumentException("bus.cors.max-age must not be negative");
        }
        this.enabled = enabled;
        this.path = path;
        this.allowedOrigins = origins;
        this.allowedHeaders = allowedHeaders == null ? new String[] { Symbol.STAR } : allowedHeaders.clone();
        this.allowedMethods = allowedMethods == null
                ? new String[] { METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_OPTIONS, METHOD_DELETE }
                : allowedMethods.clone();
        this.exposedHeaders = exposedHeaders == null ? Normal.EMPTY_STRING_ARRAY : exposedHeaders.clone();
        this.allowCredentials = allowCredentials;
        this.maxAge = maxAge;
    }

    /**
     * Indicates whether the validated CORS policy should be registered.
     *
     * @return whether CORS support is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Exposes the Servlet path pattern covered by this CORS policy.
     *
     * @return mapped request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns a defensive copy of the permitted origin patterns.
     *
     * @return a copy of permitted origins
     */
    public String[] getAllowedOrigins() {
        return allowedOrigins.clone();
    }

    /**
     * Returns a defensive copy of request headers accepted by the policy.
     *
     * @return a copy of permitted request headers
     */
    public String[] getAllowedHeaders() {
        return allowedHeaders.clone();
    }

    /**
     * Returns a defensive copy of HTTP methods accepted by the policy.
     *
     * @return a copy of permitted methods
     */
    public String[] getAllowedMethods() {
        return allowedMethods.clone();
    }

    /**
     * Returns a defensive copy of response headers visible to browser clients.
     *
     * @return a copy of exposed response headers
     */
    public String[] getExposedHeaders() {
        return exposedHeaders.clone();
    }

    /**
     * Indicates whether browser clients may attach credentials to cross-origin requests.
     *
     * @return whether credentialed requests are permitted
     */
    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    /**
     * Exposes the validated preflight response cache duration.
     *
     * @return preflight cache duration in seconds
     */
    public Long getMaxAge() {
        return maxAge.toSeconds();
    }

    /**
     * @return safe diagnostic text
     */
    @Override
    public String toString() {
        return "CorsProperties[enabled=" + enabled + ", path=" + path + ", allowedOrigins="
                + Arrays.toString(allowedOrigins) + ", allowCredentials=" + allowCredentials + ", maxAge=" + maxAge
                + "]";
    }

}
