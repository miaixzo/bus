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
package org.miaixz.bus.auth.protocol.oauth1.codec;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.codec.Codec;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlDecoder;

/**
 * Encodes and decodes RFC 5849 parameter lists using strict UTF-8 form representation.
 * <p>
 * RFC 5849 uses the RFC 3986 percent-encoding rules for these values, so spaces become {@code %20}; the HTML form
 * convention that turns a space into {@code +} is deliberately not used.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth1FormCodec implements Codec<List<OAuth1Parameter>, byte[]> {

    /**
     * Creates a stateless RFC 5849 form-encoded parameter codec.
     */
    public OAuth1FormCodec() {
        // No initialization required.
    }

    /**
     * Converts wire bytes to strict ASCII text.
     *
     * @param encoded form body bytes
     * @return ASCII form text
     */
    private static String strictAscii(final byte[] encoded) {
        for (byte value : encoded) {
            if ((value & 0x80) != 0 || value <= 0x1f || value == 0x7f) {
                throw new ValidateException("OAuth 1.0 form body must contain printable ASCII wire bytes");
            }
        }
        return new String(encoded, Charset.US_ASCII);
    }

    /**
     * Validates the encoded alphabet and strictly decodes one UTF-8 component.
     *
     * @param value encoded component
     * @return decoded Unicode component
     */
    private static String decodeComponent(final String value) {
        for (int index = 0; index < value.length(); index++) {
            final char current = value.charAt(index);
            final boolean unreserved = current >= 'A' && current <= 'Z' || current >= 'a' && current <= 'z'
                    || current >= Symbol.C_ZERO && current <= Symbol.C_NINE || current == Symbol.C_MINUS
                    || current == Symbol.C_DOT || current == Symbol.C_UNDERLINE || current == Symbol.C_TILDE;
            if (!unreserved && current != Symbol.C_PERCENT) {
                throw new ValidateException("OAuth 1.0 form component contains an unescaped reserved character");
            }
        }
        try {
            return UrlDecoder.decodeStrict(value, Charset.UTF_8, false);
        } catch (final IllegalArgumentException cause) {
            throw new ValidateException("OAuth 1.0 form component has invalid percent encoding", cause);
        }
    }

    /**
     * Encodes an ordered parameter list without collapsing duplicates.
     *
     * @param data decoded ordered parameters
     * @return newly allocated ASCII-compatible UTF-8 form bytes
     */
    @Override
    public byte[] encode(final List<OAuth1Parameter> data) {
        Assert.notNull(data, "OAuth 1.0 form parameters must not be null");
        final StringBuilder result = new StringBuilder();
        for (int index = 0; index < data.size(); index++) {
            final OAuth1Parameter parameter = Assert
                    .notNull(data.get(index), "OAuth 1.0 form parameter must not be null");
            if (index > 0) {
                result.append(Symbol.C_AND);
            }
            result.append(OAuth1AuthorizationHeaderCodec.percent(parameter.name())).append(Symbol.C_EQUAL)
                    .append(OAuth1AuthorizationHeaderCodec.percent(parameter.value()));
        }
        return result.toString().getBytes(Charset.UTF_8);
    }

    /**
     * Strictly decodes an ordered form representation without treating plus as space.
     *
     * @param encoded complete form body bytes
     * @return immutable ordered decoded parameters
     */
    @Override
    public List<OAuth1Parameter> decode(final byte[] encoded) {
        Assert.notNull(encoded, "OAuth 1.0 form body must not be null");
        if (encoded.length == 0) {
            return List.of();
        }
        final String source = strictAscii(encoded);
        final String[] pairs = source.split(Symbol.AND, -1);
        final List<OAuth1Parameter> result = new ArrayList<>(pairs.length);
        for (String pair : pairs) {
            final int equals = pair.indexOf(Symbol.C_EQUAL);
            if (equals <= 0) {
                throw new ValidateException(
                        "OAuth 1.0 form parameter must contain a non-empty name and Symbol.C_EQUAL");
            }
            if (pair.indexOf(Symbol.C_EQUAL, equals + 1) >= 0) {
                throw new ValidateException("OAuth 1.0 form parameter delimiter must be percent encoded in values");
            }
            final String name = decodeComponent(pair.substring(0, equals));
            final String value = decodeComponent(pair.substring(equals + 1));
            result.add(new OAuth1Parameter(name, value));
        }
        return List.copyOf(result);
    }

}
