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
package org.miaixz.bus.image.galaxy.media;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable HTTP header used by DICOM web access configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpTag {

    /**
     * The valid header name value.
     */
    private static final Pattern VALID_HEADER_NAME = Pattern.compile("^[a-zA-Z0-9!#$&'*+\\-.^_`|~]+$");

    /**
     * The max header length value.
     */
    private static final int MAX_HEADER_LENGTH = 8192;

    /**
     * The key value.
     */
    private final String key;

    /**
     * The value value.
     */
    private final String value;

    /**
     * Creates a new instance.
     *
     * @param key   the key.
     * @param value the value.
     */
    public HttpTag(String key, String value) {
        this.key = validateKey(Objects.requireNonNull(key, "HTTP header key cannot be null"));
        this.value = validateValue(Objects.requireNonNull(value, "HTTP header value cannot be null"));
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param key   the key.
     * @param value the value.
     * @return the operation result.
     */
    public static HttpTag of(String key, String value) {
        return new HttpTag(key, value);
    }

    /**
     * Executes the authorization operation.
     *
     * @param token the token.
     * @return the operation result.
     */
    public static HttpTag authorization(String token) {
        return new HttpTag("Authorization", "Bearer " + Objects.requireNonNull(token, "Token cannot be null"));
    }

    /**
     * Executes the content type operation.
     *
     * @param contentType the content type.
     * @return the operation result.
     */
    public static HttpTag contentType(String contentType) {
        return new HttpTag("Content-Type", Objects.requireNonNull(contentType, "Content type cannot be null"));
    }

    /**
     * Gets the key.
     *
     * @return the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value.
     *
     * @return the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Determines whether authorization header.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isAuthorizationHeader() {
        return "Authorization".equalsIgnoreCase(key);
    }

    /**
     * Determines whether sensitive.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSensitive() {
        String lowerKey = key.toLowerCase(Locale.ROOT);
        return lowerKey.contains("auth") || lowerKey.contains("token") || lowerKey.contains("password")
                || lowerKey.contains("secret") || lowerKey.contains("key");
    }

    /**
     * Converts this value to header string.
     *
     * @return the operation result.
     */
    public String toHeaderString() {
        return key + ": " + value;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return isSensitive() ? "HttpTag{key='" + key + "', value='[REDACTED]'}"
                : "HttpTag{key='" + key + "', value='" + value + "'}";
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        return this == object || (object instanceof HttpTag other && Objects.equals(key, other.key)
                && Objects.equals(value, other.value));
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    /**
     * Validates the key.
     *
     * @param key the key.
     * @return the operation result.
     */
    private static String validateKey(String key) {
        String trimmed = key.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("HTTP header key cannot be blank");
        }
        if (!VALID_HEADER_NAME.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("HTTP header key contains invalid characters: " + key);
        }
        return trimmed;
    }

    /**
     * Validates the value.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static String validateValue(String value) {
        if (value.length() > MAX_HEADER_LENGTH) {
            throw new IllegalArgumentException("HTTP header value exceeds maximum length: " + value.length());
        }
        String trimmed = value.trim();
        if (trimmed.chars().anyMatch(ch -> ch < 32 && ch != 9)) {
            throw new IllegalArgumentException("HTTP header value contains invalid control characters");
        }
        return trimmed;
    }

}
