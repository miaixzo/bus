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
package org.miaixz.bus.auth.source.vendor;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.logger.Logger;

/**
 * Compiles one complete externally loaded Vendor Source through an exact platform manifest and adapter factory.
 * <p>
 * Compilation is deterministic and performs no data loading, persistence decoding, Roster access, network operation, or
 * reflection. The external project supplies a typed VendorOptions value; this compiler only validates platform and
 * variant routing, resolves the frozen manifest, and creates the matching adapter.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorDriver implements SourceDriver<VendorOptions<?>> {

    /**
     * Immutable Vendor and variant manifest locator.
     */
    private final VendorLocator locator;

    /**
     * Immutable exact platform-variant adapter bindings.
     */
    private final AdapterBindings bindings;

    /**
     * Aggregate Vendor Source scheme exposed to runtime assembly.
     */
    private final VendorScheme scheme;

    /**
     * Creates a compiler over one frozen and consistently keyed Vendor module.
     *
     * @param locator  complete Vendor manifest locator
     * @param bindings complete platform adapter factory bindings
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public VendorDriver(final VendorLocator locator, final AdapterBindings bindings) {
        this.locator = Assert.notNull(locator, "Vendor driver locator must not be null");
        this.bindings = Assert.notNull(bindings, "Vendor driver adapter bindings must not be null");
        this.scheme = new VendorScheme(locator);
    }

    /**
     * Validates complete Source routing and its resolved required entity relationships.
     *
     * @param source   complete Source entity
     * @param provider resolved owning Provider
     * @param library  resolved Provider Library
     * @throws ValidateException if Source classification or relationships are inconsistent
     */
    private static void requireSourceGraph(final Source source, final Provider provider, final Library library) {
        final Source checked = Assert.notNull(source, "Vendor Source must not be null");
        if (!VendorScheme.ID.equals(checked.getType()) || checked.getProtocol() == null || library.getSpace_id() == null
                || library.getSpace_id().isBlank() || !provider.getId().equals(checked.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("Vendor driver requires a matching Source configuration");
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
            final VendorManifest<?> manifest,
            final VendorManifest.Variant variant,
            final VendorOptions<?> options) {
        if (!options.vendor().equals(manifest.vendor()) || !variant.platform().equals(manifest.vendor())
                || !options.variant().equals(variant.variant())
                || !variant.protocol().name().equalsIgnoreCase(source.getProtocol())) {
            throw new ValidateException(
                    "Vendor manifest, options, Source configuration, and protocol must identify one variant");
        }
    }

    /**
     * Validates common deployment values without duplicating platform-specific options rules.
     *
     * @param variant selected variant
     * @param options complete platform options
     * @throws ValidateException if scope, redirect, credential, or interaction state is inconsistent
     */
    private static void requireOptions(final VendorManifest.Variant variant, final VendorOptions<?> options) {
        Assert.notNull(options.clientId(), "Vendor client identifier must not be null");
        Assert.notNull(options.credential(), "Vendor credential reference must not be null");
        if (options.credential().type() != variant.credentialType()) {
            throw new ValidateException("Vendor credential reference type does not match the selected variant");
        }
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
    private static void requireManifest(final VendorManifest<?> manifest, final VendorManifest.Variant variant) {
        final Set<Capability.Key> keys = new HashSet<>();
        for (Capability<?, ?> capability : variant.capabilityManifest().capabilities()) {
            if (!keys.add(capability.key())) {
                throw new ValidateException("Vendor manifest contains a duplicate capability key");
            }
            final Protocol protocol = capability.key().protocol().getOrNull();
            if (protocol != null && protocol != variant.protocol()) {
                throw new ValidateException("Vendor standard capability must use the selected variant protocol");
            }
            if (protocol == null) {
                final String operation = capability.key().operation();
                final boolean sourceWorkflow = operation.equals(SourceWorkflow.INITIATE.key().operation())
                        || operation.equals(SourceWorkflow.COMPLETE.key().operation());
                final boolean realm = realm(capability.key());
                final boolean platform = operation.startsWith("vendor." + manifest.vendor().value() + Symbol.DOT);
                if (!sourceWorkflow && !realm && !platform) {
                    throw new ValidateException("Vendor application capability belongs to another platform");
                }
            }
        }
    }

    /**
     * Tests whether one application Capability key is an exact implementation-neutral Realm operation.
     *
     * @param key application Capability key declared by one Vendor Variant
     * @return {@code true} only for one of the four shared Realm Capability keys
     */
    private static boolean realm(final Capability.Key key) {
        return key.equals(Realm.DESCRIBE.key()) || key.equals(Realm.SNAPSHOT.key()) || key.equals(Realm.CHANGES.key())
                || key.equals(Realm.RETRIEVE.key());
    }

    /**
     * Accepts only the actual wire protocols declared by registered Vendor manifests.
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

    /**
     * Narrows one generic Source options value to the Vendor options contract.
     *
     * @param options candidate immutable Source options
     * @return exact Vendor options value
     * @throws ValidateException if the value is not a Vendor options implementation
     */
    @Override
    public VendorOptions<?> require(final Options<?> options) {
        if (options instanceof VendorOptions<?> value) {
            return value;
        }
        throw new ValidateException("Vendor driver requires VendorOptions");
    }

    /**
     * Validates complete Vendor routing, options, manifest ownership, and deterministic targets.
     *
     * @param source complete Vendor Source configuration
     * @return validated exact Vendor options value
     * @throws IllegalArgumentException if a required value is {@code null}
     * @throws ValidateException        if routing, options, capabilities, or targets are inconsistent
     */
    @Override
    public VendorOptions<?> validate(final Source source) {
        final Source checked = Assert.notNull(source, "Vendor Source must not be null");
        final VendorOptions<?> options = require(checked.getOptions());
        final VendorManifest<?> manifest = locator.require(options.vendor());
        final VendorManifest.Variant variant = locator.require(options.vendor(), options.variant());
        requireRoute(checked, manifest, variant, options);
        requireOptions(variant, options);
        requireManifest(manifest, variant);
        variant.targets().resolve(options);
        return options;
    }

    /**
     * Returns the external credential slots required by the selected Vendor Variant.
     *
     * @param source  validated Vendor Source configuration
     * @param options exact selected Vendor options
     * @return immutable credential and PKCE slot set
     */
    @Override
    public WorkerSlots slots(final Source source, final VendorOptions<?> options) {
        final VendorManifest.Variant variant = locator.require(options.vendor(), options.variant());
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

    /**
     * Returns the framework services required by every compiled Vendor adapter.
     *
     * @param source  validated Vendor Source configuration
     * @param options exact selected Vendor options
     * @return immutable Vendor runtime dependency set
     */
    @Override
    public Dependencies dependencies(final Source source, final VendorOptions<?> options) {
        return Dependencies.of(
                Dependencies.Service.EXECUTOR,
                Dependencies.Service.STATE_CACHE,
                Dependencies.Service.REPLAY_CACHE,
                Dependencies.Service.POLICIES);
    }

    /**
     * Compiles one validated Source configuration into a Source worker.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services capability-limited Source services
     * @return immutable executable entry wrapping the exact selected adapter
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the Source graph, routing, options, manifest, or selected variant is
     *                                  inconsistent
     */
    @Override
    public SourceWorker compile(final Prepared<VendorOptions<?>> prepared, final SourceServices services) {
        Assert.notNull(prepared, "Vendor Source preparation must not be null");
        final Blueprint.SourceEntry entry = prepared.entry();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final SourceServices runtime = Assert.notNull(services, "Vendor execution services must not be null");
        final Source source = entry.resource();
        requireSourceGraph(source, provider, library);
        final VendorOptions<?> options = prepared.options();
        final VendorManifest<?> manifest = locator.require(options.vendor());
        final VendorManifest.Variant variant = locator.require(options.vendor(), options.variant());
        Logger.debug(
                true,
                "Auth",
                "Vendor Source compilation started: sourceId={}, vendor={}, variant={}, manifest={}, options={}",
                source.getId(),
                options.vendor().value(),
                options.variant().value(),
                manifest.getClass().getName(),
                options.getClass().getName());
        requireRoute(source, manifest, variant, options);
        requireOptions(variant, options);
        requireManifest(manifest, variant);
        variant.targets().resolve(options);
        final SourceWorker worker = bindings
                .create(library.getSpace_id(), source.getId(), manifest, variant, options, runtime);
        Logger.debug(
                false,
                "Auth",
                "Vendor Source compilation completed: sourceId={}, vendor={}, variant={}, worker={}",
                source.getId(),
                options.vendor().value(),
                options.variant().value(),
                worker.getClass().getName());
        return worker;
    }

}
