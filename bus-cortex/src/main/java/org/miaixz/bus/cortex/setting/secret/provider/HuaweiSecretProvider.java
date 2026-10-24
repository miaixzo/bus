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

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.cortex.setting.secret.SecretProvider;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Direct Huawei Cloud CSMS ShowSecretVersion using SDK-HMAC-SHA256 (no signing SDK).
 *
 * @author Kimi Liu
 */
public final class HuaweiSecretProvider implements SecretProvider {

    /**
     * Validated fixed HTTPS root; never selected from an untrusted Secret reference.
     */
    private final URI endpoint;
    /**
     * Validated Huawei project identifier used in the URL and signed headers.
     */
    private final String projectId;
    /**
     * Connection configuration generation attached to every returned material.
     */
    private final long generation;
    /**
     * Fresh AK/SK source used separately for each actual HTTP attempt.
     */
    private final CloudCredentials.Source credentials;
    /**
     * Trusted factory of one native Fabric builder per attempt; does not own its runtime.
     */
    private final Supplier<HttpX.Builder> http;
    /**
     * Clock used to produce deterministic UTC signing timestamps.
     */
    private final Clock clock;

    /**
     * Creates a connection-bound HuaweiSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param projectId     Huawei project identifier included in both request path and signed headers
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param credentials   non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public HuaweiSecretProvider(URI endpoint, String projectId, long generation, CloudCredentials.Source credentials,
            Context fabricContext) {
        this(endpoint, projectId, generation, credentials, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound HuaweiSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param projectId     Huawei project identifier included in both request path and signed headers
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param credentials   non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @param clock         non-null UTC time source used for signing timestamps
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public HuaweiSecretProvider(URI endpoint, String projectId, long generation, CloudCredentials.Source credentials,
            Context fabricContext, Clock clock) {
        this(endpoint, projectId, generation, credentials, CloudProtocol.http(fabricContext), clock);
    }

    /**
     * Creates a connection-bound HuaweiSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint    fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param projectId   Huawei project identifier included in both request path and signed headers
     * @param generation  non-negative connection configuration generation attached to returned materials
     * @param credentials non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param http        trusted native Fabric builder factory, injected for deterministic protocol tests
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    HuaweiSecretProvider(URI endpoint, String projectId, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http) {
        this(endpoint, projectId, generation, credentials, http, Clock.systemUTC());
    }

    /**
     * Creates a connection-bound HuaweiSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint    fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param projectId   Huawei project identifier included in both request path and signed headers
     * @param generation  non-negative connection configuration generation attached to returned materials
     * @param credentials non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param http        trusted native Fabric builder factory, injected for deterministic protocol tests
     * @param clock       non-null UTC time source used for signing timestamps
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    HuaweiSecretProvider(URI endpoint, String projectId, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http, Clock clock) {
        if (projectId == null || !PatternKit.isMatch(Pattern.get("[a-fA-F0-9]{32}", 0), projectId) || generation < 0) {
            throw new IllegalArgumentException("Invalid Huawei connection");
        }
        this.endpoint = CloudProtocol.endpoint(endpoint);
        this.projectId = projectId;
        this.generation = generation;
        this.credentials = Objects.requireNonNull(credentials);
        this.http = Objects.requireNonNull(http);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Computes the platform canonical signature over the exact request data. Derived key buffers are erased where
     * allocated; supplied credentials remain owned by the caller.
     *
     * @param uri       complete backend request URI
     * @param projectId Huawei project identifier included in both request path and signed headers
     * @param identity  live caller-owned signing credentials; this operation does not close them
     * @param instant   UTC signing instant
     * @return request authentication fields required by this platform
     */
    static Map<String, String> sign(URI uri, String projectId, CloudCredentials identity, Instant instant) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(instant);
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("content-type", MediaType.APPLICATION_JSON);
        headers.put("host", CloudProtocol.host(uri));
        headers.put("x-project-id", projectId);
        headers.put("x-sdk-date", timestamp);
        if (identity.token() != null) {
            headers.put("x-security-token", identity.token());
        }
        String names = String.join(Symbol.SEMICOLON, headers.keySet());
        String canonicalHeaders = headers.entrySet().stream()
                .map(e -> e.getKey() + Symbol.COLON + e.getValue().trim() + Symbol.LF).collect(Collectors.joining());
        // Input is a validated CSMS read path: no arbitrary query or pre-encoded user component.
        String path = uri.getRawPath().endsWith(Symbol.SLASH) ? uri.getRawPath() : uri.getRawPath() + Symbol.SLASH;
        String canonical = "GET\n" + path + "\n\n" + canonicalHeaders + Symbol.LF + names + Symbol.LF
                + CloudProtocol.hash(Normal.EMPTY_BYTE_ARRAY);
        String toSign = "SDK-HMAC-SHA256\n" + timestamp + Symbol.LF
                + CloudProtocol.hash(canonical.getBytes(Charset.UTF_8));
        byte[] key = identity.signingKey(Normal.EMPTY, Normal.EMPTY);
        try {
            String signature = HexKit.encodeString(CloudProtocol.hmac(Algorithm.HMACSHA256, key, toSign));
            headers.put(
                    "authorization",
                    "SDK-HMAC-SHA256 Access=" + identity.accessKeyId() + ", SignedHeaders=" + names + ", Signature="
                            + signature);
            return headers;
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Returns the stable provider identifier used to validate connection bindings.
     *
     * @return provider type identifier
     */
    @Override
    public String type() {
        return "HUAWEI";
    }

    /**
     * Returns the immutable set of supported read modes. Advertising a capability does not grant authorization.
     *
     * @return supported backend operations
     */
    @Override
    public Set<SecretProvider.Capability> capabilities() {
        return Set.of(SecretProvider.Capability.READ_CURRENT, SecretProvider.Capability.READ_VERSION);
    }

    /**
     * Reads the complete backend payload using fresh credentials per HTTP attempt. Validates the actual version and
     * lifetime before returning; field extraction belongs to the manager.
     *
     * @param reference backend reference; field extraction is performed by the manager
     * @param context   authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                  reference
     * @return caller-owned material with the actual backend version
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    @Override
    public SecretMaterial read(SecretReference reference, SecretContext context) {
        if (!PatternKit.isMatch(Pattern.get("[a-zA-Z0-9_-]{1,64}", 0), reference.resource())) {
            throw new CryptoException("Invalid secret reference");
        }
        boolean fixed = reference.version() != null;
        String selected = fixed ? reference.version() : "latest";
        if (fixed && !PatternKit.isMatch(Pattern.get("v[0-9]{1,10}", 0), selected)) {
            throw new CryptoException("Invalid secret reference");
        }
        URI uri = URI
                .create(endpoint + "v1/" + projectId + "/secrets/" + reference.resource() + "/versions/" + selected);
        Map<?, ?> response = CloudProtocol.exchange(
                attempt -> request(uri, attempt),
                context,
                CloudProtocol::restError,
                (status, body) -> CloudProtocol.retryableStatus(status));
        if (!(response.get("version") instanceof Map<?, ?> version)
                || !(version.get("version_metadata") instanceof Map<?, ?> metadata)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        String actual = CloudProtocol.required(metadata, "id");
        if (!PatternKit.isMatch(Pattern.get("v[0-9]{1,10}", 0), actual)
                || !reference.resource().equals(CloudProtocol.required(metadata, "secret_name"))) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (fixed && !selected.equals(actual)) {
            throw new CryptoException("Requested secret version was not returned");
        }
        Object binaryValue = version.get("secret_binary");
        if (binaryValue != null && !(binaryValue instanceof String)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (binaryValue instanceof String binary && !binary.isEmpty()) {
            throw new CryptoException("Secret operation or payload is not supported");
        }
        Instant expiry = null;
        if (metadata.get("expire_time") != null) {
            try {
                expiry = Instant.ofEpochMilli(CloudProtocol.integer(metadata.get("expire_time")));
            } catch (RuntimeException failure) {
                throw new CryptoException("Secret backend is unavailable");
            }
            if (!Instant.now().isBefore(expiry)) {
                throw new CryptoException("Secret value has expired");
            }
        }
        context.checkDeadline();
        return CloudProtocol.material(CloudProtocol.required(version, "secret_string"), actual, generation, expiry);
    }

    /**
     * Builds a fresh native Fabric request using a newly acquired credential, closing the credential after
     * header/signature creation. Request execution and retries remain in CloudProtocol.
     *
     * @param uri     complete backend request URI
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return authenticated native request builder
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private HttpX.Builder request(URI uri, SecretContext context) {
        try (CloudCredentials identity = credentials.acquire(context)) {
            Map<String, String> headers = sign(uri, projectId, identity, clock.instant());
            HttpX.Builder request = http.get().to(uri.toString()).get();
            headers.forEach(request::header);
            return request;
        }
    }

}
