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
package org.miaixz.bus.office.excel.cell.values;

import java.time.LocalDateTime;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.office.Builder;

/**
 * Numeric type cell value. The cell value can be Long, Double, or Date.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericCellValue implements CellValue<Object> {

    /**
     * The cell object.
     */
    private final Cell cell;

    /**
     * Constructs a new {@code NumericCellValue} instance.
     *
     * @param cell The {@link Cell} object.
     */
    public NumericCellValue(final Cell cell) {
        this.cell = cell;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the cell value as Long, Double, LocalDateTime, or LocalTime
     */
    @Override
    public Object getValue() {
        final double value = cell.getNumericCellValue();

        final CellStyle style = cell.getCellStyle();
        if (null != style) {
            // Check if it is a date format.
            if (Builder.isDateFormat(cell)) {
                final LocalDateTime date = cell.getLocalDateTimeCellValue();
                // If the year is 1899, it indicates that the date information (year, month, day) is not relevant for
                // this cell.
                if (1899 == date.getYear()) {
                    return date.toLocalTime();
                }
                return date;
            }

            final String format = style.getDataFormatString();
            // Normal number.
            if (null != format && format.indexOf(Symbol.C_DOT) < 0) {
                final long longPart = (long) value;
                if (((double) longPart) == value) {
                    // For numeric types without decimal parts, convert to Long.
                    return longPart;
                }
            }
        }

        // Some Excel cell values are double calculation results, which may lead to precision issues.
        // Convert to String then parse to Double to resolve precision issues.
        return Double.parseDouble(NumberToTextConverter.toText(value));
    }

}
