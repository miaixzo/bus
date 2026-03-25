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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;

/**
 * {@link CellSetter} for {@link TemporalAccessor} values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TemporalAccessorCellSetter implements CellSetter {

    /**
     * The temporal accessor value to set in the cell.
     */
    private final TemporalAccessor value;

    /**
     * Constructs a {@code TemporalAccessorCellSetter} with the specified {@link TemporalAccessor} value.
     *
     * @param value The {@link TemporalAccessor} value to set in the cell.
     */
    TemporalAccessorCellSetter(final TemporalAccessor value) {
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param cell the cell to set the temporal value in
     */
    @Override
    public void setValue(final Cell cell) {
        if (value instanceof Instant) {
            cell.setCellValue(Date.from((Instant) value));
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue((LocalDate) value);
        }
    }

}
