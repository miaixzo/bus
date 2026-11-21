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
package org.miaixz.bus.cortex.guard.token;

import java.util.function.Supplier;

/**
 * Holds the process-wide service token used for registry and snapshot calls to Lattice.
 * <p>
 * The starter installs a supplier when outbound service identity is enabled. Registry clients read the token per
 * request, so epoch rotation takes effect without recreating the client.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TrustToken {

    /**
     * Registry token supplier, or null when outbound service identity is disabled.
     */
    private static volatile Supplier<String> registry;

    /**
     * Prevents additional instances.
     */
    private TrustToken() {
        // No initialization required.
    }

    /**
     * Installs the registry token supplier.
     *
     * @param supplier token supplier bound to the configured registry audience
     */
    public static void install(final Supplier<String> supplier) {
        registry = supplier;
    }

    /**
     * Returns the current registry token.
     *
     * @return compact service token, or null when outbound service identity is disabled
     */
    public static String registry() {
        final Supplier<String> supplier = registry;
        return supplier == null ? null : supplier.get();
    }

}
