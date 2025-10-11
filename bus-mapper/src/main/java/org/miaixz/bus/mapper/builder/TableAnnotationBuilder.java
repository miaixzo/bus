/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.mapper.builder;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Holder;
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

        // If the table name is not empty, add the table prefix.
        if (StringKit.isNotEmpty(tableMeta.table())) {
            String key = Holder.getKey() + Symbol.DOT + Args.TABLE_PREFIX_KEY;
            String prefix = Context.INSTANCE.getProperty(key, Normal.EMPTY);
            tableMeta.table(prefix + tableMeta.table());
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
