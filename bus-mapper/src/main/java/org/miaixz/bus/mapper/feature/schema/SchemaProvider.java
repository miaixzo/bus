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
package org.miaixz.bus.mapper.feature.schema;

import java.util.Collection;
import java.util.Collections;

import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * Entity schema initialization provider interface.
 * <p>
 * Implementations can provide schema initialization configuration and optional entity classes programmatically. The
 * starter adapter resolves this provider from the runtime container and uses it during mapper plugin configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SchemaProvider extends MapperProvider<SchemaConfig> {

    /**
     * Provides schema configuration for a datasource.
     * <p>
     * Returning {@code null} means the schema initializer should use mapper configuration properties and defaults.
     *
     * @param datasourceKey datasource key, or an empty value for the primary datasource
     * @return schema configuration, or {@code null}
     */
    default SchemaConfig getConfig(String datasourceKey) {
        return getConfig();
    }

    /**
     * Provides global entity classes for schema initialization.
     *
     * @return entity classes, never {@code null}
     */
    default Collection<Class<?>> getEntityClasses() {
        return Collections.emptyList();
    }

    /**
     * Provides entity classes for schema initialization of a datasource.
     *
     * @param datasourceKey datasource key, or an empty value for the primary datasource
     * @return entity classes, never {@code null}
     */
    default Collection<Class<?>> getEntityClasses(String datasourceKey) {
        return getEntityClasses();
    }

}
