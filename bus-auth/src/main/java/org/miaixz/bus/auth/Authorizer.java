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
import org.miaixz.bus.auth.worker.RegistrationLoader;

/**
 * Provides the unified public entry point for assembling a bus-auth runtime.
 * <p>
 * This facade selects either the complete built-in implementation set or an explicitly contributed implementation set,
 * then delegates all one-time assembly state to {@link RuntimeBuilder}. It does not retain a global runtime, dispatch
 * capabilities, query registration state, load project data, or implement authorization policy.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Authorizer {

    /**
     * Prevents construction of the stateless assembly facade.
     */
    private Authorizer() {
        // No initialization required.
    }

    /**
     * Creates a one-shot runtime builder containing every built-in protocol and Vendor implementation.
     * <p>
     * The returned builder owns only framework assembly. The supplied services and registration loader remain
     * externally owned project integrations.
     * </p>
     *
     * @param services           complete externally supplied runtime services
     * @param registrationLoader project registration-state input
     * @return builder containing the complete built-in implementation set
     * @throws IllegalArgumentException if an argument is {@code null} or a built-in contribution is invalid
     */
    public static RuntimeBuilder standard(final RuntimeServices services, final RegistrationLoader registrationLoader) {
        return RuntimeBuilder.standard(services, registrationLoader);
    }

    /**
     * Creates an empty one-shot runtime builder for an explicitly selected implementation set.
     * <p>
     * No protocol or Vendor driver is installed automatically. The integrating project must contribute every required
     * Source driver before building the runtime.
     * </p>
     *
     * @param services           complete externally supplied runtime services
     * @param registrationLoader project registration-state input
     * @return empty custom runtime builder
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public static RuntimeBuilder custom(final RuntimeServices services, final RegistrationLoader registrationLoader) {
        return RuntimeBuilder.custom(services, registrationLoader);
    }

}
