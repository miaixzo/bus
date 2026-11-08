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
package org.miaixz.bus.cortex.setting.secret.provider;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCache;
import org.miaixz.bus.fabric.protocol.http.retry.HttpRetryPolicy;

/**
 * Package-local cloud protocol primitives shared by all Secret providers. Uses native Bus Fabric HTTP with bounded
 * materialization, absolute deadlines and bounded retries. No SDK identity discovery, redirect following, remote
 * diagnostics or secret-bearing cache persistence is introduced.
 *
 * @author Kimi Liu
 */
final class CloudProtocol {

    /**
     * Prevents instantiation of this static utility.
     */
    private CloudProtocol() {
        // No initialization required.
    }

    /**
     * Borrows a Fabric runtime after rejecting policies that could replay signatures or retain secrets. The returned
     * factory does not take ownership of the runtime.
     *
     * @param context caller-owned Fabric runtime with all replay and persistence policies explicitly disabled
     * @return factory for fresh native Fabric builders
     * @throws IllegalArgumentException if the Fabric runtime permits replay, redirects, retries, or persistence of
     *                                  secret traffic
     */
    static Supplier<HttpX.Builder> http(Context context) {
        Objects.requireNonNull(context, "Fabric context");
        HttpRetryPolicy retry = HttpRetryPolicy.resolve(context.options());
        if (retry.maxAttempts() != 0 || retry.maxStaleRetries() != 0 || retry.maxRedirects() != 0
                || retry.retryOnConnectionFailure() || context.options().get(HttpCache.OPTION) != null
                || context.options().get(HttpAuthenticator.OPTION) != null
                || !context.options().contains(CookieJar.OPTION) || context.options().get(CookieJar.OPTION) != null
                || context.filter() != null) {
            throw new IllegalArgumentException(
                    "Secret reads require Fabric retry/redirect/cache/auth/cookie/filter disabled");
        }
        return () -> Fabric.http(context);
    }

    /**
     * Validates the HTTPS request URI, applies the remaining attempt deadline and a connect timeout capped at five
     * seconds, then executes one Fabric call without transport retries.
     *
     * @param builder native Fabric request builder, used once for execution
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return native response which the caller must close
     */
    private static HttpResponse execute(HttpX.Builder builder, SecretContext context) {
        context.checkDeadline();
        requestUri(builder.request().url().uri());
        Duration remaining = timeout(context);
        Duration connect = remaining.compareTo(Duration.ofSeconds(5)) < 0 ? remaining : Duration.ofSeconds(5);
        builder.timeout(new Timeout(connect, remaining, remaining, remaining, Duration.ZERO, remaining));
        return builder.call().execute();
    }

    /**
     * Consumes a bounded response body within the attempt deadline. Restores any original Bus Source deadline before
     * closing the source, and never returns a borrowed buffer.
     *
     * @param response native Fabric response whose body is bounded and consumed by this operation
     * @param context  authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                 reference
     * @return new response-body bytes which the caller must erase
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private static byte[] read(HttpResponse response, SecretContext context) {
        context.checkDeadline();
        try (Source source = response.body().payload().source()) {
            var timeout = source.timeout();
            boolean hadDeadline = timeout.hasDeadline();
            long previous = hadDeadline ? timeout.deadlineNanoTime() : 0;
            long deadline = System.nanoTime() + timeout(context).toNanos();
            timeout.deadlineNanoTime(hadDeadline ? Math.min(previous, deadline) : deadline);
            try {
                // Materializer closes its input. Keep the actual source owned by this scope so its
                // original deadline is restored before response closure releases a pooled connection.
                var borrowed = new AssignSource(source) {

                    /**
                     * Checks the caller deadline before forwarding each bounded read to the borrowed source.
                     *
                     * @param sink  buffer receiving the next response chunk
                     * @param count maximum bytes requested from the source
                     * @return bytes read, or -1 when exhausted
                     * @throws java.io.IOException if the source fails or its I/O deadline expires
                     */
                    @Override
                    public long read(Buffer sink, long count) throws IOException {
                        context.checkDeadline();
                        timeout.throwIfReached();
                        return super.read(sink, count);
                    }

                    /**
                     * Leaves the borrowed source open; the enclosing response-read scope restores its deadline before
                     * closing the real source.
                     */
                    @Override
                    public void close() {
                    }
                };
                byte[] bytes = Payload.source(borrowed, response.body().length())
                        .bytes(Math.min(Normal.BYTES_PER_MB, response.body().materializeMaxBytes()));
                try {
                    context.checkDeadline();
                    return bytes;
                } catch (RuntimeException failure) {
                    Arrays.fill(bytes, (byte) 0);
                    throw failure;
                }
            } finally {
                if (hadDeadline) {
                    timeout.deadlineNanoTime(previous);
                } else {
                    timeout.clearDeadline();
                }
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    /**
     * Validates and normalizes a fixed HTTPS root endpoint, stripping the default HTTPS port and rejecting embedded
     * identities or alternate paths.
     *
     * @param uri complete backend request URI
     * @return normalized HTTPS root
     * @throws IllegalArgumentException if the endpoint is not a fixed HTTPS root with a valid host and no embedded
     *                                  identity
     */
    static URI endpoint(URI uri) {
        if (uri == null || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getRawQuery() != null || uri.getFragment() != null
                || !(uri.getRawPath().isEmpty() || Symbol.SLASH.equals(uri.getRawPath()))) {
            throw new IllegalArgumentException("A fixed HTTPS root endpoint is required");
        }
        try {
            return new URI(Protocol.HTTPS.name, null, StringKit.toLowerCase(uri.getHost()),
                    uri.getPort() == Port._443.getPort() ? -1 : uri.getPort(), Symbol.SLASH, null, null);
        } catch (URISyntaxException failure) {
            throw new IllegalArgumentException("Invalid HTTPS endpoint");
        }
    }

    /**
     * Checks an outgoing request URI for HTTPS safety and encoded or decoded traversal segments before any network I/O.
     *
     * @param uri complete backend request URI
     * @throws IllegalArgumentException if the URI is not a safe absolute HTTPS request URI
     */
    static void requestUri(URI uri) {
        if (uri == null || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getFragment() != null || !uri.getRawPath().startsWith(Symbol.SLASH)
                || !uri.normalize().getRawPath().equals(uri.getRawPath())
                || Arrays.stream(uri.getPath().split(Symbol.SLASH, -1))
                        .anyMatch(part -> part.equals(Symbol.DOT) || part.equals(Symbol.DOUBLE_DOT))
                || uri.getPath().chars().anyMatch(c -> Character.isISOControl(c) || c == Symbol.C_BACKSLASH)) {
            throw new IllegalArgumentException("A safe HTTPS request URI is required");
        }
    }

    /**
     * Accepts only an exact integral JSON number within signed-long range; fractional, overflowing or non-numeric
     * values are rejected.
     *
     * @param value backend JSON numeric value requiring exact signed-long conversion
     * @return validated integral value
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the value is non-numeric, fractional, or outside
     *                                                            the signed-long range
     */
    static long integer(Object value) {
        try {
            if (!(value instanceof Number number)) {
                throw new IllegalArgumentException();
            }
            return new BigDecimal(number.toString()).longValueExact();
        } catch (RuntimeException failure) {
            throw new CryptoException("Secret backend is unavailable");
        }
    }

    /**
     * Parses a backend epoch-seconds timestamp with exact integer validation. Malformed or unrepresentable times are
     * rejected.
     *
     * @param value optional backend epoch-seconds value
     * @return absolute UTC expiration instant, or null when the backend omitted it
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the timestamp is non-integral or cannot be
     *                                                            represented as an instant
     */
    static Instant epoch(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.ofEpochSecond(integer(value));
        } catch (RuntimeException failure) {
            throw new CryptoException("Secret backend is unavailable");
        }
    }

    /**
     * Maps Azure/Vault HTTP status to a sanitized Secret exception without exposing backend messages. Authentication
     * failures are never retried.
     *
     * @param status HTTP response status
     * @param body   decoded backend JSON object; raw error messages must not escape this boundary
     * @return sanitized status classification
     */
    static UncheckedException restError(int status, Map<?, ?> body) {
        if (status == Http.Status.UNAUTHORIZED || status == Http.Status.FORBIDDEN) {
            return new ForbiddenException("Secret access denied");
        }
        return new CryptoException("Secret backend rejected the request");
    }

    /**
     * Returns the lowercase raw authority, including a non-default port, for canonical signature headers.
     *
     * @param uri complete backend request URI
     * @return canonical Host header value
     */
    static String host(URI uri) {
        return StringKit.toLowerCase(uri.getRawAuthority());
    }

    /**
     * Encodes a UTF-8 component using the existing Bus RFC 3986 encoder; only unreserved characters remain literal.
     *
     * @param text unencoded UTF-8 query or path component
     * @return percent-encoded component
     */
    static String encode(String text) {
        return UrlEncoder.encodeAll(text, Charset.UTF_8);
    }

    /**
     * Sorts query fields by key and RFC 3986-encodes names and values for the cloud canonical query representation.
     *
     * @param values query fields sorted by key before RFC 3986 encoding
     * @return canonical query string without a leading question mark
     */
    static String query(Map<String, String> values) {
        return new TreeMap<>(values).entrySet().stream()
                .map(e -> encode(e.getKey()) + Symbol.EQUAL + encode(e.getValue()))
                .collect(Collectors.joining(Symbol.AND));
    }

    /**
     * Computes a Bus Crypto HMAC over UTF-8 input. This helper does not erase caller-owned key bytes; cryptographic
     * failures are replaced with a safe category.
     *
     * @param algorithm shared HMAC algorithm selected by the cloud signing protocol
     * @param key       caller-owned signing key bytes
     * @param input     string encoded as UTF-8 before authentication
     * @return new HMAC result bytes owned by the caller
     * @throws CryptoException if the requested HMAC cannot be initialized or computed
     */
    static byte[] hmac(Algorithm algorithm, byte[] key, String input) {
        try {
            return Builder.hmac(algorithm, key).digest(input, Charset.UTF_8);
        } catch (CryptoException failure) {
            throw new CryptoException("Secret backend is unavailable");
        }
    }

    /**
     * Computes a lowercase hexadecimal SHA-256 digest through Bus Crypto.
     *
     * @param bytes input bytes; the caller retains ownership
     * @return lowercase SHA-256 digest
     * @throws CryptoException if the SHA-256 digest cannot be computed
     */
    static String hash(byte[] bytes) {
        try {
            return Builder.sha256Hex(bytes);
        } catch (CryptoException failure) {
            throw new CryptoException("Secret backend is unavailable");
        }
    }

    /**
     * Computes a strictly positive remaining duration capped at ten seconds; expired requests fail before network
     * access.
     *
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return remaining attempt timeout
     * @throws CryptoException if the invocation deadline has been reached
     */
    static Duration timeout(SecretContext context) {
        context.checkDeadline();
        Duration remaining = Duration.between(Instant.now(), context.deadline());
        if (remaining.isNegative() || remaining.isZero()) {
            throw new CryptoException("Secret operation timed out");
        }
        return remaining.compareTo(Duration.ofSeconds(10)) < 0 ? remaining : Duration.ofSeconds(10);
    }

    /**
     * Parses a strict depth-limited JSON object and removes parser diagnostics from failures. Arrays, scalar roots and
     * malformed input are rejected.
     *
     * @param bytes input bytes; the caller retains ownership
     * @return neutral JSON object map
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if parsing fails, exceeds its depth bound, or does not
     *                                                            produce an object
     */
    static Map<?, ?> object(byte[] bytes) {
        try {
            Object parsed = JsonKit.toJavaValue(JsonKit.readValue(bytes, 32, true));
            if (parsed instanceof Map<?, ?> map) {
                return map;
            }
        } catch (RuntimeException ignored) {
            /* Never expose backend payload or parser diagnostics. */
        }
        throw new CryptoException("Secret backend is unavailable");
    }

    /**
     * Extracts a required JSON string from a backend response without copying arbitrary parser or service error details
     * into exceptions.
     *
     * @param map decoded JSON object
     * @param key field name or cache key to look up
     * @return string field value
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the selected property is absent, null, or not a
     *                                                            string
     */
    static String required(Map<?, ?> map, String key) {
        if (map.get(key) instanceof String value) {
            return value;
        }
        throw new CryptoException("Secret backend is unavailable");
    }

    /**
     * Copies a complete backend value into an owned character container with the actual immutable version, generation
     * and optional backend expiration. Temporary input characters are erased.
     *
     * @param value      complete backend plaintext to copy into owned storage
     * @param version    required nonblank immutable version actually returned by the backend
     * @param generation non-negative connection configuration generation attached to returned materials
     * @return caller-owned material
     */
    static SecretMaterial material(String value, String version, long generation) {
        return material(value, version, generation, null);
    }

    /**
     * Copies a complete backend value into an owned character container with the actual immutable version, generation
     * and optional backend expiration. Temporary input characters are erased.
     *
     * @param value      complete backend plaintext to copy into owned storage
     * @param version    required nonblank immutable version actually returned by the backend
     * @param generation non-negative connection configuration generation attached to returned materials
     * @param expiresAt  absolute backend or identity expiration; null means unknown, not unlimited validity
     * @return caller-owned material
     * @throws CryptoException if the backend version is blank or material metadata is invalid
     */
    static SecretMaterial material(String value, String version, long generation, Instant expiresAt) {
        if (version.isBlank()) {
            throw new CryptoException("Secret backend is unavailable");
        }
        char[] chars = value.toCharArray();
        try {
            return new SecretMaterial(chars, version, Instant.now(), expiresAt, generation);
        } finally {
            Arrays.fill(chars, Symbol.C_NUL);
        }
    }

    /**
     * Maps platform error codes and HTTP status into sanitized exceptions without exposing remote messages or resource
     * details.
     *
     * @param status HTTP response status
     * @param body   decoded backend JSON object; raw error messages must not escape this boundary
     * @return sanitized platform failure
     */
    static UncheckedException error(int status, Map<?, ?> body) {
        Object raw = body.containsKey("Code") ? body.get("Code") : body.get("__type");
        String code = raw instanceof String text ? text : Normal.EMPTY;
        if (code.contains(Symbol.HASH)) {
            code = StringKit.subAfter(code, Symbol.C_HASH, true);
        }
        if (status == Http.Status.UNAUTHORIZED || status == Http.Status.FORBIDDEN || code.contains("AccessDenied")
                || code.contains("Forbidden") || code.contains("InvalidAccessKey")
                || code.contains("InvalidClientToken") || code.contains("DecryptionFailure")
                || code.contains("Signature") || code.contains("ExpiredToken") || code.contains("UnrecognizedClient")) {
            return new ForbiddenException("Secret access denied");
        }
        if (status == Http.Status.TOO_MANY_REQUESTS || code.contains("Throttl") || code.contains("LimitExceeded")) {
            return new CryptoException("Secret backend is rate limited");
        }
        if (status == Http.Status.NOT_FOUND || code.contains("NotFound")) {
            return new CryptoException("Secret not found");
        }
        if (code.contains("InvalidParameter") || code.contains("InvalidArgument")) {
            return new CryptoException("Invalid secret reference");
        }
        return new CryptoException("Secret backend is unavailable");
    }

    /**
     * Shared HTTP-only transient policy selected explicitly by REST adapters. Client failures, redirects and gateway
     * timeout responses are terminal; native business-code policies remain in the relevant cloud provider.
     *
     * @param status native HTTP status
     * @return true for HTTP throttling or temporary internal/service/gateway failures
     */
    static boolean retryableStatus(int status) {
        return status == Http.Status.TOO_MANY_REQUESTS || status == Http.Status.INTERNAL_SERVER_ERROR
                || status == Http.Status.BAD_GATEWAY || status == Http.Status.SERVICE_UNAVAILABLE;
    }

    /**
     * Executes read-only cloud requests using adapter-selected response retry rules. The request factory receives a
     * capped attempt deadline, including credential acquisition. Local validation, authentication and timeout failures
     * never become retryable merely because they are CryptoException instances.
     *
     * @param request   factory of a fresh native request within the supplied attempt context
     * @param context   original authenticated caller and overall deadline
     * @param errors    platform mapping to a fresh, sanitized public exception
     * @param retryable platform decision based solely on HTTP status and native response fields
     * @return parsed success object
     */
    static Map<?, ?> exchange(
            Function<SecretContext, HttpX.Builder> request,
            SecretContext context,
            BiFunction<Integer, Map<?, ?>, UncheckedException> errors,
            BiPredicate<Integer, Map<?, ?>> retryable) {
        return exchange(request, context, errors, retryable, Function.identity());
    }

    /**
     * Executes at most three attempts, closing each response and erasing body buffers before backoff. Adapter response
     * decisions also cover business errors in HTTP 200 envelopes. Only recognized transport I/O faults can trigger
     * transport retries; unknown extension exceptions, forbidden access and native timeouts fail immediately.
     *
     * @param request   freshly authenticated request factory receiving the ten-second-capped attempt deadline
     * @param context   trusted identity and overall absolute deadline
     * @param errors    platform-specific status/error response mapping without raw messages or causes
     * @param retryable adapter-owned transient response decision; it must not inspect exception messages
     * @param decode    success-envelope validation, including HTTP 200 business errors where applicable
     * @return validated success object
     * @throws ForbiddenException if authentication or authorization is denied
     * @throws CryptoException    if the operation fails, expires or exhausts approved retries
     */
    static Map<?, ?> exchange(
            Function<SecretContext, HttpX.Builder> request,
            SecretContext context,
            BiFunction<Integer, Map<?, ?>, UncheckedException> errors,
            BiPredicate<Integer, Map<?, ?>> retryable,
            Function<Map<?, ?>, Map<?, ?>> decode) {
        for (int attempt = 0;; attempt++) {
            context.checkDeadline();
            Instant until = Instant.now().plusSeconds(10);
            if (context.deadline().isBefore(until)) {
                until = context.deadline();
            }
            SecretContext attemptContext = new SecretContext(context.tenantId(), context.appId(), context.principal(),
                    context.requestId(), context.purpose(), until);
            // Credential acquisition and local request construction are outside the retryable transport boundary.
            HttpX.Builder builder;
            try {
                builder = request.apply(attemptContext);
                attemptContext.checkDeadline();
            } catch (RuntimeException failure) {
                throw sanitize(failure);
            }
            try (HttpResponse response = execute(builder, attemptContext)) {
                attemptContext.checkDeadline();
                Map<?, ?> body;
                byte[] bytes = null;
                try {
                    bytes = read(response, attemptContext);
                    body = object(bytes);
                } catch (RuntimeException failure) {
                    var causes = ExceptionKit.getThrowableList(failure);
                    if (response.code() >= Http.Status.OK && response.code() < Http.Status.MULTIPLE_CHOICES
                            || causes.stream().anyMatch(
                                    cause -> cause instanceof IOException || cause instanceof ForbiddenException
                                            || cause instanceof TimeoutException
                                            || cause instanceof org.miaixz.bus.core.lang.exception.TimeoutException)) {
                        throw failure;
                    }
                    body = Map.of();
                } finally {
                    if (bytes != null) {
                        Arrays.fill(bytes, (byte) 0);
                    }
                }
                if (response.code() < Http.Status.OK || response.code() >= Http.Status.MULTIPLE_CHOICES) {
                    UncheckedException failure = errors.apply(response.code(), body);
                    if (failure instanceof ForbiddenException || attempt >= 2
                            || !retryable.test(response.code(), body)) {
                        throw failure;
                    }
                } else {
                    try {
                        return decode.apply(body);
                    } catch (RuntimeException failure) {
                        if (ExceptionKit.getThrowableList(failure).stream()
                                .anyMatch(ForbiddenException.class::isInstance) || attempt >= 2
                                || !retryable.test(response.code(), body)) {
                            throw sanitize(failure);
                        }
                    }
                }
            } catch (RuntimeException failure) {
                if (attempt >= 2 || !retryTransport(failure)) {
                    throw sanitize(failure);
                }
            }
            long delay = RandomKit.randomLong(25L << attempt, 50L << attempt);
            if (Duration.between(Instant.now(), context.deadline()).toMillis() <= delay || !ThreadKit.sleep(delay)) {
                throw new CryptoException("Secret operation timed out");
            }
        }
    }

    /**
     * Removes all source messages, causes and suppressed diagnostics, retaining only a forbidden exception type. This
     * method deliberately does not manufacture machine-readable categories or preserve custom error fields.
     *
     * @param failure internal failure or an exception from an extension
     * @return new exception with fixed, non-sensitive text and no cause
     */
    private static UncheckedException sanitize(Throwable failure) {
        boolean forbidden = ExceptionKit.getThrowableList(failure).stream()
                .anyMatch(ForbiddenException.class::isInstance);
        return forbidden ? new ForbiddenException("Secret access denied")
                : new CryptoException("Secret request failed");
    }

    /**
     * Identifies transient failures inside native HTTP execution only, never credential discovery or local parsing. A
     * timeout or forbidden cause vetoes retries even when an outer exception also wraps an I/O failure.
     *
     * @param failure native execution or response-close failure
     * @return true only for non-timeout I/O failures
     */
    private static boolean retryTransport(Throwable failure) {
        var causes = ExceptionKit.getThrowableList(failure);
        if (causes.stream().anyMatch(
                cause -> cause instanceof ForbiddenException || cause instanceof InterruptedIOException
                        || cause instanceof TimeoutException
                        || cause instanceof org.miaixz.bus.core.lang.exception.TimeoutException)) {
            return false;
        }
        return causes.stream().anyMatch(IOException.class::isInstance);
    }

}
