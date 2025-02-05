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
package org.miaixz.bus.office.excel.style;

import org.apache.poi.hssf.usermodel.HSSFShape;

/**
 * SimpleShape中的线条风格枚举
 *
 * @see HSSFShape
 * @author Kimi Liu
 * @since Java 17+
 */
public enum LineStyle {

    /**
     * Solid (continuous) pen
     */
    SOLID(HSSFShape.LINESTYLE_SOLID),
    /**
     * PS_DASH system dash style
     */
    DASHSYS(HSSFShape.LINESTYLE_DASHSYS),
    /**
     * PS_DOT system dash style
     */
    DOTSYS(HSSFShape.LINESTYLE_DOTSYS),
    /**
     * PS_DASHDOT system dash style
     */
    DASHDOTSYS(HSSFShape.LINESTYLE_DASHDOTSYS),
    /**
     * PS_DASHDOTDOT system dash style
     */
    DASHDOTDOTSYS(HSSFShape.LINESTYLE_DASHDOTDOTSYS),
    /**
     * square dot style
     */
    DOTGEL(HSSFShape.LINESTYLE_DOTGEL),
    /**
     * dash style
     */
    DASHGEL(HSSFShape.LINESTYLE_DASHGEL),
    /**
     * long dash style
     */
    LONGDASHGEL(HSSFShape.LINESTYLE_LONGDASHGEL),
    /**
     * dash short dash
     */
    DASHDOTGEL(HSSFShape.LINESTYLE_DASHDOTGEL),
    /**
     * long dash short dash
     */
    LONGDASHDOTGEL(HSSFShape.LINESTYLE_LONGDASHDOTGEL),
    /**
     * long dash short dash short dash
     */
    LONGDASHDOTDOTGEL(HSSFShape.LINESTYLE_LONGDASHDOTDOTGEL),
    /**
     * 无
     */
    NONE(HSSFShape.LINESTYLE_NONE);

    private final int value;

    /**
     * 构造
     *
     * @param value 样式编码
     */
    LineStyle(final int value) {
        this.value = value;
    }

    /**
     * 获取样式编码
     *
     * @return 样式编码
     */
    public int getValue() {
        return value;
    }

}
