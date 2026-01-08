/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
