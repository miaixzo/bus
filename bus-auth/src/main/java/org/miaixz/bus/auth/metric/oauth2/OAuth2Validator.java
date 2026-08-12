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

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Limits;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.AuthorizationRequest;
import org.miaixz.bus.auth.metric.OAuth2.CodeChallengeMethod;
import org.miaixz.bus.auth.metric.OAuth2.GrantType;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.shared.validation.UriValidator;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Applies common OAuth client, redirect, scope, grant, PKCE, and authorization-source validation.
 * <p>
 * All comparisons required by OAuth are exact and case-sensitive. Redirect validation compares the complete URI to a
 * registered client URI. PKCE admits only S256 and validates both verifier grammar and digest. This class does not
 * resolve clients, verify JWT signatures, access state, or choose a grant from untrusted input.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth2Validator {

    /**
     * Registered prefix for pushed authorization request URIs.
     */
    private static final String REQUEST_URI_PREFIX = "urn:ietf:params:oauth:request_uri:";

    /**
     * Registered client attribute selecting token-endpoint authentication.
     */
    private static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";

    /**
     * Public-client token endpoint method.
     */
    private static final String AUTH_NONE = "none";

    /**
     * HTTP Basic client-secret method.
     */
    private static final String AUTH_SECRET_BASIC = "client_secret_basic";

    /**
     * Form client-secret method.
     */
    private static final String AUTH_SECRET_POST = "client_secret_post";

    /**
     * Prevents construction of the stateless validator.
     */
    private OAuth2Validator() {
        // No initialization required.
    }

    /**
     * Requires a resolver result to match the exact requested client identifier.
     *
     * @param resolved resolver result
     * @param clientId exact requested identifier
     * @return resolved registered client
     */
    public static Client client(final Optional<Client> resolved, final String clientId) {
        if (resolved == null || resolved.isEmpty() || StringKit.isBlank(clientId)
                || !clientId.equals(resolved.get().id())) {
            reject(ProtocolError.INVALID_CLIENT);
        }
        return resolved.get();
    }

    /**
     * Authenticates one resolved client through its exact registered token endpoint method.
     * <p>
     * Public clients must register {@code none} and present no secret. Confidential clients must register
     * {@code client_secret_basic} or {@code client_secret_post}; their secret is resolved only through the runtime
     * secret port and compared without early exit. Missing or unknown registration fails closed.
     * </p>
     *
     * @param invocation      tenant-scoped operation context
     * @param client          resolved registered client
     * @param presentedSecret presented secret copy
     * @param runtime         validated authentication runtime
     * @return stage containing the authenticated client
     */
    public static CompletionStage<Client> authenticate(
            final Invocation invocation,
            final Client client,
            final char[] presentedSecret,
            final Runtime runtime) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final Client registered = Assert.notNull(client, "Client must be not null!");
        final Runtime ports = Assert.notNull(runtime, "Authentication runtime must be not null!");
        final char[] presented = presentedSecret == null ? new char[0] : presentedSecret.clone();
        final Object configured = registered.attributes().get(TOKEN_ENDPOINT_AUTH_METHOD);
        if (!(configured instanceof String method)) {
            Arrays.fill(presented, (char) Normal._0);
            return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_CLIENT));
        }
        if (AUTH_NONE.equals(method)) {
            final boolean empty = presented.length == Normal._0;
            Arrays.fill(presented, (char) Normal._0);
            return empty ? CompletableFuture.completedFuture(registered)
                    : CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_CLIENT));
        }
        if (!AUTH_SECRET_BASIC.equals(method) && !AUTH_SECRET_POST.equals(method)) {
            Arrays.fill(presented, (char) Normal._0);
            return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_CLIENT));
        }
        final CompletionStage<char[]> resolved = Assert.notNull(
                ports.secrets().resolve(context, "oauth2-client", registered.id()),
                "Secret resolver stage must be not null!");
        return resolved.thenApply(expected -> {
            try {
                clientSecret(expected, presented);
                return registered;
            } finally {
                if (expected != null) {
                    Arrays.fill(expected, (char) Normal._0);
                }
                Arrays.fill(presented, (char) Normal._0);
            }
        });
    }

    /**
     * Requires an exact registered redirect URI after structural safety validation.
     *
     * @param client      registered client
     * @param redirectUri requested redirect URI
     * @return unchanged validated redirect URI
     */
    public static URI redirect(final Client client, final URI redirectUri) {
        final Client registered = Assert.notNull(client, "Client must be not null!");
        try {
            final URI current = UriValidator.absolute(redirectUri);
            if (!registered.redirectUris().contains(current)) {
                reject(ProtocolError.INVALID_REQUEST);
            }
            return current;
        } catch (final ProtocolException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

    /**
     * Requires requested scopes to be a bounded subset of the exact allowed scopes.
     *
     * @param requested requested scopes
     * @param allowed   client and resource policy scopes
     * @param limits    authentication input limits
     * @return immutable insertion-ordered requested scopes
     */
    public static Set<String> scopes(final Set<String> requested, final Set<String> allowed, final Limits limits) {
        final Limits bounds = Assert.notNull(limits, "Limits must be not null!");
        final Set<String> source = requested == null ? Set.of() : requested;
        final Set<String> permitted = allowed == null ? Set.of() : allowed;
        if (source.size() > bounds.maxParameters() || !permitted.containsAll(source)) {
            reject(ProtocolError.INVALID_SCOPE);
        }
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final String scope : source) {
            validateScope(scope, bounds);
            result.add(scope);
        }
        return result.isEmpty() ? Set.of() : Collections.unmodifiableSet(result);
    }

    /**
     * Requires a selected grant to be enabled for the authenticated client.
     *
     * @param grant   selected grant
     * @param allowed client grant allowlist
     * @return unchanged selected grant
     */
    public static GrantType grant(final GrantType grant, final Set<GrantType> allowed) {
        if (grant == null) {
            reject(ProtocolError.UNSUPPORTED_GRANT_TYPE);
        }
        if (allowed == null || !allowed.contains(grant)) {
            reject(ProtocolError.UNAUTHORIZED_CLIENT);
        }
        return grant;
    }

    /**
     * Validates the common authorization-code request fields before consent or state creation.
     *
     * @param request       bounded authorization request
     * @param client        registered client
     * @param allowedScopes client and resource policy scopes
     * @param limits        authentication input limits
     * @return unchanged validated request
     */
    public static AuthorizationRequest authorization(
            final AuthorizationRequest request,
            final Client client,
            final Set<String> allowedScopes,
            final Limits limits) {
        final AuthorizationRequest current = Assert.notNull(request, "Authorization request must be not null!");
        final Client registered = Assert.notNull(client, "Client must be not null!");
        if (!registered.id().equals(current.clientId())) {
            reject(ProtocolError.INVALID_CLIENT);
        }
        if (current.responseMode() == null) {
            reject(ProtocolError.INVALID_REQUEST);
        }
        redirect(registered, current.redirectUri());
        scopes(current.scopes(), allowedScopes, limits);
        challenge(current.codeChallenge(), current.codeChallengeMethod());
        authorizationSource(current.requestObject(), current.requestUri());
        return current;
    }

    /**
     * Validates a stored S256 challenge and presented PKCE verifier.
     *
     * @param expectedChallenge stored Base64url challenge
     * @param method            stored PKCE transformation method
     * @param verifier          presented verifier
     */
    public static void pkce(final String expectedChallenge, final CodeChallengeMethod method, final String verifier) {
        challenge(expectedChallenge, method);
        validateVerifier(verifier);
        final String actual = Base64.encodeUrlSafe(Builder.sha256(verifier.getBytes(Charset.US_ASCII)));
        if (!constantTime(expectedChallenge.toCharArray(), actual.toCharArray())) {
            reject(ProtocolError.INVALID_GRANT);
        }
    }

    /**
     * Requires exact client-secret equality without early exit on content mismatch.
     *
     * @param expected  resolved client secret
     * @param presented presented client secret
     */
    public static void clientSecret(final char[] expected, final char[] presented) {
        if (expected == null || presented == null || !constantTime(expected, presented)) {
            reject(ProtocolError.INVALID_CLIENT);
        }
    }

    /**
     * Validates the mutually exclusive direct JWT and pushed request URI inputs.
     *
     * @param requestObject optional JWT-secured request
     * @param requestUri    optional pushed request URI
     */
    public static void authorizationSource(final String requestObject, final String requestUri) {
        final boolean objectPresent = StringKit.isNotBlank(requestObject);
        final boolean uriPresent = StringKit.isNotBlank(requestUri);
        if (objectPresent && uriPresent || requestObject != null && !objectPresent
                || requestUri != null && !uriPresent) {
            reject(ProtocolError.INVALID_REQUEST);
        }
        if (uriPresent && !requestUri.startsWith(REQUEST_URI_PREFIX)) {
            reject(ProtocolError.INVALID_REQUEST_URI);
        }
    }

    /**
     * Validates a mandatory S256 challenge.
     *
     * @param value  Base64url challenge
     * @param method fixed PKCE method
     */
    private static void challenge(final String value, final CodeChallengeMethod method) {
        if (method != CodeChallengeMethod.S256 || value == null || value.length() != Normal._43) {
            reject(ProtocolError.INVALID_REQUEST);
        }
        for (int index = Normal._0; index < value.length(); index++) {
            if (!base64Url(value.charAt(index))) {
                reject(ProtocolError.INVALID_REQUEST);
            }
        }
    }

    /**
     * Validates the RFC 7636 verifier length and unreserved grammar.
     *
     * @param verifier presented verifier
     */
    private static void validateVerifier(final String verifier) {
        if (verifier == null || verifier.length() < Normal._43 || verifier.length() > Normal._128) {
            reject(ProtocolError.INVALID_GRANT);
        }
        for (int index = Normal._0; index < verifier.length(); index++) {
            if (!unreserved(verifier.charAt(index))) {
                reject(ProtocolError.INVALID_GRANT);
            }
        }
    }

    /**
     * Validates one exact scope token using the RFC 6749 scope-token grammar.
     *
     * @param scope  scope token
     * @param limits authentication input limits
     */
    private static void validateScope(final String scope, final Limits limits) {
        if (scope == null || scope.isEmpty() || scope.getBytes(Charset.UTF_8).length > limits.maxParameterBytes()) {
            reject(ProtocolError.INVALID_SCOPE);
        }
        for (int index = Normal._0; index < scope.length(); index++) {
            final char value = scope.charAt(index);
            if (!(value == '!' || value >= '#' && value <= '[' || value >= ']' && value <= '~')) {
                reject(ProtocolError.INVALID_SCOPE);
            }
        }
    }

    /**
     * Returns whether one character belongs to the RFC 3986 unreserved set.
     *
     * @param value character
     * @return whether the character is unreserved ASCII
     */
    private static boolean unreserved(final char value) {
        return value >= Symbol.C_ZERO && value <= Symbol.C_NINE || value >= 'A' && value <= 'Z'
                || value >= 'a' && value <= 'z' || value == Symbol.C_MINUS || value == Symbol.C_DOT
                || value == Symbol.C_UNDERLINE || value == Symbol.C_TILDE;
    }

    /**
     * Returns whether one character belongs to the unpadded Base64url alphabet.
     *
     * @param value character
     * @return whether the character is valid Base64url ASCII
     */
    private static boolean base64Url(final char value) {
        return value >= Symbol.C_ZERO && value <= Symbol.C_NINE || value >= 'A' && value <= 'Z'
                || value >= 'a' && value <= 'z' || value == Symbol.C_MINUS || value == Symbol.C_UNDERLINE;
    }

    /**
     * Compares two character arrays without early exit on content mismatch.
     *
     * @param left  expected characters
     * @param right presented characters
     * @return whether both arrays contain exactly equal characters
     */
    private static boolean constantTime(final char[] left, final char[] right) {
        int difference = left.length ^ right.length;
        final int maximum = Math.max(left.length, right.length);
        for (int index = Normal._0; index < maximum; index++) {
            final char leftValue = index < left.length ? left[index] : Normal._0;
            final char rightValue = index < right.length ? right[index] : Normal._0;
            difference |= leftValue ^ rightValue;
        }
        return difference == Normal._0;
    }

    /**
     * Rejects a protocol input with one fixed OAuth wire error.
     *
     * @param error fixed protocol error
     */
    private static void reject(final ProtocolError error) {
        throw new ProtocolException(error);
    }

}
