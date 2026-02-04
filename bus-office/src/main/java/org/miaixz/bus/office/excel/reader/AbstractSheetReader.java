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
package org.miaixz.bus.office.excel.reader;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Abstract implementation of a {@link Sheet} data reader.
 *
 * @param <T> The type of data to read.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSheetReader<T> implements SheetReader<T> {

    /**
     * The range of cells to read.
     */
    protected final CellRangeAddress cellRangeAddress;
    /**
     * Excel configuration settings.
     */
    protected ExcelReadConfig config;

    /**
     * Constructor.
     *
     * @param cellRangeAddress The range of cells to read.
     */
    public AbstractSheetReader(final CellRangeAddress cellRangeAddress) {
        this.cellRangeAddress = cellRangeAddress;
    }

    /**
     * Constructor.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @param endRowIndex   The ending row index (inclusive, 0-based).
     */
    public AbstractSheetReader(final int startRowIndex, final int endRowIndex) {
        this(new CellRangeAddress(Math.min(startRowIndex, endRowIndex), Math.max(startRowIndex, endRowIndex), 0,
                Integer.MAX_VALUE));
    }

    /**
     * Sets the Excel configuration.
     *
     * @param config The Excel configuration.
     */
    public void setExcelConfig(final ExcelReadConfig config) {
        this.config = config;
    }

}
