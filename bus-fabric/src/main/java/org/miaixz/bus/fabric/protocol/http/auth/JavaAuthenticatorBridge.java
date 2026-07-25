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

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Bridge from JDK {@link Authenticator} to {@link HttpAuthenticator}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class JavaAuthenticatorBridge implements HttpAuthenticator {

    /**
     * JDK authenticator used only to obtain credentials for the current challenge.
     */
    private final Authenticator authenticator;

    /**
     * Creates a bridge around a JDK authenticator without taking ownership of credential storage.
     *
     * @param authenticator non-null JDK credential provider borrowed by the bridge
     */
    private JavaAuthenticatorBridge(final Authenticator authenticator) {
        this.authenticator = require(authenticator, "Java authenticator");
    }

    /**
     * Wraps a JDK authenticator.
     *
     * @param authenticator JDK credential provider borrowed by the bridge
     * @return HTTP authenticator backed by the supplied credential provider
     * @throws ValidateException if {@code authenticator} is {@code null}
     */
    public static JavaAuthenticatorBridge of(final Authenticator authenticator) {
        return new JavaAuthenticatorBridge(authenticator);
    }

    /**
     * Uses the wrapped JDK authenticator to obtain credentials for the first response challenge and constructs a Basic
     * authentication retry.
     *
     * @param request  challenged request
     * @param response challenged response
     * @return authenticated request, or {@code null} when no challenge or credentials are available
     * @throws ValidateException if {@code request} or {@code response} is {@code null}
     * @throws ProtocolException if the request URL cannot be converted for the JDK authenticator
     */
    @Override
    public HttpRequest authenticate(final HttpRequest request, final HttpResponse response) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpResponse challenged = require(response, "HTTP response");
        final List<Challenge> challenges = challenged.challenges();
        if (challenges.isEmpty()) {
            return null;
        }
        final boolean proxy = challenged.code() == Http.Status.PROXY_AUTHENTICATION_REQUIRED;
        final Challenge challenge = challenges.getFirst();
        final PasswordAuthentication authentication = requestPasswordAuthentication(current, challenge, proxy);
        if (authentication == null) {
            return null;
        }
        return HttpAuth.basic(authentication.getUserName(), new String(authentication.getPassword()))
                .authenticate(current, HttpAuthenticator.challenge(challenge, proxy));
    }

    /**
     * Builds the JDK authentication request from fabric request and challenge metadata.
     *
     * @param request   challenged request supplying host, port, scheme, and URL
     * @param challenge first response challenge supplying realm and authentication scheme
     * @param proxy     {@code true} to request proxy credentials; {@code false} for origin-server credentials
     * @return credentials, or {@code null} when the authenticator declines
     * @throws ProtocolException if the request URL cannot be converted to a JDK URL
     */
    private PasswordAuthentication requestPasswordAuthentication(
            final HttpRequest request,
            final Challenge challenge,
            final boolean proxy) {
        try {
            return Authenticator.requestPasswordAuthentication(
                    authenticator,
                    request.url().host(),
                    null,
                    request.url().port(),
                    request.url().scheme(),
                    challenge.realm(),
                    challenge.scheme(),
                    request.url().toUri().toURL(),
                    proxy ? Authenticator.RequestorType.PROXY : Authenticator.RequestorType.SERVER);
        } catch (final MalformedURLException e) {
            throw new ProtocolException("Invalid authentication URL", e);
        }
    }

    /**
     * Validates bridge inputs before calling the JDK authentication API.
     *
     * @param value reference to validate
     * @param name  field name used in validation messages
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
