/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 抽象{@link Sheet}数据读取实现
 *
 * @param <T> 读取类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSheetReader<T> implements SheetReader<T> {

    protected final CellRangeAddress cellRangeAddress;
    /**
     * Excel配置
     */
    protected ExcelReadConfig config;

    /**
     * 构造
     *
     * @param cellRangeAddress 读取范围
     */
    public AbstractSheetReader(final CellRangeAddress cellRangeAddress) {
        this.cellRangeAddress = cellRangeAddress;
    }

    /**
     * 构造
     *
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     */
    public AbstractSheetReader(final int startRowIndex, final int endRowIndex) {
        this(new CellRangeAddress(Math.min(startRowIndex, endRowIndex), Math.max(startRowIndex, endRowIndex), 0,
                Integer.MAX_VALUE));
    }

    /**
     * 设置Excel配置
     *
     * @param config Excel配置
     */
    public void setExcelConfig(final ExcelReadConfig config) {
        this.config = config;
    }

}
