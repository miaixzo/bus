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
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
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
     * HTTP authentication scheme emitted before the credential token.
     */
    private final String scheme;

    /**
     * Encoded credential token; this value must never be exposed through logs.
     */
    private final String token;

    /**
     * Creates an authentication header generator.
     *
     * @param scheme authentication scheme
     * @param token  encoded credential token
     */
    private HttpAuth(final String scheme, final String token) {
        this.scheme = scheme;
        this.token = token;
    }

    /**
     * Creates basic authentication.
     *
     * @param username non-blank, single-line user name
     * @param password single-line password, which may be empty
     * @return Basic authentication generator using UTF-8 credentials
     * @throws ValidateException if either credential component is invalid
     */
    public static HttpAuth basic(final String username, final String password) {
        final String user = validateUsername(username);
        final String pass = validatePassword(password);
        return new HttpAuth(Builder.HTTP_AUTH_BASIC, Base64.encode(user + Symbol.C_COLON + pass, Charset.UTF_8));
    }

    /**
     * Applies Authorization to a headers snapshot.
     *
     * @param headers source headers to copy and augment
     * @return immutable headers with {@code Authorization} replaced by this credential
     * @throws ValidateException if {@code headers} is {@code null}
     */
    public Headers apply(final Headers headers) {
        return require(headers, "Headers").with(Http.Header.AUTHORIZATION, value());
    }

    /**
     * Applies Proxy-Authorization to a headers snapshot.
     *
     * @param headers source headers to copy and augment
     * @return immutable headers with {@code Proxy-Authorization} replaced by this credential
     * @throws ValidateException if {@code headers} is {@code null}
     */
    public Headers applyProxy(final Headers headers) {
        return require(headers, "Headers").with(Http.Header.PROXY_AUTHORIZATION, value());
    }

    /**
     * Builds an authenticated request.
     *
     * @param request   request to copy and augment
     * @param challenge Basic challenge that selects origin or proxy authorization
     * @return request copy containing the appropriate authorization header
     * @throws ProtocolException if the challenge does not use the Basic scheme
     * @throws ValidateException if the request or challenge is {@code null}
     */
    public HttpRequest authenticate(final HttpRequest request, final Challenge challenge) {
        final HttpRequest current = require(request, "Request");
        final Challenge currentChallenge = require(challenge, "Challenge");
        if (!Builder.HTTP_AUTH_BASIC_LOWER.equals(currentChallenge.scheme())) {
            throw new ProtocolException("Unsupported authentication scheme");
        }
        final String header = proxy(currentChallenge) ? Http.Header.PROXY_AUTHORIZATION : Http.Header.AUTHORIZATION;
        return current.toBuilder().headers(current.headers().with(header, value())).build();
    }

    /**
     * Returns the header value.
     *
     * @return authentication scheme, one space, and the encoded credential token
     */
    public String value() {
        return scheme + Symbol.SPACE + token;
    }

    /**
     * Returns a redacted header value for logs and metrics.
     *
     * @return authentication scheme followed by a stable fingerprint instead of the credential token
     */
    public String redactedValue() {
        return scheme + Symbol.SPACE + Tags.redact(token);
    }

    /**
     * Returns the redacted authorization header value.
     *
     * @return safe redacted representation suitable for logs and metrics
     */
    @Override
    public String toString() {
        return redactedValue();
    }

    /**
     * Returns whether the challenge targets proxy authentication.
     *
     * @param challenge parsed challenge whose parameters are inspected
     * @return {@code true} when its {@code proxy} parameter equals {@code true} ignoring case
     */
    private static boolean proxy(final Challenge challenge) {
        return "true".equalsIgnoreCase(challenge.parameters().get("proxy"));
    }

    /**
     * Validates a username.
     *
     * @param username user name to validate
     * @return unchanged non-blank, single-line user name
     * @throws ValidateException if the user name is blank or contains a line break
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
     * @param password password to validate
     * @return unchanged single-line password, including an empty password
     * @throws ValidateException if the password is {@code null} or contains a line break
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
     * @param value collaborator reference to validate
     * @param name  logical collaborator name included in the validation error
     * @param <T>   collaborator type
     * @return validated non-null collaborator
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
