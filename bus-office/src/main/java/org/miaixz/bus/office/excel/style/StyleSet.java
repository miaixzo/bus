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
package org.miaixz.bus.office.excel.style;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;

/**
 * Interface for a style set. Through a custom style set, different styles can be applied based on different cells and
 * values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface StyleSet {

    /**
     * Gets the cell style. This can be used to:
     * <ul>
     * <li>Define different styles based on cell location, such as first row, first column, even rows, even columns,
     * etc.</li>
     * <li>Define different styles based on cell values, such as numbers, dates, etc., and also define independent
     * styles for header rows.</li>
     * </ul>
     *
     * @param reference The cell reference, containing cell location information.
     * @param cellValue The cell value.
     * @param isHeader  {@code true} if it is a header row, {@code false} otherwise.
     * @return The cell style.
     */
    CellStyle getStyleFor(CellReference reference, Object cellValue, boolean isHeader);

}
