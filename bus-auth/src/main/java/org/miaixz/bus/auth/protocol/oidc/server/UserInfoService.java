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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.AccessTokenCache;
import org.miaixz.bus.auth.cache.AuthorizationCodeCache;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.UserInfoRequest;
import org.miaixz.bus.auth.protocol.oidc.UserInfoResponse;
import org.miaixz.bus.auth.protocol.oidc.codec.UserInfoCodec;
import org.miaixz.bus.auth.resolver.Attributes;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves a standard OpenID Connect UserInfo response for an active bearer access token.
 * <p>
 * The service accepts only an already decoded Authorization-header bearer token, resolves its Provider-isolated
 * server-side state, and retrieves current attributes by the bound {@link Subject.Key}. It never accepts query or form
 * tokens and never derives a subject from caller context, an ID Token, or a Vendor identity.
 * </p>
 *
 * @author Kimi Liu
 */
public final class UserInfoService {

    /**
     * Safe failure-detail member consumed by the RFC 6750 endpoint error mapper.
     */
    private static final String OAUTH_ERROR = "oauth_error";

    /**
     * Provider identifier used to isolate opaque access-token digests.
     */
    private final String providerId;

    /**
     * Frozen Provider claim-release options.
     */
    private final OpenIdServerOptions options;

    /**
     * Access-token cache, external attribute loader, and pure attribute parser.
     */
    private final ExecutionServices services;

    /**
     * Standard Claims object decoder used after authorization filtering.
     */
    private final UserInfoCodec codec;

    /**
     * Creates a UserInfo service for one compiled OpenID Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param options    validated OpenID Provider options
     * @param services   externally implemented runtime dependencies
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public UserInfoService(final String providerId, final OpenIdServerOptions options,
            final ExecutionServices services) {
        this.providerId = Assert.notBlank(providerId, "OpenID Connect UserInfo Provider id must not be blank");
        this.options = Assert.notNull(options, "OpenID Connect UserInfo Provider options must not be null");
        this.services = Assert.notNull(services, "OpenID Connect UserInfo execution services must not be null");
        this.codec = new UserInfoCodec(services.jsonProvider());
    }

    /**
     * Expands standard OIDC scope values into their registered UserInfo claim sets.
     *
     * @param entry validated access-token state
     * @return mutable ordered claim-name set
     */
    private static LinkedHashSet<String> claimsForScopes(final AccessTokenCache.Entry entry) {
        final LinkedHashSet<String> claims = new LinkedHashSet<>();
        for (String scope : entry.scope()) {
            switch (scope) {
                case OpenIdConnect.Scopes.PROFILE -> {
                    claims.add(OpenIdConnect.Claims.NAME);
                    claims.add(OpenIdConnect.Claims.FAMILY_NAME);
                    claims.add(OpenIdConnect.Claims.GIVEN_NAME);
                    claims.add(OpenIdConnect.Claims.MIDDLE_NAME);
                    claims.add(OpenIdConnect.Claims.NICKNAME);
                    claims.add(OpenIdConnect.Claims.PREFERRED_USERNAME);
                    claims.add(OpenIdConnect.Claims.PROFILE);
                    claims.add(OpenIdConnect.Claims.PICTURE);
                    claims.add(OpenIdConnect.Claims.WEBSITE);
                    claims.add(OpenIdConnect.Claims.GENDER);
                    claims.add(OpenIdConnect.Claims.BIRTHDATE);
                    claims.add(OpenIdConnect.Claims.ZONE_INFO);
                    claims.add(OpenIdConnect.Claims.LOCALE);
                    claims.add(OpenIdConnect.Claims.UPDATED_AT);
                }
                case OpenIdConnect.Scopes.EMAIL -> {
                    claims.add(OpenIdConnect.Claims.EMAIL);
                    claims.add(OpenIdConnect.Claims.EMAIL_VERIFIED);
                }
                case OpenIdConnect.Scopes.ADDRESS -> claims.add(OpenIdConnect.Claims.ADDRESS);
                case OpenIdConnect.Scopes.PHONE -> {
                    claims.add(OpenIdConnect.Claims.PHONE_NUMBER);
                    claims.add(OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED);
                }
                default -> {
                    // Unknown or application scopes do not imply standard OIDC claim release.
                }
            }
        }
        return claims;
    }

    /**
     * Reads the individual {@code userinfo} claim request bound to the authorization code.
     *
     * @param binding validated OpenID authorization binding
     * @return immutable requested claim map, or an empty map when absent
     */
    private static Map<String, JsonValue> requestedUserInfoClaims(final AuthorizationCodeCache.OpenIdBinding binding) {
        final JsonValue.ObjectValue root = binding.requestedClaims().getOrNull();
        if (root == null || root.values().get(OpenIdConnect.Claims.USERINFO) == null) {
            return Map.of();
        }
        final JsonValue value = root.values().get(OpenIdConnect.Claims.USERINFO);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            return Map.of();
        }
        return Map.copyOf(object.values());
    }

    /**
     * Reports whether an individual claim request requires the claim.
     *
     * @param request individual claim request or {@code null}
     * @return whether the request contains boolean {@code essential=true}
     */
    private static boolean essential(final JsonValue request) {
        return request instanceof JsonValue.ObjectValue object
                && object.values().get(OpenIdConnect.Claims.ESSENTIAL) instanceof JsonValue.BooleanValue value
                && value.value();
    }

    /**
     * Creates a safe protocol failure containing one registered OAuth bearer error code.
     *
     * @param error       shared Bus error definition
     * @param oauthError  registered OAuth error code
     * @param description non-sensitive diagnostic description
     * @return immutable failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode oauthError,
            final String description) {
        return new Outcome.Failure(error, description,
                new JsonValue.ObjectValue(Map.of(OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()))));
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Identifies protocol claims that subject attributes may never replace.
     *
     * @param name exact claim name
     * @return {@code true} when protocol processing owns the claim
     */
    private static boolean reservedClaim(final String name) {
        return switch (name) {
            case JwtClaims.SUBJECT, JwtClaims.ISSUER, JwtClaims.AUDIENCE, JwtClaims.EXPIRATION, JwtClaims.ISSUED_AT, JwtClaims.NOT_BEFORE, JwtClaims.JWT_ID, OpenIdConnect.Claims.NONCE, OpenIdConnect.Claims.AUTH_TIME, OpenIdConnect.Claims.ACR, OpenIdConnect.Claims.AMR, OpenIdConnect.Claims.AUTHORIZED_PARTY, OpenIdConnect.Claims.ACCESS_TOKEN_HASH, OpenIdConnect.Claims.CODE_HASH, OpenIdConnect.Claims.STATE_HASH, OpenIdConnect.Claims.SESSION_ID -> true;
            default -> false;
        };
    }

    /**
     * Returns current claims authorized by an active OpenID Connect access token.
     *
     * @param request decoded UserInfo request containing the sensitive bearer token
     * @param context immutable invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the standard UserInfo response or a closed bearer failure
     */
    public CompletionStage<Outcome<UserInfoResponse>> userInfo(
            final UserInfoRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OpenID Connect UserInfo request must not be null");
        Assert.notNull(context, "OpenID Connect UserInfo context must not be null");
        Assert.notNull(timeout, "OpenID Connect UserInfo time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OpenID Connect UserInfo request has no remaining time budget")));
        }
        final CompletionStage<ExpiringValue<AccessTokenCache.Entry>> lookup;
        try {
            lookup = services.accessTokenCache().get(tokenKey(request.accessToken()));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect UserInfo access token lookup failed")));
        }
        return lookup.handle((stored, thrown) -> new CacheResult(stored, thrown))
                .thenCompose(result -> validateAndResolve(result, context, timeout));
    }

    /**
     * Validates stored token state and resolves the bound current attributes.
     *
     * @param result  completed cache lookup result
     * @param context immutable invocation context
     * @param timeout shared operation budget
     * @return asynchronously completed UserInfo outcome
     */
    private CompletionStage<Outcome<UserInfoResponse>> validateAndResolve(
            final CacheResult result,
            final Context context,
            final Timeout.Budget timeout) {
        if (result.failure() != null) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect UserInfo access token lookup failed")));
        }
        final ExpiringValue<AccessTokenCache.Entry> stored = result.value();
        final Instant now = timeout.clock().now();
        if (stored == null || stored.value() == null || !stored.expiresAt().isAfter(now)
                || !providerId.equals(stored.value().providerId())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    OAuth2ErrorCode.INVALID_TOKEN,
                                    "OpenID Connect UserInfo bearer token is invalid or expired")));
        }
        final AccessTokenCache.Entry entry = stored.value();
        if (!entry.scope().contains(OpenIdConnect.Scopes.OPENID) || entry.openIdBinding().isEmpty()) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    OAuth2ErrorCode.INVALID_TOKEN,
                                    "OpenID Connect UserInfo bearer token has no OpenID authorization binding")));
        }
        final CompletionStage<Outcome<Attributes>> resolution;
        try {
            resolution = org.miaixz.bus.auth.runtime.LoadResult.parse(
                    services.attributeLoader().load(new Subject.Key(entry.subjectId()), context, timeout),
                    loaded -> services.attributeParser().parse(new Subject.Key(entry.subjectId()), loaded));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect UserInfo subject attribute resolution failed")));
        }
        return resolution
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<Attributes>failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OpenID Connect UserInfo subject attribute resolution failed")))
                .thenApply(outcome -> mapAttributes(outcome, entry));
    }

    /**
     * Converts the external attribute outcome into a protocol response outcome.
     *
     * @param outcome attribute-resolution outcome
     * @param entry   validated access-token state
     * @return standard UserInfo outcome
     */
    private Outcome<UserInfoResponse> mapAttributes(
            final Outcome<Attributes> outcome,
            final AccessTokenCache.Entry entry) {
        return switch (outcome) {
            case Outcome.Succeeded<Attributes> success -> success.value() == null
                    ? Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect UserInfo attribute resolution returned no value"))
                    : response(entry, success.value());
            case Outcome.Rejected<Attributes> rejected -> Outcome.rejected(
                    failure(
                            rejected.failure().error(),
                            OAuth2ErrorCode.INVALID_TOKEN,
                            "OpenID Connect UserInfo subject is unavailable"));
            case Outcome.Failed<Attributes> failed -> Outcome.failed(
                    failure(
                            failed.failure().error(),
                            OAuth2ErrorCode.SERVER_ERROR,
                            "OpenID Connect UserInfo subject attribute resolution failed"));
        };
    }

    /**
     * Selects scope-authorized and explicitly requested claims without permitting protocol-claim replacement.
     *
     * @param entry      validated access-token state
     * @param attributes current subject attributes
     * @return successful response or failure for an unavailable essential claim
     */
    private Outcome<UserInfoResponse> response(final AccessTokenCache.Entry entry, final Attributes attributes) {
        final LinkedHashSet<String> requested = claimsForScopes(entry);
        final Map<String, JsonValue> individual = requestedUserInfoClaims(entry.openIdBinding().getOrNull());
        requested.addAll(individual.keySet());
        final Map<String, JsonValue> selected = new LinkedHashMap<>();
        for (String name : requested) {
            if (JwtClaims.SUBJECT.equals(name)) {
                continue;
            }
            if (reservedClaim(name) || !options.claimsSupported().contains(name)) {
                if (essential(individual.get(name))) {
                    return Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect essential UserInfo claim is unsupported"));
                }
                continue;
            }
            final JsonValue value = attributes.values().values().get(name);
            if (value == null) {
                if (essential(individual.get(name))) {
                    return Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect essential UserInfo claim is unavailable"));
                }
                continue;
            }
            selected.put(name, value);
        }
        try {
            selected.put(JwtClaims.SUBJECT, new JsonValue.StringValue(entry.subjectId()));
            return Outcome.succeeded(codec.decodeClaims(new JsonValue.ObjectValue(selected)));
        } catch (RuntimeException exception) {
            return Outcome.failed(
                    failure(
                            ErrorCode._500,
                            OAuth2ErrorCode.SERVER_ERROR,
                            "OpenID Connect UserInfo attributes violate the registered claim schema"));
        }
    }

    /**
     * Produces a Provider-isolated irreversible lookup key for an opaque bearer token.
     *
     * @param token sensitive bearer token
     * @return SHA-256 hexadecimal cache key
     */
    private String tokenKey(final String token) {
        return Builder.sha256Hex(providerId + '\0' + token);
    }

    /**
     * Carries a cache result without allowing exceptional completion to escape protocol mapping.
     *
     * @param value   stored access-token state or {@code null}
     * @param failure asynchronous cache failure or {@code null}
     * @author Kimi Liu
     */
    private record CacheResult(ExpiringValue<AccessTokenCache.Entry> value, Throwable failure) {

    }

}
