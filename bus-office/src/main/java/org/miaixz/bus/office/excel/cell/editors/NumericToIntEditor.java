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
package org.miaixz.bus.office.excel.cell.editors;

import org.apache.poi.ss.usermodel.Cell;

/**
 * A {@link CellEditor} that converts numeric cell values (which are typically {@code Double} in POI) to {@code int}
 * type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericToIntEditor implements CellEditor {

    /**
     * Description inherited from parent class or interface.
     *
     * @param cell  the cell being edited
     * @param value the value to edit
     * @return the integer value if value is a Number, otherwise returns the original value
     */
    @Override
    public Object edit(final Cell cell, final Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value;
    }

}
