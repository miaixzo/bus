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
package org.miaixz.bus.auth.protocol.oidc;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Callback.Kind;
import org.miaixz.bus.auth.Callback.Mode;
import org.miaixz.bus.auth.Callback.Outbound;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.guard.UriValidator;
import org.miaixz.bus.auth.protocol.jwt.JWTParser;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.protocol.oidc.OIDC.LogoutRequest;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlQuery;
import org.miaixz.bus.core.net.url.UrlQuery.EncodeMode;
import org.miaixz.bus.extra.json.JsonProvider;

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
     * JSON provider used by the shared strict JWT parser.
     */
    private final JsonProvider json;

    /**
     * Closed bounds applied to ID Token hints and generated query values.
     */
    private final Limits limits;

    /**
     * Creates one logout validator.
     *
     * @param json   non-null JSON provider
     * @param limits non-null parser and allocation limits
     * @throws ValidateException if either collaborator is {@code null}
     */
    public LogoutValidator(final JsonProvider json, final Limits limits) {
        this.json = Assert.notNull(json, () -> new ValidateException("JSON provider must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("Limits must not be null"));
    }

    /**
     * Builds the common logout callback without exposing token material.
     *
     * @param context     non-null operation context
     * @param destination validated destination, or {@code null} for local completion
     * @param state       optional exact state value
     * @return immutable logout callback
     */
    private static Outbound callback(final Context context, final URI destination, final String state) {
        final Outbound.Builder callback = Callback.outbound(context).kind(Kind.LOGOUT)
                .destination(org.miaixz.bus.fabric.Address.from(destination)).mode(Mode.QUERY);
        if (state != null) {
            callback.parameter(STATE, state);
        }
        return callback.build();
    }

    /**
     * Maps internal URI and parser failures to the sole OAuth invalid-request wire error.
     *
     * @param failure internal validation failure
     * @return stable OAuth protocol failure retaining the diagnostic cause
     */
    private static ProtocolException invalid(final RuntimeException failure) {
        return new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(), ProtocolError.INVALID_REQUEST.getValue(),
                failure);
    }

    /**
     * Validates a provider-side logout request and preserves exact state.
     *
     * @param invocation          operation context
     * @param request             logout request
     * @param registeredRedirects exact registered post-logout redirects
     * @return stage containing the validated redirect result
     * @throws IllegalArgumentException if a required input is {@code null}
     * @throws ProtocolException        if the ID Token hint is invalid
     */
    public CompletionStage<Outbound> provider(
            final Context invocation,
            final LogoutRequest request,
            final Set<URI> registeredRedirects) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final LogoutRequest input = Assert.notNull(request, "Logout request must be not null!");
        final Set<URI> redirects = Set
                .copyOf(Assert.notNull(registeredRedirects, "Registered post-logout redirects must be not null!"));
        parse(input.idTokenHint());
        final URI redirect = input.postLogoutRedirectUri();
        if (redirect != null) {
            try {
                if (!redirects.contains(redirect) || !redirect.equals(UriValidator.absolute(redirect))) {
                    return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_REQUEST));
                }
            } catch (final RuntimeException failure) {
                return CompletableFuture.failedFuture(invalid(failure));
            }
        }
        return CompletableFuture.completedFuture(callback(context, redirect, input.state()));
    }

    /**
     * Constructs a relying-party logout URI from validated exact values.
     *
     * @param invocation operation context
     * @param endpoint   validated end-session endpoint
     * @param request    logout request
     * @param policy     strict HTTPS transport policy
     * @return stage containing the outbound logout URI
     * @throws IllegalArgumentException if a required input is {@code null}
     * @throws ProtocolException        if the endpoint, ID Token hint, redirect, or state is invalid
     */
    public CompletionStage<Outbound> relyingParty(
            final Context invocation,
            final URI endpoint,
            final LogoutRequest request,
            final TransportPolicy policy) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final LogoutRequest input = Assert.notNull(request, "Logout request must be not null!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        final URI target;
        try {
            target = UriValidator.transport(UriValidator.https(endpoint), transportPolicy.addressPolicy());
        } catch (final RuntimeException failure) {
            throw invalid(failure);
        }
        parse(input.idTokenHint());
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        parameters.put(ID_TOKEN_HINT, input.idTokenHint());
        if (input.postLogoutRedirectUri() != null) {
            parameters.put(
                    POST_LOGOUT_REDIRECT_URI,
                    UriValidator.absolute(input.postLogoutRedirectUri()).toASCIIString());
        }
        if (input.state() != null) {
            if (input.state().isBlank() || input.state().getBytes(Charset.UTF_8).length > limits.maxParameterBytes()) {
                return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_REQUEST));
            }
            parameters.put(STATE, input.state());
        }
        final String query = UrlQuery.of(parameters, EncodeMode.STRICT).build(Charset.UTF_8);
        if (query.getBytes(Charset.UTF_8).length > limits.maxJsonBytes()) {
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
        return CompletableFuture.completedFuture(callback(context, requestUri, input.state()));
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
                    json,
                    limits.maxJwtBytes(),
                    limits.maxHeaderBytes(),
                    limits.maxJsonBytes(),
                    limits.maxJsonDepth());
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

}
