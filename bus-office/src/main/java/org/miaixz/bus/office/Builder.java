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
package org.miaixz.bus.office;

import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;

/**
 * 为office提供辅助功能.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Builder {

    /**
     * 没有引入POI的错误消息
     */
    public static final String NO_POI_ERROR_MSG = "You need to add dependency of 'poi-ooxml' to your project, and version >= 4.1.2";
    /**
     * 某些特殊的自定义日期格式
     */
    private static final int[] CUSTOM_FORMATS = new int[]{28, 30, 31, 32, 33, 55, 56, 57, 58};

    /**
     * 某些特殊的自定义日期格式
     */
    private static final int[] customFormats = new int[]{28, 30, 31, 32, 33, 55, 56, 57, 58};

    /**
     * 是否日期格式
     *
     * @param cell 单元格
     * @return 是否日期格式
     */
    public static boolean isDateFormat(final Cell cell) {
        return isDateFormat(cell, null);
    }

    /**
     * 判断是否日期格式
     *
     * @param cell        单元格
     * @param cfEvaluator {@link ConditionalFormattingEvaluator}
     * @return 是否日期格式
     */
    public static boolean isDateFormat(final Cell cell, final ConditionalFormattingEvaluator cfEvaluator) {
        final ExcelNumberFormat nf = ExcelNumberFormat.from(cell, cfEvaluator);
        return isDateFormat(nf);
    }

    /**
     * 判断是否日期格式
     *
     * @param numFmt {@link ExcelNumberFormat}
     * @return 是否日期格式
     */
    public static boolean isDateFormat(final ExcelNumberFormat numFmt) {
        return isDateFormat(numFmt.getIdx(), numFmt.getFormat());
    }

    /**
     * 判断日期格式
     *
     * @param formatIndex  格式索引，一般用于内建格式
     * @param formatString 格式字符串
     * @return 是否为日期格式
     */
    public static boolean isDateFormat(final int formatIndex, final String formatString) {
        if (ArrayKit.contains(customFormats, formatIndex)) {
            return true;
        }

        // 自定义格式判断
        if (StringKit.isNotEmpty(formatString) &&
                StringKit.containsAny(formatString, "周", "星期", "aa")) {
            // aa  -> 周一
            // aaa -> 星期一
            return true;
        }

        return org.apache.poi.ss.usermodel.DateUtil.isADateFormat(formatIndex, formatString);
    }

}
