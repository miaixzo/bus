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
package org.miaixz.bus.starter.mapper;

import java.util.Objects;

import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.spring.ContextBuilder;

/**
 * Adapts the authenticated application context to Mapper tenant isolation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ContextTenantProvider implements TenantProvider {

    /**
     * Application-context-scoped runtime context facade.
     */
    private final ContextBuilder contextBuilder;

    /**
     * Creates a provider bound to the current Spring application context.
     *
     * @param contextBuilder authenticated context facade
     */
    public ContextTenantProvider(ContextBuilder contextBuilder) {
        this.contextBuilder = Objects.requireNonNull(contextBuilder, "contextBuilder");
    }

    /**
     * Returns only the tenant identifier established by authenticated context providers.
     *
     * @return authenticated tenant identifier, or {@code null}
     */
    @Override
    public String getTenantId() {
        return this.contextBuilder.getTenantId();
    }

}
