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
package org.miaixz.bus.office.csv;

import java.io.Serializable;

/**
 * CSV读取配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvReadConfig extends CsvConfig<CsvReadConfig> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 指定标题行号，-1表示无标题行
     */
    protected long headerLineNo = -1;
    /**
     * 是否跳过空白行，默认true
     */
    protected boolean skipEmptyRows = true;
    /**
     * 每行字段个数不同时是否抛出异常，默认false
     */
    protected boolean errorOnDifferentFieldCount;
    /**
     * 定义开始的行（包括），此处为原始文件行号
     */
    protected long beginLineNo;
    /**
     * 结束的行（包括），此处为原始文件行号
     */
    protected long endLineNo = Long.MAX_VALUE - 1;
    /**
     * 每个字段是否去除两边空白符
     */
    protected boolean trimField;

    /**
     * 默认配置
     *
     * @return 默认配置
     */
    public static CsvReadConfig of() {
        return new CsvReadConfig();
    }

    /**
     * 设置是否首行做为标题行，默认false 当设置为{@code true}时，默认标题行号是{@link #beginLineNo}，{@code false}为-1，表示无行号
     *
     * @param containsHeader 是否首行做为标题行，默认false
     * @return this
     * @see #setHeaderLineNo(long)
     */
    public CsvReadConfig setContainsHeader(final boolean containsHeader) {
        return setHeaderLineNo(containsHeader ? beginLineNo : -1);
    }

    /**
     * 设置标题行行号，默认-1，表示无标题行
     *
     * @param headerLineNo 标题行行号，-1表示无标题行
     * @return this
     */
    public CsvReadConfig setHeaderLineNo(final long headerLineNo) {
        this.headerLineNo = headerLineNo;
        return this;
    }

    /**
     * 设置是否跳过空白行，默认true
     *
     * @param skipEmptyRows 是否跳过空白行，默认true
     * @return this
     */
    public CsvReadConfig setSkipEmptyRows(final boolean skipEmptyRows) {
        this.skipEmptyRows = skipEmptyRows;
        return this;
    }

    /**
     * 设置每行字段个数不同时是否抛出异常，默认false
     *
     * @param errorOnDifferentFieldCount 每行字段个数不同时是否抛出异常，默认false
     * @return this
     */
    public CsvReadConfig setErrorOnDifferentFieldCount(final boolean errorOnDifferentFieldCount) {
        this.errorOnDifferentFieldCount = errorOnDifferentFieldCount;
        return this;
    }

    /**
     * 设置开始的行（包括），默认0，此处为原始文件行号
     *
     * @param beginLineNo 开始的行号（包括）
     * @return this
     */
    public CsvReadConfig setBeginLineNo(final long beginLineNo) {
        this.beginLineNo = beginLineNo;
        return this;
    }

    /**
     * 设置结束的行（包括），默认不限制，此处为原始文件行号
     *
     * @param endLineNo 结束的行号（包括）
     * @return this
     */
    public CsvReadConfig setEndLineNo(final long endLineNo) {
        this.endLineNo = endLineNo;
        return this;
    }

    /**
     * 设置每个字段是否去除两边空白符 如果字段以{@link #textDelimiter}包围，则保留两边空格
     *
     * @param trimField 去除两边空白符
     * @return this
     */
    public CsvReadConfig setTrimField(final boolean trimField) {
        this.trimField = trimField;
        return this;
    }

}
