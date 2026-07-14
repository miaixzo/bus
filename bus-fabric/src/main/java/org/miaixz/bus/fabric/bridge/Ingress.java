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
 * Immutable external ingress metadata used before protocol adaptation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Ingress {

    /**
     * Ingress path.
     */
    private final String path;

    /**
     * External method.
     */
    private final String method;

    /**
     * Header snapshot.
     */
    private final Headers headers;

    /**
     * Payload reference.
     */
    private final Payload payload;

    /**
     * Attribute snapshot using null sentinel values.
     */
    private final Map<String, Object> attributes;

    /**
     * Creates a bridge ingress.
     *
     * @param path       path
     * @param method     method
     * @param headers    headers
     * @param payload    payload
     * @param attributes attributes
     */
    private Ingress(final String path, final String method, final Headers headers, final Payload payload,
            final Map<String, Object> attributes) {
        this.path = normalizePath(path);
        this.method = normalizeOptionalMethod(method);
        this.headers = headers == null ? Headers.empty() : headers;
        this.payload = payload == null ? Payload.empty() : payload;
        this.attributes = immutableAttributes(attributes);
    }

    /**
     * Creates an ingress builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the ingress path.
     *
     * @return path
     */
    public String path() {
        return path;
    }

    /**
     * Returns the external method.
     *
     * @return method
     */
    public String method() {
        return method;
    }

    /**
     * Returns the header snapshot.
     *
     * @return headers
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the payload reference.
     *
     * @return payload
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Returns decoded immutable attributes.
     *
     * @return attributes
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
     * @param source attributes
     * @return immutable attributes
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
     * @return null sentinel
     */
    private static Object nullValue() {
        return Instances.get(Ingress.class.getName() + ".null", Object::new);
    }

    /**
     * Normalizes a path.
     *
     * @param value path
     * @return normalized path
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
     * @param value method
     * @return normalized method
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
     * @param value method
     * @return method
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
     * @param key key
     * @return key
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Bridge attribute key must be non-blank and single-line");
        }
        return key;
    }

    /**
     * Builder for bridge ingresses.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Candidate path.
         */
        private String path = Symbol.SLASH;

        /**
         * Candidate method.
         */
        private String method = Normal.EMPTY;

        /**
         * Candidate headers.
         */
        private Headers headers = Headers.empty();

        /**
         * Candidate payload.
         */
        private Payload payload = Payload.empty();

        /**
         * Candidate attributes.
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
         * @param path path
         * @return this builder
         */
        public Builder path(final String path) {
            this.path = normalizePath(path);
            return this;
        }

        /**
         * Sets the method.
         *
         * @param method method
         * @return this builder
         */
        public Builder method(final String method) {
            this.method = validateMethod(method);
            return this;
        }

        /**
         * Sets headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            this.headers = headers == null ? Headers.empty() : headers;
            return this;
        }

        /**
         * Sets payload.
         *
         * @param payload payload
         * @return this builder
         */
        public Builder payload(final Payload payload) {
            this.payload = payload == null ? Payload.empty() : payload;
            return this;
        }

        /**
         * Adds an attribute.
         *
         * @param key   key
         * @param value value
         * @return this builder
         */
        public Builder attribute(final String key, final Object value) {
            attributes.put(validateKey(key), value);
            return this;
        }

        /**
         * Builds a bridge ingress.
         *
         * @return bridge ingress
         */
        public Ingress build() {
            return new Ingress(path, method, headers, payload, attributes);
        }

    }

}
