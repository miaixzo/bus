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
package org.miaixz.bus.starter.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import org.miaixz.bus.auth.Authorize;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Policies;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.runtime.RuntimeBuilder;
import org.miaixz.bus.auth.runtime.RuntimeServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.SourceAggregate;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConfigurer;
import org.miaixz.bus.auth.source.vendor.VendorCredentialWriter;
import org.miaixz.bus.auth.source.vendor.VendorLocator;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorModule;
import org.miaixz.bus.auth.source.vendor.VendorOptions;
import org.miaixz.bus.auth.worker.WorkerSet;
import org.miaixz.bus.auth.worker.loader.BlueprintLoader;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Adapts Spring Boot client configuration to the bus-auth Source architecture.
 * <p>
 * One immutable {@link SourceAggregate} is shared by client configuration and Runtime assembly. Enabled
 * {@code bus.auth.<vendor>} entries are validated against its {@link VendorLocator} and converted to short-lived
 * {@link Vendor.Configuration} commands. Plaintext client secrets are never placed in {@link VendorOptions} or the
 * authentication cache; {@link VendorConfigurer} transfers them to the project-owned {@link VendorCredentialWriter}.
 * </p>
 * <p>
 * This service does not maintain a second registry, instantiate legacy Provider classes, switch on platform enums,
 * execute authentication, or persist project Blueprint data.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthService {

    /**
     * Standard form fields supplied by the starter configuration.
     */
    private static final Set<String> STANDARD_FIELDS = Set
            .of("clientId", "credential", "redirectUri", "scopes", "pkce");

    /**
     * Credential types representable by the standard {@code clientSecret} property.
     */
    private static final Set<Credential.Type> CLIENT_SECRET_TYPES = Set
            .of(Credential.Type.PASSWORD, Credential.Type.CLIENT_SECRET, Credential.Type.SHARED_SECRET);

    /**
     * Immutable protocol and Vendor Source registration snapshot.
     */
    private final SourceAggregate sources;

    /**
     * Immutable Vendor manifest and variant locator.
     */
    private final VendorLocator locator;

    /**
     * Enabled standard clients indexed in manifest order.
     */
    private final Map<Vendor.Id, AuthProperties.Client> clients;

    /**
     * Atomic authentication state backend configured through {@code bus.auth.cache}.
     */
    private final CacheX<String, Object> cache;

    /**
     * Executor used by the default Runtime assembly path.
     */
    private final Executor executor;

    /**
     * Stable deployment identifier used to isolate authentication cache keys.
     */
    private final String deployment;

    /**
     * Creates the Spring authentication facade over one frozen Source aggregate.
     *
     * @param environment protected Spring configuration environment
     * @param cache       authentication-specific atomic cache backend
     * @param executor    authentication Runtime executor
     * @param sources     immutable Source aggregate shared by every operation
     */
    public AuthService(Environment environment, CacheX<String, Object> cache, Executor executor,
            SourceAggregate sources) {
        Environment configured = Assert.notNull(environment, "Authentication environment must not be null");
        this.cache = Assert.notNull(cache, "Authentication cache must not be null");
        this.executor = Assert.notNull(executor, "Authentication executor must not be null");
        this.sources = Assert.notNull(sources, "Authentication Source aggregate must not be null");
        this.locator = this.sources.vendorModule().locator();
        this.clients = bind(configured, locator);
        this.deployment = deployment(configured);
    }

    /**
     * Returns the immutable Source aggregate shared by configuration and Runtime assembly.
     *
     * @return protocol and Vendor Source registration snapshot
     */
    public SourceAggregate sources() {
        return sources;
    }

    /**
     * Returns the frozen Vendor module retained by the shared Source aggregate.
     *
     * @return immutable Vendor module
     */
    public VendorModule vendors() {
        return sources.vendorModule();
    }

    /**
     * Returns the immutable Vendor locator used by management and configuration clients.
     *
     * @return immutable Vendor manifest locator
     */
    public VendorLocator locator() {
        return locator;
    }

    /**
     * Returns enabled Vendor identifiers in deterministic manifest order.
     *
     * @return immutable enabled Vendor identifier set
     */
    public Set<Vendor.Id> configured() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(clients.keySet()));
    }

    /**
     * Creates a Vendor configuration coordinator over the shared module.
     *
     * @param writer project-owned recoverable credential storage port
     * @return client-side Vendor configuration coordinator
     */
    public VendorConfigurer clients(VendorCredentialWriter writer) {
        return Authorize.clients(vendors(), Assert.notNull(writer, "Vendor credential writer must not be null"));
    }

    /**
     * Creates a short-lived command for the first manifest-declared Variant of an enabled Vendor.
     *
     * @param vendor exact enabled Vendor identifier
     * @return standard command owning a fresh client-secret lease
     */
    public Vendor.Configuration configuration(Vendor.Id vendor) {
        VendorManifest<?> manifest = locator.require(Assert.notNull(vendor, "Vendor identifier must not be null"));
        return configuration(vendor, manifest.variants().getFirst().variant());
    }

    /**
     * Creates a short-lived command for an exact enabled Vendor Variant.
     *
     * @param vendor  exact enabled Vendor identifier
     * @param variant exact Vendor variant
     * @return standard command owning a fresh client-secret lease
     */
    public Vendor.Configuration configuration(Vendor.Id vendor, Vendor.Variant variant) {
        AuthProperties.Client client = requireClient(vendor);
        VendorManifest.Variant selected = locator.require(vendor, variant);
        requireStandard(vendor, selected);
        return new Vendor.Configuration(vendor, variant, client.clientId(),
                new SecretLease(client.clientSecret().toCharArray()),
                org.miaixz.bus.core.lang.Optional.ofNullable(client.redirectUri()), client.scopes(),
                new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Configures the first manifest-declared Variant of an enabled Vendor.
     *
     * @param vendor  exact enabled Vendor identifier
     * @param writer  project-owned recoverable credential storage port
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return asynchronous immutable Vendor Options outcome
     */
    public CompletionStage<Outcome<VendorOptions<?>>> configure(
            Vendor.Id vendor,
            VendorCredentialWriter writer,
            Context context,
            Timeout timeout) {
        return clients(writer).configure(configuration(vendor), context, timeout);
    }

    /**
     * Configures an exact enabled Vendor Variant.
     *
     * @param vendor  exact enabled Vendor identifier
     * @param variant exact Vendor variant
     * @param writer  project-owned recoverable credential storage port
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation timeout
     * @return asynchronous immutable Vendor Options outcome
     */
    public CompletionStage<Outcome<VendorOptions<?>>> configure(
            Vendor.Id vendor,
            Vendor.Variant variant,
            VendorCredentialWriter writer,
            Context context,
            Timeout timeout) {
        return clients(writer).configure(configuration(vendor, variant), context, timeout);
    }

    /**
     * Creates a Runtime builder using the shared Source aggregate and starter-owned infrastructure.
     *
     * @param policies        immutable non-relaxable authentication policies
     * @param workers         project Worker ports required by selected Sources
     * @param blueprintLoader project Blueprint input
     * @return one-shot Runtime builder containing the shared Source modules
     */
    public RuntimeBuilder runtime(Policies policies, WorkerSet workers, BlueprintLoader blueprintLoader) {
        return runtime(policies, executor, workers, blueprintLoader);
    }

    /**
     * Creates a Runtime builder with an explicitly selected Source executor.
     *
     * @param policies        immutable non-relaxable authentication policies
     * @param executor        caller-owned Source executor
     * @param workers         project Worker ports required by selected Sources
     * @param blueprintLoader project Blueprint input
     * @return one-shot Runtime builder containing the shared Source modules
     */
    public RuntimeBuilder runtime(
            Policies policies,
            Executor executor,
            WorkerSet workers,
            BlueprintLoader blueprintLoader) {
        RuntimeServices services = new RuntimeServices(
                Assert.notNull(policies, "Authentication policies must not be null"),
                Assert.notNull(executor, "Authentication Runtime executor must not be null"),
                Assert.notNull(workers, "Authentication Worker set must not be null"), cache, deployment);
        return Authorize
                .custom(services, Assert.notNull(blueprintLoader, "Authentication Blueprint loader must not be null"))
                .modules(sources.modules());
    }

    /**
     * Binds enabled direct Vendor property blocks known by the frozen module.
     *
     * @param environment protected Spring configuration environment
     * @param locator     exact frozen Vendor locator
     * @return immutable enabled client map
     */
    private static Map<Vendor.Id, AuthProperties.Client> bind(Environment environment, VendorLocator locator) {
        Binder binder = Binder.get(environment);
        Map<Vendor.Id, AuthProperties.Client> configured = new LinkedHashMap<>();
        for (VendorManifest<?> manifest : locator.manifests()) {
            Vendor.Id vendor = manifest.vendor();
            binder.bind(prefix(vendor), Bindable.of(AuthProperties.Client.class)).ifBound(client -> {
                if (client.enabled()) {
                    validate(vendor, client);
                    configured.put(vendor, client);
                }
            });
        }
        return Collections.unmodifiableMap(configured);
    }

    /**
     * Resolves the stable cache deployment identifier from the Spring application name.
     *
     * @param environment protected Spring configuration environment
     * @return non-blank cache deployment identifier
     */
    private static String deployment(Environment environment) {
        String application = environment.getProperty("spring.application.name");
        return application == null || application.isBlank() ? Normal.DEFAULT : application.trim();
    }

    /**
     * Returns the direct property prefix of one Vendor client.
     *
     * @param vendor exact Vendor identifier
     * @return direct {@code bus.auth.<vendor>} prefix
     */
    private static String prefix(Vendor.Id vendor) {
        return GeniusBuilder.AUTH + Symbol.DOT + vendor.value();
    }

    /**
     * Validates one enabled standard client without logging secret material.
     *
     * @param vendor exact Vendor identifier
     * @param client bound standard client settings
     */
    private static void validate(Vendor.Id vendor, AuthProperties.Client client) {
        String prefix = prefix(vendor);
        Assert.notBlank(client.clientId(), prefix + ".client-id must not be blank");
        Assert.notBlank(client.clientSecret(), prefix + ".client-secret must not be blank");
        Set<String> scopes = new HashSet<>();
        for (String scope : client.scopes()) {
            String checked = Assert.notBlank(scope, prefix + ".scopes must not contain blanks");
            if (!scopes.add(checked)) {
                throw new ValidateException(prefix + ".scopes must not contain duplicates");
            }
        }
    }

    /**
     * Returns the enabled standard client bound to a Vendor.
     *
     * @param vendor exact Vendor identifier
     * @return validated enabled client
     */
    private AuthProperties.Client requireClient(Vendor.Id vendor) {
        Vendor.Id checked = Assert.notNull(vendor, "Vendor identifier must not be null");
        AuthProperties.Client client = clients.get(checked);
        if (client == null) {
            throw new ValidateException("Authentication Vendor is not enabled: " + checked.value());
        }
        return client;
    }

    /**
     * Verifies that a Variant can use only the standard starter fields.
     *
     * @param vendor   exact Vendor identifier
     * @param selected exact manifest Variant
     */
    private void requireStandard(Vendor.Id vendor, VendorManifest.Variant selected) {
        if (!CLIENT_SECRET_TYPES.contains(selected.credentialType())) {
            throw new ValidateException("Authentication Vendor Variant requires a key or certificate loader: "
                    + vendor.value() + Symbol.C_SLASH + selected.variant().value());
        }
        Scheme.Form form = locator.require(vendor).form(selected.variant());
        for (Scheme.Form.Section section : form.sections()) {
            for (Scheme.Form.Field field : section.fields()) {
                if (field.required() && !STANDARD_FIELDS.contains(field.key()) && field.defaultValue().isEmpty()) {
                    throw new ValidateException("Authentication Vendor Variant requires platform-specific field '"
                            + field.key() + "': " + vendor.value() + Symbol.C_SLASH + selected.variant().value());
                }
            }
        }
    }

}
