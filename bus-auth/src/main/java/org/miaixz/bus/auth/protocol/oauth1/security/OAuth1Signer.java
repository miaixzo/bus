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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter;
import org.miaixz.bus.auth.protocol.oauth1.SignatureMethod;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1ClientSettings;
import org.miaixz.bus.auth.resolver.KeyResolver;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.Sign;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Generates RFC 5849 request parameters and HMAC-SHA1 or RSA-SHA1 signatures.
 *
 * @author Kimi Liu
 */
public final class OAuth1Signer {

    /**
     * Uppercase hexadecimal alphabet used by RFC percent encoding.
     */
    private static final byte[] HEX = "0123456789ABCDEF".getBytes(Charset.US_ASCII);

    /**
     * Externally implemented runtime dependencies.
     */
    private final ExecutionServices services;

    /**
     * Creates a signer that resolves key material only through runtime ports.
     *
     * @param services externally owned runtime dependencies
     */
    public OAuth1Signer(final ExecutionServices services) {
        this.services = Assert.notNull(services, "OAuth 1.0 execution services must not be null");
    }

    /**
     * Validates caller ownership and freezes request parameters.
     *
     * @param parameters request parameters
     * @return immutable validated parameters
     */
    private static List<OAuth1Parameter> validateRequestParameters(final List<OAuth1Parameter> parameters) {
        final List<OAuth1Parameter> result = new ArrayList<>(parameters.size());
        final Set<String> seenOAuth = new HashSet<>();
        for (OAuth1Parameter parameter : parameters) {
            final OAuth1Parameter value = Assert.notNull(parameter, "OAuth 1.0 request parameter must not be null");
            if (owned(value.name())) {
                throw new ValidateException(
                        "OAuth 1.0 request must not pre-populate signer-owned parameter " + value.name());
            }
            if (value.name().startsWith(OAuth1.PARAMETER_PREFIX) && !seenOAuth.add(value.name())) {
                throw new ValidateException("OAuth 1.0 protocol parameter must not be duplicated: " + value.name());
            }
            result.add(value);
        }
        return List.copyOf(result);
    }

    /**
     * Identifies the standard parameters generated exclusively by this signer.
     *
     * @param name exact OAuth parameter name
     * @return {@code true} when callers must not provide the parameter
     */
    private static boolean owned(final String name) {
        return switch (name) {
            case OAuth1.Parameters.CONSUMER_KEY, OAuth1.Parameters.SIGNATURE_METHOD, OAuth1.Parameters.SIGNATURE, OAuth1.Parameters.TIMESTAMP, OAuth1.Parameters.NONCE, OAuth1.Parameters.VERSION -> true;
            default -> false;
        };
    }

    /**
     * Returns only OAuth protocol parameters and appends the generated signature.
     *
     * @param request   request parameters
     * @param generated signer-owned parameters before signature
     * @param signature Base64 signature value
     * @return immutable Authorization header parameters
     */
    private static List<OAuth1Parameter> withSignature(
            final List<OAuth1Parameter> request,
            final List<OAuth1Parameter> generated,
            final String signature) {
        final List<OAuth1Parameter> result = new ArrayList<>();
        for (OAuth1Parameter parameter : request) {
            if (parameter.name().startsWith(OAuth1.PARAMETER_PREFIX)) {
                result.add(parameter);
            }
        }
        result.addAll(generated);
        result.add(new OAuth1Parameter(OAuth1.Parameters.SIGNATURE, signature));
        return List.copyOf(result);
    }

    /**
     * Builds the RFC 5849 HMAC key without creating immutable secret Strings.
     *
     * @param consumerSecret consumer secret characters
     * @param tokenSecret    token secret characters, possibly empty
     * @return newly allocated percent-encoded signing key bytes
     */
    private static byte[] signingKey(final char[] consumerSecret, final char[] tokenSecret) {
        final byte[] consumer = percentEncode(consumerSecret);
        final byte[] token = percentEncode(tokenSecret);
        final byte[] result = new byte[consumer.length + 1 + token.length];
        try {
            System.arraycopy(consumer, 0, result, 0, consumer.length);
            result[consumer.length] = Symbol.C_AND;
            System.arraycopy(token, 0, result, consumer.length + 1, token.length);
            return result;
        } finally {
            Arrays.fill(consumer, (byte) 0);
            Arrays.fill(token, (byte) 0);
        }
    }

    /**
     * Percent-encodes secret characters directly to bytes and clears intermediate UTF-8 material.
     *
     * @param value secret characters
     * @return exact RFC 3986 unreserved encoding bytes
     */
    private static byte[] percentEncode(final char[] value) {
        final ByteBuffer encoded;
        try {
            encoded = Charset.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(value));
        } catch (final CharacterCodingException cause) {
            throw new ValidateException("OAuth 1.0 secret contains invalid Unicode", cause);
        }
        final byte[] utf8 = new byte[encoded.remaining()];
        encoded.get(utf8);
        final byte[] expanded = new byte[utf8.length * 3];
        int length = 0;
        try {
            for (byte current : utf8) {
                final int unsigned = current & 0xff;
                final boolean safe = unsigned >= 'A' && unsigned <= 'Z' || unsigned >= 'a' && unsigned <= 'z'
                        || unsigned >= Symbol.C_ZERO && unsigned <= Symbol.C_NINE || unsigned == Symbol.C_MINUS
                        || unsigned == Symbol.C_DOT || unsigned == Symbol.C_UNDERLINE || unsigned == Symbol.C_TILDE;
                if (safe) {
                    expanded[length++] = current;
                } else {
                    expanded[length++] = Symbol.C_PERCENT;
                    expanded[length++] = HEX[unsigned >>> 4];
                    expanded[length++] = HEX[unsigned & 0x0f];
                }
            }
            return Arrays.copyOf(expanded, length);
        } finally {
            Arrays.fill(utf8, (byte) 0);
            Arrays.fill(expanded, (byte) 0);
        }
    }

    /**
     * Wraps an outcome in a completed stage.
     *
     * @param outcome outcome to wrap
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe failure using an existing Bus error.
     *
     * @param error       existing Bus error
     * @param description non-sensitive description
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Signs one request according to RFC 5849 section 3.4.
     *
     * @param method            exact HTTP wire method
     * @param url               exact request URL
     * @param requestParameters decoded request parameters participating in signing
     * @param settings          validated client settings
     * @param tokenSecret       optional temporary or token credential secret lease
     * @param context           immutable invocation context
     * @param timeout           shared end-to-end time budget
     * @return stage containing immutable Authorization header parameters or a closed failure
     */
    public CompletionStage<Outcome<List<OAuth1Parameter>>> sign(
            final Http.Method method,
            final UnoUrl url,
            final List<OAuth1Parameter> requestParameters,
            final OAuth1ClientSettings settings,
            final Optional<SecretLease> tokenSecret,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(method, "OAuth 1.0 signature HTTP method must not be null");
        Assert.notNull(url, "OAuth 1.0 signature URL must not be null");
        Assert.notNull(requestParameters, "OAuth 1.0 signature request parameters must not be null");
        Assert.notNull(settings, "OAuth 1.0 client settings must not be null");
        Assert.notNull(tokenSecret, "OAuth 1.0 token secret container must not be null");
        Assert.notNull(context, "OAuth 1.0 invocation context must not be null");
        Assert.notNull(timeout, "OAuth 1.0 time budget must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "OAuth 1.0 signing has no remaining time budget")));
        }
        final List<OAuth1Parameter> request = validateRequestParameters(requestParameters);
        final List<OAuth1Parameter> generated = generated(settings);
        final List<OAuth1Parameter> baseParameters = new ArrayList<>(request.size() + generated.size());
        baseParameters.addAll(request);
        baseParameters.addAll(generated);
        final SignatureBaseString base = SignatureBaseString.create(method, url, baseParameters);
        if (SignatureMethod.HMAC_SHA1.equals(settings.signatureMethod())) {
            return hmac(base, request, generated, settings, tokenSecret, context, timeout);
        }
        if (SignatureMethod.RSA_SHA1.equals(settings.signatureMethod())) {
            return rsa(base, request, generated, settings, context, timeout);
        }
        return completed(Outcome.rejected(failure(ErrorCode._400, "OAuth 1.0 signature method is not enabled")));
    }

    /**
     * Resolves the configured consumer secret and produces an HMAC-SHA1 signature.
     *
     * @param base        normalized signature base string
     * @param request     original request parameters
     * @param generated   signer-generated protocol parameters
     * @param settings    client settings
     * @param tokenSecret optional token secret
     * @param context     invocation context
     * @param timeout     shared budget
     * @return asynchronous signed header parameters
     */
    private CompletionStage<Outcome<List<OAuth1Parameter>>> hmac(
            final SignatureBaseString base,
            final List<OAuth1Parameter> request,
            final List<OAuth1Parameter> generated,
            final OAuth1ClientSettings settings,
            final Optional<SecretLease> tokenSecret,
            final Context context,
            final Timeout.Budget timeout) {
        return services.secretResolver().resolve(settings.signingCredential(), context, timeout)
                .handle((resolved, thrown) -> {
                    if (thrown != null) {
                        return Outcome.<List<OAuth1Parameter>>failed(
                                failure(ErrorCode._500, "OAuth 1.0 consumer secret resolution failed"));
                    }
                    return switch (resolved) {
                        case Outcome.Succeeded<SecretLease> success -> {
                            try (SecretLease consumerSecret = success.value()) {
                                final char[] token = tokenSecret.isEmpty() ? Normal.EMPTY_CHAR_ARRAY
                                        : tokenSecret.getOrNull().material();
                                final byte[] key = signingKey(consumerSecret.material(), token);
                                final byte[] data = base.value().getBytes(Charset.UTF_8);
                                try {
                                    final byte[] signature = Builder.hmacSha1(key).digest(data);
                                    try {
                                        yield Outcome
                                                .succeeded(withSignature(request, generated, Base64.encode(signature)));
                                    } finally {
                                        Arrays.fill(signature, (byte) 0);
                                    }
                                } finally {
                                    Arrays.fill(key, (byte) 0);
                                    Arrays.fill(data, (byte) 0);
                                }
                            }
                        }
                        case Outcome.Rejected<SecretLease> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<SecretLease> failed -> Outcome.failed(failed.failure());
                    };
                });
    }

    /**
     * Resolves the configured private key and produces an RSA-SHA1 signature.
     *
     * @param base      normalized signature base string
     * @param request   original request parameters
     * @param generated signer-generated protocol parameters
     * @param settings  client settings
     * @param context   invocation context
     * @param timeout   shared budget
     * @return asynchronous signed header parameters
     */
    private CompletionStage<Outcome<List<OAuth1Parameter>>> rsa(
            final SignatureBaseString base,
            final List<OAuth1Parameter> request,
            final List<OAuth1Parameter> generated,
            final OAuth1ClientSettings settings,
            final Context context,
            final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final KeyResolver.Query query = new KeyResolver.Query(settings.consumerKey(),
                Optional.of(settings.signingCredential().id()), "sig", SignatureMethod.RSA_SHA1.value(), now);
        return services.keyResolver().resolve(query, context, timeout).handle((resolved, thrown) -> {
            if (thrown != null) {
                return Outcome.<List<OAuth1Parameter>>failed(
                        failure(ErrorCode._500, "OAuth 1.0 private key resolution failed"));
            }
            return switch (resolved) {
                case Outcome.Succeeded<KeyResolver.ResolvedKey> success -> {
                    final KeyResolver.ResolvedKey key = success.value();
                    if (!(key.key() instanceof PrivateKey privateKey) || now.isBefore(key.notBefore())
                            || !now.isBefore(key.notAfter())) {
                        yield Outcome.rejected(
                                failure(
                                        ErrorCode._401,
                                        "OAuth 1.0 RSA signing key is not a currently valid private key"));
                    }
                    final byte[] data = base.value().getBytes(Charset.UTF_8);
                    try {
                        final Sign signer = new Sign(Algorithm.SHA1WITHRSA, new KeyPair(null, privateKey));
                        final byte[] signature = signer.sign(data);
                        try {
                            yield Outcome.succeeded(withSignature(request, generated, Base64.encode(signature)));
                        } finally {
                            Arrays.fill(signature, (byte) 0);
                        }
                    } finally {
                        Arrays.fill(data, (byte) 0);
                    }
                }
                case Outcome.Rejected<KeyResolver.ResolvedKey> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyResolver.ResolvedKey> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Generates the six signer-owned RFC 5849 protocol parameters.
     *
     * @param settings client settings
     * @return immutable signer-owned parameter list
     */
    private List<OAuth1Parameter> generated(final OAuth1ClientSettings settings) {
        return List.of(
                new OAuth1Parameter(OAuth1.Parameters.CONSUMER_KEY, settings.consumerKey()),
                new OAuth1Parameter(OAuth1.Parameters.SIGNATURE_METHOD, settings.signatureMethod().value()),
                new OAuth1Parameter(OAuth1.Parameters.TIMESTAMP,
                        Long.toString(services.fabricContext().clock().now().getEpochSecond())),
                new OAuth1Parameter(OAuth1.Parameters.NONCE, ID.simpleUUID()),
                new OAuth1Parameter(OAuth1.Parameters.VERSION, OAuth1.VERSION));
    }

}
