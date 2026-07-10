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
package org.miaixz.bus.fabric.protocol.http.auth;

import java.util.LinkedHashMap;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * HTTP authenticator strategy for origin and proxy challenges.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface HttpAuthenticator {

    /**
     * Returns no follow-up authentication.
     *
     * @return authenticator
     */
    static HttpAuthenticator none() {
        return (request, response) -> null;
    }

    /**
     * Creates a basic authenticator.
     *
     * @param auth auth header generator
     * @return authenticator
     */
    static HttpAuthenticator basic(final HttpAuth auth) {
        final HttpAuth current = require(auth, "HTTP auth");
        return (request, response) -> {
            if (response.challenges().isEmpty()) {
                return null;
            }
            return current.authenticate(request, challenge(response.challenges().getFirst(), response.code() == 407));
        };
    }

    /**
     * Authenticates a failed request.
     *
     * @param request  failed request
     * @param response challenge response
     * @return authenticated request, or null when no follow-up is available
     */
    HttpRequest authenticate(HttpRequest request, HttpResponse response);

    /**
     * Marks a challenge as proxy or origin targeted.
     *
     * @param challenge challenge
     * @param proxy     proxy flag
     * @return challenge
     */
    static Challenge challenge(final Challenge challenge, final boolean proxy) {
        final Challenge current = require(challenge, "Challenge");
        if (!proxy) {
            return current;
        }
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<>(current.parameters());
        parameters.put("proxy", "true");
        return new Challenge(current.scheme(), current.realm(), parameters);
    }

    /**
     * Validates authenticator collaborators before they can create follow-up requests.
     *
     * @param value value
     * @param name  field name used in validation messages
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
