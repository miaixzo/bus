/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.builder;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.provider.NamingProvider;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The default table builder, which supports processing entity classes annotated with `jakarta.persistence` annotations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TableAnnotationBuilder implements TableSchemaBuilder {

    /**
     * Creates table metadata for an entity class based on annotations or default naming conventions.
     *
     * @param entityClass The entity class.
     * @param chain       The table schema builder chain.
     * @return The table metadata.
     */
    @Override
    public TableMeta createTable(Class<?> entityClass, Chain chain) {
        TableMeta tableMeta = chain.createTable(entityClass);
        if (tableMeta == null) {
            tableMeta = TableMeta.of(entityClass);
        }

        // Process table-related annotations and set the default table name.
        processTableAnnotations(tableMeta, entityClass);

        // Enable automatic result mapping for classes annotated with @Entity.
        if (entityClass.isAnnotationPresent(Entity.class)) {
            tableMeta.autoResultMap(true);
        }

        EntityClassBuilder.setTableMeta(tableMeta);

        return tableMeta;
    }

    /**
     * Processes the {@link Table} annotation to set the table name, catalog, and schema, or uses default naming
     * conventions if the annotation is not present.
     *
     * @param entityClass The entity class.
     * @param tableMeta   The table metadata.
     */
    protected void processTableAnnotations(TableMeta tableMeta, Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (StringKit.isNotEmpty(table.name())) {
                tableMeta.table(table.name());
            }
            if (StringKit.isNotEmpty(table.catalog())) {
                tableMeta.catalog(table.catalog());
            }
            if (StringKit.isNotEmpty(table.schema())) {
                tableMeta.schema(table.schema());
            }
        } else if (StringKit.isEmpty(tableMeta.table())) {
            // If the table name is not set, use the default naming convention (class name to underscore format).
            tableMeta.table(NamingProvider.getDefaultStyle().tableName(entityClass));
        }
    }

}
