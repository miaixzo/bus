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
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Library;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Compiles one complete externally loaded Vendor Source through an exact platform manifest and adapter factory.
 * <p>
 * Compilation is deterministic and performs no data loading, persistence decoding, Registry access, network operation,
 * or reflection. The external project supplies a typed VendorOptions value; this compiler only validates platform and
 * variant routing, resolves the frozen manifest, and creates the matching adapter.
 * </p>
 *
 * @author Kimi Liu
 */
final class VendorCompiler implements SourceDriver<VendorOptions<?>> {

    /**
     * Immutable Vendor and variant manifest directory.
     */
    private final VendorDirectory directory;

    /**
     * Immutable exact platform-variant adapter bindings.
     */
    private final AdapterBindings bindings;

    /**
     * Aggregate Vendor Source scheme exposed to runtime assembly.
     */
    private final VendorScheme scheme;

    /**
     * Creates a compiler over one frozen and consistently keyed Vendor inventory.
     *
     * @param directory complete Vendor manifest directory
     * @param bindings  complete platform adapter factory bindings
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    VendorCompiler(final VendorDirectory directory, final AdapterBindings bindings) {
        this.directory = Assert.notNull(directory, "Vendor compiler directory must not be null");
        this.bindings = Assert.notNull(bindings, "Vendor compiler adapter bindings must not be null");
        this.scheme = new VendorScheme(directory);
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
        if (!VendorScheme.ID.equals(checked.getType()) || checked.getProtocol() == null
                || library.getNamespace_id() == null || library.getNamespace_id().isBlank()
                || !provider.getId().equals(checked.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("Vendor compiler requires a matching Source registration");
        }
    }

    /**
     * Verifies that Source, typed options, platform manifest, and protocol identify one exact variant.
     *
     * @param source   complete Source entity
     * @param manifest platform manifest selected by the configured Vendor identifier
     * @param variant  exact variant selected from that manifest
     * @param options  complete typed options object
     * @throws ValidateException if any routing value differs
     */
    private static void requireRoute(
            final Source source,
            final VariantManifest<?> manifest,
            final VariantManifest.Variant variant,
            final VendorOptions<?> options) {
        if (!options.vendor().equals(manifest.vendor()) || !variant.platform().equals(manifest.vendor())
                || !options.variant().equals(variant.variant())
                || !variant.protocol().name().equalsIgnoreCase(source.getProtocol())) {
            throw new ValidateException(
                    "Vendor manifest, options, registration, and protocol must identify one variant");
        }
    }

    /**
     * Validates common deployment values without duplicating platform-specific options rules.
     *
     * @param variant selected variant
     * @param options complete platform options
     * @throws ValidateException if scope, redirect, credential, or interaction state is inconsistent
     */
    private static void requireOptions(final VariantManifest.Variant variant, final VendorOptions<?> options) {
        Assert.notBlank(options.clientId(), "Vendor client identifier must not be blank");
        Assert.notNull(options.credential(), "Vendor credential reference must not be null");
        Assert.notNull(options.scopes(), "Vendor scopes must not be null");
        variant.pkce().resolve(options);
        final Set<String> scopes = new HashSet<>(options.scopes().size());
        for (String scope : options.scopes()) {
            if (!scopes.add(Assert.notBlank(scope, "Vendor scope must not be blank"))) {
                throw new ValidateException("Vendor scopes must not contain duplicates");
            }
        }
        boolean redirect = false;
        boolean direct = false;
        for (Capability<?, ?> capability : variant.capabilityManifest().capabilities()) {
            redirect |= capability.interactions().contains(Capability.Interaction.REDIRECT);
            direct |= capability.interactions().contains(Capability.Interaction.DIRECT);
        }
        if ((variant.targets().authorization().isPresent() || redirect) && options.redirectUri().isEmpty()) {
            throw new ValidateException("Browser Vendor variant requires an exact registered redirect URI");
        }
        if (variant.targets().authorization().isEmpty() && direct && !redirect && options.redirectUri().isPresent()) {
            throw new ValidateException("Direct-only Vendor variant must not declare a redirect URI");
        }
    }

    /**
     * Validates capability ownership without interpreting platform wire documents.
     *
     * @param manifest platform manifest selected by the configured Vendor identifier
     * @param variant  exact variant selected from that manifest
     * @throws ValidateException if a capability key is duplicated or belongs to another protocol/platform
     */
    private static void requireManifest(final VariantManifest<?> manifest, final VariantManifest.Variant variant) {
        final Set<Capability.Key> keys = new HashSet<>();
        for (Capability<?, ?> capability : variant.capabilityManifest().capabilities()) {
            if (!keys.add(capability.key())) {
                throw new ValidateException("Vendor manifest contains a duplicate capability key");
            }
            final Protocol protocol = capability.key().protocol().getOrNull();
            if (protocol != null && (variant.protocol() == Protocol.VENDOR_AUTH || protocol != variant.protocol())) {
                throw new ValidateException("Vendor standard capability must use the selected variant protocol");
            }
            if (protocol == null) {
                final String operation = capability.key().operation();
                final boolean sourceAuthentication = operation.equals(SourceWorkflow.INITIATE.key().operation())
                        || operation.equals(SourceWorkflow.COMPLETE.key().operation());
                final boolean platform = operation.startsWith("vendor." + manifest.vendor().value() + Symbol.DOT);
                if (!sourceAuthentication && !platform) {
                    throw new ValidateException("Vendor application capability belongs to another platform");
                }
            }
        }
    }

    /**
     * Accepts only the actual wire protocols declared by built-in Vendor manifests.
     *
     * @param protocol persisted protocol identifier
     * @return whether the Vendor layer supports the protocol
     */
    @Override
    public boolean supports(final String protocol) {
        return SourceDriver.super.supports(protocol);
    }

    /**
     * Returns the aggregate Vendor Source scheme bound to this compiler.
     *
     * @return immutable Vendor Source scheme
     */
    @Override
    public VendorScheme scheme() {
        return scheme;
    }

    @Override
    public VendorOptions<?> require(final Options<?> options) {
        if (options instanceof VendorOptions<?> value) {
            return value;
        }
        throw new ValidateException("Vendor compiler requires VendorOptions");
    }

    @Override
    public VendorOptions<?> validate(final Source source) {
        final Source checked = Assert.notNull(source, "Vendor Source must not be null");
        final VendorOptions<?> options = require(checked.getOptions());
        final VariantManifest<?> manifest = directory.require(options.vendor());
        final VariantManifest.Variant variant = directory.require(options.vendor(), options.variant());
        requireRoute(checked, manifest, variant, options);
        requireOptions(variant, options);
        requireManifest(manifest, variant);
        variant.targets().resolve(options);
        return options;
    }

    @Override
    public WorkerSlots slots(final Source source, final VendorOptions<?> options) {
        final VariantManifest.Variant variant = directory.require(options.vendor(), options.variant());
        final Set<WorkerSlots.Slot> slots = new HashSet<>();
        slots.add(switch (options.credential().type()) {
            case PRIVATE_KEY -> WorkerSlots.Slot.KEY;
            case CERTIFICATE -> WorkerSlots.Slot.CERTIFICATE;
            case PASSWORD, CLIENT_SECRET, SHARED_SECRET -> WorkerSlots.Slot.SECRET;
        });
        if (variant.pkce().resolve(options)) {
            slots.add(WorkerSlots.Slot.CREDENTIAL);
        }
        return new WorkerSlots(slots);
    }

    @Override
    public Dependencies dependencies(final Source source, final VendorOptions<?> options) {
        return Dependencies.of(
                Dependencies.Service.FABRIC_CONTEXT,
                Dependencies.Service.JSON_PROVIDER,
                Dependencies.Service.EXECUTOR,
                Dependencies.Service.STATE_CACHE,
                Dependencies.Service.REPLAY_CACHE,
                Dependencies.Service.SECURITY_BASELINE);
    }

    /**
     * Compiles one validated Source registration into a Source worker.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return immutable executable entry wrapping the exact selected adapter
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration, relationships, routing, options, manifest, or selected variant
     *                                  is inconsistent
     */
    @Override
    public SourceWorker compile(final Prepared<VendorOptions<?>> prepared, final DriverServices services) {
        Assert.notNull(prepared, "Vendor Source preparation must not be null");
        final Registration.SourceEntry record = prepared.registration();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final DriverServices runtime = Assert.notNull(services, "Vendor execution services must not be null");
        final Source source = record.resource();
        requireRegistration(source, provider, library);
        final VendorOptions<?> options = prepared.options();
        final VariantManifest<?> manifest = directory.require(options.vendor());
        final VariantManifest.Variant variant = directory.require(options.vendor(), options.variant());
        requireRoute(source, manifest, variant, options);
        requireOptions(variant, options);
        requireManifest(manifest, variant);
        variant.targets().resolve(options);
        return new CompiledAdapter(
                bindings.create(library.getNamespace_id(), source.getId(), manifest, variant, options, runtime));
    }

    /**
     * Adapts a validated Vendor adapter to the non-exported runtime execution contract.
     */
    private static final class CompiledAdapter implements SourceWorker {

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

        @Override
        public void close() {
            adapter.close();
        }

    }

}
