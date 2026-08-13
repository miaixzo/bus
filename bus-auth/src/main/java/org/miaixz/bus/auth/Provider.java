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

/**
 * Base contract implemented by authentication capability providers.
 *
 * @author Kimi Liu
 */
public interface Provider extends org.miaixz.bus.core.Provider<Descriptor> {

    /**
     * Returns the immutable descriptor for this provider.
     *
     * @return provider descriptor
     */
    Descriptor descriptor();

    /**
     * Returns the descriptor identifier as the stable Bus provider type.
     *
     * @return stable provider identifier
     */
    @Override
    default Object type() {
        return descriptor().id();
    }

    /**
     * Creates provider instances from typed configuration.
     *
     * @param <C> configuration type
     * @param <P> provider type
     * @author Kimi Liu
     */
    @FunctionalInterface
    interface Factory<C, P extends Provider> {

        /**
         * Creates a provider instance.
         *
         * @param configuration provider configuration
         * @return created provider
         */
        P create(C configuration);

    }

}
