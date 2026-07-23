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
package org.miaixz.bus.fabric.protocol.http;

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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * SOAP message builder and HTTP sender backed by {@link HttpX}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SoapX {

    /**
     * Runtime context used to build the backing HTTP exchange.
     */
    private final Context context;

    /**
     * Current HTTP target URL.
     */
    private UnoUrl url;

    /**
     * SOAP envelope protocol version.
     */
    private Protocol protocol;

    /**
     * Character encoding used for SOAP XML serialization.
     */
    private Charset charset;

    /**
     * Additional HTTP request headers supplied by the caller.
     */
    private final Headers.Builder headers;

    /**
     * Optional SOAPAction request header value.
     */
    private String action;

    /**
     * Optional filter applied to the backing SOAP HTTP exchange.
     */
    private Filter filter;

    /**
     * Message factory matching the selected SOAP protocol version.
     */
    private MessageFactory factory;

    /**
     * Mutable SOAP request message under construction.
     */
    private SOAPMessage message;

    /**
     * Current operation element in the SOAP body, or {@code null} before selection.
     */
    private SOAPBodyElement methodElement;

    /**
     * Creates a SOAP exchange.
     *
     * @param context runtime context used by the backing HTTP exchange
     * @param url     initial HTTP target URL
     */
    private SoapX(final Context context, final UnoUrl url) {
        this.context = require(context, "Context");
        this.url = require(url, "URL");
        this.protocol = Protocol.SOAP_1_2;
        this.charset = org.miaixz.bus.core.lang.Charset.UTF_8;
        this.headers = Headers.builder();
        reset();
    }

    /**
     * Creates a SOAP exchange with a default context.
     *
     * @param url HTTP target URL
     * @return new SOAP exchange backed by a newly created context
     */
    public static SoapX of(final String url) {
        return of(Context.create(), UnoUrl.parse(url));
    }

    /**
     * Creates a SOAP exchange.
     *
     * @param context runtime context used by the backing HTTP exchange
     * @param url     HTTP target URL
     * @return new SOAP exchange for the parsed target URL
     */
    public static SoapX of(final Context context, final String url) {
        return of(context, UnoUrl.parse(url));
    }

    /**
     * Creates a SOAP exchange.
     *
     * @param context runtime context used by the backing HTTP exchange
     * @param url     parsed HTTP target URL
     * @return new SOAP exchange for the supplied context and target
     */
    public static SoapX of(final Context context, final UnoUrl url) {
        return new SoapX(context, url);
    }

    /**
     * Sets target URL.
     *
     * @param url replacement HTTP target URL
     * @return this exchange
     */
    public SoapX url(final String url) {
        this.url = UnoUrl.parse(url);
        return this;
    }

    /**
     * Sets SOAP protocol.
     *
     * @param protocol SOAP protocol
     * @return this exchange
     */
    public SoapX protocol(final Protocol protocol) {
        Assert.isTrue(
                protocol == Protocol.SOAP_1_1 || protocol == Protocol.SOAP_1_2,
                () -> new ValidateException("SOAP protocol must be SOAP_1_1 or SOAP_1_2"));
        this.protocol = protocol;
        reset();
        return this;
    }

    /**
     * Sets charset.
     *
     * @param charset XML character encoding, or {@code null} to restore UTF-8
     * @return this exchange
     */
    public SoapX charset(final Charset charset) {
        this.charset = charset == null ? org.miaixz.bus.core.lang.Charset.UTF_8 : charset;
        applyMessageProperties();
        return this;
    }

    /**
     * Sets SOAPAction.
     *
     * @param action SOAPAction header value, or {@code null} to clear it
     * @return this exchange
     */
    public SoapX action(final String action) {
        this.action = optionalLine(action, "SOAPAction");
        return this;
    }

    /**
     * Adds an HTTP header.
     *
     * @param name  HTTP request header name
     * @param value HTTP request header value, or {@code null} for an empty value
     * @return this exchange
     */
    public SoapX header(final String name, final String value) {
        headers.add(name(name, "HTTP header"), optionalLine(value, "HTTP header value"));
        return this;
    }

    /**
     * Sets SOAP-scoped message filter.
     *
     * @param filter filter applied to the backing HTTP exchange
     * @return this exchange
     */
    public SoapX filter(final Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Resets the SOAP message.
     *
     * @return this exchange
     */
    public SoapX reset() {
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
     * @param localName local name resolved against the current operation namespace
     * @param value     text value
     * @return newly added SOAP header element containing the supplied text
     */
    public SOAPHeaderElement soapHeader(final String localName, final String value) {
        final SOAPHeaderElement element = soapHeader(headerName(localName));
        element.setTextContent(value == null ? Normal.EMPTY : value);
        return element;
    }

    /**
     * Adds a SOAP header.
     *
     * @param name qualified SOAP header name
     * @return newly added SOAP header element
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
     * @param localName unqualified SOAP operation name
     * @return this exchange
     */
    public SoapX method(final String localName) {
        return method(new QName(name(localName, "SOAP method")));
    }

    /**
     * Sets SOAP body method.
     *
     * @param namespace optional SOAP operation namespace URI
     * @param localName SOAP operation local name
     * @return this exchange
     */
    public SoapX method(final String namespace, final String localName) {
        final String checkedNamespace = optionalLine(namespace, "SOAP namespace");
        final QName qName = checkedNamespace.isBlank() ? new QName(name(localName, "SOAP method"))
                : new QName(checkedNamespace, name(localName, "SOAP method"), Builder.SOAP_METHOD_PREFIX);
        return method(qName);
    }

    /**
     * Sets SOAP body method.
     *
     * @param name qualified SOAP operation name
     * @return this exchange
     */
    public SoapX method(final QName name) {
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
     * @param name  parameter element name
     * @param value parameter value or nested SOAP/map structure
     * @return this exchange
     */
    public SoapX param(final String name, final Object value) {
        return param(name, value, true);
    }

    /**
     * Adds method parameters.
     *
     * @param params parameter names and values, or {@code null} to add nothing
     * @return this exchange
     */
    public SoapX params(final Map<String, ?> params) {
        if (params != null) {
            params.forEach(this::param);
        }
        return this;
    }

    /**
     * Adds a method parameter.
     *
     * @param name            parameter element name
     * @param value           parameter value or nested SOAP/map structure
     * @param useMethodPrefix whether nested elements use method prefix
     * @return this exchange
     */
    public SoapX param(final String name, final Object value, final boolean useMethodPrefix) {
        if (methodElement == null) {
            throw new StatefulException("SOAP method must be set before parameters");
        }
        addElement(methodElement, name, value, useMethodPrefix ? methodElement.getPrefix() : null);
        return this;
    }

    /**
     * Returns the current SOAP message.
     *
     * @return mutable SOAP request message currently under construction
     */
    public SOAPMessage message() {
        return message;
    }

    /**
     * Returns the current method element.
     *
     * @return current SOAP body operation element, or {@code null} when none is set
     */
    public SOAPBodyElement methodElement() {
        return methodElement;
    }

    /**
     * Returns the current SOAP XML.
     *
     * @return serialized SOAP XML using the configured charset
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
     * Returns the payload body for the current SOAP message.
     *
     * @return repeatable HTTP request body containing the current SOAP XML
     */
    public PayloadBody body() {
        return PayloadBody.of(Payload.of(xml(), charset), media());
    }

    /**
     * Returns HTTP request headers.
     *
     * @return immutable HTTP request headers including SOAP content metadata
     */
    public Headers headers() {
        final Headers.Builder builder = Headers.builder();
        headers.build().asMap().forEach((name, values) -> values.forEach(value -> builder.add(name, value)));
        builder.set(Http.Header.CONTENT_TYPE, contentType());
        if (StringKit.isNotBlank(action)) {
            builder.set(Http.Header.SOAP_ACTION, action);
        }
        return builder.build();
    }

    /**
     * Creates a call for the current SOAP exchange.
     *
     * @return new call for the current SOAP HTTP exchange
     */
    public Call<HttpResponse> call() {
        return exchange().call();
    }

    /**
     * Executes the current SOAP exchange.
     *
     * @return HTTP response produced by synchronous SOAP execution
     */
    public HttpResponse execute() {
        return exchange().execute();
    }

    /**
     * Executes the current SOAP exchange and returns response text.
     *
     * @return SOAP HTTP response body decoded as text
     */
    public String executeText() {
        return execute().text();
    }

    /**
     * Enqueues the current SOAP exchange.
     *
     * @return asynchronously enqueued call for the current SOAP exchange
     */
    public Call<HttpResponse> enqueue() {
        return call().enqueue();
    }

    /**
     * Executes the current SOAP exchange and parses the response message.
     *
     * @return parsed SOAP response message
     */
    public SOAPMessage executeMessage() {
        final HttpResponse response = execute();
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
     * Sends the SOAP request and returns response text.
     *
     * @return SOAP HTTP response body decoded as text
     */
    public String send() {
        return executeText();
    }

    /**
     * Sends the SOAP request and returns the HTTP response.
     *
     * @return HTTP response produced by the SOAP request
     */
    public HttpResponse sendForResponse() {
        return execute();
    }

    /**
     * Sends the SOAP request and parses the response message.
     *
     * @return parsed SOAP response message
     */
    public SOAPMessage sendForMessage() {
        return executeMessage();
    }

    /**
     * Extracts a SOAP fault when present.
     *
     * @param message SOAP message whose body is inspected
     * @return contained SOAP fault, or {@code null} when the body has no fault
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
     * @param parent parent SOAP element receiving the child
     * @param name   child element name
     * @param value  scalar value, nested map, SOAP element, or {@code null}
     * @param prefix optional namespace prefix inherited by nested map elements
     * @return newly added child SOAP element
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
     * @param localName unqualified header name
     * @return qualified header name based on the operation or fallback namespace
     */
    private QName headerName(final String localName) {
        final String checked = name(localName, "SOAP header");
        if (methodElement != null && StringKit.isNotBlank(methodElement.getNamespaceURI())) {
            return new QName(methodElement.getNamespaceURI(), checked,
                    StringKit.isBlank(methodElement.getPrefix()) ? Builder.SOAP_METHOD_PREFIX
                            : methodElement.getPrefix());
        }
        return new QName(Builder.SOAP_X_HEADER_NAMESPACE, checked, Builder.SOAP_X_HEADER_PREFIX);
    }

    /**
     * Creates a SOAP message factory.
     *
     * @param protocol selected SOAP protocol version
     * @return Jakarta SOAP message factory for that protocol
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
     * @param headers HTTP response headers to copy
     * @return Jakarta SOAP MIME headers containing every supplied value
     */
    private static MimeHeaders mimeHeaders(final Headers headers) {
        final Headers checkedHeaders = require(headers, "Headers");
        final MimeHeaders mimeHeaders = new MimeHeaders();
        checkedHeaders.asMap().forEach((name, values) -> {
            for (final String value : values) {
                mimeHeaders.addHeader(name, value);
            }
        });
        return mimeHeaders;
    }

    /**
     * Builds the backing HTTP exchange.
     *
     * @return configured HTTP POST exchange carrying the current SOAP message
     */
    private HttpX exchange() {
        return HttpX.builder(context).post(url.encoded()).headers(headers()).body(body())
                .tag(Builder.HTTP_TAG_SOAP_REQUEST).filter(filter).build();
    }

    /**
     * Returns request content type.
     *
     * @return serialized media type value for the HTTP Content-Type header
     */
    private String contentType() {
        return media().value();
    }

    /**
     * Returns request media.
     *
     * @return SOAP-version-specific request media type with the configured charset
     */
    private MediaType media() {
        final MediaType base = protocol == Protocol.SOAP_1_1 ? MediaType.TEXT_XML_TYPE
                : MediaType.APPLICATION_SOAP_XML_TYPE;
        return base.withCharset(charset);
    }

    /**
     * Returns default SOAPAction.
     *
     * @param name qualified SOAP operation name
     * @return default SOAPAction derived from its namespace and local part
     */
    private static String defaultAction(final QName name) {
        final String namespace = name.getNamespaceURI();
        if (StringKit.isBlank(namespace)) {
            return name.getLocalPart();
        }
        if (namespace.endsWith(Symbol.HASH) || namespace.endsWith(Symbol.SLASH) || namespace.endsWith(Symbol.COLON)) {
            return namespace + name.getLocalPart();
        }
        return namespace + Symbol.HASH + name.getLocalPart();
    }

    /**
     * Validates a name.
     *
     * @param value candidate XML or header name
     * @param field logical field name included in validation failures
     * @return validated non-blank single-line name
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
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
