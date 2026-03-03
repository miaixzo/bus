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
package org.miaixz.bus.mapper.parsing;

import java.util.function.Predicate;

/**
 * Records the class and field name corresponding to a field, used to match entity class fields with database column
 * properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassField implements Predicate<ColumnMeta> {

    /**
     * The entity class.
     */
    private final Class<?> clazz;

    /**
     * The field name.
     */
    private final String field;

    /**
     * Constructs a new ClassField, initializing the class and field information.
     *
     * @param clazz The entity class.
     * @param field The field name.
     */
    public ClassField(Class<?> clazz, String field) {
        this.clazz = clazz;
        this.field = field;
    }

    /**
     * Tests if the property name of the specified column matches the current field name (case-insensitive).
     *
     * @param column The database column metadata.
     * @return {@code true} if the property names match, {@code false} otherwise.
     */
    @Override
    public boolean test(ColumnMeta column) {
        return getField().equalsIgnoreCase(column.property());
    }

    /**
     * Gets the entity class.
     *
     * @return The entity class.
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Gets the field name.
     *
     * @return The field name.
     */
    public String getField() {
        return field;
    }

}
