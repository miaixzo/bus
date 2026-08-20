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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.codec.Codec;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.net.url.UrlDecoder;

/**
 * Encodes and decodes the RFC 5849 HTTP Authorization header representation.
 *
 * @author Kimi Liu
 */
public final class OAuth1AuthorizationHeaderCodec implements Codec<List<OAuth1Parameter>, String> {

    /**
     * Creates a stateless RFC 5849 Authorization header codec.
     */
    public OAuth1AuthorizationHeaderCodec() {
        // No initialization required.
    }

    /**
     * Validates parameter location, cardinality, and control-character constraints.
     *
     * @param source parameter list
     * @return immutable detached parameter list
     */
    private static List<OAuth1Parameter> validate(final List<OAuth1Parameter> source) {
        Assert.notNull(source, "OAuth Authorization parameters must not be null");
        if (source.isEmpty()) {
            throw new ValidateException("OAuth Authorization parameters must not be empty");
        }
        final List<OAuth1Parameter> result = new ArrayList<>(source.size());
        final Set<String> seen = new HashSet<>();
        for (OAuth1Parameter parameter : source) {
            final OAuth1Parameter value = Assert.notNull(parameter, "OAuth Authorization parameter must not be null");
            if (!(OAuth1.REALM.equals(value.name()) || value.name().startsWith(OAuth1.PARAMETER_PREFIX))) {
                throw new ValidateException("Authorization header accepts only OAuth protocol parameters and realm");
            }
            if (controls(value.name()) || controls(value.value())) {
                throw new ValidateException("OAuth Authorization parameter must not contain control characters");
            }
            if (singleton(value.name()) && !seen.add(value.name())) {
                throw new ValidateException("OAuth Authorization parameter must not be duplicated: " + value.name());
            }
            result.add(value);
        }
        return List.copyOf(result);
    }

    /**
     * Identifies RFC 5849 Authorization parameters whose cardinality is at most one.
     *
     * @param name exact decoded parameter name
     * @return {@code true} for realm or a standard OAuth protocol parameter
     */
    private static boolean singleton(final String name) {
        return switch (name) {
            case OAuth1.REALM, OAuth1.Parameters.CONSUMER_KEY, OAuth1.Parameters.TOKEN, OAuth1.Parameters.SIGNATURE_METHOD, OAuth1.Parameters.SIGNATURE, OAuth1.Parameters.TIMESTAMP, OAuth1.Parameters.NONCE, OAuth1.Parameters.VERSION, OAuth1.Parameters.CALLBACK, OAuth1.Parameters.VERIFIER -> true;
            default -> false;
        };
    }

    /**
     * Percent-encodes one decoded RFC 5849 name or value using UTF-8.
     *
     * @param value decoded value
     * @return RFC 3986 unreserved encoding
     */
    static String percent(final String value) {
        return RFC3986.UNRESERVED.encode(value, Charset.UTF_8);
    }

    /**
     * Strictly percent-decodes one RFC 5849 component without plus-to-space conversion.
     *
     * @param value encoded component
     * @return decoded Unicode value
     */
    private static String decodePercent(final String value) {
        final String decoded = UrlDecoder.decodeStrict(value, Charset.UTF_8, false);
        if (controls(decoded)) {
            throw new ValidateException("OAuth Authorization parameter contains decoded control characters");
        }
        return decoded;
    }

    /**
     * Skips RFC HTTP optional whitespace.
     *
     * @param value complete field value
     * @param start initial cursor
     * @return first non-OWS cursor
     */
    private static int skipOws(final String value, final int start) {
        int cursor = start;
        while (cursor < value.length() && (value.charAt(cursor) == Symbol.C_SPACE || value.charAt(cursor) == '\t')) {
            cursor++;
        }
        return cursor;
    }

    /**
     * Detects ASCII control characters and DEL.
     *
     * @param value text to inspect
     * @return whether a forbidden control character exists
     */
    private static boolean controls(final String value) {
        for (int index = 0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current <= 0x1f || current == 0x7f) {
                return true;
            }
        }
        return false;
    }

    /**
     * Encodes ordered OAuth protocol parameters into an Authorization field value.
     *
     * @param data ordered decoded OAuth parameters and optional realm
     * @return complete OAuth Authorization field value
     */
    @Override
    public String encode(final List<OAuth1Parameter> data) {
        final List<OAuth1Parameter> parameters = validate(data);
        final StringBuilder result = new StringBuilder(OAuth1.AUTHORIZATION_SCHEME).append(Symbol.C_SPACE);
        for (int index = 0; index < parameters.size(); index++) {
            if (index > 0) {
                result.append(", ");
            }
            final OAuth1Parameter parameter = parameters.get(index);
            result.append(percent(parameter.name())).append("=\"").append(percent(parameter.value()))
                    .append(Symbol.C_DOUBLE_QUOTES);
        }
        return result.toString();
    }

    /**
     * Strictly decodes one OAuth Authorization field value.
     *
     * @param encoded complete Authorization field value
     * @return immutable ordered decoded protocol parameters
     */
    @Override
    public List<OAuth1Parameter> decode(final String encoded) {
        Assert.notBlank(encoded, "OAuth Authorization header must not be blank");
        final int separator = encoded.indexOf(Symbol.C_SPACE);
        if (separator <= 0 || !OAuth1.AUTHORIZATION_SCHEME.equalsIgnoreCase(encoded.substring(0, separator))) {
            throw new ValidateException("Authorization header must use the OAuth scheme");
        }
        int cursor = skipOws(encoded, separator + 1);
        if (cursor == encoded.length()) {
            throw new ValidateException("OAuth Authorization header must contain auth parameters");
        }
        final List<OAuth1Parameter> parameters = new ArrayList<>();
        while (cursor < encoded.length()) {
            final int equals = encoded.indexOf(Symbol.C_EQUAL, cursor);
            if (equals <= cursor) {
                throw new ValidateException("OAuth Authorization parameter is missing Symbol.C_EQUAL");
            }
            final String encodedName = encoded.substring(cursor, equals).trim();
            if (encodedName.isEmpty()) {
                throw new ValidateException("OAuth Authorization parameter name must not be empty");
            }
            cursor = skipOws(encoded, equals + 1);
            if (cursor >= encoded.length() || encoded.charAt(cursor) != Symbol.C_DOUBLE_QUOTES) {
                throw new ValidateException("OAuth Authorization parameter value must be quoted");
            }
            final int end = encoded.indexOf(Symbol.C_DOUBLE_QUOTES, cursor + 1);
            if (end < 0) {
                throw new ValidateException("OAuth Authorization parameter has an unterminated quoted value");
            }
            final String encodedValue = encoded.substring(cursor + 1, end);
            if (encodedValue.indexOf(Symbol.C_BACKSLASH) >= 0 || controls(encodedValue)) {
                throw new ValidateException("OAuth Authorization parameter contains an invalid quoted value");
            }
            final String name = decodePercent(encodedName);
            final String value = decodePercent(encodedValue);
            parameters.add(new OAuth1Parameter(name, value));
            cursor = skipOws(encoded, end + 1);
            if (cursor == encoded.length()) {
                break;
            }
            if (encoded.charAt(cursor) != Symbol.C_COMMA) {
                throw new ValidateException("OAuth Authorization parameters must be comma separated");
            }
            cursor = skipOws(encoded, cursor + 1);
            if (cursor == encoded.length()) {
                throw new ValidateException("OAuth Authorization header must not end with a comma");
            }
        }
        return validate(parameters);
    }

}
