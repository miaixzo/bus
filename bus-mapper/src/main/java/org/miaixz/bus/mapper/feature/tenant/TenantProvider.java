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
package org.miaixz.bus.mapper.feature.tenant;

import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * Supplies the trusted tenant identifier used by Mapper tenant isolation.
 * <p>
 * Implementations must return only a tenant identifier established by the application's authenticated context. This
 * protocol does not receive or interpret HTTP requests, headers, parameters, request bodies, tokens, or security
 * framework objects. Authentication adapters must validate those inputs before exposing the final tenant identifier
 * through this interface.
 * <p>
 * Implementations may also provide tenant configuration through {@link #getConfig()}. When tenant isolation is
 * optional, a missing authenticated tenant is represented by an empty result; required-mode enforcement belongs to the
 * Mapper tenant handler.
 *
 * @see TenantConfig
 * @see TenantHandler
 * @see MapperProvider
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface TenantProvider extends MapperProvider<TenantConfig> {

    /**
     * Returns the tenant identifier already established by the authenticated application context.
     *
     * @return the authenticated tenant ID, or {@code null} or an empty string when no tenant is available and tenant
     *         isolation is optional
     */
    String getTenantId();

}
