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
package org.miaixz.bus.mapper.support;

import java.util.function.Predicate;

import org.miaixz.bus.mapper.parsing.ColumnMeta;

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
