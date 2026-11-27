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
package org.miaixz.bus.spring.boot.environment;

import java.io.IOException;
import java.net.*;
import java.nio.channels.UnresolvedAddressException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.cortex.setting.secret.SealingSecretCodec;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.cert.CertificatePolicy;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.retry.HttpRetryPolicy;

/**
 * Retrieves and authenticates a published configuration before the Spring application context starts.
 * <p>
 * A fetch owns its Fabric context and applies a shared deadline across all attempts. The result distinguishes temporary
 * unavailability from authentication, transport security, and invalid snapshot failures.
 *
 * @author Kimi Liu
 */
public class EnvironmentConfigurationFetcher {

    /**
     * Maximum number of HTTP requests per fetch.
     */
    private static final int ATTEMPTS = 3;
    /**
     * Shared request and backoff budget in nanoseconds.
     */
    private static final long BUDGET = Duration.ofSeconds(10).toNanos();
    /**
     * Fabric policy that leaves retries to the fetcher and disables redirects.
     */
    private static final HttpRetryPolicy RETRY = HttpRetryPolicy.builder().maxAttempts(0).maxStaleRetries(0)
            .maxRedirects(0).retryOnConnectionFailure(false).build();

    /**
     * Configured startup configuration endpoint.
     */
    private final URI endpoint;
    /**
     * Application code used for authorization and response validation.
     */
    private final String app;
    /**
     * Bearer credential scoped to the application.
     */
    private final String credential;
    /**
     * Optional application delivery key file required by encrypted configuration items.
     */
    private final Path keyFile;
    /**
     * Optional PEM trust file for the gateway connection.
     */
    private final Path caFile;

    /**
     * Creates a fetcher for one registered application.
     *
     * @param endpoint   validated HTTPS registration URL
     * @param app        application code
     * @param credential application reader credential
     * @param keyFile    delivery key file, or {@code null} when delivery keys are absent
     * @param caFile     PEM trust file, or {@code null} to use the default trust roots
     */
    EnvironmentConfigurationFetcher(URI endpoint, String app, String credential, Path keyFile, Path caFile) {
        this.endpoint = endpoint;
        this.app = app;
        this.credential = credential;
        this.keyFile = keyFile;
        this.caFile = caFile;
    }

    /**
     * Represents one validated release or a status that permits file fallback.
     *
     * @param revision    release identifier, or {@code null} when unavailable
     * @param configs     decoded JSON objects by configuration code, empty when unavailable
     * @param unavailable fallback reason, or {@code null} when a release was read
     */
    record Snapshot(String revision, Map<String, Object> configs, String unavailable) {

        /**
         * Creates a result that permits the caller to read its fallback file.
         *
         * @param reason stable reason code for startup diagnostics
         * @return unavailable snapshot result
         */
        static Snapshot unavailable(String reason) {
            return new Snapshot(null, Map.of(), reason);
        }

    }

    /**
     * Fetches and decodes a stable release within the retry budget.
     *
     * @return the published snapshot or the reason it is temporarily unavailable
     * @throws IOException           if the gateway trust configuration cannot be loaded
     * @throws IllegalStateException if the response is rejected, the snapshot is invalid, or startup is interrupted
     */
    Snapshot fetch() throws IOException {
        final long deadline = System.nanoTime() + BUDGET;
        try (Context context = Context.builder().options(Options.of(TlsPolicy.OPTION, tls())).build()) {
            for (int attempt = 0; attempt < ATTEMPTS && deadline - System.nanoTime() > 0; attempt++) {
                try {
                    Snapshot snapshot = request(context, deadline);
                    if (snapshot != null) {
                        return snapshot;
                    }
                } catch (RuntimeException failure) {
                    if (!transientFailure(failure)) {
                        throw failure;
                    }
                }
                if (attempt + 1 < ATTEMPTS) {
                    try {
                        long pause = Duration.ofMillis(ThreadLocalRandom.current().nextLong(100, 300)).toNanos();
                        long remaining = deadline - System.nanoTime();
                        if (remaining > 0) {
                            Thread.sleep(Duration.ofNanos(Math.min(pause, remaining)));
                        }
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("SDC startup read interrupted");
                    }
                }
            }
        }
        return Snapshot.unavailable("SDC_UNAVAILABLE");
    }

    /**
     * Requests configuration using the remaining budget and a nonce unique to this attempt.
     *
     * @param context  Fabric context for the fetch
     * @param deadline monotonic deadline in nanoseconds
     * @return decoded result, or {@code null} when there is no time left or the gateway reports a transient failure
     * @throws IllegalStateException if the gateway rejects the request or returns an invalid response
     */
    private Snapshot request(Context context, long deadline) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
            return null;
        }
        Duration call = Duration.ofNanos(Math.min(remaining, Duration.ofSeconds(4).toNanos()));
        Timeout timeout = Timeout.builder().call(call).connect(shorter(call, Duration.ofSeconds(1)))
                .read(shorter(call, Duration.ofSeconds(3))).build();
        String nonce = SealingSecretCodec.nonce();
        try (var response = Fabric.http(context).get(endpoint.toASCIIString()).query("app_id", app)
                .query("configs", "all").header(Http.Header.ACCEPT, MediaType.APPLICATION_VND_MIAIXZ_JSON)
                .header("Authorization", "Bearer " + credential).header(SealingSecretCodec.NONCE, nonce)
                .retryPolicy(RETRY).timeout(timeout).execute()) {
            int status = response.code();
            if (status == 502 || status == 503 || status == 504) {
                return null;
            }
            if (status != 200 && status != 404) {
                throw new IllegalStateException(
                        "SDC refused the startup request (HTTP " + status + Symbol.PARENTHESE_RIGHT);
            }
            String media = response.headers().get(Http.Header.CONTENT_TYPE);
            if (media == null || !MediaType.APPLICATION_VND_MIAIXZ_JSON
                    .equalsIgnoreCase(media.split(Symbol.SEMICOLON, 2)[0].strip())) {
                throw new IllegalStateException("Invalid SDC startup response media type");
            }
            Map<?, ?> envelope = object(response.bytes(SealingSecretCodec.MAX_BYTES));
            Map<?, ?> payload = envelope.get("data") instanceof Map<?, ?> map ? map : Map.of();
            String code = String.valueOf(envelope.get("code"));
            if (!app.equals(payload.get("app_id"))) {
                throw new IllegalStateException("SDC startup response has a different application");
            }
            if (SealingSecretCodec.NOT_PUBLISHED.equals(code) && (status == 200 || status == 404)) {
                return Snapshot.unavailable(code);
            }
            if (status != 200 || !"0".equals(code)
                    || !String.valueOf(SealingSecretCodec.SCHEMA).equals(String.valueOf(payload.get("config_schema")))
                    || !(payload.get("config_revision") instanceof String revision)
                    || !SealingSecretCodec.validCoordinate(revision)
                    || !(payload.get("configs") instanceof Map<?, ?> configs) || configs.isEmpty()) {
                throw new IllegalStateException("Invalid SDC startup response");
            }
            return new Snapshot(revision, decode(configs, revision, nonce), null);
        }
    }

    /**
     * Decodes each JSON item and authenticates encrypted items against the application, release, and nonce.
     *
     * @param configs  response items indexed by configuration code
     * @param revision published release identifier
     * @param nonce    request nonce bound to encrypted response items
     * @return immutable map of decoded configuration items
     * @throws IllegalStateException if an item cannot be authenticated or decoded
     */
    private Map<String, Object> decode(Map<?, ?> configs, String revision, String nonce) {
        var values = new LinkedHashMap<String, Object>();
        Map<String, byte[]> keys = null;
        try {
            for (var entry : configs.entrySet()) {
                if (!(entry.getKey() instanceof String code) || !SealingSecretCodec.validCoordinate(code)
                        || !(entry.getValue() instanceof String value)) {
                    throw new IllegalStateException("Invalid SDC configuration item");
                }
                if (SealingSecretCodec.sealed(value)) {
                    if (keys == null) {
                        keys = keys();
                    }
                    var codec = new SealingSecretCodec(keys, app, revision, code, nonce);
                    byte[] clear = codec.open(value);
                    try {
                        values.put(code, object(clear));
                    } finally {
                        Arrays.fill(clear, (byte) 0);
                    }
                } else {
                    values.put(code, object(value.getBytes(Charset.UTF_8)));
                }
            }
            return Map.copyOf(values);
        } catch (IOException | GeneralSecurityException | IllegalArgumentException failure) {
            throw new IllegalStateException("SDC configuration could not be authenticated or decoded");
        } finally {
            if (keys != null) {
                keys.values().forEach(key -> Arrays.fill(key, (byte) 0));
            }
        }
    }

    /**
     * Loads application delivery keys from the configured properties file.
     *
     * @return decoded key material by key identifier
     * @throws IOException              if the key file is unavailable, exceeds its size limit, or cannot be read
     * @throws IllegalArgumentException if a key value is incorrectly encoded or has the wrong length
     */
    private Map<String, byte[]> keys() throws IOException {
        if (keyFile == null || !Files.isRegularFile(keyFile) || Files.size(keyFile) > 4096) {
            throw new IOException("SDC key file is missing or invalid");
        }
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(keyFile, Charset.UTF_8)) {
            properties.load(reader);
        }
        Map<String, byte[]> keys = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            keys.put(key, SealingSecretCodec.key(properties.getProperty(key)));
        }
        return keys;
    }

    /**
     * Builds a hostname-verifying TLS policy with the selected gateway trust roots.
     *
     * @return Fabric transport policy
     * @throws IOException if the configured PEM trust file cannot be read or parsed
     */
    private TlsPolicy tls() throws IOException {
        var settings = TlsPolicy.defaults().settings().toBuilder().versions(List.of("TLSv1.3", "TLSv1.2"))
                .allEnabledCipherSuites().verifyHostname(true);
        if (caFile == null) {
            return TlsPolicy.of(TlsContext.defaults(), settings.build());
        }
        if (!Files.isRegularFile(caFile) || Files.size(caFile) > SealingSecretCodec.MAX_BYTES) {
            throw new IOException("Invalid SDC trust file");
        }
        try (var input = Files.newInputStream(caFile)) {
            var roots = CertificateFactory.getInstance("X.509").generateCertificates(input).stream()
                    .map(X509Certificate.class::cast).toArray(X509Certificate[]::new);
            if (roots.length == 0) {
                throw new GeneralSecurityException("Missing SDC trust roots");
            }
            var certificate = CertificatePolicy.builder().trustRoots(roots).build();
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(null, new TrustManager[] { certificate.trustManager() }, null);
            return TlsPolicy.of(TlsContext.of(ssl), settings.certificate(certificate).build());
        } catch (GeneralSecurityException failure) {
            throw new IOException("Invalid SDC trust configuration");
        }
    }

    /**
     * Chooses the smaller duration so an individual timeout stays within the remaining budget.
     *
     * @param first  first duration
     * @param second second duration
     * @return the shorter duration
     */
    private static Duration shorter(Duration first, Duration second) {
        return first.compareTo(second) < 0 ? first : second;
    }

    /**
     * Classifies transport availability failures across the cause chain.
     *
     * @param error transport failure
     * @return {@code true} when the cause chain contains a retryable availability failure and no TLS or certificate
     *         error
     */
    private static boolean transientFailure(Throwable error) {
        boolean availability = false;
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SSLException || cause instanceof java.security.cert.CertificateException) {
                return false;
            }
            availability |= cause instanceof ConnectException || cause instanceof NoRouteToHostException
                    || cause instanceof SocketTimeoutException || cause instanceof TimeoutException
                    || cause instanceof UnknownHostException || cause instanceof UnresolvedAddressException
                    || cause instanceof java.util.concurrent.TimeoutException;
        }
        return availability;
    }

    /**
     * Parses one JSON object using the startup protocol's depth and duplicate-key checks.
     *
     * @param bytes UTF-8 JSON
     * @return object members represented as Java values
     * @throws IllegalStateException if valid JSON has a top-level value other than an object
     */
    private static Map<?, ?> object(byte[] bytes) {
        if (JsonKit.toJavaValue(JsonKit.readValue(bytes, 16, true)) instanceof Map<?, ?> values) {
            return values;
        }
        throw new IllegalStateException("Expected an SDC configuration object");
    }

}
