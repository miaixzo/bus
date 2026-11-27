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
package org.miaixz.bus.cortex.registry;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Validates the configured service registration and configuration endpoint.
 */
public final class RegistryEndpoint {

    /**
     * Shared deployment setting containing the complete registration URL.
     */
    public static final String REGISTER_URL = "REGISTER_URL";

    /**
     * Derived local property used when binding route and authentication rules.
     */
    public static final String PATH_PROPERTY = "bus.cortex.register-path";

    /**
     * Matches unambiguous path segments without encoded characters or dot segments.
     */
    private static final Pattern PATH = Pattern.compile("(/[A-Za-z0-9_-][A-Za-z0-9._~-]*)+");

    /**
     * Prevents utility class construction.
     */
    private RegistryEndpoint() {
        // No initialization required.
    }

    /**
     * Validates a complete registration URL before it is used for routing or authenticated transport.
     *
     * @param value registration URL
     * @return validated endpoint
     * @throws IllegalArgumentException if the deployment URL is missing or invalid
     */
    public static URI parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("REGISTER_URL is required");
        }
        URI uri = URI.create(value);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            throw new IllegalArgumentException(
                    "REGISTER_URL must be a complete HTTP or HTTPS URL without credentials, query or fragment");
        }
        validatePath(uri.getRawPath());
        return uri;
    }

    /**
     * Extracts the gateway path from the registration URL.
     *
     * @param configured complete registration URL
     * @return gateway registration path
     */
    public static String path(String configured) {
        return parse(configured).getRawPath();
    }

    /**
     * Validates the gateway path used by the registration route.
     *
     * @param path absolute endpoint path
     * @throws IllegalArgumentException if the path contains an invalid segment
     */
    public static void validatePath(String path) {
        if (path == null || !PATH.matcher(path).matches()) {
            throw new IllegalArgumentException("REGISTER_URL must contain a non-root path with plain URL segments");
        }
    }

}
