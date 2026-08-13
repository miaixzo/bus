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

import org.miaixz.bus.auth.runtime.AuthRuntime;
import org.miaixz.bus.auth.runtime.RuntimeBuilder;
import org.miaixz.bus.auth.vendor.VendorConfiguration;
import org.miaixz.bus.auth.vendor.VendorProvider;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Protocol-neutral authentication entry point backed by a shared default runtime.
 *
 * @author Kimi Liu
 */
public final class Authorizer {

    /**
     * Prevents construction of the static authentication entry point.
     */
    private Authorizer() {
        // No initialization required.
    }

    /**
     * Creates a runtime builder preloaded with built-in vendor definitions.
     *
     * @return mutable runtime builder
     */
    public static RuntimeBuilder builder() {
        return AuthRuntime.builder();
    }

    /**
     * Creates an empty runtime builder for explicit registration.
     *
     * @return empty mutable runtime builder
     */
    public static RuntimeBuilder emptyBuilder() {
        return AuthRuntime.emptyBuilder();
    }

    /**
     * Returns the lazily shared authentication runtime.
     *
     * @return shared runtime
     * @throws InternalException if runtime construction fails
     */
    public static AuthRuntime runtime() {
        synchronized (DefaultRuntime.class) {
            try {
                return Instances.get(DefaultRuntime.class.getName(), DefaultRuntime::new).runtime;
            } catch (final RuntimeException failure) {
                if (failure instanceof InternalException) {
                    throw failure;
                }
                throw new InternalException("Unable to create authentication runtime", failure);
            }
        }
    }

    /**
     * Creates a third-party authentication client from the shared runtime.
     *
     * @param name          vendor definition name
     * @param configuration explicit vendor construction dependencies
     * @return configured vendor provider
     * @throws RuntimeException if the definition is absent or provider construction fails
     */
    public static VendorProvider vendor(final String name, final VendorConfiguration configuration) {
        return runtime().provider(name, configuration);
    }

    /**
     * Looks up a typed protocol-neutral provider.
     *
     * @param id   stable provider identifier
     * @param type required provider type
     * @param <P>  provider type
     * @return registered provider
     * @throws RuntimeException if the provider is absent or has a different type
     */
    public static <P extends Provider> P provider(final String id, final Class<P> type) {
        return runtime().provider(id, type);
    }

    /**
     * Closes and removes the shared authentication runtime when initialized.
     *
     * @throws RuntimeException if runtime shutdown fails
     */
    public static void shutdown() {
        synchronized (DefaultRuntime.class) {
            if (!Instances.exists(DefaultRuntime.class)) {
                return;
            }
            try {
                Instances.get(DefaultRuntime.class.getName(), DefaultRuntime::new).runtime.close();
            } finally {
                Instances.remove(DefaultRuntime.class);
            }
        }
    }

    /**
     * Lazily constructed value stored through the shared Bus instance registry.
     *
     * @author Kimi Liu
     */
    private static final class DefaultRuntime {

        /**
         * Runtime owned by this singleton holder.
         */
        private final AuthRuntime runtime = AuthRuntime.builder().build();

        /**
         * Creates the singleton runtime holder.
         */
        private DefaultRuntime() {
            // Field initialization creates the runtime.
        }
    }

}
