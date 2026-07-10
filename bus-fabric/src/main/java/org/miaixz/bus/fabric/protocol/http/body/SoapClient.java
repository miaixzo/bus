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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * SOAP message builder and HTTP sender backed by {@link HttpX}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SoapClient {

    /**
     * Default method prefix.
     */
    private static final String METHOD_PREFIX = "m";

    /**
     * Default SOAP header prefix.
     */
    private static final String HEADER_PREFIX = "h";

    /**
     * Default SOAP header namespace for local-name header convenience methods.
     */
    private static final String HEADER_NAMESPACE = "urn:bus:fabric:soap:header";

    /**
     * Runtime context.
     */
    private final Context context;

    /**
     * Target URL.
     */
    private UnoUrl url;

    /**
     * SOAP protocol.
     */
    private Protocol protocol;

    /**
     * Charset.
     */
    private Charset charset;

    /**
     * HTTP headers.
     */
    private final Headers.Builder headers;

    /**
     * SOAPAction.
     */
    private String action;

    /**
     * Message factory.
     */
    private MessageFactory factory;

    /**
     * SOAP message.
     */
    private SOAPMessage message;

    /**
     * Current method element.
     */
    private SOAPBodyElement methodElement;

    /**
     * Creates a SOAP client.
     *
     * @param context context
     * @param url     target URL
     */
    private SoapClient(final Context context, final UnoUrl url) {
        this.context = require(context, "Context");
        this.url = require(url, "URL");
        this.protocol = Protocol.SOAP_1_2;
        this.charset = org.miaixz.bus.core.lang.Charset.UTF_8;
        this.headers = Headers.builder();
        reset();
    }

    /**
     * Creates a SOAP client with a default context.
     *
     * @param url target URL
     * @return client
     */
    public static SoapClient of(final String url) {
        return of(Context.create(), UnoUrl.parse(url));
    }

    /**
     * Creates a SOAP client.
     *
     * @param context context
     * @param url     target URL
     * @return client
     */
    public static SoapClient of(final Context context, final String url) {
        return of(context, UnoUrl.parse(url));
    }

    /**
     * Creates a SOAP client.
     *
     * @param context context
     * @param url     target URL
     * @return client
     */
    public static SoapClient of(final Context context, final UnoUrl url) {
        return new SoapClient(context, url);
    }

    /**
     * Sets target URL.
     *
     * @param url target URL
     * @return this client
     */
    public SoapClient url(final String url) {
        this.url = UnoUrl.parse(url);
        return this;
    }

    /**
     * Sets SOAP protocol.
     *
     * @param protocol SOAP protocol
     * @return this client
     */
    public SoapClient protocol(final Protocol protocol) {
        if (protocol != Protocol.SOAP_1_1 && protocol != Protocol.SOAP_1_2) {
            throw new ValidateException("SOAP protocol must be SOAP_1_1 or SOAP_1_2");
        }
        this.protocol = protocol;
        reset();
        return this;
    }

    /**
     * Sets charset.
     *
     * @param charset charset
     * @return this client
     */
    public SoapClient charset(final Charset charset) {
        this.charset = charset == null ? org.miaixz.bus.core.lang.Charset.UTF_8 : charset;
        applyMessageProperties();
        return this;
    }

    /**
     * Sets SOAPAction.
     *
     * @param action action
     * @return this client
     */
    public SoapClient action(final String action) {
        this.action = optionalLine(action, "SOAPAction");
        return this;
    }

    /**
     * Adds an HTTP header.
     *
     * @param name  name
     * @param value value
     * @return this client
     */
    public SoapClient header(final String name, final String value) {
        headers.add(name(name, "HTTP header"), optionalLine(value, "HTTP header value"));
        return this;
    }

    /**
     * Resets the SOAP message.
     *
     * @return this client
     */
    public SoapClient reset() {
        this.factory = factory(protocol);
        try {
            this.message = factory.createMessage();
            this.methodElement = null;
            applyMessageProperties();
            return this;
        } catch (final SOAPException e) {
            throw new InternalException("Unable to create SOAP message", e);
        }
    }

    /**
     * Adds a SOAP header.
     *
     * @param localName local name
     * @param value     text value
     * @return header element
     */
    public SOAPHeaderElement soapHeader(final String localName, final String value) {
        final SOAPHeaderElement element = soapHeader(headerName(localName));
        element.setTextContent(value == null ? Normal.EMPTY : value);
        return element;
    }

    /**
     * Adds a SOAP header.
     *
     * @param name header name
     * @return header element
     */
    public SOAPHeaderElement soapHeader(final QName name) {
        try {
            SOAPHeader header = message.getSOAPHeader();
            if (header == null) {
                header = message.getSOAPPart().getEnvelope().addHeader();
            }
            return header.addHeaderElement(require(name, "SOAP header name"));
        } catch (final SOAPException e) {
            throw new InternalException("Unable to add SOAP header", e);
        }
    }

    /**
     * Sets SOAP body method.
     *
     * @param localName local name
     * @return this client
     */
    public SoapClient method(final String localName) {
        return method(new QName(name(localName, "SOAP method")));
    }

    /**
     * Sets SOAP body method.
     *
     * @param namespace namespace
     * @param localName local name
     * @return this client
     */
    public SoapClient method(final String namespace, final String localName) {
        final String checkedNamespace = optionalLine(namespace, "SOAP namespace");
        final QName qName = checkedNamespace.isBlank() ? new QName(name(localName, "SOAP method"))
                : new QName(checkedNamespace, name(localName, "SOAP method"), METHOD_PREFIX);
        return method(qName);
    }

    /**
     * Sets SOAP body method.
     *
     * @param name method name
     * @return this client
     */
    public SoapClient method(final QName name) {
        try {
            this.methodElement = message.getSOAPBody().addBodyElement(require(name, "SOAP method"));
            if (action == null || action.isBlank()) {
                action(defaultAction(name));
            }
            return this;
        } catch (final SOAPException e) {
            throw new InternalException("Unable to add SOAP method", e);
        }
    }

    /**
     * Adds a method parameter.
     *
     * @param name  name
     * @param value value
     * @return this client
     */
    public SoapClient param(final String name, final Object value) {
        return param(name, value, true);
    }

    /**
     * Adds method parameters.
     *
     * @param params params
     * @return this client
     */
    public SoapClient params(final Map<String, ?> params) {
        if (params != null) {
            params.forEach(this::param);
        }
        return this;
    }

    /**
     * Adds a method parameter.
     *
     * @param name            name
     * @param value           value
     * @param useMethodPrefix whether nested elements use method prefix
     * @return this client
     */
    public SoapClient param(final String name, final Object value, final boolean useMethodPrefix) {
        if (methodElement == null) {
            throw new StatefulException("SOAP method must be set before parameters");
        }
        addElement(methodElement, name, value, useMethodPrefix ? methodElement.getPrefix() : null);
        return this;
    }

    /**
     * Returns the current SOAP message.
     *
     * @return SOAP message
     */
    public SOAPMessage message() {
        return message;
    }

    /**
     * Returns the current method element.
     *
     * @return method element
     */
    public SOAPBodyElement methodElement() {
        return methodElement;
    }

    /**
     * Returns the current SOAP XML.
     *
     * @return XML text
     */
    public String xml() {
        try {
            message.saveChanges();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            message.writeTo(output);
            return output.toString(charset);
        } catch (final SOAPException | IOException e) {
            throw new InternalException("Unable to write SOAP message", e);
        }
    }

    /**
     * Returns the HTTP body for the current SOAP message.
     *
     * @return body
     */
    public HttpBody body() {
        return HttpBody.of(Payload.of(xml(), charset), MediaType.parse(contentType()));
    }

    /**
     * Returns HTTP request headers.
     *
     * @return headers
     */
    public Headers headers() {
        final Headers.Builder builder = Headers.builder();
        headers.build().asMap().forEach((name, values) -> values.forEach(value -> builder.add(name, value)));
        builder.set(HTTP.CONTENT_TYPE, contentType());
        if (StringKit.isNotBlank(action)) {
            builder.set("SOAPAction", action);
        }
        return builder.build();
    }

    /**
     * Sends the SOAP request and returns response text.
     *
     * @return response text
     */
    public String send() {
        return sendForResponse().text();
    }

    /**
     * Sends the SOAP request and returns the HTTP response.
     *
     * @return HTTP response
     */
    public HttpResponse sendForResponse() {
        return HttpX.builder(context).post(url.encoded()).headers(headers()).body(body()).execute();
    }

    /**
     * Sends the SOAP request and parses the response message.
     *
     * @return SOAP message
     */
    public SOAPMessage sendForMessage() {
        final HttpResponse response = sendForResponse();
        try {
            final MimeHeaders mimeHeaders = mimeHeaders(response.headers());
            return factory.createMessage(mimeHeaders, new ByteArrayInputStream(response.bytes()));
        } catch (final IOException | SOAPException e) {
            throw new InternalException("Unable to read SOAP response", e);
        } finally {
            response.close();
        }
    }

    /**
     * Extracts a SOAP fault when present.
     *
     * @param message message
     * @return fault or null
     */
    public static SOAPFault fault(final SOAPMessage message) {
        try {
            return require(message, "SOAP message").getSOAPBody().hasFault() ? message.getSOAPBody().getFault() : null;
        } catch (final SOAPException e) {
            throw new InternalException("Unable to read SOAP fault", e);
        }
    }

    /**
     * Adds a SOAP element.
     *
     * @param parent parent
     * @param name   name
     * @param value  value
     * @param prefix optional prefix
     * @return child element
     */
    private static SOAPElement addElement(
            final SOAPElement parent,
            final String name,
            final Object value,
            final String prefix) {
        try {
            final SOAPElement child = StringKit.isBlank(prefix) ? parent.addChildElement(name(name, "SOAP element"))
                    : parent.addChildElement(name(name, "SOAP element"), prefix);
            if (value instanceof SOAPElement soapElement) {
                child.addChildElement(soapElement);
            } else if (value instanceof Map<?, ?> map) {
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    addElement(child, String.valueOf(entry.getKey()), entry.getValue(), prefix);
                }
            } else if (value != null) {
                child.setValue(value.toString());
            }
            return child;
        } catch (final SOAPException e) {
            throw new InternalException("Unable to add SOAP element", e);
        }
    }

    /**
     * Returns a namespace-qualified header name for convenience local-name headers.
     *
     * @param localName local name
     * @return qualified header name
     */
    private QName headerName(final String localName) {
        final String checked = name(localName, "SOAP header");
        if (methodElement != null && StringKit.isNotBlank(methodElement.getNamespaceURI())) {
            return new QName(methodElement.getNamespaceURI(), checked,
                    StringKit.isBlank(methodElement.getPrefix()) ? METHOD_PREFIX : methodElement.getPrefix());
        }
        return new QName(HEADER_NAMESPACE, checked, HEADER_PREFIX);
    }

    /**
     * Creates a SOAP message factory.
     *
     * @param protocol protocol
     * @return factory
     */
    private static MessageFactory factory(final Protocol protocol) {
        try {
            return MessageFactory.newInstance(
                    protocol == Protocol.SOAP_1_1 ? SOAPConstants.SOAP_1_1_PROTOCOL : SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (final SOAPException | RuntimeException e) {
            throw new InternalException("Jakarta SOAP MessageFactory is not available", e);
        }
    }

    /**
     * Applies charset properties to the current message.
     */
    private void applyMessageProperties() {
        if (message == null) {
            return;
        }
        try {
            message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, charset.name());
            message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        } catch (final SOAPException ignored) {
            // Some providers do not support all optional message properties.
        }
    }

    /**
     * Builds MIME headers for SOAP response parsing.
     *
     * @param headers headers
     * @return MIME headers
     */
    private static MimeHeaders mimeHeaders(final Headers headers) {
        final MimeHeaders mimeHeaders = new MimeHeaders();
        headers.asMap().forEach((name, values) -> {
            for (final String value : values) {
                mimeHeaders.addHeader(name, value);
            }
        });
        return mimeHeaders;
    }

    /**
     * Returns request content type.
     *
     * @return content type
     */
    private String contentType() {
        final String media = protocol == Protocol.SOAP_1_1 ? "text/xml" : "application/soap+xml";
        return media + "; charset=" + charset.name();
    }

    /**
     * Returns default SOAPAction.
     *
     * @param name method name
     * @return action
     */
    private static String defaultAction(final QName name) {
        final String namespace = name.getNamespaceURI();
        if (StringKit.isBlank(namespace)) {
            return name.getLocalPart();
        }
        if (namespace.endsWith("#") || namespace.endsWith("/") || namespace.endsWith(":")) {
            return namespace + name.getLocalPart();
        }
        return namespace + "#" + name.getLocalPart();
    }

    /**
     * Validates a name.
     *
     * @param value value
     * @param field field
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
     * @param field field
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
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
