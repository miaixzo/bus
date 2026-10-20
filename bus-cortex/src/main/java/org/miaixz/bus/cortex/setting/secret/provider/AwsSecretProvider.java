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
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.cortex.setting.secret.SecretProvider;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * AWS Secrets Manager GetSecretValue, signed directly with AWS Signature Version 4.
 */
public final class AwsSecretProvider implements SecretProvider {

    /**
     * Cloud region included in protocol routing or signature scope.
     */
    private final String region;
    /**
     * Validated fixed HTTPS root; never selected from an untrusted Secret reference.
     */
    private final URI endpoint;
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
     * Creates a connection-bound AwsSecretProvider. The provider borrows its Fabric runtime and obtains fresh
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
    public AwsSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Context fabricContext) {
        this(region, endpoint, generation, credentials, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound AwsSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param region        non-empty cloud region used for routing and signature scope
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param credentials   non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @param clock         non-null UTC time source used for signing timestamps
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public AwsSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Context fabricContext, Clock clock) {
        this(region, endpoint, generation, credentials, CloudProtocol.http(fabricContext), clock);
    }

    /**
     * Creates a connection-bound AwsSecretProvider. The provider borrows its Fabric runtime and obtains fresh
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
    AwsSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http) {
        this(region, endpoint, generation, credentials, http, Clock.systemUTC());
    }

    /**
     * Creates a connection-bound AwsSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param region      non-empty cloud region used for routing and signature scope
     * @param endpoint    fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation  non-negative connection configuration generation attached to returned materials
     * @param credentials non-null source returning fresh, caller-owned signing credentials for each HTTP attempt
     * @param http        trusted native Fabric builder factory, injected for deterministic protocol tests
     * @param clock       non-null UTC time source used for signing timestamps
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    AwsSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http, Clock clock) {
        if (region == null || !region.matches("[a-z0-9-]+") || generation < 0) {
            throw new IllegalArgumentException("Invalid AWS connection");
        }
        this.region = region;
        this.endpoint = CloudProtocol.endpoint(endpoint);
        this.generation = generation;
        this.credentials = Objects.requireNonNull(credentials);
        this.http = Objects.requireNonNull(http);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Computes the platform canonical signature over the exact request data. Derived key buffers are erased where
     * allocated; supplied credentials remain owned by the caller.
     *
     * @param endpoint fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param region   non-empty cloud region used for routing and signature scope
     * @param payload  exact UTF-8 request body bytes covered by the signature
     * @param identity live caller-owned signing credentials; this operation does not close them
     * @param time     UTC signing instant
     * @return request authentication fields required by this platform
     */
    static Map<String, String> sign(
            URI endpoint,
            String region,
            byte[] payload,
            CloudCredentials identity,
            Instant time) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(time);
        String date = timestamp.substring(0, 8);
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("content-type", "application/x-amz-json-1.1");
        headers.put("host", CloudProtocol.host(endpoint));
        headers.put("x-amz-date", timestamp);
        headers.put("x-amz-target", "secretsmanager.GetSecretValue");
        if (identity.token() != null) {
            headers.put("x-amz-security-token", identity.token());
        }
        String names = String.join(";", headers.keySet());
        String canonicalHeaders = headers.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue().trim() + "\n")
                .collect(java.util.stream.Collectors.joining());
        String canonical = "POST\n/\n\n" + canonicalHeaders + "\n" + names + "\n" + CloudProtocol.hash(payload);
        String scope = date + "/" + region + "/secretsmanager/aws4_request";
        String toSign = "AWS4-HMAC-SHA256\n" + timestamp + "\n" + scope + "\n"
                + CloudProtocol.hash(canonical.getBytes(StandardCharsets.UTF_8));
        byte[] initial = identity.signingKey("AWS4", "");
        byte[] day = null, area = null, service = null, key = null;
        try {
            day = CloudProtocol.hmac("HmacSHA256", initial, date);
            area = CloudProtocol.hmac("HmacSHA256", day, region);
            service = CloudProtocol.hmac("HmacSHA256", area, "secretsmanager");
            key = CloudProtocol.hmac("HmacSHA256", service, "aws4_request");
            String signature = HexKit.encodeString(CloudProtocol.hmac("HmacSHA256", key, toSign));
            headers.put(
                    "authorization",
                    "AWS4-HMAC-SHA256 Credential=" + identity.accessKeyId() + "/" + scope + ", SignedHeaders=" + names
                            + ", Signature=" + signature);
            return headers;
        } finally {
            for (byte[] bytes : new byte[][] { initial, day, area, service, key }) {
                if (bytes != null) {
                    Arrays.fill(bytes, (byte) 0);
                }
            }
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
        return "AWS";
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
                AwsSecretProvider::retryable);
        if (!body.containsKey("SecretString") && body.containsKey("SecretBinary")) {
            throw new CryptoException("Secret operation or payload is not supported");
        }
        String version = CloudProtocol.required(body, "VersionId");
        if (reference.version() != null && !reference.version().equals(version)) {
            throw new CryptoException("Requested secret version was not returned");
        }
        return CloudProtocol.material(CloudProtocol.required(body, "SecretString"), version, generation);
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
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("SecretId", reference.resource());
        if (reference.version() != null) {
            payload.put("VersionId", reference.version());
        } else {
            payload.put("VersionStage", "AWSCURRENT");
        }
        byte[] body = JsonKit.toJsonString(payload).getBytes(StandardCharsets.UTF_8);
        try (CloudCredentials identity = credentials.acquire(context)) {
            Map<String, String> signed = sign(endpoint, region, body, identity, clock.instant());
            HttpX.Builder request = http.get().to(endpoint.toString()).post().body(body);
            signed.forEach(request::header);
            return request;
        }
    }

}
