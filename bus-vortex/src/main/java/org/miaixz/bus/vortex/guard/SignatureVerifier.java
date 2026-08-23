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
package org.miaixz.bus.vortex.guard;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UnicodeKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Signing;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Verifies REST, MCP, and Slug request signatures selected by resolved route assets.
 * <p>
 * This class is the single signature-verification boundary for bus-vortex. It preserves each protocol's canonical input
 * and credential rules while centralizing signing-mode dispatch, credential selection, bounded credential validation,
 * key derivation, HMAC calculation, constant-time comparison, replay-body ownership, and temporary key cleanup. It
 * validates request integrity only; credential authenticity remains the responsibility of the authorization provider.
 *
 * @author Kimi Liu
 */
public class SignatureVerifier {

    /**
     * Exchange attribute that owns an MCP signed POST body's replay buffer until request termination.
     */
    private static final String MCP_CACHED_BODY = "X.MCP_CACHED_BODY";

    /**
     * Prevents additional verifier instances.
     */
    public SignatureVerifier() {
        // No initialization required.
    }

    /**
     * Verifies the REST signature required by the resolved asset.
     *
     * @param context current request context
     * @param assets  resolved route asset
     * @return verification completion signal
     */
    public static Mono<Void> verifyRest(Context context, Assets assets) {
        if (assets == null) {
            return Mono.empty();
        }
        Signing signing = Signing.of(assets.getSigning());
        if (!signing.required()) {
            return Mono.empty();
        }
        if (StringKit.isBlank(value(context, Args.SIGN))) {
            return Mono.error(new ValidateException(ErrorCode._100108));
        }
        return Mono.fromCallable(() -> {
            Map<String, Object> parameters = context.getParameters();
            KeyBinding binding = resolve(context, signing);
            try {
                if (!matches(
                        binding.key(),
                        context.getHttpMethod().value(),
                        parameters,
                        signing,
                        binding.credentialType(),
                        "rest")) {
                    throw new SignatureException(ErrorCode._100109);
                }
            } finally {
                Arrays.fill(binding.key(), (byte) 0);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verifies the legacy parameter signature used by public Slug routes.
     *
     * @param context current request context
     * @param assets  resolved Slug route asset
     * @return verification completion signal
     */
    public static Mono<Void> verifySlug(Context context, Assets assets) {
        if (assets == null) {
            return Mono.empty();
        }
        Signing signing = Signing.of(assets.getSigning());
        if (signing == Signing.NONE) {
            return Mono.empty();
        }
        if (signing != Signing.LEGACY) {
            return Mono.error(new ValidateException(ErrorCode._116000));
        }
        if (StringKit.isBlank(value(context, Args.SIGN))) {
            return Mono.error(new ValidateException(ErrorCode._100108));
        }
        return Mono.fromCallable(() -> {
            KeyBinding binding = resolve(context, signing);
            try {
                if (!matches(
                        binding.key(),
                        context.getHttpMethod().value(),
                        context.getParameters(),
                        signing,
                        binding.credentialType(),
                        "slug")) {
                    throw new SignatureException(ErrorCode._100109);
                }
            } finally {
                Arrays.fill(binding.key(), (byte) 0);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verifies an MCP route signature, exposes any restored POST body to the continuation, and releases the body lease
     * when downstream processing terminates.
     *
     * @param exchange     current web exchange
     * @param context      current request context
     * @param assets       resolved MCP route asset
     * @param continuation downstream processing that consumes the verified exchange
     * @return downstream completion signal
     */
    public static Mono<Void> verifyMcp(
            ServerWebExchange exchange,
            Context context,
            Assets assets,
            Function<ServerWebExchange, Mono<Void>> continuation) {
        Objects.requireNonNull(continuation, "Continuation cannot be null");
        return verifyMcpExchange(exchange, context, assets).flatMap(
                validatedExchange -> Mono.defer(() -> continuation.apply(validatedExchange))
                        .doFinally(signal -> releaseMcpBody(validatedExchange)));
    }

    /**
     * Verifies an MCP route signature and restores a consumed signed POST body for downstream processing.
     *
     * @param exchange current web exchange
     * @param context  current request context
     * @param assets   resolved MCP route asset
     * @return exchange carrying the original or restored request body
     */
    private static Mono<ServerWebExchange> verifyMcpExchange(
            ServerWebExchange exchange,
            Context context,
            Assets assets) {
        if (assets == null) {
            return Mono.error(new ValidateException(ErrorCode._100800));
        }
        Signing signing = Signing.of(assets.getSigning());
        if (signing == Signing.NONE) {
            return Mono.just(exchange);
        }
        if (signing != Signing.LEGACY) {
            return Mono.error(new ValidateException(ErrorCode._116000));
        }
        if (context.getHttpMethod() != Http.Method.POST) {
            return Mono
                    .fromRunnable(
                            () -> verifyMcpRequest(exchange.getRequest(), context, assets, Normal.EMPTY_BYTE_ARRAY))
                    .thenReturn(exchange);
        }
        return Octets
                .readForParsing(
                        exchange.getRequest().getBody(),
                        Math.toIntExact(Holder.get().getMaxBufferedRequestSize()),
                        Holder.requestBufferBudget(),
                        exchange.getRequest().getHeaders().getContentLength())
                .onErrorMap(DataBufferLimitException.class, error -> new ValidateException(ErrorCode._100530)).flatMap(
                        body -> Mono
                                .fromRunnable(
                                        () -> verifyMcpRequest(exchange.getRequest(), context, assets, body.bytes()))
                                .thenReturn(cacheMcpBody(exchange, body)).doOnError(error -> {
                                    exchange.getAttributes().remove(MCP_CACHED_BODY);
                                    body.close();
                                }));
    }

    /**
     * Releases an MCP replay body owned by the supplied exchange.
     *
     * @param exchange exchange that may own a cached signed body
     */
    private static void releaseMcpBody(ServerWebExchange exchange) {
        Object cached = exchange.getAttributes().remove(MCP_CACHED_BODY);
        if (cached instanceof Octets.BufferedBody body) {
            body.close();
        }
    }

    /**
     * Builds and verifies the MCP canonical signature payload.
     *
     * @param request current HTTP request
     * @param context request context
     * @param assets  resolved MCP route asset
     * @param body    cached request bytes, or an empty array
     */
    private static void verifyMcpRequest(ServerHttpRequest request, Context context, Assets assets, byte[] body) {
        String timestamp = request.getHeaders().getFirst(Args.X_TIMESTAMP);
        String nonce = request.getHeaders().getFirst(Args.X_NONCE);
        String signature = request.getHeaders().getFirst(Args.X_SIGN);
        if (StringKit.isBlank(timestamp) || StringKit.isBlank(nonce) || StringKit.isBlank(signature)) {
            throw new SignatureException(ErrorCode._100109);
        }
        String canonical = request.getMethod().name() + Symbol.LF + request.getURI().getRawPath() + Symbol.LF
                + canonicalMcpQuery(request) + Symbol.LF + timestamp + Symbol.LF + nonce + Symbol.LF
                + StringKit.toStringOrEmpty(request.getHeaders().getFirst(Args.MCP_PROTOCOL_VERSION)) + Symbol.LF
                + StringKit.toStringOrEmpty(request.getHeaders().getFirst(Args.MCP_SESSION_ID)) + Symbol.LF
                + Builder.sha256().digestHex(body == null ? Normal.EMPTY_BYTE_ARRAY : body);
        String secret = StringKit.isNotBlank(context.getBearer()) ? context.getBearer()
                : Http.Auth.bearerToken(context.getHeaders());
        if (StringKit.isNotBlank(secret)) {
            context.setBearer(secret);
        } else {
            secret = assets.getMethod();
        }
        if (StringKit.isBlank(secret)) {
            throw new SignatureException(ErrorCode._100109);
        }
        HMac hmac = Builder.hmacSha256(secret);
        String expected = Base64.encode(hmac.digest(canonical.getBytes(Charset.UTF_8)));
        if (!constantTimeEquals(hmac, expected, signature)) {
            throw new SignatureException(ErrorCode._100109);
        }
    }

    /**
     * Builds the stable sorted MCP query representation.
     *
     * @param request current HTTP request
     * @return query parameters sorted by key and value
     */
    private static String canonicalMcpQuery(ServerHttpRequest request) {
        Map<String, List<String>> sorted = new TreeMap<>();
        request.getQueryParams().forEach((key, values) -> sorted.put(key, values.stream().sorted().toList()));
        return sorted.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> entry.getKey() + Symbol.EQUAL + value))
                .collect(Collectors.joining(Symbol.AND));
    }

    /**
     * Replaces a consumed MCP request body with a replay publisher backed by the leased byte array.
     *
     * @param exchange current web exchange
     * @param body     cached body and its request-byte lease
     * @return exchange carrying the restored request body
     */
    private static ServerWebExchange cacheMcpBody(ServerWebExchange exchange, Octets.BufferedBody body) {
        exchange.getAttributes().put(MCP_CACHED_BODY, body);
        ServerHttpRequest request = new ServerHttpRequestDecorator(exchange.getRequest()) {

            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(body.bytes())));
            }

        };
        return exchange.mutate().request(request).build();
    }

    /**
     * Resolves the one signing key allowed by the asset-selected mode.
     *
     * @param context request context
     * @param signing asset-selected signing mode
     * @return isolated per-request key binding
     */
    private static KeyBinding resolve(Context context, Signing signing) {
        String method = value(context, Args.METHOD);
        String timestamp = value(context, Args.TIMESTAMP);
        if (signing == Signing.LEGACY) {
            return new KeyBinding((method + timestamp).getBytes(Charset.UTF_8), signing.key());
        }
        if (signing != Signing.V1) {
            throw new IllegalArgumentException("Unsupported REST signing mode: " + signing);
        }

        String token = Http.Auth.bearerToken(context.getHeaders());
        if (StringKit.isNotBlank(token)) {
            validateJwt(token);
            return new KeyBinding(derive(Signing.Credential.TOKEN, token, method, timestamp),
                    Signing.Credential.TOKEN.key());
        }

        String apiKey = Http.Auth.apiKey(context.getHeaders());
        if (StringKit.isNotBlank(apiKey)) {
            validateApiKey(apiKey);
            return new KeyBinding(derive(Signing.Credential.API_KEY, apiKey, method, timestamp),
                    Signing.Credential.API_KEY.key());
        }

        throw new ValidateException(ErrorCode._116001);
    }

    /**
     * Derives a fixed-size v1 HMAC key without constructing a credential-sized concatenated string.
     *
     * @param credentialType credential domain
     * @param credential     raw Bearer JWT or API key
     * @param method         logical API method
     * @param timestamp      request timestamp
     * @return 32-byte SHA-256 key material
     */
    private static byte[] derive(
            Signing.Credential credentialType,
            String credential,
            String method,
            String timestamp) {
        MessageDigest digest = Builder.sha256().getRaw();
        update(digest, Signing.V1.key());
        update(digest, credentialType.key());
        update(digest, credential);
        update(digest, method);
        update(digest, timestamp);
        return digest.digest();
    }

    /**
     * Recalculates and compares one parameter-based REST or Slug request signature.
     *
     * @param key            binary HMAC key
     * @param httpMethod     current HTTP method name
     * @param parameters     complete request parameters
     * @param signing        asset-selected signing mode
     * @param credentialType selected credential type for diagnostics
     * @param protocol       signature protocol label for diagnostics
     * @return {@code true} when the request signature matches
     */
    private static boolean matches(
            byte[] key,
            String httpMethod,
            Map<String, Object> parameters,
            Signing signing,
            String credentialType,
            String protocol) {
        if (parameters == null) {
            return false;
        }
        String clientSignature = value(parameters, Args.SIGN);
        if (clientSignature == null) {
            return false;
        }

        Logger.debug(
                true,
                "Vortex",
                "Signature validation started: protocol={}, httpMethod={}, parameterCount={}, clientSignatureLength={}",
                protocol,
                httpMethod,
                parameters.size(),
                clientSignature.length());

        Map<String, Object> signedParameters = MapKit.removeIf(
                new TreeMap<>(parameters),
                entry -> entry.getKey() == null || Args.SIGN.equalsIgnoreCase(entry.getKey()));
        HMac hmac = Builder.hmacSha256(key);
        String serverSignature = generate(hmac, httpMethod, signedParameters);
        boolean matched = constantTimeEquals(hmac, clientSignature, serverSignature);
        Logger.info(
                false,
                "Vortex",
                "Signature validation completed: protocol={}, signing={}, credentialType={}, httpMethod={}, signedParameterCount={}, matched={}, serverSignatureLength={}",
                protocol,
                signing,
                credentialType,
                httpMethod,
                signedParameters.size(),
                matched,
                serverSignature.length());
        return matched;
    }

    /**
     * Generates one parameter-based REST or Slug request signature using HMAC-SHA256 and Base64.
     *
     * @param hmac       initialized HMAC-SHA256 engine
     * @param httpMethod current HTTP method name
     * @param parameters request parameters excluding the signature field
     * @return Base64-encoded signature
     */
    private static String generate(HMac hmac, String httpMethod, Map<String, Object> parameters) {
        if (hmac == null || httpMethod == null || parameters == null) {
            throw new IllegalArgumentException("HMAC, http method, and params cannot be null.");
        }

        String encodedParameters = parameters.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), normalize(entry.getValue())))
                .filter(entry -> StringKit.isNotEmpty(entry.getValue())).sorted(Map.Entry.comparingByKey())
                .map(
                        entry -> UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8)
                                + UrlEncoder.encodeAll(entry.getValue(), Charset.UTF_8))
                .collect(Collectors.joining());
        String stringToSign = httpMethod + Symbol.LF + encodedParameters;
        return Base64.encode(hmac.digest(stringToSign.getBytes(Charset.UTF_8)));
    }

    /**
     * Converts one request parameter value to its stable signature representation.
     *
     * @param value source parameter value
     * @return canonical parameter value
     */
    private static String normalize(Object value) {
        String text = value instanceof Map || value instanceof Collection
                || (value != null && value.getClass().isArray()) ? JsonKit.toJsonString(value) : String.valueOf(value);
        return UnicodeKit.toString(text);
    }

    /**
     * Writes one UTF-8 protocol field followed by one LF byte.
     *
     * @param digest target digest
     * @param value  protocol field
     */
    private static void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(Charset.UTF_8);
        try {
            digest.update(bytes);
            digest.update((byte) '\n');
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Validates the bounded canonical three-segment representation required for v1 Bearer JWTs.
     *
     * @param token raw Bearer JWT
     */
    private static void validateJwt(String token) {
        if (!isAsciiWithin(token, Normal._8192)) {
            throw new ValidateException(ErrorCode._100109);
        }
        int first = token.indexOf(Symbol.DOT);
        int second = first < 0 ? -1 : token.indexOf(Symbol.DOT, first + 1);
        if (first <= 0 || second <= first + 1 || second >= token.length() - 1
                || token.indexOf(Symbol.DOT, second + 1) >= 0 || !isBase64Url(token, 0, first)
                || !isBase64Url(token, first + 1, second) || !isBase64Url(token, second + 1, token.length())) {
            throw new ValidateException(ErrorCode._100109);
        }
    }

    /**
     * Tests whether one compact JWT segment contains only unpadded Base64URL characters.
     *
     * @param value source JWT
     * @param start inclusive segment start
     * @param end   exclusive segment end
     * @return {@code true} when the segment is valid
     */
    private static boolean isBase64Url(String value, int start, int end) {
        for (int index = start; index < end; index++) {
            char character = value.charAt(index);
            if (!(character >= 'A' && character <= 'Z') && !(character >= 'a' && character <= 'z')
                    && !(character >= '0' && character <= '9') && character != '-' && character != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a bounded ASCII API key before v1 key derivation.
     *
     * @param apiKey raw API key
     */
    private static void validateApiKey(String apiKey) {
        if (!isAsciiWithin(apiKey, Normal._512)) {
            throw new ValidateException(ErrorCode._100109);
        }
    }

    /**
     * Tests whether a value is non-empty ASCII within an inclusive character limit.
     *
     * @param value   value to inspect
     * @param maximum inclusive maximum length
     * @return {@code true} when the value satisfies the protocol boundary
     */
    private static boolean isAsciiWithin(String value, int maximum) {
        if (StringKit.isEmpty(value) || value.length() > maximum) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (!CharKit.isAsciiPrintable(character) || Character.isWhitespace(character)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two signature strings without returning early on the first differing byte.
     *
     * @param hmac  HMAC engine providing constant-time digest comparison
     * @param left  first signature
     * @param right second signature
     * @return {@code true} when both signatures are identical
     */
    private static boolean constantTimeEquals(HMac hmac, String left, String right) {
        if (hmac == null || left == null || right == null) {
            return false;
        }
        byte[] leftBytes = left.getBytes(Charset.UTF_8);
        byte[] rightBytes = right.getBytes(Charset.UTF_8);
        try {
            return hmac.verify(leftBytes, rightBytes);
        } finally {
            Arrays.fill(leftBytes, (byte) 0);
            Arrays.fill(rightBytes, (byte) 0);
        }
    }

    /**
     * Resolves one request parameter from the parsed parameter and query maps.
     *
     * @param context current request context
     * @param key     canonical parameter name
     * @return resolved value, or {@code null}
     */
    private static String value(Context context, String key) {
        String value = value(context == null ? null : context.getParameters(), key);
        return value != null || context == null ? value : value(context.getQuery(), key);
    }

    /**
     * Resolves one value by exact key first and then by an unambiguous case-insensitive match.
     *
     * @param values source values
     * @param key    canonical key
     * @return resolved value, or {@code null}
     */
    private static String value(Map<?, ?> values, String key) {
        if (values == null || values.isEmpty() || StringKit.isBlank(key)) {
            return null;
        }
        if (values.containsKey(key)) {
            Object exact = values.get(key);
            return exact == null ? null : exact.toString();
        }
        boolean matched = false;
        String matchedValue = null;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            Object name = entry.getKey();
            if (name != null && key.equalsIgnoreCase(name.toString())) {
                Object candidate = entry.getValue();
                String candidateValue = candidate == null ? null : candidate.toString();
                if (matched && !Objects.equals(matchedValue, candidateValue)) {
                    throw new ValidateException(ErrorCode._100101);
                }
                matched = true;
                matchedValue = candidateValue;
            }
        }
        return matchedValue;
    }

    /**
     * Binds isolated per-request key material to its non-sensitive credential label.
     *
     * @param key            binary HMAC key
     * @param credentialType selected credential type
     */
    private record KeyBinding(byte[] key, String credentialType) {

    }

}
