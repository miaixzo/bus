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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.cortex.setting.secret.SecretProvider;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Alibaba KMS 2016-01-20 shared-gateway RPC API; not the dedicated KMS gateway protocol.
 */
public final class AliyunSecretProvider implements SecretProvider {

    /**
     * Validated fixed HTTPS root; never selected from an untrusted Secret reference.
     */
    private final URI endpoint;
    /**
     * Cloud region included in protocol routing or signature scope.
     */
    private final String region;
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
     * Generator of a new Aliyun signature nonce on every actual attempt.
     */
    private final Supplier<String> nonce;

    /**
     * Creates a connection-bound AliyunSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param region        non-empty cloud region used for routing and signature scope
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param credentials   non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public AliyunSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Context fabricContext) {
        this(region, endpoint, generation, credentials, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound AliyunSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param region        non-empty cloud region used for routing and signature scope
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param credentials   non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @param clock         non-null UTC time source used for signing timestamps
     * @param nonce         non-null generator of a fresh signature nonce for each Aliyun attempt
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public AliyunSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Context fabricContext, Clock clock, Supplier<String> nonce) {
        this(region, endpoint, generation, credentials, CloudProtocol.http(fabricContext), clock, nonce);
    }

    /**
     * Creates a connection-bound AliyunSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param region      non-empty cloud region used for routing and signature scope
     * @param endpoint    fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation  non-negative connection configuration generation attached to returned materials
     * @param credentials non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param http        trusted native Fabric builder factory, injected for deterministic protocol tests
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    AliyunSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http) {
        this(region, endpoint, generation, credentials, http, Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    /**
     * Creates a connection-bound AliyunSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param region      non-empty cloud region used for routing and signature scope
     * @param endpoint    fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation  non-negative connection configuration generation attached to returned materials
     * @param credentials non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param http        trusted native Fabric builder factory, injected for deterministic protocol tests
     * @param clock       non-null UTC time source used for signing timestamps
     * @param nonce       non-null generator of a fresh signature nonce for each Aliyun attempt
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    AliyunSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http, Clock clock, Supplier<String> nonce) {
        if (region == null || !region.matches("[a-z0-9-]+") || generation < 0) {
            throw new IllegalArgumentException("Invalid Alibaba connection");
        }
        this.region = region;
        this.endpoint = CloudProtocol.endpoint(endpoint);
        this.generation = generation;
        this.credentials = Objects.requireNonNull(credentials);
        this.http = Objects.requireNonNull(http);
        this.clock = Objects.requireNonNull(clock);
        this.nonce = Objects.requireNonNull(nonce);
    }

    /**
     * Computes the Aliyun RPC HMAC-SHA1 signature over sorted RFC 3986 form parameters. The protocol appends an
     * ampersand to the secret key; derived key bytes are erased before return.
     *
     * @param parameters unsigned RPC form parameters sorted and encoded by the signing protocol
     * @param identity   live caller-owned signing credentials; this operation does not close them
     * @return Base64-encoded signature text
     */
    static String signature(Map<String, String> parameters, CloudCredentials identity) {
        byte[] key = identity.signingKey("", "&");
        try {
            return Base64.encode(
                    CloudProtocol.hmac(
                            "HmacSHA1",
                            key,
                            "POST&%2F&" + CloudProtocol.encode(CloudProtocol.query(parameters))));
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Selects response retries from native cloud error fields and transient HTTP status, never exception messages.
     * Invalid references and missing resources are terminal even if a service returns an unusual server status.
     *
     * @param status native HTTP status
     * @param body   native decoded error response
     * @return true only for explicit throttling or temporary service failures
     */
    private static boolean retryable(int status, Map<?, ?> body) {
        Object raw = body.containsKey("Code") ? body.get("Code") : body.get("__type");
        String code = raw instanceof String text ? text : "";
        if (code.contains("NotFound") || code.contains("InvalidParameter") || code.contains("InvalidArgument")
                || code.contains("InvalidRequest")) {
            return false;
        }
        return CloudProtocol.retryableStatus(status) || code.contains("Throttl") || code.contains("LimitExceeded")
                || code.contains("InternalError") || code.contains("InternalServiceError")
                || code.contains("ServiceUnavailable") || code.contains("SystemBusy");
    }

    /**
     * Returns the stable provider identifier used to validate connection bindings.
     *
     * @return provider type identifier
     */
    public String type() {
        return "ALIYUN";
    }

    /**
     * Returns the immutable set of supported read modes. Advertising a capability does not grant authorization.
     *
     * @return supported backend operations
     */
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
    public SecretMaterial read(SecretReference reference, SecretContext context) {
        Map<?, ?> body = CloudProtocol.exchange(
                attempt -> request(reference, attempt),
                context,
                CloudProtocol::error,
                AliyunSecretProvider::retryable);
        if (!"text".equals(body.get("SecretDataType"))) {
            throw new CryptoException("Secret operation or payload is not supported");
        }
        String version = CloudProtocol.required(body, "VersionId");
        if (reference.version() != null && !reference.version().equals(version)) {
            throw new CryptoException("Requested secret version was not returned");
        }
        return CloudProtocol.material(CloudProtocol.required(body, "SecretData"), version, generation);
    }

    /**
     * Builds a fresh native Fabric request using a newly acquired credential, closing the credential after
     * header/signature creation. Request execution and retries remain in CloudProtocol.
     *
     * @param reference backend reference; field extraction is performed by the manager
     * @param context   authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                  reference
     * @return authenticated native request builder
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private HttpX.Builder request(SecretReference reference, SecretContext context) {
        TreeMap<String, String> parameters = new TreeMap<>();
        parameters.put("Action", "GetSecretValue");
        parameters.put("Version", "2016-01-20");
        parameters.put("Format", "JSON");
        parameters.put("RegionId", region);
        parameters.put("SignatureMethod", "HMAC-SHA1");
        parameters.put("SignatureVersion", "1.0");
        parameters.put("SignatureNonce", nonce.get());
        parameters.put(
                "Timestamp",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                        .format(clock.instant()));
        parameters.put("SecretName", reference.resource());
        if (reference.version() != null) {
            parameters.put("VersionId", reference.version());
        } else {
            parameters.put("VersionStage", "ACSCurrent");
        }
        try (CloudCredentials identity = credentials.acquire(context)) {
            parameters.put("AccessKeyId", identity.accessKeyId());
            if (identity.token() != null) {
                parameters.put("SecurityToken", identity.token());
            }
            parameters.put("Signature", signature(parameters, identity));
            // POST form keeps the signed identity and resource out of the request URI.
            return http.get().to(endpoint.toString()).header("Content-Type", "application/x-www-form-urlencoded").post()
                    .body(CloudProtocol.query(parameters));
        }
    }

}
