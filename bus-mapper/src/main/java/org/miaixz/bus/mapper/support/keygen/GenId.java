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
package org.miaixz.bus.mapper.support.keygen;

import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Primary key generator interface, used to generate primary key values through an interface.
 *
 * @param <T> The type of the primary key value.
 * @author Kimi Liu
 * @since Java 21+
 */
public interface GenId<T> {

    /**
     * Generates a primary key value.
     *
     * @param table  The entity table information.
     * @param column The primary key column information.
     * @return The generated primary key value.
     */
    T genId(TableMeta table, ColumnMeta column);

    /**
     * Default null implementation, throws an {@link UnsupportedOperationException}.
     */
    class NULL implements GenId<Object> {

        /**
         * Default implementation, throws an {@link UnsupportedOperationException}.
         *
         * @param table  The entity table information.
         * @param column The primary key column information.
         * @return No return value, always throws an exception.
         * @throws UnsupportedOperationException if the primary key generation operation is not supported.
         */
        @Override
        public Object genId(TableMeta table, ColumnMeta column) {
            throw new UnsupportedOperationException();
        }
    }

}
