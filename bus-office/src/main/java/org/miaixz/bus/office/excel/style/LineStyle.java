/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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

    /**
     * The integer code representing the line style.
     */
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
