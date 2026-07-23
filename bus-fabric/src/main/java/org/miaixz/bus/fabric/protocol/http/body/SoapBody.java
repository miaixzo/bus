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
package org.miaixz.bus.fabric.protocol.http.body;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.RequestBody;

/**
 * SOAP envelope body for HTTP requests.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SoapBody implements RequestBody {

    /**
     * Optional namespace URI for the SOAP operation element.
     */
    private final String namespace;

    /**
     * Local name of the SOAP operation element.
     */
    private final String method;

    /**
     * SOAP header values.
     */
    private final Map<String, Object> headers;

    /**
     * Method parameter values.
     */
    private final Map<String, Object> params;

    /**
     * Character encoding used for XML declaration and payload bytes.
     */
    private final Charset charset;

    /**
     * Optional SOAPAction.
     */
    private final String action;

    /**
     * Creates a SOAP body.
     *
     * @param namespace method namespace
     * @param method    method name
     * @param headers   SOAP headers
     * @param params    method params
     * @param charset   XML character encoding, or {@code null} for UTF-8
     * @param action    optional explicit SOAPAction value
     */
    private SoapBody(final String namespace, final String method, final Map<String, Object> headers,
            final Map<String, Object> params, final Charset charset, final String action) {
        this.namespace = optionalLine(namespace, "SOAP namespace");
        this.method = name(method, "SOAP method");
        this.headers = Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        Assert.notNull(headers, () -> new ValidateException("SOAP headers must not be null"))));
        this.params = Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        Assert.notNull(params, () -> new ValidateException("SOAP params must not be null"))));
        this.charset = charset == null ? org.miaixz.bus.core.lang.Charset.UTF_8 : charset;
        this.action = optionalLine(action, "SOAPAction");
    }

    /**
     * Creates a SOAP envelope builder.
     *
     * @return new SOAP envelope builder
     */
    public static Builder envelope() {
        return new Builder();
    }

    /**
     * Returns SOAPAction.
     *
     * @return explicit SOAPAction or one derived from the operation namespace and name
     */
    public String action() {
        return action.isBlank() ? defaultAction(namespace, method) : action;
    }

    /**
     * Returns the envelope text.
     *
     * @return XML envelope
     */
    public String xml() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("<?xml version=\"1.0\" encoding=\"").append(charset.name()).append("\"?>");
        builder.append('<').append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append(":Envelope xmlns:")
                .append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append("=\"")
                .append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_NAMESPACE).append('"');
        if (!namespace.isBlank()) {
            builder.append(" xmlns:").append(org.miaixz.bus.fabric.Builder.SOAP_METHOD_PREFIX).append("=\"")
                    .append(escapeAttribute(namespace)).append('"');
        }
        builder.append('>');
        if (!headers.isEmpty()) {
            builder.append('<').append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append(":Header>");
            appendElements(builder, headers);
            builder.append("</").append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append(":Header>");
        }
        builder.append('<').append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append(":Body>");
        appendOpen(builder, method, !namespace.isBlank());
        appendElements(builder, params);
        appendClose(builder, method, !namespace.isBlank());
        builder.append("</").append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append(":Body>");
        builder.append("</").append(org.miaixz.bus.fabric.Builder.SOAP_BODY_SOAP_PREFIX).append(":Envelope>");
        return builder.toString();
    }

    /**
     * Returns the payload body.
     *
     * @return payload body
     */
    public PayloadBody body() {
        return PayloadBody.of(payload(), media());
    }

    /**
     * Returns SOAP media.
     *
     * @return XML media type carrying the configured charset
     */
    @Override
    public MediaType media() {
        return MediaType.TEXT_XML_TYPE.withCharset(charset);
    }

    /**
     * Returns SOAP payload.
     *
     * @return repeatable payload containing the serialized SOAP envelope
     */
    @Override
    public Payload payload() {
        return Payload.of(xml(), charset);
    }

    /**
     * Appends map entries as XML elements.
     *
     * @param builder target builder
     * @param values  insertion-ordered element names and scalar or nested values
     */
    private static void appendElements(final StringBuilder builder, final Map<?, ?> values) {
        for (final Map.Entry<?, ?> entry : values.entrySet()) {
            final String element = name(entry.getKey() == null ? null : entry.getKey().toString(), "SOAP element");
            final Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                appendOpen(builder, element, false);
                appendElements(builder, nested);
                appendClose(builder, element, false);
            } else {
                appendOpen(builder, element, false);
                if (value != null) {
                    builder.append(escapeText(value.toString()));
                }
                appendClose(builder, element, false);
            }
        }
    }

    /**
     * Appends an opening element.
     *
     * @param builder target builder
     * @param name    element name
     * @param method  whether to use method namespace prefix
     */
    private static void appendOpen(final StringBuilder builder, final String name, final boolean method) {
        builder.append('<');
        if (method) {
            builder.append(org.miaixz.bus.fabric.Builder.SOAP_METHOD_PREFIX).append(Symbol.C_COLON);
        }
        builder.append(name).append('>');
    }

    /**
     * Appends a closing element.
     *
     * @param builder target builder
     * @param name    element name
     * @param method  whether to use method namespace prefix
     */
    private static void appendClose(final StringBuilder builder, final String name, final boolean method) {
        builder.append("</");
        if (method) {
            builder.append(org.miaixz.bus.fabric.Builder.SOAP_METHOD_PREFIX).append(Symbol.C_COLON);
        }
        builder.append(name).append('>');
    }

    /**
     * Escapes XML text.
     *
     * @param value raw XML character data
     * @return escaped text
     */
    private static String escapeText(final String value) {
        return value.replace(Symbol.AND, "&amp;").replace(Symbol.LT, "&lt;").replace(Symbol.GT, "&gt;");
    }

    /**
     * Escapes XML attribute text.
     *
     * @param value raw XML attribute value
     * @return escaped text
     */
    private static String escapeAttribute(final String value) {
        return escapeText(value).replace("\"", "&quot;").replace("'", "&apos;");
    }

    /**
     * Validates that a configured XML element name is non-blank and single-line.
     *
     * @param value candidate element name
     * @param field logical field name included in validation failures
     * @return validated non-blank single-line element name
     */
    private static String name(final String value, final String field) {
        final String checked = Assert
                .notBlank(value, () -> new ValidateException(field + " must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(field + " must be non-blank and single-line"));
        return checked;
    }

    /**
     * Validates an optional single-line value.
     *
     * @param value candidate optional text
     * @param field logical field name included in validation failures
     * @return validated single-line text, or an empty string for {@code null}
     */
    private static String optionalLine(final String value, final String field) {
        if (value == null) {
            return Normal.EMPTY;
        }
        Assert.isFalse(
                StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(field + " must be single-line"));
        return value;
    }

    /**
     * Builds a default action.
     *
     * @param namespace optional operation namespace URI
     * @param method    operation local name
     * @return SOAPAction derived from the namespace separator and operation name
     */
    private static String defaultAction(final String namespace, final String method) {
        if (namespace == null || namespace.isBlank()) {
            return method;
        }
        if (namespace.endsWith(Symbol.HASH) || namespace.endsWith(Symbol.SLASH) || namespace.endsWith(Symbol.COLON)) {
            return namespace + method;
        }
        return namespace + Symbol.HASH + method;
    }

    /**
     * SOAP envelope builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Candidate operation namespace URI.
         */
        private String namespace;

        /**
         * Candidate operation local name.
         */
        private String method;

        /**
         * Mutable insertion-ordered SOAP header elements.
         */
        private final Map<String, Object> headers = new LinkedHashMap<>();

        /**
         * Mutable insertion-ordered operation parameter elements.
         */
        private final Map<String, Object> params = new LinkedHashMap<>();

        /**
         * XML character encoding candidate.
         */
        private Charset charset = org.miaixz.bus.core.lang.Charset.UTF_8;

        /**
         * Optional explicit SOAPAction candidate.
         */
        private String action;

        /**
         * Creates a builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets method namespace.
         *
         * @param namespace optional operation namespace URI
         * @return this builder
         */
        public Builder namespace(final String namespace) {
            this.namespace = namespace;
            return this;
        }

        /**
         * Sets method name.
         *
         * @param method operation local name
         * @return this builder
         */
        public Builder method(final String method) {
            this.method = method;
            return this;
        }

        /**
         * Adds a SOAP header element.
         *
         * @param name  SOAP header element name
         * @param value scalar value or nested map rendered inside the element
         * @return this builder
         */
        public Builder header(final String name, final Object value) {
            headers.put(name(name, "SOAP header"), value);
            return this;
        }

        /**
         * Adds a method parameter.
         *
         * @param name  operation parameter element name
         * @param value scalar value or nested map rendered inside the element
         * @return this builder
         */
        public Builder param(final String name, final Object value) {
            params.put(name(name, "SOAP parameter"), value);
            return this;
        }

        /**
         * Adds method parameters.
         *
         * @param params parameter names and values, or {@code null} to add nothing
         * @return this builder
         */
        public Builder params(final Map<String, ?> params) {
            if (params != null) {
                params.forEach(this::param);
            }
            return this;
        }

        /**
         * Sets charset.
         *
         * @param charset XML character encoding, or {@code null} to restore UTF-8
         * @return this builder
         */
        public Builder charset(final Charset charset) {
            this.charset = charset == null ? org.miaixz.bus.core.lang.Charset.UTF_8 : charset;
            return this;
        }

        /**
         * Sets SOAPAction.
         *
         * @param action optional explicit SOAPAction value
         * @return this builder
         */
        public Builder action(final String action) {
            this.action = action;
            return this;
        }

        /**
         * Builds a SOAP body.
         *
         * @return immutable SOAP body snapshot from the current configuration
         */
        public SoapBody build() {
            return new SoapBody(namespace, method, headers, params, charset, action);
        }

    }

}
