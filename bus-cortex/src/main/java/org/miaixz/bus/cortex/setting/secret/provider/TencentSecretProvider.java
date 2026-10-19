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
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.cortex.setting.secret.SecretProvider;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Direct Tencent Cloud SSM GetSecretValue with TC3 signing, including HTTP-200 business errors.
 */
public final class TencentSecretProvider implements SecretProvider {

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
     * Creates a connection-bound TencentSecretProvider. The provider borrows its Fabric runtime and obtains fresh
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
    public TencentSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Context fabricContext) {
        this(region, endpoint, generation, credentials, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound TencentSecretProvider. The provider borrows its Fabric runtime and obtains fresh
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
    public TencentSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Context fabricContext, Clock clock) {
        this(region, endpoint, generation, credentials, CloudProtocol.http(fabricContext), clock);
    }

    /**
     * Creates a connection-bound TencentSecretProvider. The provider borrows its Fabric runtime and obtains fresh
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
    TencentSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http) {
        this(region, endpoint, generation, credentials, http, Clock.systemUTC());
    }

    /**
     * Creates a connection-bound TencentSecretProvider. The provider borrows its Fabric runtime and obtains fresh
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
    TencentSecretProvider(String region, URI endpoint, long generation, CloudCredentials.Source credentials,
            Supplier<HttpX.Builder> http, Clock clock) {
        if (region == null || !region.matches("[a-z0-9-]+") || generation < 0) {
            throw new IllegalArgumentException("Invalid Tencent connection");
        }
        this.region = region;
        this.endpoint = CloudProtocol.endpoint(endpoint);
        this.generation = generation;
        this.credentials = Objects.requireNonNull(credentials);
        this.http = Objects.requireNonNull(http);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Unwraps the Tencent Response object and checks business errors that can be returned with HTTP 200.
     *
     * @param body decoded backend JSON object; raw error messages must not escape this boundary
     * @return validated response envelope
     */
    private static Map<?, ?> response(Map<?, ?> body) {
        if (!(body.get("Response") instanceof Map<?, ?> response)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (response.containsKey("Error")) {
            throw error(200, body);
        }
        return response;
    }

    /**
     * Handles native Tencent throttling and internal failures, including Error envelopes returned with HTTP 200. Other
     * business errors remain terminal and are not inferred from the public exception type.
     *
     * @param status native HTTP status
     * @param body   decoded root response object
     * @return true for native throttling/internal failures or transient HTTP status without a terminal business code
     */
    private static boolean retryable(int status, Map<?, ?> body) {
        Map<?, ?> response = body.get("Response") instanceof Map<?, ?> nested ? nested : body;
        Map<?, ?> error = response.get("Error") instanceof Map<?, ?> nested ? nested : response;
        String code = error.get("Code") instanceof String text ? text : "";
        if (!code.isEmpty()) {
            return code.startsWith("RequestLimitExceeded") || code.startsWith("LimitExceeded")
                    || code.startsWith("InternalError");
        }
        return CloudProtocol.retryableStatus(status);
    }

    /**
     * Maps platform error codes and HTTP status into sanitized exceptions without exposing remote messages or resource
     * details.
     *
     * @param status HTTP response status
     * @param body   decoded backend JSON object; raw error messages must not escape this boundary
     * @return sanitized platform failure
     */
    private static UncheckedException error(int status, Map<?, ?> body) {
        Map<?, ?> response = body.get("Response") instanceof Map<?, ?> nested ? nested : body;
        Map<?, ?> error = response.get("Error") instanceof Map<?, ?> nested ? nested : response;
        String code = error.get("Code") instanceof String text ? text : "";
        if (code.startsWith("AuthFailure") || code.startsWith("UnauthorizedOperation")
                || code.startsWith("OperationDenied") || code.equals("ResourceUnavailable.ResourceDisabled")) {
            return new ForbiddenException("Secret access denied");
        }
        if (code.startsWith("RequestLimitExceeded") || code.startsWith("LimitExceeded")) {
            return new CryptoException("Secret backend is rate limited");
        }
        if (code.startsWith("ResourceNotFound") || code.equals("ResourceUnavailable.ResourcePendingDeleted")) {
            return new CryptoException("Secret not found");
        }
        if (code.startsWith("InvalidParameter") || code.startsWith("MissingParameter")) {
            return new CryptoException("Invalid secret reference");
        }
        if (code.startsWith("UnsupportedOperation") || code.equals("ResourceUnavailable.NotPurchased")) {
            return new CryptoException("Secret operation or payload is not supported");
        }
        return CloudProtocol.restError(status, body);
    }

    /**
     * Computes the platform canonical signature over the exact request data. Derived key buffers are erased where
     * allocated; supplied credentials remain owned by the caller.
     *
     * @param endpoint fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param region   non-empty cloud region used for routing and signature scope
     * @param payload  exact UTF-8 request body bytes covered by the signature
     * @param identity live caller-owned signing credentials; this operation does not close them
     * @param instant  UTC signing instant
     * @return request authentication fields required by this platform
     */
    static Map<String, String> sign(
            URI endpoint,
            String region,
            byte[] payload,
            CloudCredentials identity,
            Instant instant) {
        String date = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(instant);
        String timestamp = Long.toString(instant.getEpochSecond());
        String names = "content-type;host;x-tc-action";
        String canonical = "POST\n/\n\ncontent-type:application/json; charset=utf-8\nhost:"
                + CloudProtocol.host(endpoint) + "\nx-tc-action:getsecretvalue\n\n" + names + "\n"
                + CloudProtocol.hash(payload);
        String scope = date + "/ssm/tc3_request";
        String toSign = "TC3-HMAC-SHA256\n" + timestamp + "\n" + scope + "\n"
                + CloudProtocol.hash(canonical.getBytes(StandardCharsets.UTF_8));
        byte[] initial = identity.signingKey("TC3", ""), day = null, service = null, key = null;
        try {
            day = CloudProtocol.hmac("HmacSHA256", initial, date);
            service = CloudProtocol.hmac("HmacSHA256", day, "ssm");
            key = CloudProtocol.hmac("HmacSHA256", service, "tc3_request");
            String signature = HexKit.encodeString(CloudProtocol.hmac("HmacSHA256", key, toSign));
            Map<String, String> headers = new TreeMap<>();
            headers.put("content-type", "application/json; charset=utf-8");
            headers.put("host", CloudProtocol.host(endpoint));
            headers.put("x-tc-action", "GetSecretValue");
            headers.put("x-tc-version", "2019-09-23");
            headers.put("x-tc-region", region);
            headers.put("x-tc-timestamp", timestamp);
            if (identity.token() != null) {
                headers.put("x-tc-token", identity.token());
            }
            headers.put(
                    "authorization",
                    "TC3-HMAC-SHA256 Credential=" + identity.accessKeyId() + "/" + scope + ", SignedHeaders=" + names
                            + ", Signature=" + signature);
            return headers;
        } finally {
            for (byte[] bytes : new byte[][] { initial, day, service, key }) {
                if (bytes != null) {
                    Arrays.fill(bytes, (byte) 0);
                }
            }
        }
    }

    /**
     * Returns the stable provider identifier used to validate connection bindings.
     *
     * @return provider type identifier
     */
    @Override
    public String type() {
        return "TENCENT";
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
        String selected = reference.version() != null ? reference.version() : "SSM_Current";
        if (reference.version() != null && "SSM_Current".equals(selected)) {
            throw new CryptoException("Invalid secret reference");
        }
        Map<?, ?> response = CloudProtocol.exchange(
                attempt -> request(reference.resource(), selected, attempt),
                context,
                TencentSecretProvider::error,
                TencentSecretProvider::retryable,
                TencentSecretProvider::response);
        if (!reference.resource().equals(CloudProtocol.required(response, "SecretName"))) {
            throw new CryptoException("Secret backend is unavailable");
        }
        String actual = CloudProtocol.required(response, "VersionId");
        if (actual.isBlank() || "SSM_Current".equals(actual)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (reference.version() != null && !selected.equals(actual)) {
            throw new CryptoException("Requested secret version was not returned");
        }
        Object binaryValue = response.get("SecretBinary");
        if (binaryValue != null && !(binaryValue instanceof String)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (binaryValue instanceof String binary && !binary.isEmpty()) {
            throw new CryptoException("Secret operation or payload is not supported");
        }
        context.checkDeadline();
        return CloudProtocol.material(CloudProtocol.required(response, "SecretString"), actual, generation);
    }

    /**
     * Builds a fresh native Fabric request using a newly acquired credential, closing the credential after
     * header/signature creation. Request execution and retries remain in CloudProtocol.
     *
     * @param name    unencoded cloud secret name
     * @param version fixed immutable version; required only in VERSION mode
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return authenticated native request builder
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    private HttpX.Builder request(String name, String version, SecretContext context) {
        byte[] body = JsonKit.toJsonString(Map.of("SecretName", name, "VersionId", version))
                .getBytes(StandardCharsets.UTF_8);
        try (CloudCredentials identity = credentials.acquire(context)) {
            Map<String, String> headers = sign(endpoint, region, body, identity, clock.instant());
            HttpX.Builder request = http.get().to(endpoint.toString()).post().body(body);
            headers.forEach(request::header);
            return request;
        }
    }

}
