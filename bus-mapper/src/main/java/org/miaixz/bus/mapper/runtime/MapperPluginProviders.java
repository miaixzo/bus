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
package org.miaixz.bus.mapper.runtime;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.mapper.feature.affix.AffixValueProvider;
import org.miaixz.bus.mapper.feature.audit.AuditProvider;
import org.miaixz.bus.mapper.feature.populate.PopulateProvider;
import org.miaixz.bus.mapper.feature.schema.SchemaProvider;
import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.mapper.feature.visible.VisibleProvider;

/**
 * Runtime collaborators used during Mapper plugin construction.
 * <p>
 * Object providers are deliberately kept out of {@code MapperOptions} because they come from a runtime container
 * instead of user configuration. The JDBC key supplier is also a callback only: this object does not copy or own data
 * source routing state.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
public class MapperPluginProviders {

    /**
     * Provider used to resolve tenant identifiers and optional tenant handler configuration.
     */
    private TenantProvider tenantProvider;

    /**
     * Provider used to resolve dynamic table prefixes, suffixes, and optional affix configuration.
     */
    private AffixValueProvider affixProvider;

    /**
     * Provider used to resolve data visibility conditions and optional visible handler configuration.
     */
    private VisibleProvider visibleProvider;

    /**
     * Provider used to resolve automatic fill values and optional populate handler configuration.
     */
    private PopulateProvider populateProvider;

    /**
     * Provider used to resolve audit behavior and optional audit handler configuration.
     */
    private AuditProvider auditProvider;

    /**
     * Provider used to resolve schema initialization configuration and entity classes.
     */
    private SchemaProvider schemaProvider;

    /**
     * Read-only supplier for the effective JDBC data source key, owned by the data-access integration.
     */
    private Supplier<String> datasourceKeyProvider;

    /**
     * Initializes an empty runtime collaborator holder.
     */
    public MapperPluginProviders() {
        // No initialization required.
    }

}
