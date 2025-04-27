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
package org.miaixz.bus.core.lang.ansi;

import java.util.LinkedHashMap;

/**
 * ANSI 4bit 颜色和Lab颜色映射关系
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ansi4bitMapping extends AnsiLabMapping {

    /**
     * 单例
     */
    public static final Ansi4bitMapping INSTANCE = new Ansi4bitMapping();

    /**
     * 构造
     */
    public Ansi4bitMapping() {
        ansiLabMap = new LinkedHashMap<>(16, 1);
        ansiLabMap.put(Ansi4BitColor.BLACK, new LabColor(0x000000));
        ansiLabMap.put(Ansi4BitColor.RED, new LabColor(0xAA0000));
        ansiLabMap.put(Ansi4BitColor.GREEN, new LabColor(0x00AA00));
        ansiLabMap.put(Ansi4BitColor.YELLOW, new LabColor(0xAA5500));
        ansiLabMap.put(Ansi4BitColor.BLUE, new LabColor(0x0000AA));
        ansiLabMap.put(Ansi4BitColor.MAGENTA, new LabColor(0xAA00AA));
        ansiLabMap.put(Ansi4BitColor.CYAN, new LabColor(0x00AAAA));
        ansiLabMap.put(Ansi4BitColor.WHITE, new LabColor(0xAAAAAA));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_BLACK, new LabColor(0x555555));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_RED, new LabColor(0xFF5555));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_GREEN, new LabColor(0x55FF00));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_YELLOW, new LabColor(0xFFFF55));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_BLUE, new LabColor(0x5555FF));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_MAGENTA, new LabColor(0xFF55FF));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_CYAN, new LabColor(0x55FFFF));
        ansiLabMap.put(Ansi4BitColor.BRIGHT_WHITE, new LabColor(0xFFFFFF));
    }

}
