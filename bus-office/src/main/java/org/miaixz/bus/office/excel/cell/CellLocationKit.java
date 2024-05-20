/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.office.excel.cell;

import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 单元格位置工具类，提供包括行号转行名称、列号转列名称等功能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellLocationKit {

    /**
     * 将Sheet列号变为列名
     *
     * @param index 列号, 从0开始
     * @return 0-A; 1-B...26-AA
     */
    public static String indexToColName(int index) {
        if (index < 0) {
            return null;
        }
        final StringBuilder colName = StringKit.builder();
        do {
            if (colName.length() > 0) {
                index--;
            }
            final int remainder = index % 26;
            colName.append((char) (remainder + 'A'));
            index = (index - remainder) / 26;
        } while (index > 0);
        return colName.reverse().toString();
    }

    /**
     * 根据表元的列名转换为列号
     *
     * @param colName 列名, 从A开始
     * @return A1-0; B1-1...AA1-26
     */
    public static int colNameToIndex(final String colName) {
        final int length = colName.length();
        char c;
        int index = -1;
        for (int i = 0; i < length; i++) {
            c = Character.toUpperCase(colName.charAt(i));
            if (Character.isDigit(c)) {
                break;// 确定指定的char值是否为数字
            }
            index = (index + 1) * 26 + (int) c - 'A';
        }
        return index;
    }

    /**
     * 将Excel中地址标识符（例如A11，B5）等转换为行列表示
     * 例如：A11 - x:0,y:10，B5-x:1,y:4
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return 坐标点，x表示行，从0开始，y表示列，从0开始
     */
    public static CellLocation toLocation(final String locationRef) {
        final int x = colNameToIndex(locationRef);
        final int y = PatternKit.getFirstNumber(locationRef) - 1;
        return new CellLocation(x, y);
    }

}
