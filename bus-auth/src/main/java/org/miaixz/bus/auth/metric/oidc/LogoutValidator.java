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
package org.miaixz.bus.auth.metric.oidc;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.AuthMetric.TransportPolicy;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.OIDC.LogoutRequest;
import org.miaixz.bus.auth.metric.OIDC.LogoutResponse;
import org.miaixz.bus.auth.metric.jwt.JWTParser;
import org.miaixz.bus.auth.metric.shared.validation.UriValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlQuery;
import org.miaixz.bus.core.net.url.UrlQuery.EncodeMode;

/**
 * Validates provider-side RP-Initiated Logout inputs and constructs relying-party logout requests. ID Token hints are
 * parsed by the shared strict JWT parser before use, post-logout redirects use exact registered-URI equality, and the
 * outbound end-session endpoint is constrained by the same closed HTTPS transport policy as other OIDC endpoints.
 *
 * @author Kimi Liu
 */
public final class LogoutValidator {

    /**
     * ID Token hint parameter.
     */
    private static final String ID_TOKEN_HINT = "id_token_hint";

    /**
     * Post-logout redirect parameter.
     */
    private static final String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";

    /**
     * State parameter.
     */
    private static final String STATE = "state";

    /**
     * Authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one logout validator.
     *
     * @param runtime authentication runtime
     */
    public LogoutValidator(final Runtime runtime) {
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
    }

    /**
     * Validates a provider-side logout request and preserves exact state.
     *
     * @param invocation          operation context
     * @param request             logout request
     * @param registeredRedirects exact registered post-logout redirects
     * @return stage containing the validated redirect result
     */
    public CompletionStage<LogoutResponse> provider(
            final Invocation invocation,
            final LogoutRequest request,
            final Set<URI> registeredRedirects) {
        Assert.notNull(invocation, "Invocation must be not null!");
        final LogoutRequest input = Assert.notNull(request, "Logout request must be not null!");
        final Set<URI> redirects = Set
                .copyOf(Assert.notNull(registeredRedirects, "Registered post-logout redirects must be not null!"));
        parse(input.idTokenHint());
        final URI redirect = input.postLogoutRedirectUri();
        if (redirect != null && (!redirects.contains(redirect) || !redirect.equals(UriValidator.absolute(redirect)))) {
            return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_REQUEST));
        }
        return CompletableFuture.completedFuture(new LogoutResponse(redirect, input.state()));
    }

    /**
     * Constructs a relying-party logout URI from validated exact values.
     *
     * @param invocation operation context
     * @param endpoint   validated end-session endpoint
     * @param request    logout request
     * @param policy     strict HTTPS transport policy
     * @return stage containing the outbound logout URI
     */
    public CompletionStage<LogoutResponse> relyingParty(
            final Invocation invocation,
            final URI endpoint,
            final LogoutRequest request,
            final TransportPolicy policy) {
        Assert.notNull(invocation, "Invocation must be not null!");
        final LogoutRequest input = Assert.notNull(request, "Logout request must be not null!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        final URI target = UriValidator.transport(UriValidator.https(endpoint), transportPolicy);
        parse(input.idTokenHint());
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        parameters.put(ID_TOKEN_HINT, input.idTokenHint());
        if (input.postLogoutRedirectUri() != null) {
            parameters.put(
                    POST_LOGOUT_REDIRECT_URI,
                    UriValidator.absolute(input.postLogoutRedirectUri()).toASCIIString());
        }
        if (input.state() != null) {
            if (input.state().isBlank()
                    || input.state().getBytes(Charset.UTF_8).length > runtime.limits().maxParameterBytes()) {
                return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_REQUEST));
            }
            parameters.put(STATE, input.state());
        }
        final String query = UrlQuery.of(parameters, EncodeMode.STRICT).build(Charset.UTF_8);
        if (query.getBytes(Charset.UTF_8).length > runtime.limits().maxJsonBytes()) {
            return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_REQUEST));
        }
        final String separator = target.getRawQuery() == null ? Symbol.QUESTION_MARK : Symbol.AND;
        final URI requestUri;
        try {
            requestUri = URI.create(target.toASCIIString() + separator + query);
        } catch (final IllegalArgumentException failure) {
            return CompletableFuture.failedFuture(
                    new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                            ProtocolError.INVALID_REQUEST.getValue(), failure));
        }
        return CompletableFuture.completedFuture(new LogoutResponse(requestUri, input.state()));
    }

    /**
     * Strictly parses an ID Token hint without exposing its claims.
     *
     * @param token ID Token hint
     */
    private void parse(final String token) {
        try {
            JWTParser.parse(
                    Assert.notBlank(token, "ID Token hint must be not blank!"),
                    runtime.json(),
                    runtime.limits());
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

}
