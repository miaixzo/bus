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
import org.miaixz.bus.core.lang.exception.ForbiddenException;
import org.miaixz.bus.cortex.setting.secret.SecretContext;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;
import org.miaixz.bus.cortex.setting.secret.SecretProvider;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Direct Azure Key Vault Secrets REST reads with an externally supplied OAuth access token.
 */
public final class AzureSecretProvider implements SecretProvider {

    /**
     * Default explicit Azure Key Vault Secrets REST API version.
     */
    public static final String DEFAULT_API_VERSION = "2025-07-01";
    /**
     * Validated fixed HTTPS root; never selected from an untrusted Secret reference.
     */
    private final URI endpoint;
    /**
     * Explicit Azure data-plane REST API version.
     */
    private final String apiVersion;
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
     * Creates a connection-bound AzureSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param tokens        non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public AzureSecretProvider(URI endpoint, long generation, CloudCredentials.Source tokens, Context fabricContext) {
        this(endpoint, generation, tokens, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound AzureSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint      fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param apiVersion    explicit Azure REST API version; no discovery or fallback is performed
     * @param generation    non-negative connection configuration generation attached to returned materials
     * @param tokens        non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param fabricContext caller-owned Fabric runtime with retries, redirects, caches, authenticators, cookies and
     *                      filters disabled
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    public AzureSecretProvider(URI endpoint, String apiVersion, long generation, CloudCredentials.Source tokens,
            Context fabricContext) {
        this(endpoint, apiVersion, generation, tokens, CloudProtocol.http(fabricContext));
    }

    /**
     * Creates a connection-bound AzureSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint   fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param generation non-negative connection configuration generation attached to returned materials
     * @param tokens     non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param http       trusted native Fabric builder factory, injected for deterministic protocol tests
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    AzureSecretProvider(URI endpoint, long generation, CloudCredentials.Source tokens, Supplier<HttpX.Builder> http) {
        this(endpoint, DEFAULT_API_VERSION, generation, tokens, http);
    }

    /**
     * Creates a connection-bound AzureSecretProvider. The provider borrows its Fabric runtime and obtains fresh
     * credentials per HTTP attempt; the caller retains ownership of that runtime.
     *
     * @param endpoint   fixed HTTPS backend root without user info, query, fragment, or a non-root path
     * @param apiVersion explicit Azure REST API version; no discovery or fallback is performed
     * @param generation non-negative connection configuration generation attached to returned materials
     * @param tokens     non-null source returning fresh, caller-owned token credentials for each HTTP attempt
     * @param http       trusted native Fabric builder factory, injected for deterministic protocol tests
     * @throws NullPointerException     if a required collaborator or required component is null
     * @throws IllegalArgumentException if configuration values violate the documented constraints
     */
    AzureSecretProvider(URI endpoint, String apiVersion, long generation, CloudCredentials.Source tokens,
            Supplier<HttpX.Builder> http) {
        if (generation < 0 || apiVersion == null
                || !apiVersion.matches("([0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]+[.][0-9]+)(-preview)?")) {
            throw new IllegalArgumentException("Invalid Azure secret connection");
        }
        this.endpoint = CloudProtocol.endpoint(endpoint);
        this.apiVersion = apiVersion;
        this.generation = generation;
        this.tokens = Objects.requireNonNull(tokens);
        this.http = Objects.requireNonNull(http);
    }

    /**
     * Validates one Azure secret name or version component before adding it to the request path.
     *
     * @param value   secret name or version component to validate
     * @param maximum maximum permitted component length
     * @return validated alphanumeric/hyphen component
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the documented validation, expiration or lifecycle
     *                                                            constraint fails
     */
    private static String component(String value, int maximum) {
        if (value == null || value.length() > maximum || !value.matches("[a-zA-Z0-9-]+")) {
            throw new CryptoException("Invalid secret reference");
        }
        return value;
    }

    /**
     * Returns the stable provider identifier used to validate connection bindings.
     *
     * @return provider type identifier
     */
    @Override
    public String type() {
        return "AZURE";
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
        String name = component(reference.resource(), 127);
        String selected = reference.version() != null ? component(reference.version(), 128) : null;
        URI uri = URI.create(
                endpoint + "secrets/" + name + (selected == null ? "" : "/" + selected) + "?api-version=" + apiVersion);
        Map<?, ?> response = CloudProtocol.exchange(
                attempt -> request(uri, attempt),
                context,
                CloudProtocol::restError,
                (status, body) -> CloudProtocol.retryableStatus(status));
        String version = returnedVersion(CloudProtocol.required(response, "id"), name);
        if (selected != null && !selected.equals(version)) {
            throw new CryptoException("Requested secret version was not returned");
        }
        if (!(response.get("attributes") instanceof Map<?, ?> attributes)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        Object enabled = attributes.get("enabled");
        if (enabled != null && !(enabled instanceof Boolean)) {
            throw new CryptoException("Secret backend is unavailable");
        }
        if (Boolean.FALSE.equals(enabled)) {
            throw new ForbiddenException("Secret access denied");
        }
        Instant now = Instant.now();
        Instant before = CloudProtocol.epoch(attributes.get("nbf"));
        if (before != null && now.isBefore(before)) {
            throw new ForbiddenException("Secret access denied");
        }
        Instant expiry = CloudProtocol.epoch(attributes.get("exp"));
        if (expiry != null && !now.isBefore(expiry)) {
            throw new CryptoException("Secret value has expired");
        }
        context.checkDeadline();
        return CloudProtocol.material(CloudProtocol.required(response, "value"), version, generation, expiry);
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
            return http.get().to(uri.toString()).header("Authorization", "Bearer " + token.header())
                    .header("Accept", "application/json").get();
        }
    }

    /**
     * Verifies that Azure's returned secret ID belongs to this exact HTTPS origin and requested secret name. Rejects
     * alternate hosts, ports, user information, extra paths and malformed version components.
     *
     * @param id   absolute resource ID returned by Key Vault
     * @param name requested unencoded secret name, compared using Azure name casing rules
     * @return validated actual immutable version from the returned resource ID
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if the response identity is untrusted or malformed
     */
    private String returnedVersion(String id, String name) {
        try {
            URI parsed = URI.create(id);
            int expectedPort = endpoint.getPort() == -1 ? 443 : endpoint.getPort();
            int actualPort = parsed.getPort() == -1 ? 443 : parsed.getPort();
            String[] parts = parsed.getRawPath().split("/", -1);
            if (!"https".equalsIgnoreCase(parsed.getScheme()) || !endpoint.getHost().equalsIgnoreCase(parsed.getHost())
                    || expectedPort != actualPort || parsed.getUserInfo() != null || parsed.getRawQuery() != null
                    || parsed.getFragment() != null || parts.length != 4 || !parts[1].equals("secrets")
                    || !parts[2].equalsIgnoreCase(name)) {
                throw new IllegalArgumentException();
            }
            return component(parts[3], 128);
        } catch (RuntimeException failure) {
            throw new CryptoException("Secret backend is unavailable");
        }
    }

}
