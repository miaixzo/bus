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
 * Enumeration of line styles for {@code SimpleShape} in Excel.
 *
 * @see HSSFShape
 * @author Kimi Liu
 * @since Java 17+
 */
public enum LineStyle {

    /**
     * Solid (continuous) pen.
     */
    SOLID(HSSFShape.LINESTYLE_SOLID),
    /**
     * PS_DASH system dash style.
     */
    DASHSYS(HSSFShape.LINESTYLE_DASHSYS),
    /**
     * PS_DOT system dash style.
     */
    DOTSYS(HSSFShape.LINESTYLE_DOTSYS),
    /**
     * PS_DASHDOT system dash style.
     */
    DASHDOTSYS(HSSFShape.LINESTYLE_DASHDOTSYS),
    /**
     * PS_DASHDOTDOT system dash style.
     */
    DASHDOTDOTSYS(HSSFShape.LINESTYLE_DASHDOTDOTSYS),
    /**
     * Square dot style.
     */
    DOTGEL(HSSFShape.LINESTYLE_DOTGEL),
    /**
     * Dash style.
     */
    DASHGEL(HSSFShape.LINESTYLE_DASHGEL),
    /**
     * Long dash style.
     */
    LONGDASHGEL(HSSFShape.LINESTYLE_LONGDASHGEL),
    /**
     * Dash short dash style.
     */
    DASHDOTGEL(HSSFShape.LINESTYLE_DASHDOTGEL),
    /**
     * Long dash short dash style.
     */
    LONGDASHDOTGEL(HSSFShape.LINESTYLE_LONGDASHDOTGEL),
    /**
     * Long dash short dash short dash style.
     */
    LONGDASHDOTDOTGEL(HSSFShape.LINESTYLE_LONGDASHDOTDOTGEL),
    /**
     * No line style.
     */
    NONE(HSSFShape.LINESTYLE_NONE);

    private final int value;

    /**
     * Constructs a {@code LineStyle} enum with the specified style code.
     *
     * @param value The integer code representing the line style.
     */
    LineStyle(final int value) {
        this.value = value;
    }

    /**
     * Gets the integer code representing this line style.
     *
     * @return The style code.
     */
    public int getValue() {
        return value;
    }

}
