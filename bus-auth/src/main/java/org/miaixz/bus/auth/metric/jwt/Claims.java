/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.auth.metric.jwt;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;

/**
 * JWT Claims authentication class for storing and processing JWT header or payload data.
 * <p>
 * Claims represents a collection of key-value pairs in JWT, supporting parsing of Base64-encoded JSON strings, storing
 * them as a Map structure, and providing setting, getting, and serialization functionality.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Claims implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852289137231L;

    /**
     * Claims data storage, using Map to save key-value pairs.
     */
    private Map<String, Object> claims;

    /**
     * Sets a Claims property.
     * <p>
     * If the property value is null, removes the property; otherwise stores the property name and value in the Map.
     * </p>
     *
     * @param name  the property name, cannot be null
     * @param value the property value
     * @throws IllegalArgumentException if the property name is null
     */
    public void setClaim(final String name, final Object value) {
        Assert.notNull(name, "Name must be not null!");
        init();
        if (value == null) {
            claims.remove(name);
            return;
        }
        claims.put(name, value);
    }

    /**
     * Batch adds Claims properties.
     * <p>
     * Iterates through the provided Map and adds each key-value pair to the Claims.
     * </p>
     *
     * @param headerClaims a Map containing multiple properties
     */
    public void putAll(final Map<String, ?> headerClaims) {
        if (headerClaims != null && !headerClaims.isEmpty()) {
            for (final Map.Entry<String, ?> entry : headerClaims.entrySet()) {
                setClaim(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Gets the property value for the specified name.
     *
     * @param name the property name
     * @return the property value, or null if it does not exist
     */
    public Object getClaim(final String name) {
        init();
        return claims.get(name);
    }

    /**
     * Gets the key-value pair collection of Claims.
     *
     * @return the Map representation of Claims
     */
    public Map<String, Object> getClaimsJson() {
        init();
        return claims;
    }

    /**
     * Parses a Base64-encoded JSON string and stores it as Claims.
     * <p>
     * Parses the JSON string after Base64 decoding into key-value pairs and stores them in the internal Map.
     * </p>
     *
     * @param tokenPart the Base64-encoded JSON string
     * @param charset   the character encoding
     * @throws IllegalArgumentException if the JSON format is incorrect
     */
    public void parse(final String tokenPart, final Charset charset) {
        String decoded = Base64.decodeString(tokenPart, charset);
        this.claims = parseJsonString(decoded);
    }

    /**
     * Converts Claims to a JSON string.
     *
     * @return the string representation in JSON format
     */
    @Override
    public String toString() {
        init();
        return toJsonString(claims);
    }

    /**
     * Initializes the Map storage for Claims.
     * <p>
     * If claims is not initialized, creates a new HashMap.
     * </p>
     */
    private void init() {
        if (this.claims == null) {
            this.claims = new HashMap<>();
        }
    }

    /**
     * Parses a JSON string into a Map.
     * <p>
     * Assumes the JSON is simple key-value pairs (no nesting), supporting string and numeric values.
     * </p>
     *
     * @param json the JSON string
     * @return the parsed Map
     * @throws IllegalArgumentException if the JSON format is incorrect
     */
    private Map<String, Object> parseJsonString(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return result;
        }
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) {
            return result;
        }
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            String value = keyValue[1].trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                result.put(key, value.substring(1, value.length() - 1));
            } else if (value.matches("-?\\d+")) {
                result.put(key, Long.parseLong(value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Converts a Map to a JSON string.
     * <p>
     * Serializes key-value pairs in the Map into a JSON format string, with string values quoted and numeric values
     * unquoted.
     * </p>
     *
     * @param map the key-value pair Map
     * @return the JSON string
     */
    private String toJsonString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

}
