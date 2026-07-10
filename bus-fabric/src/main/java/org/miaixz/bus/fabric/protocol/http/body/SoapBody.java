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
     * SOAP envelope namespace.
     */
    private static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * SOAP prefix.
     */
    private static final String SOAP_PREFIX = "soap";

    /**
     * Method prefix.
     */
    private static final String METHOD_PREFIX = "m";

    /**
     * Method namespace.
     */
    private final String namespace;

    /**
     * Method name.
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
     * Charset.
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
     * @param charset   charset
     * @param action    action
     */
    private SoapBody(final String namespace, final String method, final Map<String, Object> headers,
            final Map<String, Object> params, final Charset charset, final String action) {
        this.namespace = optionalLine(namespace, "SOAP namespace");
        this.method = name(method, "SOAP method");
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.params = Collections.unmodifiableMap(new LinkedHashMap<>(params));
        this.charset = charset == null ? org.miaixz.bus.core.lang.Charset.UTF_8 : charset;
        this.action = optionalLine(action, "SOAPAction");
    }

    /**
     * Creates a SOAP envelope builder.
     *
     * @return builder
     */
    public static Builder envelope() {
        return new Builder();
    }

    /**
     * Returns SOAPAction.
     *
     * @return action
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
        builder.append('<').append(SOAP_PREFIX).append(":Envelope xmlns:").append(SOAP_PREFIX).append("=\"")
                .append(SOAP_NAMESPACE).append('"');
        if (!namespace.isBlank()) {
            builder.append(" xmlns:").append(METHOD_PREFIX).append("=\"").append(escapeAttribute(namespace))
                    .append('"');
        }
        builder.append('>');
        if (!headers.isEmpty()) {
            builder.append('<').append(SOAP_PREFIX).append(":Header>");
            appendElements(builder, headers);
            builder.append("</").append(SOAP_PREFIX).append(":Header>");
        }
        builder.append('<').append(SOAP_PREFIX).append(":Body>");
        appendOpen(builder, method, !namespace.isBlank());
        appendElements(builder, params);
        appendClose(builder, method, !namespace.isBlank());
        builder.append("</").append(SOAP_PREFIX).append(":Body>");
        builder.append("</").append(SOAP_PREFIX).append(":Envelope>");
        return builder.toString();
    }

    /**
     * Returns the HTTP body.
     *
     * @return HTTP body
     */
    public HttpBody body() {
        return HttpBody.of(Payload.of(xml(), charset), MediaType.parse("text/xml; charset=" + charset.name()));
    }

    /**
     * Returns SOAP media.
     *
     * @return media
     */
    @Override
    public MediaType media() {
        return body().media();
    }

    /**
     * Returns SOAP payload.
     *
     * @return payload
     */
    @Override
    public Payload payload() {
        return body().payload();
    }

    /**
     * Appends map entries as XML elements.
     *
     * @param builder target builder
     * @param values  values
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
            builder.append(METHOD_PREFIX).append(Symbol.C_COLON);
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
            builder.append(METHOD_PREFIX).append(Symbol.C_COLON);
        }
        builder.append(name).append('>');
    }

    /**
     * Escapes XML text.
     *
     * @param value value
     * @return escaped text
     */
    private static String escapeText(final String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Escapes XML attribute text.
     *
     * @param value value
     * @return escaped text
     */
    private static String escapeAttribute(final String value) {
        return escapeText(value).replace("\"", "&quot;").replace("'", "&apos;");
    }

    /**
     * Validates an XML element name.
     *
     * @param value value
     * @param field field name
     * @return value
     */
    private static String name(final String value, final String field) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(field + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Validates an optional single-line value.
     *
     * @param value value
     * @param field field name
     * @return value
     */
    private static String optionalLine(final String value, final String field) {
        if (value == null) {
            return Normal.EMPTY;
        }
        if (StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(field + " must be single-line");
        }
        return value;
    }

    /**
     * Builds a default action.
     *
     * @param namespace namespace
     * @param method    method
     * @return action
     */
    private static String defaultAction(final String namespace, final String method) {
        if (namespace == null || namespace.isBlank()) {
            return method;
        }
        if (namespace.endsWith("#") || namespace.endsWith("/") || namespace.endsWith(":")) {
            return namespace + method;
        }
        return namespace + "#" + method;
    }

    /**
     * SOAP envelope builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Namespace.
         */
        private String namespace;

        /**
         * Method.
         */
        private String method;

        /**
         * Headers.
         */
        private final Map<String, Object> headers = new LinkedHashMap<>();

        /**
         * Params.
         */
        private final Map<String, Object> params = new LinkedHashMap<>();

        /**
         * Charset.
         */
        private Charset charset = org.miaixz.bus.core.lang.Charset.UTF_8;

        /**
         * SOAPAction.
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
         * @param namespace namespace
         * @return this builder
         */
        public Builder namespace(final String namespace) {
            this.namespace = namespace;
            return this;
        }

        /**
         * Sets method name.
         *
         * @param method method
         * @return this builder
         */
        public Builder method(final String method) {
            this.method = method;
            return this;
        }

        /**
         * Adds a SOAP header element.
         *
         * @param name  name
         * @param value value
         * @return this builder
         */
        public Builder header(final String name, final Object value) {
            headers.put(name(name, "SOAP header"), value);
            return this;
        }

        /**
         * Adds a method parameter.
         *
         * @param name  name
         * @param value value
         * @return this builder
         */
        public Builder param(final String name, final Object value) {
            params.put(name(name, "SOAP parameter"), value);
            return this;
        }

        /**
         * Adds method parameters.
         *
         * @param params params
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
         * @param charset charset
         * @return this builder
         */
        public Builder charset(final Charset charset) {
            this.charset = charset == null ? org.miaixz.bus.core.lang.Charset.UTF_8 : charset;
            return this;
        }

        /**
         * Sets SOAPAction.
         *
         * @param action action
         * @return this builder
         */
        public Builder action(final String action) {
            this.action = action;
            return this;
        }

        /**
         * Builds a SOAP body.
         *
         * @return SOAP body
         */
        public SoapBody build() {
            return new SoapBody(namespace, method, headers, params, charset, action);
        }

    }

}
