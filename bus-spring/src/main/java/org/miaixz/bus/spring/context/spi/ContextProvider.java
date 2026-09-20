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
package org.miaixz.bus.spring.context.spi;

import org.springframework.core.Ordered;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.spring.context.ContextState;

/**
 * Resolves an authenticated subject from an already normalized initial context.
 * <p>
 * Implementations must not mutate the supplied state or read Servlet transport objects. Returning {@code null} means
 * that the provider does not recognize the current credentials.
 *
 * @author Kimi Liu
 */
public interface ContextProvider extends Ordered {

    /**
     * Resolves an authenticated subject from normalized correlation and credential values.
     *
     * @param context immutable initial context without an authenticated subject
     * @return authenticated subject, or {@code null} when this provider does not recognize the credentials
     */
    @Nullable
    default Authorize getAuthorize(ContextState context) {
        return null;
    }

    /**
     * Returns the provider precedence used when multiple authentication providers are registered.
     *
     * @return provider order; lower values execute first
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
