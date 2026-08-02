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
package org.miaixz.bus.spring;

import org.springframework.core.Ordered;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.lang.annotation.Nullable;

/**
 * An interface for providing authenticated request context information.
 * <p>
 * Implementations provide an already authenticated {@link Authorize} value. They are not responsible for reading or
 * interpreting raw request headers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ContextProvider extends Ordered {

    /**
     * Gets the authenticated authorization information for the current user. Implementations must be side-effect free
     * and must not consume or mutate transport input.
     *
     * @return An {@link Authorize} object, or null if not available.
     */
    @Nullable
    default Authorize getAuthorize() {
        return null;
    }

    /**
     * Orders providers before the first side-effect-free authorization resolution pass.
     *
     * @return lowest precedence unless an implementation declares a stronger priority
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
