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
package org.miaixz.bus.fabric.protocol.http.auth;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.observe.tags.Tags;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * HTTP authentication header generator.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpAuth {

    /**
     * Basic scheme.
     */
    private static final String BASIC = "Basic";

    /**
     * Lower-case basic scheme.
     */
    private static final String BASIC_LOWER = "basic";

    /**
     * Authentication scheme.
     */
    private final String scheme;

    /**
     * Encoded credential token.
     */
    private final String token;

    /**
     * Creates an authentication header generator.
     *
     * @param scheme authentication scheme
     * @param token  encoded token
     */
    private HttpAuth(final String scheme, final String token) {
        this.scheme = scheme;
        this.token = token;
    }

    /**
     * Creates basic authentication.
     *
     * @param username username
     * @param password password
     * @return authentication generator
     */
    public static HttpAuth basic(final String username, final String password) {
        final String user = validateUsername(username);
        final String pass = validatePassword(password);
        return new HttpAuth(BASIC, Base64.encode(user + Symbol.C_COLON + pass, Charset.UTF_8));
    }

    /**
     * Applies Authorization to a headers snapshot.
     *
     * @param headers headers
     * @return updated headers
     */
    public Headers apply(final Headers headers) {
        return require(headers, "Headers").with(HTTP.AUTHORIZATION, value());
    }

    /**
     * Applies Proxy-Authorization to a headers snapshot.
     *
     * @param headers headers
     * @return updated headers
     */
    public Headers applyProxy(final Headers headers) {
        return require(headers, "Headers").with(HTTP.PROXY_AUTHORIZATION, value());
    }

    /**
     * Builds an authenticated request.
     *
     * @param request   request
     * @param challenge challenge
     * @return authenticated request
     */
    public HttpRequest authenticate(final HttpRequest request, final Challenge challenge) {
        final HttpRequest current = require(request, "Request");
        final Challenge currentChallenge = require(challenge, "Challenge");
        if (!BASIC_LOWER.equals(currentChallenge.scheme())) {
            throw new ProtocolException("Unsupported authentication scheme");
        }
        final String header = proxy(currentChallenge) ? HTTP.PROXY_AUTHORIZATION : HTTP.AUTHORIZATION;
        return current.toBuilder().headers(current.headers().with(header, value())).build();
    }

    /**
     * Returns the header value.
     *
     * @return header value
     */
    public String value() {
        return scheme + Symbol.SPACE + token;
    }

    /**
     * Returns a redacted header value for logs and metrics.
     *
     * @return redacted header value
     */
    public String redactedValue() {
        return scheme + Symbol.SPACE + Tags.redact(token);
    }

    @Override
    public String toString() {
        return redactedValue();
    }

    /**
     * Returns whether the challenge targets proxy authentication.
     *
     * @param challenge challenge
     * @return true when proxy authentication is requested
     */
    private static boolean proxy(final Challenge challenge) {
        return "true".equalsIgnoreCase(challenge.parameters().get("proxy"));
    }

    /**
     * Validates a username.
     *
     * @param username username
     * @return username
     */
    private static String validateUsername(final String username) {
        Assert.isFalse(
                StringKit.isBlank(username) || StringKit.containsAny(username, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Username must be non-blank and single-line"));
        return username;
    }

    /**
     * Validates a password.
     *
     * @param password password
     * @return password
     */
    private static String validatePassword(final String password) {
        final String current = Assert.notNull(password, () -> new ValidateException("Password must be single-line"));
        Assert.isFalse(
                StringKit.containsAny(current, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Password must be single-line"));
        return current;
    }

    /**
     * Validates a required collaborator.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
