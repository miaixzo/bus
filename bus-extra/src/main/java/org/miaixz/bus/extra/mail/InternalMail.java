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
package org.miaixz.bus.extra.mail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An internal utility class for mail-related operations, such as parsing addresses and encoding text. This class is not
 * intended for public use and provides helper methods for the mail API.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class InternalMail {

    /**
     * Parses an array of email address strings into an array of {@link InternetAddress} objects. Each string in the
     * input array can contain multiple addresses separated by standard delimiters.
     *
     * @param addrStrs An array of email address strings.
     * @param charset  The character set to use for encoding personal names (e.g., for non-ASCII characters).
     * @return An array of {@link InternetAddress} objects.
     */
    public static InternetAddress[] parseAddressFromStrs(final String[] addrStrs, final Charset charset) {
        if (ArrayKit.isEmpty(addrStrs)) {
            return new InternetAddress[0];
        }
        final List<InternetAddress> resultList = new ArrayList<>(addrStrs.length);
        InternetAddress[] addrs;
        for (final String text : addrStrs) {
            addrs = parseAddress(text, charset);
            if (ArrayKit.isNotEmpty(addrs)) {
                Collections.addAll(resultList, addrs);
            }
        }
        return resultList.toArray(new InternetAddress[0]);
    }

    /**
     * Parses an address string and returns the first valid {@link InternetAddress} found.
     *
     * @param address The address string to parse.
     * @param charset The character set to use for encoding the personal name. If null, the system default is used.
     * @return The first parsed {@link InternetAddress}.
     * @throws InternalException if parsing fails.
     */
    public static InternetAddress parseFirstAddress(final String address, final Charset charset) {
        final InternetAddress[] internetAddresses = parseAddress(address, charset);
        if (ArrayKit.isEmpty(internetAddresses)) {
            try {
                return new InternetAddress(address);
            } catch (final AddressException e) {
                throw new InternalException(e);
            }
        }
        return internetAddresses[0];
    }

    /**
     * Parses a single address string, which may contain multiple addresses separated by spaces, commas, or semicolons,
     * into an array of {@link InternetAddress} objects.
     *
     * @param address The address string to parse.
     * @param charset The character set to use for encoding personal names. If null, the system default is used.
     * @return An array of {@link InternetAddress} objects.
     * @throws InternalException if parsing fails.
     */
    public static InternetAddress[] parseAddress(final String address, final Charset charset) {
        if (StringKit.isBlank(address)) {
            return new InternetAddress[0];
        }
        final InternetAddress[] addresses;
        try {
            addresses = InternetAddress.parse(address);
        } catch (final AddressException e) {
            throw new InternalException(e);
        }
        // Encode personal names
        if (ArrayKit.isNotEmpty(addresses)) {
            final String charsetStr = ObjectKit.apply(charset, Charset::name);
            for (final InternetAddress internetAddress : addresses) {
                try {
                    internetAddress.setPersonal(internetAddress.getPersonal(), charsetStr);
                } catch (final UnsupportedEncodingException e) {
                    throw new InternalException(e);
                }
            }
        }

        return addresses;
    }

    /**
     * Encodes a string with non-ASCII characters for use in email headers, using MIME encoding. If encoding fails, the
     * original string is returned.
     *
     * @param text    The text to be encoded.
     * @param charset The character set to use for encoding.
     * @return The encoded string.
     */
    public static String encodeText(final String text, final Charset charset) {
        if (StringKit.isNotBlank(text)) {
            try {
                return MimeUtility.encodeText(text, charset.name(), null);
            } catch (final UnsupportedEncodingException e) {
                // Ignore and return the original string if encoding fails
            }
        }
        return text;
    }

}
