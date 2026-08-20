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

import org.miaixz.bus.auth.Authenticator;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.protocol.ldap.Ldap;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.radius.Radius;
import org.miaixz.bus.auth.protocol.saml.Saml;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.auth.registry.AtomicRegistryState;
import org.miaixz.bus.auth.registry.DefaultRegistry;
import org.miaixz.bus.auth.registry.RegistrationValidator;
import org.miaixz.bus.auth.registry.RegistryView;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorModule;
import org.miaixz.bus.auth.worker.RegistrationLoader;
import org.miaixz.bus.auth.worker.RegistryListener;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Performs deterministic one-time assembly of the bus-auth Registry and runtime lifecycle.
 * <p>
 * The standard factory explicitly assembles every built-in protocol and Vendor driver owned by bus-auth. The custom
 * factory starts without built-ins for deployments that intentionally select another implementation set. Build freezes
 * the resulting indexes, validates type and profile uniqueness, publishes a synchronous revision-zero empty view, and
 * returns a running AuthRuntime. External registration data is loaded only by an explicit reload after build.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RuntimeBuilder {

    /**
     * Complete externally supplied execution service set.
     */
    private final ExecutionServices services;

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
     * Whether the one-shot build process has begun.
     */
    private boolean built;

    /**
     * Creates an empty private builder used only by the two named assembly factories.
     *
     * @param services complete externally owned execution services
     */
    private RuntimeBuilder(final ExecutionServices services) {
        this.services = Assert.notNull(services, "Runtime execution services must not be null");
        this.registrationLoader = services.registrationLoader();
        this.sources = new ArrayList<>();
        this.listeners = new ArrayList<>(services.registryListeners());
    }

    /**
     * Creates a standard one-shot builder containing every built-in protocol and Vendor implementation.
     *
     * @param services complete externally owned execution services
     * @return builder with the complete built-in implementation set
     * @throws IllegalArgumentException if an argument is {@code null} or a built-in contribution is invalid
     */
    public static RuntimeBuilder standard(final ExecutionServices services) {
        final RuntimeBuilder builder = new RuntimeBuilder(services);
        final VendorModule vendors = Vendor.module();
        return builder.sources(
                List.of(
                        OAuth2.source(),
                        OAuth2.provider(),
                        OpenIdConnect.source(),
                        OpenIdConnect.provider(),
                        Saml.source(),
                        Saml.provider(),
                        Ldap.source(),
                        Ldap.provider(),
                        Scim.provider(),
                        Radius.provider(),
                        vendors.source()));
    }

    /**
     * Creates an empty one-shot builder for an explicitly selected implementation set.
     *
     * @param services complete externally owned execution services
     * @return empty custom builder
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public static RuntimeBuilder custom(final ExecutionServices services) {
        return new RuntimeBuilder(services);
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
     * Freezes contributions and synchronously assembles a running revision-zero runtime.
     *
     * @return fully assembled running AuthRuntime
     * @throws ValidateException        if build was already attempted or contributions conflict
     * @throws IllegalArgumentException if a driver or listener is invalid
     */
    public synchronized AuthRuntime build() {
        mutable();
        built = true;
        final SnapshotCompiler snapshotCompiler = new SnapshotCompiler(List.copyOf(sources), services);
        final Registry.Revision revision = new Registry.Revision(0L);
        final Registry.Snapshot snapshot = new Registry.Snapshot(revision, List.of());
        final RegistryView initial = snapshotCompiler.compile(snapshot);
        final AtomicRegistryState state = new AtomicRegistryState(initial);
        final Registry registry = new DefaultRegistry(state);
        final Authenticator authenticator = new DefaultAuthenticator(state);
        final RuntimeReloadService reloadService = new RuntimeReloadService(registrationLoader,
                new RegistrationValidator(), snapshotCompiler, state, List.copyOf(listeners));
        return new AuthRuntime(registry, authenticator, reloadService);
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
