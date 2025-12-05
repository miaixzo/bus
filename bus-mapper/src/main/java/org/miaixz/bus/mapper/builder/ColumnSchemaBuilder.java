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

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Order;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * An interface for an entity class information factory. It can be added to the processing chain via SPI (Service
 * Provider Interface) to extend the logic for creating column information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ColumnSchemaBuilder extends Order {

    /**
     * A default value for ignored fields, representing an empty list of column information.
     */
    Optional<List<ColumnMeta>> IGNORE = Optional.of(Collections.emptyList());

    /**
     * Creates column information. A field may not be a column, it may be a single column, or it may correspond to
     * multiple columns (e.g., a ValueObject).
     *
     * @param tableMeta The entity table information.
     * @param fieldMeta The field information.
     * @param chain     The factory chain, used to invoke the next processing logic.
     * @return An {@link Optional} containing the column information for the entity class. An empty Optional indicates
     *         that the field is not a column in the entity.
     */
    Optional<List<ColumnMeta>> createColumn(TableMeta tableMeta, FieldMeta fieldMeta, Chain chain);

    /**
     * A factory chain interface for invoking column information creation logic in a chained manner.
     */
    interface Chain {

        /**
         * Creates column information. A field may not be a column, it may be a single column, or it may correspond to
         * multiple columns (e.g., a ValueObject).
         *
         * @param tableMeta The entity table information.
         * @param fieldMeta The field information.
         * @return An {@link Optional} containing the column information for the entity class. An empty Optional
         *         indicates that the field is not a column in the entity.
         */
        Optional<List<ColumnMeta>> createColumn(TableMeta tableMeta, FieldMeta fieldMeta);
    }

}
