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
package org.miaixz.bus.auth.vendor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.SourceProfile;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Compiles one complete externally loaded Vendor Source through an exact platform definition and adapter factory.
 * <p>
 * Compilation is deterministic and performs no data loading, Registry access, network operation, or reflection. Raw
 * Source settings are decoded once at the driver boundary using the selected Vendor definition.
 * </p>
 *
 * @author Kimi Liu
 */
final class VendorCompiler implements SourceDriver<VendorSettings> {

    /**
     * Immutable Vendor and variant definition directory.
     */
    private final VendorDirectory directory;

    /**
     * Immutable exact platform-variant adapter bindings.
     */
    private final AdapterBindings bindings;

    /**
     * Aggregate Vendor Source profile exposed to runtime assembly.
     */
    private final SourceProfile<VendorSettings> profile;

    /**
     * Creates a compiler over one frozen and consistently keyed Vendor inventory.
     *
     * @param directory complete Vendor definition directory
     * @param bindings  complete platform adapter factory bindings
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    VendorCompiler(final VendorDirectory directory, final AdapterBindings bindings) {
        this.directory = Assert.notNull(directory, "Vendor compiler directory must not be null");
        this.bindings = Assert.notNull(bindings, "Vendor compiler adapter bindings must not be null");
        this.profile = new Profile();
    }

    /**
     * Validates complete Source routing and its resolved required entity relationships.
     *
     * @param source   complete Source entity
     * @param provider resolved owning Provider
     * @param library  resolved Provider Library
     * @throws ValidateException if Source classification or relationships are inconsistent
     */
    private static void requireRegistration(final Source source, final Provider provider, final Library library) {
        final Source checked = Assert.notNull(source, "Vendor Source must not be null");
        if (!"vendor".equals(checked.getType()) || checked.getProtocol() == null || checked.getNamespace_id() == null
                || checked.getNamespace_id().isBlank() || !provider.getId().equals(checked.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("Vendor compiler requires a matching Source registration");
        }
    }

    /**
     * Verifies that Source, typed settings, platform definition, and protocol identify one exact variant.
     *
     * @param source           complete Source entity
     * @param vendorDefinition selected platform definition
     * @param definition       selected variant definition
     * @param settings         complete typed settings object
     * @throws ValidateException if any routing value differs
     */
    private static void requireRoute(
            final Source source,
            final VendorDefinition<?> vendorDefinition,
            final VendorDefinition.Definition definition,
            final VendorSettings settings) {
        if (settings.getClass() != vendorDefinition.settingsType() || !settings.vendor().equals(vendorDefinition.type())
                || !definition.platform().equals(vendorDefinition.type())
                || !settings.variant().equals(definition.variant())
                || !definition.protocol().name().equalsIgnoreCase(source.getProtocol())) {
            throw new ValidateException(
                    "Vendor definition, settings, registration, and protocol must identify one variant");
        }
    }

    /**
     * Validates common deployment values without duplicating platform-specific settings rules.
     *
     * @param definition selected variant definition
     * @param settings   complete platform settings
     * @throws ValidateException if scope, redirect, credential, or interaction state is inconsistent
     */
    private static void requireSettings(final VendorDefinition.Definition definition, final VendorSettings settings) {
        Assert.notBlank(settings.clientId(), "Vendor client identifier must not be blank");
        Assert.notNull(settings.credential(), "Vendor credential reference must not be null");
        Assert.notNull(settings.scopes(), "Vendor scopes must not be null");
        final Set<String> scopes = new HashSet<>(settings.scopes().size());
        for (String scope : settings.scopes()) {
            if (!scopes.add(Assert.notBlank(scope, "Vendor scope must not be blank"))) {
                throw new ValidateException("Vendor scopes must not contain duplicates");
            }
        }
        boolean redirect = false;
        boolean direct = false;
        for (Capability<?, ?> capability : definition.manifest().capabilities()) {
            redirect |= capability.interactions().contains(Capability.Interaction.REDIRECT);
            direct |= capability.interactions().contains(Capability.Interaction.DIRECT);
        }
        if ((definition.targets().authorization().isPresent() || redirect) && settings.redirectUri().isEmpty()) {
            throw new ValidateException("Browser Vendor variant requires an exact registered redirect URI");
        }
        if (definition.targets().authorization().isEmpty() && direct && !redirect
                && settings.redirectUri().isPresent()) {
            throw new ValidateException("Direct-only Vendor variant must not declare a redirect URI");
        }
    }

    /**
     * Validates capability ownership without interpreting platform wire documents.
     *
     * @param vendorDefinition selected platform definition
     * @param definition       selected variant definition
     * @throws ValidateException if a capability key is duplicated or belongs to another protocol/platform
     */
    private static void requireManifest(
            final VendorDefinition<?> vendorDefinition,
            final VendorDefinition.Definition definition) {
        final Set<Capability.Key> keys = new HashSet<>();
        for (Capability<?, ?> capability : definition.manifest().capabilities()) {
            if (!keys.add(capability.key())) {
                throw new ValidateException("Vendor manifest contains a duplicate capability key");
            }
            final Protocol protocol = capability.key().protocol().getOrNull();
            if (protocol != null
                    && (definition.protocol() == Protocol.VENDOR_AUTH || protocol != definition.protocol())) {
                throw new ValidateException("Vendor standard capability must use the selected variant protocol");
            }
            if (protocol == null) {
                final String operation = capability.key().operation();
                final boolean sourceAuthentication = operation.equals(SourceAuthentication.INITIATE.key().operation())
                        || operation.equals(SourceAuthentication.COMPLETE.key().operation());
                final boolean platform = operation.startsWith("vendor." + vendorDefinition.type().value() + Symbol.DOT);
                if (!sourceAuthentication && !platform) {
                    throw new ValidateException("Vendor application capability belongs to another platform");
                }
            }
        }
    }

    /**
     * Accepts only the actual wire protocols declared by built-in Vendor definitions.
     *
     * @param protocol persisted protocol identifier
     * @return whether the Vendor layer supports the protocol
     */
    @Override
    public boolean supports(final String protocol) {
        return Protocol.OAUTH1.name().equalsIgnoreCase(protocol) || Protocol.OAUTH2.name().equalsIgnoreCase(protocol)
                || Protocol.OIDC.name().equalsIgnoreCase(protocol)
                || Protocol.VENDOR_AUTH.name().equalsIgnoreCase(protocol);
    }

    /**
     * Decodes Vendor JSON after resolving the concrete platform settings class from its vendor identifier.
     *
     * @param source complete Vendor Source entity
     * @return concrete immutable Vendor settings
     */
    @Override
    public VendorSettings decode(final Source source) {
        final String json = Assert.notBlank(
                Assert.notNull(source, "Vendor Source must not be null").getSettings(),
                "Vendor Source settings must not be blank");
        try {
            final Map<?, ?> values = JsonKit.toMap(json);
            Object vendorValue = values.get("vendor");
            if (vendorValue instanceof Map<?, ?> nested) {
                vendorValue = nested.get("value");
            }
            final String vendor = Assert.notBlank(
                    vendorValue == null ? null : vendorValue.toString(),
                    "Vendor Source settings require vendor");
            final VendorDefinition<?> definition = directory.require(new Vendor.Id(vendor));
            return Assert.notNull(
                    JsonKit.toPojo(json, definition.settingsType()),
                    "Decoded Vendor Source settings must not be null");
        } catch (RuntimeException cause) {
            throw new ValidateException("Vendor Source settings cannot be decoded", cause);
        }
    }

    /**
     * Returns the aggregate Vendor Source profile bound to this compiler.
     *
     * @return immutable Vendor Source profile
     */
    @Override
    public SourceProfile<VendorSettings> profile() {
        return profile;
    }

    /**
     * Compiles one validated Source registration into a Registry-internal executable provider.
     *
     * @param registration complete Vendor Source registration
     * @param provider     resolved owning Provider
     * @param library      Library resolved through the owning Provider
     * @param services     externally supplied execution services
     * @return immutable executable entry wrapping the exact selected adapter
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration, relationships, routing, settings, definition, or manifest is
     *                                  inconsistent
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> registration,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        final Registration.Record<Source> record = Assert
                .notNull(registration, "Vendor Source registration must not be null");
        Assert.notNull(provider, "Vendor Source Provider must not be null");
        Assert.notNull(library, "Vendor Source Library must not be null");
        final ExecutionServices runtime = Assert.notNull(services, "Vendor execution services must not be null");
        final Source source = record.resource();
        requireRegistration(source, provider, library);
        final VendorSettings settings = decode(source);
        final VendorDefinition<?> vendorDefinition = directory.require(settings.vendor());
        final VendorDefinition.Definition definition = directory.require(settings.vendor(), settings.variant());
        requireRoute(source, vendorDefinition, definition, settings);
        requireSettings(definition, settings);
        requireManifest(vendorDefinition, definition);
        definition.targets().resolve(settings);
        return new CompiledAdapter(bindings
                .create(source.getNamespace_id(), source.getId(), vendorDefinition, definition, settings, runtime));
    }

    /**
     * Adapts a validated Vendor adapter to the non-exported runtime execution contract.
     */
    private static final class CompiledAdapter implements RuntimeProvider {

        /**
         * Validated third-party platform adapter.
         */
        private final VendorAdapter adapter;

        /**
         * Creates a runtime wrapper without changing the Vendor contract.
         *
         * @param adapter validated adapter
         */
        private CompiledAdapter(final VendorAdapter adapter) {
            this.adapter = Assert.notNull(adapter, "Compiled Vendor adapter must not be null");
        }

        /**
         * Returns the exact adapter capability manifest.
         *
         * @return immutable capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return adapter.manifest();
        }

        /**
         * Delegates one exact typed invocation to the compiled adapter.
         *
         * @param capability exact declared capability
         * @param request    exact request
         * @param context    invocation context
         * @param timeout    shared end-to-end budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated outcome stage
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            return adapter.invoke(capability, request, context, timeout);
        }

    }

    /**
     * Exposes the single aggregate Source profile for all explicitly registered Vendor platforms.
     */
    private final class Profile implements SourceProfile<VendorSettings> {

        /**
         * Returns the stable aggregate profile identifier.
         *
         * @return Vendor Source profile identifier
         */
        @Override
        public String id() {
            return "vendor";
        }

        /**
         * Returns the Vendor Source classification.
         *
         * @return Vendor authentication protocol classification
         */
        @Override
        public Protocol type() {
            return Protocol.VENDOR_AUTH;
        }

        /**
         * Returns the common settings marker narrowed by the selected definition during compilation.
         *
         * @return Vendor settings marker class
         */
        @Override
        public Class<VendorSettings> settingsType() {
            return VendorSettings.class;
        }

        /**
         * Returns only the stable framework-level Vendor Source entry capabilities.
         *
         * @return immutable Source authentication capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return new Capability.Manifest(
                    java.util.List.of(SourceAuthentication.INITIATE, SourceAuthentication.COMPLETE));
        }

        /**
         * Returns no aggregate conformance because each platform variant declares its actual wire category.
         *
         * @return empty aggregate conformance
         */
        @Override
        public Optional<Conformance> conformance() {
            return Optional.empty();
        }

        /**
         * Returns an empty aggregate form because management fields belong to each typed settings class.
         *
         * @return empty aggregate management form
         */
        @Override
        public Form form() {
            return new Form(java.util.List.of());
        }

        /**
         * Returns no aggregate defaults because platform settings are definition-specific.
         *
         * @return empty Vendor settings defaults
         */
        @Override
        public Optional<VendorSettings> defaults() {
            return Optional.empty();
        }

    }

}
