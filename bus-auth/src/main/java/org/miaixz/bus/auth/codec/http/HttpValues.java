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
package org.miaixz.bus.auth.codec.http;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Reads cardinality-sensitive values from normalized authentication HTTP headers and query parameters.
 *
 * @author Kimi Liu
 */
public final class HttpValues {

    /**
     * Prevents construction of the stateless reader.
     */
    private HttpValues() {
        // No initialization required.
    }

    /**
     * Reads one optional single-valued header using a locale-independent normalized name.
     *
     * @param headers normalized headers
     * @param name    header name
     * @param error   duplicate-value failure factory
     * @return absent or exact header value
     */
    public static String header(
            final Map<String, List<String>> headers,
            final String name,
            final Supplier<? extends RuntimeException> error) {
        final Map<String, List<String>> source = Assert
                .notNull(headers, () -> new ValidateException("HTTP headers must not be null"));
        final String key = required(name, "HTTP header name").toLowerCase(Locale.ROOT);
        return single(source.get(key), error);
    }

    /**
     * Reads one optional single-valued decoded query parameter.
     *
     * @param query decoded query values
     * @param name  parameter name
     * @param error duplicate-value failure factory
     * @return absent or exact query value
     */
    public static String query(
            final Map<String, List<String>> query,
            final String name,
            final Supplier<? extends RuntimeException> error) {
        final Map<String, List<String>> source = Assert
                .notNull(query, () -> new ValidateException("HTTP query must not be null"));
        return single(source.get(required(name, "HTTP query parameter name")), error);
    }

    /**
     * Tests whether exactly one Content-Type declares application/json, with optional media-type parameters.
     *
     * @param headers normalized response headers
     * @param error   duplicate-value failure factory
     * @return whether JSON is declared
     */
    public static boolean json(
            final Map<String, List<String>> headers,
            final Supplier<? extends RuntimeException> error) {
        final String value = header(headers, Http.Header.CONTENT_TYPE, error);
        if (value == null) {
            return false;
        }
        final int separator = value.indexOf(';');
        final String type = (separator < 0 ? value : value.substring(0, separator)).trim();
        return MediaType.APPLICATION_JSON.equalsIgnoreCase(type);
    }

    /**
     * Reads one absent or exactly single list value.
     */
    private static String single(final List<String> values, final Supplier<? extends RuntimeException> error) {
        if (values == null) {
            return null;
        }
        if (values.size() != 1 || values.getFirst() == null) {
            throw failure(error);
        }
        return values.getFirst();
    }

    /**
     * Validates one required structural name.
     */
    private static String required(final String value, final String label) {
        final String current = Assert.notNull(value, () -> new ValidateException(label + " must not be null"));
        if (current.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
        return current;
    }

    /**
     * Creates one caller-owned cardinality failure.
     */
    private static RuntimeException failure(final Supplier<? extends RuntimeException> error) {
        final Supplier<? extends RuntimeException> factory = Assert
                .notNull(error, () -> new ValidateException("HTTP error factory must not be null"));
        return Assert.notNull(factory.get(), () -> new ValidateException("HTTP error factory returned null"));
    }

}
