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
package org.miaixz.bus.auth.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Dispatcher;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.Ldap;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.radius.Radius;
import org.miaixz.bus.auth.protocol.saml.Saml;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.auth.registry.CurrentRegistry;
import org.miaixz.bus.auth.registry.SnapshotFault;
import org.miaixz.bus.auth.registry.SnapshotValidator;
import org.miaixz.bus.auth.registry.SourceValidator;
import org.miaixz.bus.auth.source.DriverDirectory;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorLocator;
import org.miaixz.bus.auth.vendor.VendorModule;
import org.miaixz.bus.auth.worker.RegistryListener;
import org.miaixz.bus.auth.worker.loader.RegistrationLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Performs deterministic one-time assembly of the bus-auth Registry and runtime lifecycle.
 * <p>
 * The standard factory explicitly assembles every built-in protocol and Vendor driver owned by bus-auth. The custom
 * factory starts without built-ins for deployments that intentionally select another implementation set. Build freezes
 * the resulting indexes and validates type and profile uniqueness. Normal build loads and commits the project's first
 * complete registration snapshot before exposing the runtime. Empty startup is available only through the explicitly
 * named {@link #buildEmpty()} method. RegistrationLoader and RegistryListener are direct assembly inputs and never
 * become protocol execution services.
 * </p>
 *
 * @author Kimi Liu
 */
public class RuntimeBuilder {

    /**
     * Formats the first structured, non-sensitive startup fault for an actionable build failure.
     *
     * @param report rejected initial Registry report containing at least one fault
     * @return safe startup rejection description
     */
    private static String rejection(final Registry.Report report) {
        final SnapshotFault fault = report.faults().getFirst();
        final String resource = fault.id().isPresent() ? fault.id().getOrNull() : "snapshot";
        final String field = fault.field().isPresent() ? fault.field().getOrNull() : "unknown";
        return "Initial authentication registration snapshot was rejected: " + report.faults().size()
                + " fault(s); first=" + fault.stage().name() + "/" + resource + "/" + field + ": "
                + fault.safeDescription();
    }

    /**
     * Externally supplied protocol execution service set.
     */
    private final RuntimeServices services;

    /**
     * External complete registration snapshot loader.
     */
    private final RegistrationLoader registrationLoader;

    /**
     * Explicit Source drivers retained in caller-provided order.
     */
    private final List<SourceDriver<?>> sources;

    /**
     * Explicit Registry listeners retained in caller-provided order.
     */
    private final List<RegistryListener> listeners;

    /**
     * Vendor inventory paired with the assembled Vendor Source driver, when selected.
     */
    private VendorLocator vendorLocator;

    /**
     * Whether the one-shot build process has begun.
     */
    private boolean built;

    /**
     * Creates an empty private builder used only by the two named assembly factories.
     *
     * @param services           complete externally owned execution services
     * @param registrationLoader project registration-state input
     */
    public RuntimeBuilder(final RuntimeServices services, final RegistrationLoader registrationLoader) {
        this.services = Assert.notNull(services, "Runtime execution services must not be null");
        this.registrationLoader = Assert.notNull(registrationLoader, "Registration loader must not be null");
        this.sources = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Creates a standard one-shot builder containing every built-in protocol and Vendor implementation.
     *
     * @param services           complete externally owned execution services
     * @param registrationLoader project registration-state input
     * @return builder with the complete built-in implementation set
     * @throws IllegalArgumentException if an argument is {@code null} or a built-in contribution is invalid
     */
    public static RuntimeBuilder standard(final RuntimeServices services, final RegistrationLoader registrationLoader) {
        final RuntimeBuilder builder = new RuntimeBuilder(services, registrationLoader);
        final VendorModule vendors = Vendor.module();
        builder.sources(
                List.of(
                        OAuth2.client(),
                        OAuth2.server(),
                        OpenIdConnect.client(),
                        OpenIdConnect.server(),
                        Saml.client(),
                        Saml.server(),
                        Ldap.client(),
                        Ldap.server(),
                        Scim.server(),
                        Radius.server()));
        return builder.vendors(vendors);
    }

    /**
     * Creates an empty one-shot builder for an explicitly selected implementation set.
     *
     * @param services           complete externally owned execution services
     * @param registrationLoader project registration-state input
     * @return empty custom builder
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public static RuntimeBuilder custom(final RuntimeServices services, final RegistrationLoader registrationLoader) {
        return new RuntimeBuilder(services, registrationLoader);
    }

    /**
     * Adds one explicit client-role, server-role, or Vendor Source driver before build.
     *
     * @param driver Source driver
     * @return this builder
     * @throws IllegalArgumentException if {@code driver} is {@code null}
     * @throws ValidateException        if build has already begun
     */
    public synchronized RuntimeBuilder source(final SourceDriver<?> driver) {
        mutable();
        sources.add(Assert.notNull(driver, "Source driver must not be null"));
        return this;
    }

    /**
     * Adds Source drivers in caller-provided deterministic order.
     *
     * @param drivers Source drivers
     * @return this builder
     * @throws IllegalArgumentException if the list or an entry is {@code null}
     * @throws ValidateException        if build has already begun
     */
    public synchronized RuntimeBuilder sources(final List<? extends SourceDriver<?>> drivers) {
        mutable();
        Assert.notNull(drivers, "Source driver list must not be null");
        for (SourceDriver<?> driver : drivers) {
            sources.add(Assert.notNull(driver, "Source driver must not be null"));
        }
        return this;
    }

    /**
     * Adds one frozen Vendor module as paired descriptor metadata and a Source driver contribution.
     *
     * @param module Vendor module
     * @return this builder
     */
    public synchronized RuntimeBuilder vendors(final VendorModule module) {
        mutable();
        final VendorModule checked = Assert.notNull(module, "Vendor module must not be null");
        Assert.isTrue(vendorLocator == null, "Vendor module has already been configured");
        vendorLocator = checked.locator();
        sources.add(checked.source());
        return this;
    }

    /**
     * Adds one externally implemented Registry commit listener before build.
     *
     * @param listener Registry listener
     * @return this builder
     * @throws IllegalArgumentException if {@code listener} is {@code null}
     * @throws ValidateException        if build has already begun
     */
    public synchronized RuntimeBuilder listener(final RegistryListener listener) {
        mutable();
        listeners.add(Assert.notNull(listener, "Registry listener must not be null"));
        return this;
    }

    /**
     * Freezes contributions, loads the initial project snapshot, and exposes the runtime only after a successful atomic
     * commit.
     *
     * @param context immutable non-secret startup context
     * @param timeout shared end-to-end startup timeout
     * @return stage containing the fully initialized RuntimeManager
     * @throws ValidateException        if build was already attempted or contributions conflict
     * @throws IllegalArgumentException if a driver or listener is invalid
     */
    public synchronized CompletionStage<RuntimeManager> build(final Context context, final Timeout timeout) {
        Assert.notNull(context, "Runtime startup context must not be null");
        Assert.notNull(timeout, "Runtime startup timeout must not be null");
        final RuntimeManager runtime = assemble();
        return runtime.reload(context, timeout).thenApply(report -> {
            if (!report.faults().isEmpty()) {
                runtime.close();
                throw new ValidateException(rejection(report));
            }
            return runtime;
        }).whenComplete((started, failure) -> {
            if (failure != null) {
                runtime.close();
            }
        });
    }

    /**
     * Assembles a running revision-zero runtime without loading project registrations.
     * <p>
     * This entry is intentionally explicit for administrative processes that must construct the framework before any
     * registration source is available. Authentication calls will find no Sources until a successful reload.
     * </p>
     *
     * @return fully assembled empty runtime
     */
    public synchronized RuntimeManager buildEmpty() {
        return assemble();
    }

    /**
     * Freezes contributions and assembles the shared revision-zero runtime state.
     *
     * @return assembled runtime awaiting either an initial or later reload
     */
    private RuntimeManager assemble() {
        mutable();
        built = true;
        final List<SourceDriver<?>> frozenSources = List.copyOf(sources);
        final DriverDirectory directory = new DriverDirectory(frozenSources);
        final RuntimeDescriptor descriptor = new RuntimeDescriptor(directory, Optional.ofNullable(vendorLocator));
        final SnapshotCompiler snapshotCompiler = new SnapshotCompiler(directory, services);
        final Registry.Revision revision = new Registry.Revision(0L);
        final Registry.Snapshot snapshot = new Registry.Snapshot(revision, List.of());
        final RuntimeContainer initial = snapshotCompiler.compile(snapshot);
        final RuntimeContainer.Cell containers = new RuntimeContainer.Cell(initial);
        final RuntimeLifecycle lifecycle = new RuntimeLifecycle();
        final Registry registry = new CurrentRegistry(() -> containers.current().registry());
        final Dispatcher dispatcher = new DefaultDispatcher(containers, lifecycle);
        final RegistryNotifier notifier = new RegistryNotifier(List.copyOf(listeners), services.executor());
        final RuntimeReloadService reloadService = new RuntimeReloadService(registrationLoader,
                new SnapshotValidator(new SourceValidator(directory)), snapshotCompiler, containers, notifier,
                lifecycle);
        return new RuntimeManager(registry, dispatcher, reloadService, descriptor, lifecycle, containers);
    }

    /**
     * Rejects mutation after the one-shot build process has begun.
     *
     * @throws ValidateException if this builder is frozen
     */
    private void mutable() {
        if (built) {
            throw new ValidateException("Runtime builder is already frozen");
        }
    }

}
