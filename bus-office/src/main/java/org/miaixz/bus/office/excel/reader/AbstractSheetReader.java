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
