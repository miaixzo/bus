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
package org.miaixz.bus.fabric.bridge;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;

/**
 * Immutable external ingestion metadata used before protocol adaptation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Ingestion {

    /**
     * Normalized non-empty ingestion path beginning with {@code /}.
     */
    private final String path;

    /**
     * Trimmed external operation name, or an empty string when unspecified.
     */
    private final String method;

    /**
     * Immutable external header collection.
     */
    private final Headers headers;

    /**
     * External payload, represented by the shared empty payload when absent.
     */
    private final Payload payload;

    /**
     * Immutable attributes whose explicit {@code null} values are stored as a private sentinel.
     */
    private final Map<String, Object> attributes;

    /**
     * Creates a bridge ingestion.
     *
     * @param path       external ingestion path to normalize
     * @param method     optional external operation name
     * @param headers    immutable headers, or {@code null} for an empty collection
     * @param payload    external payload, or {@code null} for an empty payload
     * @param attributes attributes copied into an immutable snapshot
     */
    private Ingestion(final String path, final String method, final Headers headers, final Payload payload,
            final Map<String, Object> attributes) {
        this.path = normalizePath(path);
        this.method = normalizeOptionalMethod(method);
        this.headers = headers == null ? Headers.empty() : headers;
        this.payload = payload == null ? Payload.empty() : payload;
        this.attributes = immutableAttributes(attributes);
    }

    /**
     * Creates an ingestion builder.
     *
     * @return a new ingestion builder initialized with empty metadata
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the ingestion path.
     *
     * @return normalized ingestion path beginning with {@code /}
     */
    public String path() {
        return path;
    }

    /**
     * Returns the external method.
     *
     * @return trimmed external operation name, or an empty string when unspecified
     */
    public String method() {
        return method;
    }

    /**
     * Returns the header snapshot.
     *
     * @return immutable external headers
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the payload reference.
     *
     * @return external payload associated with the ingestion
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Returns decoded immutable attributes.
     *
     * @return immutable attribute map with sentinel values decoded back to {@code null}
     */
    public Map<String, Object> attributes() {
        final Map<String, Object> copy = MapKit.newHashMap(attributes.size(), true);
        for (final Map.Entry<String, Object> entry : attributes.entrySet()) {
            copy.put(entry.getKey(), entry.getValue() == nullValue() ? null : entry.getValue());
        }
        return MapKit.view(copy);
    }

    /**
     * Creates immutable attributes with null sentinels.
     *
     * @param source source attributes to validate and copy, or {@code null}
     * @return immutable attribute snapshot with explicit {@code null} values encoded as a sentinel
     */
    private static Map<String, Object> immutableAttributes(final Map<String, Object> source) {
        final Map<String, Object> copy = MapKit.newHashMap(source == null ? 0 : source.size(), true);
        if (source != null) {
            for (final Map.Entry<String, Object> entry : source.entrySet()) {
                copy.put(validateKey(entry.getKey()), entry.getValue() == null ? nullValue() : entry.getValue());
            }
        }
        return MapKit.view(copy);
    }

    /**
     * Returns the shared sentinel used to preserve null attribute values.
     *
     * @return process-local singleton used to encode explicit {@code null} attribute values
     */
    private static Object nullValue() {
        return Instances.get(Ingestion.class.getName() + ".null", Object::new);
    }

    /**
     * Normalizes a path.
     *
     * @param value path to validate and normalize
     * @return path with a leading slash
     * @throws ValidateException if the path is blank or contains a line break
     */
    private static String normalizePath(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Bridge path must be non-blank and single-line");
        }
        return value.startsWith(Symbol.SLASH) ? value : Symbol.SLASH + value;
    }

    /**
     * Normalizes an optional method.
     *
     * @param value optional operation name to normalize
     * @return trimmed operation name, or an empty string for {@code null}
     * @throws ValidateException if the operation name contains a line break
     */
    private static String normalizeOptionalMethod(final String value) {
        if (value == null) {
            return Normal.EMPTY;
        }
        if (StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Bridge method must be single-line");
        }
        return StringKit.trim(value);
    }

    /**
     * Validates a required method setter value.
     *
     * @param value required operation name to normalize
     * @return trimmed, non-blank operation name
     * @throws ValidateException if the operation name is blank or contains a line break
     */
    private static String validateMethod(final String value) {
        final String normalized = normalizeOptionalMethod(value);
        if (StringKit.isBlank(normalized)) {
            throw new ValidateException("Bridge method must be non-blank");
        }
        return normalized;
    }

    /**
     * Validates an attribute key.
     *
     * @param key attribute key to validate
     * @return validated key without modifying its original text
     * @throws ValidateException if the key is blank or contains a line break
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Bridge attribute key must be non-blank and single-line");
        }
        return key;
    }

    /**
     * Builder for bridge ingestions.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Validated ingestion path, initially the root path.
         */
        private String path = Symbol.SLASH;

        /**
         * Validated external operation name, initially unspecified.
         */
        private String method = Normal.EMPTY;

        /**
         * External headers, initially empty.
         */
        private Headers headers = Headers.empty();

        /**
         * External payload, initially empty.
         */
        private Payload payload = Payload.empty();

        /**
         * Mutable attributes retained in insertion order until build time.
         */
        private final LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();

        /**
         * Creates a builder with default lightweight values.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the path.
         *
         * @param path ingestion path to normalize and store
         * @return this builder
         * @throws ValidateException if the path is blank or contains a line break
         */
        public Builder path(final String path) {
            this.path = normalizePath(path);
            return this;
        }

        /**
         * Sets the method.
         *
         * @param method required external operation name
         * @return this builder
         * @throws ValidateException if the operation name is blank or contains a line break
         */
        public Builder method(final String method) {
            this.method = validateMethod(method);
            return this;
        }

        /**
         * Sets headers.
         *
         * @param headers external headers, or {@code null} to select an empty collection
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            this.headers = headers == null ? Headers.empty() : headers;
            return this;
        }

        /**
         * Sets payload.
         *
         * @param payload external payload, or {@code null} to select an empty payload
         * @return this builder
         */
        public Builder payload(final Payload payload) {
            this.payload = payload == null ? Payload.empty() : payload;
            return this;
        }

        /**
         * Adds an attribute.
         *
         * @param key   non-blank, single-line attribute key
         * @param value attribute value, which may be {@code null}
         * @return this builder
         * @throws ValidateException if the key is blank or contains a line break
         */
        public Builder attribute(final String key, final Object value) {
            attributes.put(validateKey(key), value);
            return this;
        }

        /**
         * Builds a bridge ingestion.
         *
         * @return immutable ingestion containing snapshots of this builder's values
         */
        public Ingestion build() {
            return new Ingestion(path, method, headers, payload, attributes);
        }

    }

}
