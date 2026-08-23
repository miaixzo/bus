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
package org.miaixz.bus.auth.worker;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Resolves one already assembled project runtime binding for an exact source and contract.
 * <p>
 * This port performs synchronous typed dependency resolution during Source compilation. It does not load external
 * project data and therefore does not implement {@link Loader}.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface BindingResolver {

    /**
     * Resolves one project binding for the exact Source Blueprint entry and typed key.
     *
     * @param <T>    binding contract type
     * @param source exact Source Blueprint entry requesting the binding
     * @param key    stable typed binding key
     * @return resolved binding implementing the requested contract
     */
    <T> T resolve(Blueprint.SourceEntry source, Key<T> key);

    /**
     * Identifies one project binding without using a raw class as a service-locator key.
     *
     * @param name stable binding name
     * @param type exact binding contract
     * @param <T>  binding type
     * @author Kimi Liu
     */
    record Key<T>(String name, Class<T> type) {

        /**
         * Validates one stable typed project binding key.
         */
        public Key {
            Assert.notBlank(name, "Binding key name must not be blank");
            Assert.notNull(type, "Binding key type must not be null");
        }

        /**
         * Requires one resolved binding to implement this exact project contract.
         *
         * @param binding resolved project binding
         * @return binding narrowed to the exact contract
         */
        public T require(final Object binding) {
            final Object resolved = Assert.notNull(binding, "Resolved binding must not be null");
            if (!type.isInstance(resolved)) {
                throw new ValidateException("Resolved binding does not implement the required contract");
            }
            return type.cast(resolved);
        }

    }

}
