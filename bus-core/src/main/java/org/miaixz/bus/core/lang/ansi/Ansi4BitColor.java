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

import org.miaixz.bus.core.xyz.StringKit;

/**
 * ANSI标准颜色
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Ansi4BitColor implements AnsiElement {

    /**
     * 默认前景色
     */
    DEFAULT(39),

    /**
     * 黑
     */
    BLACK(30),

    /**
     * 红
     */
    RED(31),

    /**
     * 绿
     */
    GREEN(32),

    /**
     * 黄
     */
    YELLOW(33),

    /**
     * 蓝
     */
    BLUE(34),

    /**
     * 品红
     */
    MAGENTA(35),

    /**
     * 青
     */
    CYAN(36),

    /**
     * 白
     */
    WHITE(37),

    /**
     * 亮黑
     */
    BRIGHT_BLACK(90),

    /**
     * 亮红
     */
    BRIGHT_RED(91),

    /**
     * 亮绿
     */
    BRIGHT_GREEN(92),

    /**
     * 亮黄
     */
    BRIGHT_YELLOW(93),

    /**
     * 亮蓝
     */
    BRIGHT_BLUE(94),

    /**
     * 亮品红
     */
    BRIGHT_MAGENTA(95),

    /**
     * 亮青
     */
    BRIGHT_CYAN(96),

    /**
     * 亮白
     */
    BRIGHT_WHITE(97),

    /**
     * 默认背景色
     */
    BG_DEFAULT(49),

    /**
     * 黑色
     */
    BG_BLACK(40),

    /**
     * 红
     */
    BG_RED(41),

    /**
     * 绿
     */
    BG_GREEN(42),

    /**
     * 黄
     */
    BG_YELLOW(43),

    /**
     * 蓝
     */
    BG_BLUE(44),

    /**
     * 品红
     */
    BG_MAGENTA(45),

    /**
     * 青
     */
    BG_CYAN(46);

    private final int code;

    Ansi4BitColor(int code) {
        this.code = code;
    }

    /**
     * 根据code查找对应的AnsiColor
     *
     * @param code AnsiEncoder 4bit 颜色代码
     * @return Color4Bit
     */
    public static Ansi4BitColor of(int code) {
        for (Ansi4BitColor item : Ansi4BitColor.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        throw new IllegalArgumentException(StringKit.format("No matched Color4Bit instance,code={}", code));
    }

    /**
     * 获取ANSI颜色代码（前景色）
     *
     * @return 颜色代码
     */
    public int getCode() {
        return getCode(false);
    }

    /**
     * 获取ANSI颜色代码
     *
     * @param isBackground 是否背景色
     * @return 颜色代码
     */
    public int getCode(boolean isBackground) {
        return isBackground ? this.code + 10 : this.code;
    }

    /**
     * 获取前景色对应的背景色
     *
     * @return 背景色
     */
    public Ansi4BitColor asBackground() {
        return Ansi4BitColor.of(getCode(true));
    }

    @Override
    public String toString() {
        return StringKit.toString(this.code);
    }

}
