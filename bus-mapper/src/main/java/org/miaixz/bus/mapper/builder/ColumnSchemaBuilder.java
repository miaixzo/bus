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
package org.miaixz.bus.mapper.builder;

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
 * @since Java 21+
 */
public interface ColumnSchemaBuilder extends Order {

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
