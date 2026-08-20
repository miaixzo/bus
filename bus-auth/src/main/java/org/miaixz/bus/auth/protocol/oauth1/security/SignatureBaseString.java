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
package org.miaixz.bus.auth.protocol.oauth1.security;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.auth.protocol.oauth1.OAuth1;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Holds the normalized RFC 5849 signature base string for one HTTP request.
 *
 * @author Kimi Liu
 */
public final class SignatureBaseString {

    /**
     * Fully normalized signature base string.
     */
    private final String value;

    /**
     * Creates a redaction-aware immutable base string value.
     *
     * @param value normalized base string
     */
    private SignatureBaseString(final String value) {
        this.value = Assert.notBlank(value, "OAuth 1.0 signature base string must not be blank");
    }

    /**
     * Creates a signature base string according to RFC 5849 section 3.4.1.
     *
     * @param method     exact HTTP wire method
     * @param url        exact request URL containing decoded query parameters
     * @param parameters decoded header and entity parameters participating in the signature
     * @return immutable normalized signature base string
     */
    public static SignatureBaseString create(
            final Http.Method method,
            final UnoUrl url,
            final List<OAuth1Parameter> parameters) {
        Assert.notNull(method, "OAuth 1.0 signature HTTP method must not be null");
        Assert.notNull(url, "OAuth 1.0 signature URL must not be null");
        Assert.notNull(parameters, "OAuth 1.0 signature parameters must not be null");
        if (method == Http.Method.ALL || method == Http.Method.NONE || method == Http.Method.BEFORE
                || method == Http.Method.AFTER) {
            throw new ValidateException("OAuth 1.0 signature requires an HTTP wire method");
        }
        final List<EncodedParameter> normalized = new ArrayList<>(url.querySize() + parameters.size());
        for (int index = 0; index < url.querySize(); index++) {
            normalized.add(encode(url.queryParameterName(index), url.queryParameterValue(index)));
        }
        for (OAuth1Parameter parameter : parameters) {
            final OAuth1Parameter item = Assert.notNull(parameter, "OAuth 1.0 signature parameter must not be null");
            if (!OAuth1.Parameters.SIGNATURE.equals(item.name()) && !"realm".equals(item.name())) {
                normalized.add(encode(item.name(), item.value()));
            }
        }
        normalized.sort(Comparator.comparing(EncodedParameter::name).thenComparing(EncodedParameter::value));
        final StringBuilder parameterString = new StringBuilder();
        for (int index = 0; index < normalized.size(); index++) {
            if (index > 0) {
                parameterString.append(Symbol.C_AND);
            }
            parameterString.append(normalized.get(index).name()).append(Symbol.C_EQUAL)
                    .append(normalized.get(index).value());
        }
        final String baseUri = baseUri(url);
        final String base = method.name().toUpperCase(Locale.ROOT) + Symbol.C_AND + percent(baseUri) + Symbol.C_AND
                + percent(parameterString.toString());
        return new SignatureBaseString(base);
    }

    /**
     * Encodes one decoded parameter pair before sorting.
     *
     * @param name  decoded parameter name
     * @param value decoded parameter value
     * @return encoded pair
     */
    private static EncodedParameter encode(final String name, final String value) {
        return new EncodedParameter(percent(name), percent(value));
    }

    /**
     * Applies the RFC 5849 UTF-8 percent-encoding function.
     *
     * @param value decoded value
     * @return percent-encoded value
     */
    private static String percent(final String value) {
        return RFC3986.UNRESERVED.encode(value, Charset.UTF_8);
    }

    /**
     * Produces the RFC 5849 base string URI without query, fragment, credentials, or default port.
     *
     * @param url parsed request URL
     * @return normalized base string URI
     */
    private static String baseUri(final UnoUrl url) {
        final String scheme = url.scheme().toLowerCase(Locale.ROOT);
        String host = url.host().toLowerCase(Locale.ROOT);
        if (host.indexOf(Symbol.C_COLON) >= 0 && !host.startsWith(Symbol.BRACKET_LEFT)) {
            host = Symbol.C_BRACKET_LEFT + host + Symbol.C_BRACKET_RIGHT;
        }
        final int port = url.port();
        final boolean defaultPort = port == UnoUrl.defaultPort(scheme);
        final URI uri = url.uri();
        final String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? Symbol.SLASH : uri.getRawPath();
        return scheme + "://" + host + (defaultPort ? Normal.EMPTY : Symbol.COLON + port) + path;
    }

    /**
     * Returns the normalized value for immediate cryptographic signing.
     *
     * @return RFC 5849 signature base string
     */
    public String value() {
        return value;
    }

    /**
     * Returns a redacted diagnostic representation.
     *
     * @return redacted type label
     */
    @Override
    public String toString() {
        return "SignatureBaseString[value=[REDACTED]]";
    }

    /**
     * Holds an already encoded sortable parameter pair.
     *
     * @param name  percent-encoded name
     * @param value percent-encoded value
     */
    private record EncodedParameter(String name, String value) {

    }

}
