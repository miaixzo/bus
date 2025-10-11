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

import java.util.Collections;
import java.util.List;

import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.ORDER;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.provider.NamingProvider;

import jakarta.persistence.*;

/**
 * The default column builder, which supports entity classes annotated with `jakarta.persistence` annotations. It parses
 * field annotations to generate column information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColumnAnnotationBuilder implements ColumnSchemaBuilder {

    /**
     * A marker to indicate that a field should not be mapped to a database column.
     */
    private static final Optional<List<ColumnMeta>> IGNORE = Optional.of(Collections.emptyList());

    /**
     * Creates entity column information by parsing field annotations and generating column metadata.
     *
     * @param tableMeta The entity table information, containing table metadata.
     * @param fieldMeta The field information, containing field metadata.
     * @param chain     The column factory processing chain, used in the chain of responsibility pattern.
     * @return An {@link Optional} containing a list of column information. Returns an empty list if the field is
     *         ignored or marked as {@link Transient}.
     */
    @Override
    public Optional<List<ColumnMeta>> createColumn(
            TableMeta tableMeta,
            FieldMeta fieldMeta,
            ColumnSchemaBuilder.Chain chain) {
        // First, invoke the next processor in the chain of responsibility.
        Optional<List<ColumnMeta>> columns = chain.createColumn(tableMeta, fieldMeta);
        if (columns == IGNORE || fieldMeta.isAnnotationPresent(Transient.class)) {
            return IGNORE;
        }

        // If no column information is present and the field is not marked as @Transient,
        // generate default column information (e.g., camelCase to underscore).
        if (!columns.isPresent()) {
            String columnName = NamingProvider.getDefaultStyle().columnName(tableMeta, fieldMeta);
            columns = Optional.of(Collections.singletonList(ColumnMeta.of(fieldMeta).column(columnName)));
        }

        // Process the annotations on the column information.
        if (columns.isPresent()) {
            List<ColumnMeta> columnList = columns.getOrNull();
            for (ColumnMeta columnMeta : columnList) {
                processAnnotations(columnMeta, fieldMeta);
                EntityClassBuilder.setColumnMeta(tableMeta.entityClass(), columnMeta);
            }
        }

        return columns;
    }

    /**
     * Processes annotations on a field to set the metadata properties of the column.
     *
     * @param columnMeta The column metadata object.
     * @param fieldMeta  The field metadata object.
     */
    protected void processAnnotations(ColumnMeta columnMeta, FieldMeta fieldMeta) {
        // Process the primary key annotation.
        if (!columnMeta.id() && fieldMeta.isAnnotationPresent(Id.class)) {
            columnMeta.id(true);
        }

        // Process the column annotation.
        if (fieldMeta.isAnnotationPresent(Column.class)) {
            Column column = fieldMeta.getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                columnMeta.column(column.name());
            }
            columnMeta.insertable(column.insertable()).updatable(column.updatable());
            if (column.scale() != 0) {
                columnMeta.numericScale(String.valueOf(column.scale()));
            }
        }

        // Process the order-by annotation.
        if (fieldMeta.isAnnotationPresent(OrderBy.class)) {
            OrderBy orderBy = fieldMeta.getAnnotation(OrderBy.class);
            columnMeta.orderBy(orderBy.value().isEmpty() ? ORDER.ASC : orderBy.value());
        }

        // Process the type converter annotation.
        if (fieldMeta.isAnnotationPresent(Convert.class)) {
            Convert convert = fieldMeta.getAnnotation(Convert.class);
            Class converter = convert.converter();
            if (converter != void.class && TypeHandler.class.isAssignableFrom(converter)) {
                columnMeta.typeHandler(converter);
            }
        }
    }

}
