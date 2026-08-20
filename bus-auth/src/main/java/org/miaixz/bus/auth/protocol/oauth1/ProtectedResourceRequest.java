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
package org.miaixz.bus.auth.protocol.oauth1;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Carries an exact protected-resource HTTP request before OAuth 1.0 signature and Authorization header generation.
 *
 * @param method     standard HTTP wire method
 * @param url        exact protected-resource target including query parameters
 * @param parameters ordered decoded request parameters participating where RFC 5849 permits
 * @param headers    immutable non-Authorization request headers
 * @param body       exact request body octets
 * @author Kimi Liu
 */
public record ProtectedResourceRequest(Http.Method method, UnoUrl url, List<OAuth1Parameter> parameters,
        Headers headers, byte[] body) {

    /**
     * Validates request ownership and defensively copies mutable body bytes.
     *
     * @throws IllegalArgumentException if a component or parameter is {@code null}
     * @throws ValidateException        if a routing method, signature parameter, or Authorization header is
     *                                  pre-populated
     */
    public ProtectedResourceRequest {
        Assert.notNull(method, "Protected resource HTTP method must not be null");
        Assert.notNull(url, "Protected resource URL must not be null");
        Assert.notNull(headers, "Protected resource headers must not be null");
        Assert.notNull(body, "Protected resource body must not be null");
        if (method == Http.Method.ALL || method == Http.Method.NONE || method == Http.Method.BEFORE
                || method == Http.Method.AFTER) {
            throw new ValidateException("Protected resource request requires an HTTP wire method");
        }
        parameters = OAuth1Parameter.immutable(parameters);
        if (OAuth1Parameter.contains(parameters, OAuth1.Parameters.SIGNATURE)) {
            throw new ValidateException("Protected resource request must not pre-populate oauth_signature");
        }
        if (headers.contains(Http.Header.AUTHORIZATION)) {
            throw new ValidateException("Protected resource request must not pre-populate Authorization");
        }
        body = body.clone();
    }

    /**
     * Returns a defensive copy of request body octets.
     *
     * @return copied body bytes
     */
    @Override
    public byte[] body() {
        return body.clone();
    }

    /**
     * Returns a safe representation without URL query, headers, parameters, or body.
     *
     * @return redacted request label
     */
    @Override
    public String toString() {
        return "ProtectedResourceRequest[method=" + method + ", url=" + url.redact()
                + ", parameters=[REDACTED], headers=[REDACTED], body=[REDACTED]]";
    }

}
