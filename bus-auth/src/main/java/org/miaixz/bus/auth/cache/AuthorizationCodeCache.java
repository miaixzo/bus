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
package org.miaixz.bus.auth.cache;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Session;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Stores the server-side binding required to redeem an OAuth 2.0 authorization code exactly once.
 * <p>
 * The isolated backend key is an irreversible digest of the authorization code. The value binds the code to Provider,
 * client, subject, redirect URI, approved scope, and optional PKCE challenge. Token processing must use atomic take;
 * raw authorization codes and token material are never stored in the value.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationCodeCache extends AuthCache<AuthorizationCodeCache.Entry> {

    /**
     * Isolates authorization-code state from every other bus-cache consumer.
     */
    private static final String PURPOSE = "authorization-code";

    /**
     * Creates an authorization-code cache view backed entirely by bus-cache.
     *
     * @param cache shared bus-cache backend
     */
    public AuthorizationCodeCache(final CacheX<String, Object> cache, final String deployment,
            final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, clock);
    }

    public CompletionStage<Boolean> issue(final String key, final ExpiringValue<Entry> value) {
        return super.doIssue(key, value);
    }

    public CompletionStage<ExpiringValue<Entry>> consume(final String key) {
        return super.doConsume(key);
    }

    /**
     * Carries the immutable security binding of one issued authorization code.
     *
     * @param providerId          server-side OAuth Provider identifier
     * @param clientId            authenticated OAuth client identifier
     * @param subjectId           authorized internal subject identifier
     * @param redirectUri         exact redirect URI used by the authorization request
     * @param redirectUriRequired whether the authorization request explicitly supplied {@code redirect_uri}
     * @param scope               approved OAuth scope values
     * @param resource            ordered RFC 8707 resource indicators bound to the authorization grant
     * @param codeChallenge       optional PKCE code challenge
     * @param codeChallengeMethod optional PKCE method; it may be absent when the challenge uses the protocol default
     * @param openIdBinding       optional OpenID Connect authorization context atomically bound to this code
     * @author Kimi Liu
     */
    public record Entry(String providerId, String clientId, String subjectId, String redirectUri,
            boolean redirectUriRequired, List<String> scope, List<String> resource, Optional<String> codeChallenge,
            Optional<String> codeChallengeMethod, Optional<OpenIdBinding> openIdBinding) implements Serializable {

        /**
         * Creates an immutable authorization-code binding.
         *
         * @param providerId          OAuth Provider identifier
         * @param clientId            OAuth client identifier
         * @param subjectId           authorized subject identifier
         * @param redirectUri         exact registered redirect URI
         * @param redirectUriRequired whether token redemption must repeat the exact redirect URI
         * @param scope               approved scope values
         * @param resource            ordered resource indicators
         * @param codeChallenge       optional PKCE challenge
         * @param codeChallengeMethod optional PKCE method
         * @param openIdBinding       optional OpenID Connect authorization context
         * @throws IllegalArgumentException if a required value is missing, scope contains a blank entry, or a PKCE
         *                                  method is present without a challenge
         */
        public Entry {
            Assert.notBlank(providerId, "Authorization code Provider id must not be blank");
            Assert.notBlank(clientId, "Authorization code client id must not be blank");
            Assert.notBlank(subjectId, "Authorization code subject id must not be blank");
            Assert.notBlank(redirectUri, "Authorization code redirect URI must not be blank");
            scope = immutableText(scope, "Authorization code scope must not contain blank values");
            resource = immutableText(resource, "Authorization code resource must not contain blank values");
            Assert.notNull(codeChallenge, "PKCE code challenge container must not be null");
            if (!codeChallenge.isEmpty()) {
                Assert.notBlank(codeChallenge.getOrNull(), "PKCE code challenge must not be blank");
            }
            codeChallenge = Optional.ofNullable(codeChallenge.getOrNull());
            Assert.notNull(codeChallengeMethod, "PKCE code challenge method container must not be null");
            if (!codeChallengeMethod.isEmpty()) {
                Assert.notBlank(codeChallengeMethod.getOrNull(), "PKCE code challenge method must not be blank");
                Assert.isTrue(!codeChallenge.isEmpty(), "PKCE method requires a code challenge");
            }
            codeChallengeMethod = Optional.ofNullable(codeChallengeMethod.getOrNull());
            Assert.notNull(openIdBinding, "OpenID Connect authorization binding container must not be null");
            openIdBinding = Optional.ofNullable(openIdBinding.getOrNull());
        }

        /**
         * Validates and freezes an ordered list of protocol text values.
         *
         * @param values  protocol text values
         * @param message validation message used for a blank entry
         * @return immutable detached list
         */
        private static List<String> immutableText(final List<String> values, final String message) {
            Assert.notNull(values, "Authorization code bound value list must not be null");
            final List<String> copy = new ArrayList<>(values.size());
            for (String value : values) {
                copy.add(Assert.notBlank(value, message));
            }
            return List.copyOf(copy);
        }

    }

    /**
     * Carries the OpenID Connect authentication facts that must survive authorization-code redemption atomically.
     * <p>
     * This state is internal and never appears in an OAuth response. It binds the nonce and authentication event to the
     * exact code from which the token endpoint later issues the ID Token and UserInfo-capable token state.
     * </p>
     *
     * @param nonce                      nonce received in the Authentication Request
     * @param authenticatedAt            instant at which the external authentication event occurred
     * @param authenticationContextClass optional Authentication Context Class Reference
     * @param authenticationMethods      ordered Authentication Methods References
     * @param sessionKey                 stable root Session key represented by the eventual {@code sid} claim
     * @param requestedClaims            optional OIDC claims request object
     * @author Kimi Liu
     */
    public record OpenIdBinding(Optional<String> nonce, Instant authenticatedAt,
            Optional<String> authenticationContextClass, List<String> authenticationMethods, Session.Key sessionKey,
            Optional<JsonValue.ObjectValue> requestedClaims) implements Serializable {

        /**
         * Validates and freezes one authorization-code-bound OpenID Connect context.
         *
         * @param nonce                      optional non-empty opaque nonce copied from the Authentication Request
         * @param authenticatedAt            authentication event instant
         * @param authenticationContextClass optional StringOrURI ACR
         * @param authenticationMethods      ordered, unique StringOrURI AMR values
         * @param sessionKey                 stable active-session key
         * @param requestedClaims            optional detached OIDC claims request
         * @throws IllegalArgumentException if a component is missing or a registered value is invalid
         */
        public OpenIdBinding {
            Assert.notNull(nonce, "OpenID Connect authorization nonce container must not be null");
            final String nonceValue = nonce.getOrNull();
            if (nonceValue != null) {
                Assert.notEmpty(nonceValue, "OpenID Connect authorization nonce must not be empty when present");
            }
            nonce = Optional.ofNullable(nonceValue);
            Assert.notNull(authenticatedAt, "OpenID Connect authentication time must not be null");
            Assert.notNull(
                    authenticationContextClass,
                    "OpenID Connect authentication context class container must not be null");
            final String contextClass = authenticationContextClass.getOrNull();
            if (contextClass != null) {
                validateStringOrUri(contextClass, "OpenID Connect authentication context class");
            }
            authenticationContextClass = Optional.ofNullable(contextClass);
            Assert.notNull(authenticationMethods, "OpenID Connect authentication methods must not be null");
            final LinkedHashSet<String> unique = new LinkedHashSet<>(authenticationMethods.size());
            for (String method : authenticationMethods) {
                validateStringOrUri(method, "OpenID Connect authentication method");
                Assert.isTrue(unique.add(method), "OpenID Connect authentication methods must not contain duplicates");
            }
            authenticationMethods = List.copyOf(unique);
            Assert.notNull(sessionKey, "OpenID Connect Session key must not be null");
            Assert.notNull(requestedClaims, "OpenID Connect requested claims container must not be null");
            final JsonValue.ObjectValue claims = requestedClaims.getOrNull();
            requestedClaims = claims == null ? Optional.empty()
                    : Optional.of(new JsonValue.ObjectValue(claims.values()));
        }

        /**
         * Validates a non-blank JWT StringOrURI without rewriting its lexical value.
         *
         * @param value candidate value
         * @param label safe semantic label used in diagnostics
         * @throws IllegalArgumentException if the value is blank or has invalid URI syntax when it contains a colon
         */
        private static void validateStringOrUri(final String value, final String label) {
            Assert.notBlank(value, label + " must not be blank");
            if (!value.contains(Symbol.COLON)) {
                return;
            }
            try {
                new URI(value);
            } catch (URISyntaxException exception) {
                throw new IllegalArgumentException(label + " must satisfy JWT StringOrURI syntax", exception);
            }
        }

    }

}
