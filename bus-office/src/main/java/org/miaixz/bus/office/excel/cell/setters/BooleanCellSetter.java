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
package org.miaixz.bus.office.excel.cell.setters;

import org.apache.poi.ss.usermodel.Cell;

/**
 * {@link CellSetter} for {@link Boolean} values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BooleanCellSetter implements CellSetter {

    /**
     * The boolean value to set in the cell.
     */
    private final Boolean value;

    /**
     * Constructs a {@code BooleanCellSetter} with the specified {@link Boolean} value.
     *
     * @param value The {@link Boolean} value to set in the cell.
     */
    BooleanCellSetter(final Boolean value) {
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param cell the cell to set the boolean value in
     */
    @Override
    public void setValue(final Cell cell) {
        cell.setCellValue(value);
    }

}
