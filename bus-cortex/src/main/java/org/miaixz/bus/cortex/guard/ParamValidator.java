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
package org.miaixz.bus.cortex.guard;

import java.util.regex.Pattern;

/**
 * Input validation utilities for cortex parameters.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ParamValidator {

    /**
     * Pattern used to validate namespace-like identifiers such as namespaces and data IDs.
     */
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{1,128}$");

    /**
     * Utility class constructor.
     */
    private ParamValidator() {
    }

    /**
     * Validates a namespace or dataId value.
     *
     * @param value value to check
     * @throws IllegalArgumentException if invalid
     */
    public static void validateNamespace(String value) {
        if (value == null || !NAMESPACE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid namespace: " + value);
        }
    }

    /**
     * Compatibility alias for legacy callers. Prefer {@link #validateNamespace(String)}.
     *
     * @param value value to check
     * @throws IllegalArgumentException if invalid
     */
    public static void validateScope(String value) {
        validateNamespace(value);
    }

    /**
     * Validates a configuration data identifier.
     *
     * @param value value to check
     * @throws IllegalArgumentException if invalid
     */
    public static void validateDataId(String value) {
        if (value == null || !NAMESPACE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid dataId: " + value);
        }
    }

    /**
     * Validates a host name or IP address.
     *
     * @param value value to check
     * @throws IllegalArgumentException if invalid
     */
    public static void validateHost(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Host must not be empty");
        }
    }

    /**
     * Validates a TCP port number.
     *
     * @param port value to check
     * @throws IllegalArgumentException if invalid
     */
    public static void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535, was: " + port);
        }
    }

}
