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
package org.miaixz.bus.auth.metric.oauth2;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.metric.AuthMetric.Limits;
import org.miaixz.bus.auth.metric.AuthMetric.Response;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.net.url.UrlQuery;
import org.miaixz.bus.core.net.url.UrlQuery.EncodeMode;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Strictly decodes and encodes OAuth form/query parameters and standard wire errors.
 * <p>
 * Parsing rejects duplicate decoded names, empty segments, malformed percent escapes, malformed UTF-8, non-ASCII names,
 * control characters, and configured count or size overruns. Encoding delegates percent-encoding to Bus URL facilities
 * after enforcing the same bounded parameter model.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth2Codec {

    /**
     * OAuth wire member carrying the error key.
     */
    private static final String ERROR = "error";

    /**
     * OAuth wire member carrying the fixed safe error description.
     */
    private static final String ERROR_DESCRIPTION = "error_description";

    /**
     * OAuth wire member carrying client state.
     */
    private static final String STATE = "state";

    /**
     * Prevents construction of the stateless codec.
     */
    private OAuth2Codec() {
        // No initialization required.
    }

    /**
     * Parses one bounded application/x-www-form-urlencoded body.
     *
     * @param body   original body bytes
     * @param limits authentication input limits
     * @return immutable insertion-ordered parameter map
     */
    public static Map<String, String> parseForm(final byte[] body, final Limits limits) {
        final Limits bounds = Assert.notNull(limits, "Limits must be not null!");
        final byte[] input = body == null ? new byte[0] : body.clone();
        if (input.length > bounds.maxJsonBytes()) {
            reject();
        }
        return parse(decodeUtf8(input), bounds);
    }

    /**
     * Parses one bounded raw query without removing a path or question mark.
     *
     * @param rawQuery exact raw query text
     * @param limits   authentication input limits
     * @return immutable insertion-ordered parameter map
     */
    public static Map<String, String> parseQuery(final String rawQuery, final Limits limits) {
        final Limits bounds = Assert.notNull(limits, "Limits must be not null!");
        final String input = rawQuery == null ? Normal.EMPTY : rawQuery;
        validateText(input);
        if (input.getBytes(Charset.UTF_8).length > bounds.maxJsonBytes()) {
            reject();
        }
        return parse(input, bounds);
    }

    /**
     * Encodes bounded parameters as application/x-www-form-urlencoded bytes.
     *
     * @param parameters unique parameter values
     * @param limits     authentication input limits
     * @return UTF-8 encoded form body
     */
    public static byte[] encodeForm(final Map<String, String> parameters, final Limits limits) {
        return encode(parameters, limits, EncodeMode.FORM_URL_ENCODED).getBytes(Charset.UTF_8);
    }

    /**
     * Encodes bounded parameters as an RFC 3986 query.
     *
     * @param parameters unique parameter values
     * @param limits     authentication input limits
     * @return encoded query without a leading question mark
     */
    public static String encodeQuery(final Map<String, String> parameters, final Limits limits) {
        return encode(parameters, limits, EncodeMode.STRICT);
    }

    /**
     * Creates immutable standard OAuth error parameters from a fixed protocol error.
     *
     * @param error fixed OAuth protocol error
     * @param state optional client state
     * @return immutable error parameter map
     */
    public static Map<String, String> errorParameters(final ProtocolError error, final String state) {
        final ProtocolError selected = Assert.notNull(error, "OAuth protocol error must be not null!");
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put(ERROR, selected.getKey());
        result.put(ERROR_DESCRIPTION, selected.getValue());
        if (state != null) {
            validateText(state);
            result.put(STATE, state);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Encodes a redirect-safe standard OAuth error query.
     *
     * @param error  fixed OAuth protocol error
     * @param state  optional client state
     * @param limits authentication input limits
     * @return encoded error query
     */
    public static String errorQuery(final ProtocolError error, final String state, final Limits limits) {
        return encodeQuery(errorParameters(error, state), limits);
    }

    /**
     * Creates a no-store JSON OAuth error response without serializing exception data.
     *
     * @param error    fixed OAuth protocol error
     * @param state    optional client state
     * @param status   exact HTTP error status
     * @param provider product JSON provider
     * @param limits   authentication input limits
     * @return immutable HTTP response
     */
    public static Response errorResponse(
            final ProtocolError error,
            final String state,
            final int status,
            final JsonProvider provider,
            final Limits limits) {
        if (status < Http.Status.BAD_REQUEST || status > Http.Status.NETWORK_AUTHENTICATION_REQUIRED) {
            reject();
        }
        final JsonProvider json = Assert.notNull(provider, "JSON provider must be not null!");
        final Limits bounds = Assert.notNull(limits, "Limits must be not null!");
        final byte[] body = json.write(errorParameters(error, state));
        if (body == null || body.length == Normal._0 || body.length > bounds.maxJsonBytes()) {
            reject();
        }
        final Map<String, List<String>> headers = Map.of(
                Http.Header.CONTENT_TYPE,
                List.of(MediaType.APPLICATION_JSON),
                Http.Header.CACHE_CONTROL,
                List.of(Http.Cache.NO_STORE),
                Http.Header.PRAGMA,
                List.of(Http.Cache.NO_CACHE));
        return new Response(status, headers, body);
    }

    /**
     * Parses a validated text representation into unique parameters.
     *
     * @param input  raw form or query text
     * @param limits authentication input limits
     * @return immutable parameter map
     */
    private static Map<String, String> parse(final String input, final Limits limits) {
        if (input.isEmpty()) {
            return Map.of();
        }
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        int offset = Normal._0;
        while (offset <= input.length()) {
            final int separator = input.indexOf(Symbol.C_AND, offset);
            final int end = separator < Normal._0 ? input.length() : separator;
            if (end == offset || result.size() >= limits.maxParameters()) {
                reject();
            }
            final int equals = input.indexOf(Symbol.C_EQUAL, offset);
            if (equals < offset || equals >= end || input.indexOf(Symbol.C_EQUAL, equals + 1) >= Normal._0
                    && input.indexOf(Symbol.C_EQUAL, equals + 1) < end) {
                reject();
            }
            final String encodedName = input.substring(offset, equals);
            final String encodedValue = input.substring(equals + 1, end);
            final String name = decode(encodedName);
            final String value = decode(encodedValue);
            validateName(name);
            validateValue(value);
            bound(name, limits);
            bound(value, limits);
            if (result.putIfAbsent(name, value) != null) {
                reject();
            }
            if (separator < Normal._0) {
                break;
            }
            offset = separator + 1;
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Encodes already unique parameters through Bus URL query facilities.
     *
     * @param parameters parameter values
     * @param limits     authentication input limits
     * @param mode       exact Bus encoding mode
     * @return encoded parameters
     */
    private static String encode(final Map<String, String> parameters, final Limits limits, final EncodeMode mode) {
        final Limits bounds = Assert.notNull(limits, "Limits must be not null!");
        if (parameters == null || parameters.isEmpty()) {
            return Normal.EMPTY;
        }
        if (parameters.size() > bounds.maxParameters()) {
            reject();
        }
        final LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            validateName(name);
            validateValue(value);
            bound(name, bounds);
            bound(value, bounds);
            values.put(name, value);
        }
        final String encoded = UrlQuery.of(values, mode).build(Charset.UTF_8);
        if (encoded.getBytes(Charset.UTF_8).length > bounds.maxJsonBytes()) {
            reject();
        }
        return encoded;
    }

    /**
     * Decodes one component after validating percent-escaped UTF-8 byte runs.
     *
     * @param value encoded component
     * @return decoded component
     */
    private static String decode(final String value) {
        validatePercentUtf8(value);
        try {
            final String decoded = UrlDecoder.decodeStrict(value);
            validateText(decoded);
            return decoded;
        } catch (final IllegalArgumentException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

    /**
     * Strictly decodes original bytes as UTF-8.
     *
     * @param value original bytes
     * @return decoded text
     */
    private static String decodeUtf8(final byte[] value) {
        try {
            final CharBuffer decoded = Charset.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(value));
            final String result = decoded.toString();
            validateText(result);
            return result;
        } catch (final CharacterCodingException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

    /**
     * Validates every contiguous percent-escaped byte run as strict UTF-8.
     *
     * @param value encoded component
     */
    private static void validatePercentUtf8(final String value) {
        int index = Normal._0;
        while (index < value.length()) {
            if (value.charAt(index) != Symbol.C_PERCENT) {
                index++;
                continue;
            }
            final int start = index;
            int count = Normal._0;
            while (index < value.length() && value.charAt(index) == Symbol.C_PERCENT) {
                if (index + 2 >= value.length()) {
                    reject();
                }
                final int high = Character.digit(value.charAt(index + 1), Normal._16);
                final int low = Character.digit(value.charAt(index + 2), Normal._16);
                if (high < Normal._0 || low < Normal._0) {
                    reject();
                }
                count++;
                index += 3;
            }
            final byte[] bytes = new byte[count];
            for (int item = Normal._0; item < count; item++) {
                final int offset = start + item * 3;
                bytes[item] = (byte) ((Character.digit(value.charAt(offset + 1), Normal._16) << 4)
                        | Character.digit(value.charAt(offset + 2), Normal._16));
            }
            decodeUtf8(bytes);
        }
    }

    /**
     * Validates a decoded parameter name as non-empty visible ASCII without delimiters.
     *
     * @param name decoded parameter name
     */
    private static void validateName(final String name) {
        if (name == null || name.isEmpty()) {
            reject();
        }
        for (int index = Normal._0; index < name.length(); index++) {
            final char value = name.charAt(index);
            if (value < '!' || value > '~' || value == Symbol.C_AND || value == Symbol.C_EQUAL) {
                reject();
            }
        }
    }

    /**
     * Validates a decoded parameter value for safe text handling.
     *
     * @param value decoded parameter value
     */
    private static void validateValue(final String value) {
        if (value == null) {
            reject();
        }
        validateText(value);
    }

    /**
     * Rejects NUL, line breaks, and unpaired UTF-16 surrogates.
     *
     * @param value decoded text
     */
    private static void validateText(final String value) {
        for (int index = Normal._0; index < value.length(); index++) {
            final char current = value.charAt(index);
            if (current == Normal._0 || current == Symbol.C_CR || current == Symbol.C_LF) {
                reject();
            }
            if (Character.isHighSurrogate(current)) {
                if (index + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(index + 1))) {
                    reject();
                }
                index++;
            } else if (Character.isLowSurrogate(current)) {
                reject();
            }
        }
    }

    /**
     * Enforces the per-parameter UTF-8 byte bound.
     *
     * @param value  decoded parameter text
     * @param limits authentication input limits
     */
    private static void bound(final String value, final Limits limits) {
        if (value.getBytes(Charset.UTF_8).length > limits.maxParameterBytes()) {
            reject();
        }
    }

    /**
     * Rejects invalid codec input with the standard OAuth invalid-request error.
     */
    private static void reject() {
        throw new ProtocolException(ProtocolError.INVALID_REQUEST);
    }

}
