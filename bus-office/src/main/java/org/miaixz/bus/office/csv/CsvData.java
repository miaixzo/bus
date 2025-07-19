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

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.xyz.ListKit;

/**
 * CSV数据，包括头部信息和行数据，参考：FastCSV
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvData implements Iterable<CsvRow>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852282752523L;

    /**
     * 头信息
     */
    private final List<String> header;
    /**
     * 行数据
     */
    private final List<CsvRow> rows;

    /**
     * 构造
     *
     * @param header 头信息, 可以为null
     * @param rows   行
     */
    public CsvData(final List<String> header, final List<CsvRow> rows) {
        this.header = header;
        this.rows = rows;
    }

    /**
     * 总行数
     *
     * @return 总行数
     */
    public int getRowCount() {
        return this.rows.size();
    }

    /**
     * 获取头信息列表，如果无头信息为{@code Null}，返回列表为只读列表
     *
     * @return the header row - might be {@code null} if no header exists
     */
    public List<String> getHeader() {
        return ListKit.unmodifiable(this.header);
    }

    /**
     * 获取指定行，从0开始
     *
     * @param index 行号
     * @return 行数据
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public CsvRow getRow(final int index) {
        return this.rows.get(index);
    }

    /**
     * 获取所有行
     *
     * @return 所有行
     */
    public List<CsvRow> getRows() {
        return this.rows;
    }

    @Override
    public Iterator<CsvRow> iterator() {
        return this.rows.iterator();
    }

    @Override
    public String toString() {
        return "CsvData{" + "header=" + header + ", rows=" + rows + '}';
    }

}
