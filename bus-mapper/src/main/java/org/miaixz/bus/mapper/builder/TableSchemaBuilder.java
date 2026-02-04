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

import org.miaixz.bus.mapper.Order;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * An interface for an entity class information factory. It can be added to the processing chain via SPI (Service
 * Provider Interface) to extend the logic for creating table information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface TableSchemaBuilder extends Order {

    /**
     * Creates table information based on an entity class. This method should only return table-level information and
     * not process fields. Custom annotations can be used for implementation.
     *
     * @param entityClass The entity class type.
     * @param chain       The factory chain, used to invoke the next processing logic.
     * @return The entity class table information.
     */
    TableMeta createTable(Class<?> entityClass, Chain chain);

    /**
     * A factory chain interface for invoking table information creation logic in a chained manner.
     */
    interface Chain {

        /**
         * Creates table information based on an entity class, returning only table-level information without processing
         * fields.
         *
         * @param entityClass The entity class type.
         * @return The entity class table information.
         */
        TableMeta createTable(Class<?> entityClass);
    }

}
