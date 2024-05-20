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
package org.miaixz.bus.core.lang.ansi;

import java.awt.*;
import java.util.Map;

/**
 * ANSI颜色和Lab颜色的对应查找表
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AnsiLabMapping {

    protected Map<AnsiElement, LabColor> ansiLabMap;

    /**
     * 查找与给定LabColor最接近的AnsiElement颜色
     *
     * @param color {@link LabColor}
     * @return 最接近的Ansi4BitColor
     */
    public AnsiElement lookupClosest(final Color color) {
        return lookupClosest(new LabColor(color));
    }

    /**
     * 查找与给定LabColor最接近的AnsiElement颜色
     *
     * @param color {@link LabColor}
     * @return 最接近的Ansi4BitColor
     */
    public AnsiElement lookupClosest(final LabColor color) {
        AnsiElement closest = null;
        double closestDistance = Float.MAX_VALUE;
        for (final Map.Entry<AnsiElement, LabColor> entry : ansiLabMap.entrySet()) {
            final double candidateDistance = color.getDistance(entry.getValue());
            if (closest == null || candidateDistance < closestDistance) {
                closestDistance = candidateDistance;
                closest = entry.getKey();
            }
        }
        return closest;
    }

}
