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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * ANSI 8-bit前景或背景色（即8位编码，共256种颜色（2^8） ）
 * <ul>
 *     <li>0-7：                        标准颜色（同ESC [ 30–37 m）</li>
 *     <li>8-15：                       高强度颜色（同ESC [ 90–97 m）</li>
 *     <li>16-231（6 × 6 × 6 共 216色）： 16 + 36 × r + 6 × g + b (0 ≤ r, g, b ≤ 5)</li>
 *     <li>232-255：                    从黑到白的24阶灰度色</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ansi8BitColor implements AnsiElement {

    private static final String PREFIX_FORE = "38;5;";
    private static final String PREFIX_BACK = "48;5;";
    private final String prefix;
    private final int code;

    /**
     * 构造
     *
     * @param prefix 前缀
     * @param code   颜色代码(0-255)
     * @throws IllegalArgumentException 颜色代码不在0~255范围内
     */
    private Ansi8BitColor(String prefix, int code) {
        Assert.isTrue(code >= 0 && code <= 255, "Code must be between 0 and 255");
        this.prefix = prefix;
        this.code = code;
    }

    /**
     * 前景色ANSI颜色实例
     *
     * @param code 颜色代码(0-255)
     * @return 前景色ANSI颜色实例
     */
    public static Ansi8BitColor foreground(int code) {
        return new Ansi8BitColor(PREFIX_FORE, code);
    }

    /**
     * 背景色ANSI颜色实例
     *
     * @param code 颜色代码(0-255)
     * @return 背景色ANSI颜色实例
     */
    public static Ansi8BitColor background(int code) {
        return new Ansi8BitColor(PREFIX_BACK, code);
    }

    /**
     * 获取颜色代码(0-255)
     *
     * @return 颜色代码(0 - 255)
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 转换为前景色
     *
     * @return 前景色
     */
    public Ansi8BitColor asForeground() {
        if (PREFIX_FORE.equals(this.prefix)) {
            return this;
        }
        return Ansi8BitColor.foreground(this.code);
    }

    /**
     * 转换为背景色
     *
     * @return 背景色
     */
    public Ansi8BitColor asBackground() {
        if (PREFIX_BACK.equals(this.prefix)) {
            return this;
        }
        return Ansi8BitColor.background(this.code);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Ansi8BitColor other = (Ansi8BitColor) obj;
        return ObjectKit.equals(this.prefix, other.prefix) && this.code == other.code;
    }

    @Override
    public int hashCode() {
        return this.prefix.hashCode() * 31 + this.code;
    }

    @Override
    public String toString() {
        return this.prefix + this.code;
    }

}
