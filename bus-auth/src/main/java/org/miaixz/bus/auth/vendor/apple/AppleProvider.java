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
package org.miaixz.bus.auth.vendor.apple;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.protocol.jwt.signature.ECDSAJWTSigner;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party authentication client for Sign in with Apple authorization and token operations.
 *
 * <p>
 * The Apple client-secret JWT is generated with the injected Fabric clock and the shared Bus ES256 signer. The private
 * key is resolved by the injected secret resolver, parsed once, and retained only as non-exportable key material. Apple
 * identity mapping is local and decodes the ID-token payload without an additional HTTP request.
 * </p>
 *
 * @author Kimi Liu
 */
public class AppleProvider extends AbstractProvider {

    /**
     * Apple client-secret audience.
     */
    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";

    /**
     * Fixed Apple client-secret lifetime.
     */
    private static final Duration CLIENT_SECRET_LIFETIME = Duration.ofMinutes(3);

    /**
     * Lazily parsed P-256 signing key; the immutable key object never exposes source PEM text.
     */
    private volatile PrivateKey privateKey;

    /**
     * Creates an Apple client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or one of its required dependencies is null
     * @throws AuthorizedException  if the Apple key identifier or team identifier is absent
     */
    public AppleProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.APPLE);
        validateAppleRegistration();
    }

    /**
     * Parses optional first-login Apple user JSON without affecting token exchange success.
     *
     * @param json callback user JSON, or null
     * @return typed user data, or null when absent or malformed
     */
    private static AppleUser parseUser(final String json) {
        if (StringKit.isEmpty(json)) {
            return null;
        }
        try {
            return JsonKit.toPojo(json, AppleUser.class);
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Builds the Apple form-post authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete Apple authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).queryParam("response_mode", "form_post")
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(AppleScope.values()))).build());
    }

    /**
     * Exchanges the Apple callback code using a freshly signed three-minute client-secret JWT.
     *
     * @param context  immutable root operation context used for key resolution and security time
     * @param callback immutable inbound callback containing code and optional first-login user JSON
     * @return successful client message containing the mapped Apple token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Apple reports an error or the token response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String callbackError = inbound.value("error").orElse(null);
        if (!StringKit.isEmpty(callbackError)) {
            throw new AuthorizedException(callbackError);
        }
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", registration.clientId());
        form.put("client_secret", clientSecret(current));
        form.put("code", inbound.value("code").orElse(null));
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", registration.redirectUri());
        final TokenResponse response = JsonKit.toPojo(post(endpoint(VendorEndpoint.TOKEN), form), TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse access token response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? response.error() : response.error_description());
        }
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        final VendorTokenSet.VendorTokenSetBuilder builder = VendorTokenSet.builder().token(response.access_token())
                .expireIn(response.expires_in()).refresh(response.refresh_token()).tokenType(response.token_type())
                .idToken(response.id_token());
        final AppleUser user = parseUser(inbound.value("user").orElse(null));
        if (user != null && user.name() != null && user.name().firstName() != null && user.name().lastName() != null) {
            builder.username(user.name().firstName() + Symbol.SPACE + user.name().lastName());
        }
        return Message.success(builder.build());
    }

    /**
     * Maps the Apple ID-token payload into a vendor identity without sending a network request.
     *
     * @param context immutable root operation context for this local mapping
     * @param token   non-null Apple token set containing a compact ID token
     * @return successful client message containing the mapped Apple identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the compact token or payload is invalid or omits {@code sub}
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        try {
            final String[] segments = Objects.requireNonNull(authorization.getIdToken(), "ID token must not be null")
                    .split("\\.", -1);
            if (segments.length != 3) {
                throw new AuthorizedException("Malformed Apple id_token");
            }
            final String payload = new String(Base64.decode(segments[1]), StandardCharsets.UTF_8);
            final IdentityResponse response = JsonKit.toPojo(payload, IdentityResponse.class);
            if (response == null || response.sub() == null) {
                throw new AuthorizedException("Missing sub in id_token payload");
            }
            return Message.success(
                    VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.sub())
                            .email(response.email()).username(authorization.getUsername()).token(authorization)
                            .source(descriptor().id()).build());
        } catch (final AuthorizedException failure) {
            throw failure;
        } catch (final RuntimeException failure) {
            throw new AuthorizedException("Failed to parse id_token payload: " + failure.getMessage());
        }
    }

    /**
     * Creates one compact Apple ES256 client-secret JWT.
     *
     * @param context root operation context used to resolve the PEM key on first use
     * @return signed compact JWT containing Apple-required header and claim fields
     */
    private String clientSecret(final Context context) {
        final Instant issuedAt = clock.now();
        final Map<String, Object> header = Map.of("alg", "ES256", "kid", registration.kid());
        final Map<String, Object> payload = Map.of(
                "iss",
                registration.teamId(),
                "iat",
                issuedAt.getEpochSecond(),
                "exp",
                issuedAt.plus(CLIENT_SECRET_LIFETIME).getEpochSecond(),
                "aud",
                APPLE_AUDIENCE,
                "sub",
                registration.clientId());
        final String headerSegment = Base64.encodeUrlSafe(JsonKit.toJsonString(header));
        final String payloadSegment = Base64.encodeUrlSafe(JsonKit.toJsonString(payload));
        final String signingInput = headerSegment + Symbol.DOT + payloadSegment;
        final ECDSAJWTSigner signer = new ECDSAJWTSigner(Algorithm.SHA256WITHECDSA.getValue(), privateKey(context));
        final byte[] signature = signer.sign(signingInput.getBytes(StandardCharsets.US_ASCII));
        return signingInput + Symbol.DOT + Base64.encodeUrlSafe(signature);
    }

    /**
     * Resolves and parses the configured PKCS#8 PEM key once.
     *
     * @param context root operation context used by the secret resolver
     * @return cached non-exportable private key
     * @throws AuthorizedException if the secret is not a PKCS#8 private key
     */
    private PrivateKey privateKey(final Context context) {
        PrivateKey current = privateKey;
        if (current == null) {
            synchronized (this) {
                current = privateKey;
                if (current == null) {
                    final String pem = secret(context);
                    try (PEMParser parser = new PEMParser(new StringReader(pem))) {
                        final Object parsed = parser.readObject();
                        if (!(parsed instanceof PrivateKeyInfo keyInfo)) {
                            throw new AuthorizedException("Apple secret must contain a PKCS#8 private key");
                        }
                        current = new JcaPEMKeyConverter().getPrivateKey(keyInfo);
                        privateKey = current;
                    } catch (final IOException failure) {
                        throw new AuthorizedException("Failed to get Apple private key", failure);
                    }
                }
            }
        }
        return current;
    }

    /**
     * Validates Apple-only static registration fields.
     *
     * @throws AuthorizedException if key or team identity is absent
     */
    private void validateAppleRegistration() {
        if (StringKit.isEmpty(registration.kid())) {
            throw new AuthorizedException(VendorErrors._110008);
        }
        if (StringKit.isEmpty(registration.teamId())) {
            throw new AuthorizedException(VendorErrors._110009);
        }
    }

    /**
     * Typed Apple token response.
     *
     * @param access_token      access token
     * @param expires_in        access-token lifetime in seconds
     * @param refresh_token     refresh token
     * @param token_type        token scheme label
     * @param id_token          compact OpenID Connect identity token
     * @param error             Apple error code
     * @param error_description Apple diagnostic message
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String refresh_token, String token_type,
            String id_token, String error, String error_description) {
    }

    /**
     * Typed optional first-login user document.
     *
     * @param name  optional name components
     * @param email optional email address
     * @author Kimi Liu
     */
    private record AppleUser(AppleName name, String email) {
    }

    /**
     * Typed Apple first-login name components.
     *
     * @param firstName given name
     * @param lastName  family name
     * @author Kimi Liu
     */
    private record AppleName(String firstName, String lastName) {
    }

    /**
     * Typed local Apple ID-token identity payload.
     *
     * @param sub   stable Apple subject identifier
     * @param email optional email address
     * @author Kimi Liu
     */
    private record IdentityResponse(String sub, String email) {
    }

}
