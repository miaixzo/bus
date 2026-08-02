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
package org.miaixz.bus.spring.web.wrapper;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Immutable request and response body-cache boundaries for one application context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BodyCacheOptions {

    /**
     * Default maximum cached request size in bytes.
     */
    public static final long DEFAULT_REQUEST_LIMIT = 1024L * 1024L;
    /**
     * Default maximum cached response size in bytes.
     */
    public static final long DEFAULT_RESPONSE_LIMIT = 1024L * 1024L;

    /**
     * Whether eligible request bodies may be cached.
     */
    private final boolean requestCacheEnabled;
    /**
     * Whether eligible response bodies may be cached.
     */
    private final boolean responseCacheEnabled;
    /**
     * Maximum cached request size in bytes.
     */
    private final long requestLimit;
    /**
     * Maximum cached response size in bytes.
     */
    private final long responseLimit;
    /**
     * Whether multipart request bodies may be cached.
     */
    private final boolean includeMultipart;

    /**
     * Creates explicit immutable body-cache options.
     *
     * @param requestCacheEnabled  whether request caching is enabled
     * @param responseCacheEnabled whether response caching is enabled
     * @param requestLimit         maximum cached request size in bytes
     * @param responseLimit        maximum cached response size in bytes
     * @param includeMultipart     whether multipart requests are eligible
     */
    public BodyCacheOptions(boolean requestCacheEnabled, boolean responseCacheEnabled, long requestLimit,
            long responseLimit, boolean includeMultipart) {
        if (requestLimit <= 0 || responseLimit <= 0) {
            throw new IllegalArgumentException("Body cache limits must be positive");
        }
        this.requestCacheEnabled = requestCacheEnabled;
        this.responseCacheEnabled = responseCacheEnabled;
        this.requestLimit = requestLimit;
        this.responseLimit = responseLimit;
        this.includeMultipart = includeMultipart;
    }

    /**
     * Returns whether request caching is enabled.
     *
     * @return request-cache flag
     */
    public boolean isRequestCacheEnabled() {
        return this.requestCacheEnabled;
    }

    /**
     * Returns whether response caching is enabled.
     *
     * @return response-cache flag
     */
    public boolean isResponseCacheEnabled() {
        return this.responseCacheEnabled;
    }

    /**
     * Returns the maximum cached request size.
     *
     * @return request limit in bytes
     */
    public long getRequestLimit() {
        return this.requestLimit;
    }

    /**
     * Returns the maximum cached response size.
     *
     * @return response limit in bytes
     */
    public long getResponseLimit() {
        return this.responseLimit;
    }

    /**
     * Returns whether multipart request caching is enabled.
     *
     * @return multipart-cache flag
     */
    public boolean isIncludeMultipart() {
        return this.includeMultipart;
    }

    /**
     * Returns whether the request is a bounded, cacheable body request.
     *
     * @param request current HTTP request
     * @return {@code true} when its method and content type are eligible for caching
     */
    public boolean shouldCacheRequest(HttpServletRequest request) {
        if (!this.requestCacheEnabled || request == null) {
            return false;
        }
        String method = request.getMethod();
        if (!Http.Method.POST.value().equals(method) && !Http.Method.PUT.value().equals(method)
                && !Http.Method.PATCH.value().equals(method)) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        if (normalized.startsWith(MediaType.MULTIPART_FORM_DATA)) {
            return this.includeMultipart;
        }
        return normalized.startsWith(MediaType.APPLICATION_JSON) || normalized.contains("+json")
                || normalized.startsWith(MediaType.APPLICATION_FORM_URLENCODED);
    }

}
