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
package org.miaixz.bus.office.excel.sax.handler;

import java.util.List;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;

/**
 * 抽象行数据处理器，通过实现{@link #handle(int, long, List)} 处理原始数据 并调用{@link #handleData(int, long, Object)}处理经过转换后的数据。
 *
 * @param <T> 转换后的数据类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRowHandler<T> implements RowHandler {

    /**
     * 读取起始行（包含，从0开始计数）
     */
    protected final int startRowIndex;
    /**
     * 读取结束行（包含，从0开始计数）
     */
    protected final int endRowIndex;
    /**
     * 行数据转换函数
     */
    protected Function<List<Object>, T> convertFunc;

    /**
     * 构造
     *
     * @param startRowIndex 读取起始行（包含，从0开始计数）
     * @param endRowIndex   读取结束行（包含，从0开始计数）
     */
    public AbstractRowHandler(final int startRowIndex, final int endRowIndex) {
        this.startRowIndex = startRowIndex;
        this.endRowIndex = endRowIndex;
    }

    @Override
    public void handle(final int sheetIndex, final long rowIndex, final List<Object> rowCells) {
        Assert.notNull(convertFunc);
        if (rowIndex < this.startRowIndex || rowIndex > this.endRowIndex) {
            return;
        }
        handleData(sheetIndex, rowIndex, convertFunc.apply(rowCells));
    }

    /**
     * 处理转换后的数据
     *
     * @param sheetIndex 当前Sheet序号
     * @param rowIndex   当前行号，从0开始计数
     * @param data       行数据
     */
    public abstract void handleData(int sheetIndex, long rowIndex, T data);

}
