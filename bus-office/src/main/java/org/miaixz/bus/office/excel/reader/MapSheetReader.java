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
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取{@link Sheet}为Map的List列表形式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapSheetReader extends AbstractSheetReader<List<Map<String, Object>>> {

    private final int headerRowIndex;

    /**
     * 构造
     *
     * @param headerRowIndex 标题所在行，如果标题行在读取的内容行中间，这行做为数据将忽略
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    结束行（包含，从0开始计数）
     */
    public MapSheetReader(final int headerRowIndex, final int startRowIndex, final int endRowIndex) {
        super(startRowIndex, endRowIndex);
        this.headerRowIndex = headerRowIndex;
    }

    @Override
    public List<Map<String, Object>> read(final Sheet sheet) {
        // 边界判断
        final int firstRowNum = sheet.getFirstRowNum();
        final int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 0) {
            return ListKit.empty();
        }

        if (headerRowIndex < firstRowNum) {
            throw new IndexOutOfBoundsException(StringKit
                    .format("Header row index {} is lower than first row index {}.", headerRowIndex, firstRowNum));
        } else if (headerRowIndex > lastRowNum) {
            throw new IndexOutOfBoundsException(StringKit
                    .format("Header row index {} is greater than last row index {}.", headerRowIndex, lastRowNum));
        } else if (startRowIndex > lastRowNum) {
            // 只有标题行的Excel，起始行是1，标题行（最后的行号是0）
            return ListKit.empty();
        }
        final int startRowIndex = Math.max(this.startRowIndex, firstRowNum);// 读取起始行（包含）
        final int endRowIndex = Math.min(this.endRowIndex, lastRowNum);// 读取结束行（包含）

        // 读取header
        final List<String> headerList = aliasHeader(readRow(sheet, headerRowIndex));

        final List<Map<String, Object>> result = new ArrayList<>(endRowIndex - startRowIndex + 1);
        List<Object> rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            // 跳过标题行
            if (i != headerRowIndex) {
                rowList = readRow(sheet, i);
                if (CollKit.isNotEmpty(rowList) || !ignoreEmptyRow) {
                    result.add(IteratorKit.toMap(headerList, rowList, true));
                }
            }
        }
        return result;
    }

}
