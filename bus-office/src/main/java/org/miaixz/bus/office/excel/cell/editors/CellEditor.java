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
 * Cell editor interface. When reading Excel values, it is sometimes necessary to process all cell values uniformly
 * (e.g., converting null to a default value). This can be achieved by implementing this interface and setting the
 * editor via {@code reader.setCellEditor()}. This interface can perform the following functions:
 * <ul>
 * <li>Edit cells, such as modifying styles.</li>
 * <li>Edit cell values, such as modifying different values based on the cell and returning the processed value.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface CellEditor {

    /**
     * Edits the cell and processes the result value based on cell information, returning the processed result.
     *
     * @param cell  The cell object, from which cell row, column, style, and other information can be obtained.
     * @param value The original cell value.
     * @return The edited value.
     */
    Object edit(Cell cell, Object value);

}
