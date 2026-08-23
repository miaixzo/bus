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
package org.miaixz.bus.auth;

import org.miaixz.bus.auth.runtime.RuntimeBuilder;
import org.miaixz.bus.auth.runtime.RuntimeServices;
import org.miaixz.bus.auth.source.SourceAggregate;
import org.miaixz.bus.auth.source.SourceSuite;
import org.miaixz.bus.auth.source.vendor.VendorConfigurer;
import org.miaixz.bus.auth.source.vendor.VendorCredentialWriter;
import org.miaixz.bus.auth.source.vendor.VendorModule;
import org.miaixz.bus.auth.worker.loader.BlueprintLoader;
import org.miaixz.bus.core.lang.Assert;

/**
 * Provides the unified public entry point for assembling bus-auth authentication and delegated-authorization
 * integrations.
 * <p>
 * This facade selects either the complete built-in implementation set or an explicitly registered implementation set,
 * then delegates all one-time assembly state to {@link RuntimeBuilder}. It does not retain a global runtime, dispatch
 * capabilities, query Roster state, load project data, or decide application access-control policy.
 * </p>
 *
 * @author Kimi Liu
 */
public class Authorize {

    /**
     * Creates a stateless authentication and delegated-authorization assembly facade.
     */
    public Authorize() {
        // No initialization required.
    }

    /**
     * Creates a one-shot runtime builder containing every built-in protocol and Vendor implementation.
     * <p>
     * The returned builder owns only framework assembly. The supplied runtime services and Blueprint loader remain
     * externally owned project integrations.
     * </p>
     *
     * @param runtimeServices complete externally supplied runtime services
     * @param blueprintLoader project Blueprint input
     * @return builder containing the complete built-in implementation set
     * @throws IllegalArgumentException if an argument is {@code null} or a built-in module declaration is invalid
     */
    public static RuntimeBuilder standard(
            final RuntimeServices runtimeServices,
            final BlueprintLoader blueprintLoader) {
        final SourceAggregate aggregate = SourceSuite.load().freeze();
        return RuntimeBuilder.custom(runtimeServices, blueprintLoader).modules(aggregate.modules());
    }

    /**
     * Creates an empty one-shot runtime builder for an explicitly selected implementation set.
     * <p>
     * No protocol or Vendor module is installed automatically. The integrating project must add every required
     * {@link org.miaixz.bus.auth.source.SourceModule} before building the runtime.
     * </p>
     *
     * @param runtimeServices complete externally supplied runtime services
     * @param blueprintLoader project Blueprint input
     * @return empty custom runtime builder
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public static RuntimeBuilder custom(final RuntimeServices runtimeServices, final BlueprintLoader blueprintLoader) {
        return RuntimeBuilder.custom(runtimeServices, blueprintLoader);
    }

    /**
     * Creates the unified client-side Vendor configuration entry point.
     * <p>
     * The returned coordinator accepts plaintext only through a short-lived lease, delegates secure storage to the
     * supplied project Worker, and returns immutable concrete Vendor Options. This facade does not store credentials,
     * create a Source, mutate a Roster, or start authentication.
     * </p>
     *
     * @param vendors immutable built-in, custom, or combined Vendor module
     * @param writer  project-owned recoverable credential storage port
     * @return client-side Vendor configuration coordinator
     */
    public static VendorConfigurer clients(final VendorModule vendors, final VendorCredentialWriter writer) {
        return Assert.notNull(vendors, "Vendor module must not be null").configurer(writer);
    }

}
