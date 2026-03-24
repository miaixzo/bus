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
 * @since Java 21+
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
