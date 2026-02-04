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
package org.miaixz.bus.office.excel.cell.setters;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;

/**
 * {@link CellSetter} for {@link Date} values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateCellSetter implements CellSetter {

    /**
     * The date value to set in the cell.
     */
    private final Date value;

    /**
     * Constructs a {@code DateCellSetter} with the specified {@link Date} value.
     *
     * @param value The {@link Date} value to set in the cell.
     */
    DateCellSetter(final Date value) {
        this.value = value;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param cell the cell to set the value in
     */
    @Override
    public void setValue(final Cell cell) {
        cell.setCellValue(value);
    }

}
