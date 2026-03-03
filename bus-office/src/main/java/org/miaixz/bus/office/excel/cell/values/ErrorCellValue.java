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
package org.miaixz.bus.office.excel.cell.values;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaError;
import org.miaixz.bus.core.lang.Normal;

/**
 * Represents an ERROR type cell value.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorCellValue implements CellValue<String> {

    /**
     * The cell object representing the error cell.
     */
    private final Cell cell;

    /**
     * Constructs an {@code ErrorCellValue} instance.
     *
     * @param cell The {@link Cell} object representing the error cell.
     */
    public ErrorCellValue(final Cell cell) {
        this.cell = cell;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the error string, or empty string if error code is invalid
     */
    @Override
    public String getValue() {
        final FormulaError error = FormulaError.forInt(cell.getErrorCellValue());
        return (null == error) ? Normal.EMPTY : error.getString();
    }

}
