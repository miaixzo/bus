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
package org.miaixz.bus.http;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.http.plugin.soap.SoapBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * A utility class for SOAP-related operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SoapX {

    /**
     * Creates a SOAP client with the default SOAP 1.2 protocol.
     *
     * @param url The URL of the web service.
     * @return a new {@link SoapBuilder} instance.
     */
    public static SoapBuilder of(final String url) {
        return SoapBuilder.of(url);
    }

    /**
     * Creates a SOAP client with a specified protocol.
     *
     * @param url      The URL of the web service.
     * @param protocol The SOAP protocol to use (e.g., SOAP 1.1 or 1.2).
     * @return a new {@link SoapBuilder} instance.
     */
    public static SoapBuilder of(final String url, final Protocol protocol) {
        return SoapBuilder.of(url, protocol);
    }

    /**
     * Creates a SOAP client with a specified protocol and namespace URI.
     *
     * @param url          The URL of the web service.
     * @param protocol     The SOAP protocol to use.
     * @param namespaceURI The namespace URI for the SOAP method.
     * @return a new {@link SoapBuilder} instance.
     */
    public static SoapBuilder of(final String url, final Protocol protocol, final String namespaceURI) {
        return SoapBuilder.of(url, protocol, namespaceURI);
    }

    /**
     * Converts a {@link SOAPMessage} to its string representation.
     *
     * @param message The SOAP message object.
     * @param pretty  Whether to format the output XML.
     * @return The SOAP XML as a string.
     */
    public static String toString(final SOAPMessage message, final boolean pretty) {
        return toString(message, pretty, Charset.UTF_8);
    }

    /**
     * Converts a {@link SOAPMessage} to its string representation with a specified character set.
     *
     * @param message The SOAP message object.
     * @param pretty  Whether to format the output XML.
     * @param charset The character set to use for encoding.
     * @return The SOAP XML as a string.
     */
    public static String toString(
            final SOAPMessage message,
            final boolean pretty,
            final java.nio.charset.Charset charset) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.writeTo(out);
        } catch (final SOAPException | IOException e) {
            throw new InternalException(e);
        }
        final String messageToString;
        try {
            messageToString = out.toString(charset.name());
        } catch (final UnsupportedEncodingException e) {
            throw new InternalException(e);
        }
        return pretty ? XmlKit.format(messageToString) : messageToString;
    }

}
