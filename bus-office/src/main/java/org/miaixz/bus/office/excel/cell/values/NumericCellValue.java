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
     * {@inheritDoc}
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
