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
import org.apache.poi.ss.usermodel.RichTextString;

/**
 * {@link CellSetter} for {@link RichTextString} values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RichTextCellSetter implements CellSetter {

    /**
     * The rich text string value to set in the cell.
     */
    private final RichTextString value;

    /**
     * Constructs a {@code RichTextCellSetter} with the specified {@link RichTextString} value.
     *
     * @param value The {@link RichTextString} value to set in the cell.
     */
    RichTextCellSetter(final RichTextString value) {
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param cell the cell to set the rich text string value in
     */
    @Override
    public void setValue(final Cell cell) {
        cell.setCellValue(value);
    }

}
