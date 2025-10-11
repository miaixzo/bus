/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.plugin.soap;

import jakarta.xml.soap.*;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.http.Httpz;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.SoapX;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A builder for creating and sending SOAP messages.
 * <p>
 * This class is used to construct a SOAP message and send it via an HTTP interface. The SOAP message is essentially an
 * XML text, which can be retrieved by calling the {@link #getString(boolean)} method.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SoapBuilder {

    /**
     * The SOAP protocol version (SOAP 1.1: text/xml, SOAP 1.2: application/soap+xml).
     */
    private final Protocol protocol;
    /**
     * The namespace URI to be applied to the method.
     */
    private final String namespaceURI;
    /**
     * A map to store header information.
     */
    private final Map<String, String> headers = new HashMap<>();
    /**
     * The URL of the web service.
     */
    private String url;
    /**
     * The SOAP message.
     */
    private SOAPMessage message;
    /**
     * The message factory for creating SOAP messages.
     */
    private MessageFactory factory;
    /**
     * The SOAP body element representing the method.
     */
    private SOAPBodyElement methodEle;
    /**
     * The default character encoding.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * Constructs a new {@code SoapBuilder} with the default SOAP 1.2 protocol.
     *
     * @param url The URL of the web service.
     */
    public SoapBuilder(final String url) {
        this(url, Protocol.SOAP_1_2);
    }

    /**
     * Constructs a new {@code SoapBuilder} with a specified protocol version.
     *
     * @param url      The URL of the web service.
     * @param protocol The protocol version, see {@link Protocol}.
     */
    public SoapBuilder(final String url, final Protocol protocol) {
        this(url, protocol, null);
    }

    /**
     * Constructs a new {@code SoapBuilder} with a specified protocol version and namespace URI.
     *
     * @param url          The URL of the web service.
     * @param protocol     The protocol version, see {@link Protocol}.
     * @param namespaceURI The namespace URI for the method.
     */
    public SoapBuilder(final String url, final Protocol protocol, final String namespaceURI) {
        this.url = url;
        this.namespaceURI = namespaceURI;
        this.protocol = protocol;
        init(protocol);
    }

    /**
     * Creates a new SOAP client with the default SOAP 1.2 protocol.
     *
     * @param url The URL of the web service.
     * @return A new {@code SoapBuilder} instance.
     */
    public static SoapBuilder of(final String url) {
        return new SoapBuilder(url);
    }

    /**
     * Creates a new SOAP client with a specified protocol.
     *
     * @param url      The URL of the web service.
     * @param protocol The protocol version, see {@link Protocol}.
     * @return A new {@code SoapBuilder} instance.
     */
    public static SoapBuilder of(final String url, final Protocol protocol) {
        return new SoapBuilder(url, protocol);
    }

    /**
     * Creates a new SOAP client with a specified protocol and namespace URI.
     *
     * @param url          The URL of the web service.
     * @param protocol     The protocol version, see {@link Protocol}.
     * @param namespaceURI The namespace URI for the method.
     * @return A new {@code SoapBuilder} instance.
     */
    public static SoapBuilder of(final String url, final Protocol protocol, final String namespaceURI) {
        return new SoapBuilder(url, protocol, namespaceURI);
    }

    /**
     * Sets a parameter for a method node.
     *
     * @param ele    The method node.
     * @param name   The parameter name.
     * @param value  The parameter value.
     * @param prefix The namespace prefix, or {@code null} for no prefix.
     * @return The child {@link SOAPElement}.
     */
    private static SOAPElement setParam(
            final SOAPElement ele,
            final String name,
            final Object value,
            final String prefix) {
        final SOAPElement childEle;
        try {
            if (StringKit.isNotBlank(prefix)) {
                childEle = ele.addChildElement(name, prefix);
            } else {
                childEle = ele.addChildElement(name);
            }
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }

        if (null != value) {
            if (value instanceof SOAPElement) {
                // Single child node.
                try {
                    ele.addChildElement((SOAPElement) value);
                } catch (final SOAPException e) {
                    throw new InternalException(e);
                }
            } else if (value instanceof Map) {
                // Multiple child nodes.
                for (final Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    setParam(childEle, StringKit.toStringOrNull(entry.getKey()), entry.getValue(), prefix);
                }
            } else {
                // Single value.
                childEle.setValue(value.toString());
            }
        }

        return childEle;
    }

    /**
     * Initializes the SOAP client.
     *
     * @param protocol The protocol version enum, see {@link Protocol}.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder init(final Protocol protocol) {
        try {
            this.factory = MessageFactory.newInstance(protocol.name);
            this.message = factory.createMessage();
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Resets the SOAP client for reuse. After resetting, you must call {@code setMethod} to specify a new request
     * method and {@code setParam} to redefine the parameters.
     *
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder reset() {
        try {
            this.message = factory.createMessage();
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }
        this.methodEle = null;
        return this;
    }

    /**
     * Sets the character encoding.
     *
     * @param charset The character encoding.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder charset(final java.nio.charset.Charset charset) {
        if (null != charset) {
            this.charset = charset;
            try {
                this.message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, charset.name());
                this.message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
            } catch (final SOAPException e) {
                // Ignore.
            }
        }
        return this;
    }

    /**
     * Sets the web service request URL.
     *
     * @param url The web service request URL.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setUrl(final String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets a header. If in override mode, it replaces the previous value; otherwise, it adds to the list of values.
     *
     * @param name       The header name.
     * @param value      The header value.
     * @param isOverride Whether to override an existing value.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder header(final String name, final String value, final boolean isOverride) {
        if (null != name && null != value) {
            final String values = headers.get(name.trim());
            if (isOverride || StringKit.isEmpty(values)) {
                headers.put(name.trim(), value);
            }
        }
        return this;
    }

    /**
     * Gets the headers.
     *
     * @return The header map.
     */
    public Map<String, String> headers() {
        this.headers.put(HTTP.CONTENT_TYPE, getXmlContentType());
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Clears all headers, including global headers.
     *
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder clearHeaders() {
        this.headers.clear();
        return this;
    }

    /**
     * Adds a SOAP header. The method returns a {@link SOAPHeaderElement} that can be used to set attributes and child
     * nodes.
     *
     * @param name           The name of the header element.
     * @param actorURI       The URI of the intermediary actor.
     * @param roleUri        The URI of the role.
     * @param mustUnderstand Whether the header is mandatory for the recipient.
     * @param relay          The relay attribute.
     * @return The {@link SOAPHeaderElement}.
     */
    public SOAPHeaderElement addSOAPHeader(
            final QName name,
            final String actorURI,
            final String roleUri,
            final Boolean mustUnderstand,
            final Boolean relay) {
        final SOAPHeaderElement ele = addSOAPHeader(name);
        try {
            if (StringKit.isNotBlank(roleUri)) {
                ele.setRole(roleUri);
            }
            if (null != relay) {
                ele.setRelay(relay);
            }
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }

        if (StringKit.isNotBlank(actorURI)) {
            ele.setActor(actorURI);
        }
        if (null != mustUnderstand) {
            ele.setMustUnderstand(mustUnderstand);
        }

        return ele;
    }

    /**
     * Adds a SOAP header. The method returns a {@link SOAPHeaderElement} that can be used to set attributes and child
     * nodes.
     *
     * @param localName The local name of the header element.
     * @return The {@link SOAPHeaderElement}.
     */
    public SOAPHeaderElement addSOAPHeader(final String localName) {
        return addSOAPHeader(new QName(localName));
    }

    /**
     * Adds a SOAP header with a value. The method returns a {@link SOAPHeaderElement} that can be used to set
     * attributes and child nodes.
     *
     * @param localName The local name of the header element.
     * @param value     The value of the header element.
     * @return The {@link SOAPHeaderElement}.
     */
    public SOAPHeaderElement addSOAPHeader(final String localName, final String value) {
        final SOAPHeaderElement soapHeaderElement = addSOAPHeader(localName);
        soapHeaderElement.setTextContent(value);
        return soapHeaderElement;
    }

    /**
     * Adds a SOAP header. The method returns a {@link SOAPHeaderElement} that can be used to set attributes and child
     * nodes.
     *
     * @param name The qualified name of the header element.
     * @return The {@link SOAPHeaderElement}.
     */
    public SOAPHeaderElement addSOAPHeader(final QName name) {
        final SOAPHeaderElement ele;
        try {
            ele = this.message.getSOAPHeader().addHeaderElement(name);
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }
        return ele;
    }

    /**
     * Sets the request method.
     *
     * @param name            The name and namespace of the method.
     * @param params          The parameters.
     * @param useMethodPrefix Whether to use the method's namespace prefix.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setMethod(final Name name, final Map<String, Object> params, final boolean useMethodPrefix) {
        return setMethod(new QName(name.getURI(), name.getLocalName(), name.getPrefix()), params, useMethodPrefix);
    }

    /**
     * Sets the request method.
     *
     * @param name            The name and namespace of the method.
     * @param params          The parameters.
     * @param useMethodPrefix Whether to use the method's namespace prefix.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setMethod(final QName name, final Map<String, Object> params, final boolean useMethodPrefix) {
        setMethod(name);
        final String prefix = useMethodPrefix ? name.getPrefix() : null;
        final SOAPBodyElement methodEle = this.methodEle;
        for (final Entry<String, Object> entry : MapKit.wrap(params)) {
            setParam(methodEle, entry.getKey(), entry.getValue(), prefix);
        }

        return this;
    }

    /**
     * Sets the request method. The method name automatically recognizes a prefix, separated by a colon. When a prefix
     * is recognized, an xmlns attribute is automatically added, associated with the default namespace URI.
     *
     * @param methodName The method name.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setMethod(final String methodName) {
        return setMethod(methodName, ObjectKit.defaultIfNull(this.namespaceURI, XMLConstants.NULL_NS_URI));
    }

    /**
     * Sets the request method. The method name automatically recognizes a prefix, separated by a colon. When a prefix
     * is recognized, an xmlns attribute is automatically added, associated with the given namespace URI.
     *
     * @param methodName   The method name (with or without a prefix).
     * @param namespaceURI The namespace URI.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setMethod(final String methodName, final String namespaceURI) {
        final List<String> methodNameList = StringKit.split(methodName, Symbol.COLON);
        final QName qName;
        if (2 == methodNameList.size()) {
            qName = new QName(namespaceURI, methodNameList.get(1), methodNameList.get(0));
        } else {
            qName = new QName(namespaceURI, methodName);
        }
        return setMethod(qName);
    }

    /**
     * Sets the request method.
     *
     * @param name The name and namespace of the method.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setMethod(final QName name) {
        try {
            this.methodEle = this.message.getSOAPBody().addBodyElement(name);
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }

        return this;
    }

    /**
     * Sets a method parameter, using the method's prefix.
     *
     * @param name  The parameter name.
     * @param value The parameter value, which can be a string, map, or {@link SOAPElement}.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setParam(final String name, final Object value) {
        return setParam(name, value, true);
    }

    /**
     * Sets a method parameter.
     *
     * @param name            The parameter name.
     * @param value           The parameter value, which can be a string, map, or {@link SOAPElement}.
     * @param useMethodPrefix Whether to use the method's namespace prefix.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setParam(final String name, final Object value, final boolean useMethodPrefix) {
        setParam(this.methodEle, name, value, useMethodPrefix ? this.methodEle.getPrefix() : null);
        return this;
    }

    /**
     * Sets multiple parameters, using the method's prefix.
     *
     * @param params The list of parameters.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setParams(final Map<String, Object> params) {
        return setParams(params, true);
    }

    /**
     * Sets multiple parameters.
     *
     * @param params          The list of parameters.
     * @param useMethodPrefix Whether to use the method's namespace prefix.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder setParams(final Map<String, Object> params, final boolean useMethodPrefix) {
        for (final Entry<String, Object> entry : MapKit.wrap(params)) {
            setParam(entry.getKey(), entry.getValue(), useMethodPrefix);
        }
        return this;
    }

    /**
     * Gets the method element, which can be used to create child nodes.
     *
     * @return The {@link SOAPBodyElement}.
     */
    public SOAPBodyElement getMethodEle() {
        return this.methodEle;
    }

    /**
     * Gets the SOAP message object.
     *
     * @return The {@link SOAPMessage}.
     */
    public SOAPMessage getMessage() {
        return this.message;
    }

    /**
     * Gets the SOAP request message as a string.
     *
     * @param pretty Whether to format the XML.
     * @return The message string.
     */
    public String getString(final boolean pretty) {
        return SoapX.toString(this.message, pretty, this.charset);
    }

    /**
     * Writes the XML content of the SOAP message to an output stream.
     *
     * @param out The output stream.
     * @return This {@code SoapBuilder} instance.
     */
    public SoapBuilder write(final OutputStream out) {
        try {
            this.message.writeTo(out);
        } catch (final SOAPException | IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Executes the web service request by sending the SOAP content.
     *
     * @return The resulting {@link SOAPMessage}.
     */
    public SOAPMessage sendForMessage() {
        final Response res = sendForResponse();
        final MimeHeaders headers = new MimeHeaders();
        for (final Entry<String, List<String>> entry : res.headers().toMultimap().entrySet()) {
            if (StringKit.isNotEmpty(entry.getKey())) {
                headers.setHeader(entry.getKey(), CollKit.get(entry.getValue(), 0));
            }
        }
        try {
            return this.factory.createMessage(headers, res.body().byteStream());
        } catch (final IOException | SOAPException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(res);
        }
    }

    /**
     * Executes the web service request by sending the SOAP content.
     *
     * @return The result as a string.
     */
    public String send() {
        return send(false);
    }

    /**
     * Executes the web service request by sending the SOAP content.
     *
     * @param pretty Whether to format the result.
     * @return The result as a string.
     */
    public String send(final boolean pretty) {
        final String body;
        try {
            body = sendForResponse().body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pretty ? XmlKit.format(body) : body;
    }

    /**
     * Sends the request and gets an asynchronous response.
     *
     * @return The response object.
     */
    public Response sendForResponse() {
        try {
            return Httpz.post().url(this.url).addHeader(this.headers).addParam(getString(false)).build().execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the Content-Type of the request, with character encoding information appended.
     *
     * @return The Content-Type of the request.
     */
    private String getXmlContentType() {
        switch (this.protocol) {
            case SOAP_1_1:
                return MediaType.TEXT_XML + ";charset=" + this.charset.toString();

            case SOAP_1_2:
                return MediaType.APPLICATION_SOAP_XML + ";charset=" + this.charset.toString();

            default:
                throw new InternalException("Unsupported protocol: {}", this.protocol);
        }
    }

}
