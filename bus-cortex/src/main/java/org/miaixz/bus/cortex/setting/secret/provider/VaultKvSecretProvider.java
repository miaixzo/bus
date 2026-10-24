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
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.cortex.setting.secret.SecretProvider;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Direct Vault KV v2 reads. Mount and namespace are connection-bound, not request-controlled.
 */
public final class VaultKvSecretProvider implements SecretProvider {

    /**
     * Validated fixed HTTPS root; never selected from an untrusted Secret reference.
     */
    private final URI endpoint;
    /**
     * Validated and encoded Vault KV v2 mount path.
     */
    private final String mount;
    /**
     * Fixed Vault namespace header, or null to omit it.
     */
    private final String namespace;
    /**
     * Connection configuration generation attached to every returned material.
     */
    private final long generation;
    /**
     * Fresh standalone-token source used separately for each actual HTTP attempt.
     */
    private final CloudCredentials.Source tokens;
    /**
     * Trusted factory of one native Fabric builder per attempt; does not own its runtime.
     */
    private final Supplier<HttpX.Builder> http;

    /**
     * Creates a connection-bound VaultKvSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param mount         unencoded Vault KV v2 mount path, validated segment by segment
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param tokens        non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public VaultKvSecretProvider(URI endpoint, String mount, long generation, CloudCredentials.Source tokens,
            Context fabricContext) {
        this(endpoint, mount, generation, tokens, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound VaultKvSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param mount         unencoded Vault KV v2 mount path, validated segment by segment
     * @param namespace     fixed Vault namespace header; null omits it
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param tokens        non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public VaultKvSecretProvider(URI endpoint, String mount, String namespace, long generation,
            CloudCredentials.Source tokens, Context fabricContext) {
        this(endpoint, mount, namespace, generation, tokens, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound VaultKvSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint   fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param mount      unencoded Vault KV v2 mount path, validated segment by segment
     * @param generation non-negative connection configuration generation attached to returned materials
     * @param tokens     non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param http       trusted native Fabric builder factory, injected for deterministic protocol tests
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    VaultKvSecretProvider(URI endpoint, String mount, long generation, CloudCredentials.Source tokens,
            Supplier<HttpX.Builder> http) {
        this(endpoint, mount, null, generation, tokens, http);
    }

    /**
     * Creates a connection-bound VaultKvSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint   fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param mount      unencoded Vault KV v2 mount path, validated segment by segment
     * @param namespace  fixed Vault namespace header; null omits it
     * @param generation non-negative connection configuration generation attached to returned materials
     * @param tokens     non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param http       trusted native Fabric builder factory, injected for deterministic protocol tests
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    VaultKvSecretProvider(URI endpoint, String mount, String namespace, long generation, CloudCredentials.Source tokens,
            Supplier<HttpX.Builder> http) {
        if (generation < 0) {
            throw new IllegalArgumentException("Invalid Vault connection generation");
        }
        this.endpoint = CloudProtocol.endpoint(endpoint);
        this.mount = path(mount);
        if (namespace != null && !namespace.matches("[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*")) {
            throw new IllegalArgumentException("Invalid fixed Vault namespace");
        }
        this.namespace = namespace;
        this.generation = generation;
        this.tokens = Objects.requireNonNull(tokens);
        this.http = Objects.requireNonNull(http);
    }

    /**
     * Validates and percent-encodes each Vault logical path segment, rejecting traversal, empty segments, pre-encoding
     * and control characters.
     *
     * @param value unencoded Vault logical resource path
     * @return encoded path preserving legitimate hierarchy separators
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the documented validation, expiration or lifecycle
     *                                                            constraint fails
     */
    private static String path(String value) {
        if (value == null || value.isEmpty() || value.length() > 4096) {
            throw new CryptoException("Invalid secret reference");
        }
        String[] parts = value.split("/", -1);
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            if (part.isEmpty() || part.equals(".") || part.equals("..") || part.indexOf('%') >= 0
                    || part.indexOf('\\') >= 0 || part.chars().anyMatch(c -> Character.isISOControl(c))) {
                throw new CryptoException("Invalid secret reference");
            }
            parts[index] = CloudProtocol.encode(part);
        }
        return String.join("/", parts);
    }

    /**
     * Validates a positive immutable Vault version and rejects aliases or malformed numeric values.
     *
     * @param value immutable backend version text
     * @return validated numeric version text
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the documented validation, expiration or lifecycle
     *                                                            constraint fails
     */
    private static String version(String value) {
        try {
            if (!value.matches("[1-9][0-9]{0,18}") || Long.parseLong(value) <= 0) {
                throw new IllegalArgumentException();
            }
            return value;
        } catch (RuntimeException failure) {
            throw new CryptoException("Invalid secret reference");
        }
    }

    /**
     * Returns the stable provider identifier used to validate connection bindings.
     *
     * @return provider type identifier
     */
    @Override
    public String type() {
        return "VAULT";
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
        String encoded = path(reference.resource());
        String selected = reference.version() != null ? version(reference.version()) : null;
        URI uri = URI.create(
                endpoint + "v1/" + mount + "/data/" + encoded + (selected == null ? "" : "?version=" + selected));
        Map<?, ?> response = CloudProtocol.exchange(
                attempt -> request(uri, attempt),
                context,
                CloudProtocol::restError,
                (status, body) -> CloudProtocol.retryableStatus(status));
        if (!(response.get("data") instanceof Map<?, ?> envelope)
                || !(envelope.get("metadata") instanceof Map<?, ?> metadata)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        long actual = CloudProtocol.integer(metadata.get("version"));
        if (actual <= 0) {
            throw new CryptoException("Secret backend is unavailable");
        }
        String resolved = Long.toString(actual);
        if (selected != null && !selected.equals(resolved)) {
            throw new CryptoException("Requested secret version was not returned");
        }
        if (!(metadata.get("destroyed") instanceof Boolean destroyed)
                || !(metadata.get("deletion_time") instanceof String deletionTime)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        Instant deleteAt = null;
        try {
            if (!deletionTime.isEmpty()) {
                deleteAt = Instant.parse(deletionTime);
            }
        } catch (RuntimeException failure) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (destroyed || deleteAt != null && !Instant.now().isBefore(deleteAt)) {
            throw new CryptoException("Secret value is unavailable for the selected version");
        }
        if (!(envelope.get("data") instanceof Map<?, ?> data)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        context.checkDeadline();
        // Return only the KV object; root lease fields and custom_metadata are not secret payload or TTL.
        return CloudProtocol.material(JsonKit.toJsonString(data), resolved, generation, deleteAt);
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
        try (CloudCredentials token = tokens.acquire(context)) {
            HttpX.Builder request = http.get().to(uri.toString()).header("X-Vault-Token", token.header())
                    .header("Accept", "application/json").get();
            if (namespace != null) {
                request.header("X-Vault-Namespace", namespace);
            }
            return request;
        }
    }

}
