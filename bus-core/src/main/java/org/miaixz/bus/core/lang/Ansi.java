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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * ANSI文本样式风格
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ansi {

    private static final String ENCODE_JOIN = ";";
    private static final String ENCODE_START = "\033[";
    private static final String ENCODE_END = "m";
    private static final String RESET = "0;" + Color.DEFAULT;

    /**
     * 创建ANSI字符串，参数中的{@link Element}会被转换为编码形式。
     *
     * @param args 节点数组
     * @return ANSI字符串
     */
    public static String encode(final Object... args) {
        final StringBuilder sb = new StringBuilder();
        buildEnabled(sb, args);
        return sb.toString();
    }

    /**
     * 追加需要需转义的节点
     *
     * @param sb   {@link StringBuilder}
     * @param args 节点列表
     */
    private static void buildEnabled(final StringBuilder sb, final Object[] args) {
        boolean writingAnsi = false;
        boolean containsEncoding = false;
        for (final Object element : args) {
            if (null == element) {
                continue;
            }
            if (element instanceof Element) {
                containsEncoding = true;
                if (writingAnsi) {
                    sb.append(ENCODE_JOIN);
                } else {
                    sb.append(ENCODE_START);
                    writingAnsi = true;
                }
            } else {
                if (writingAnsi) {
                    sb.append(ENCODE_END);
                    writingAnsi = false;
                }
            }
            sb.append(element);
        }

        // 恢复默认
        if (containsEncoding) {
            sb.append(writingAnsi ? ENCODE_JOIN : ENCODE_START);
            sb.append(RESET);
            sb.append(ENCODE_END);
        }
    }

    /**
     * 命名模式
     */
    public enum Mode {

        /**
         * 默认/正常
         */
        NORMAL(0, "默认"),

        /**
         * 粗体或增加强度
         */
        BOLD(1, "粗体"),

        /**
         * 弱化（降低强度）
         */
        FAINT(2, "弱化"),

        /**
         * 斜体
         */
        ITALIC(3, "斜体"),
        /**
         * 转换为大写
         */
        UPPER_CASE(4, "大写"),
        /**
         * 转换为小写
         */
        LOWER_CASE(5, "小写"),
        /**
         * 驼峰转下划线
         */
        CAMEL(6, "驼峰"),
        /**
         * 驼峰转下划线大写形式
         */
        CAMEL_UNDERLINE_UPPER_CASE(7, "驼峰转下划线大写"),
        /**
         * 驼峰转下划线小写形式
         */
        CAMEL_UNDERLINE_LOWER_CASE(8, "驼峰转下划线小写");

        /**
         * 编码
         */
        private final long code;
        /**
         * 名称
         */
        private final String name;

        Mode(long code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * @return 单位对应的编码
         */
        public long getCode() {
            return this.code;
        }

        /**
         * @return 对应的名称
         */
        public String getName() {
            return this.name;
        }

    }

    /**
     * ANSI标准颜色
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public enum Color implements Element {

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

        Color(int code) {
            this.code = code;
        }

        /**
         * 根据code查找对应的AnsiColor
         *
         * @param code Ansi 4bit 颜色代码
         * @return Color4Bit
         */
        public static Color of(int code) {
            for (Color item : Color.values()) {
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
        public Color asBackground() {
            return Color.of(getCode(true));
        }

        @Override
        public String toString() {
            return StringKit.toString(this.code);
        }

    }

    /**
     * ANSI可转义节点接口，实现为ANSI颜色等
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public interface Element {

        /**
         * 获取ANSI代码
         *
         * @return ANSI代码
         */
        int getCode();

        /**
         * @return ANSI转义编码
         */
        @Override
        String toString();

    }

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
    public static class Color8Bit implements Element {

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
        private Color8Bit(String prefix, int code) {
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
        public static Color8Bit foreground(int code) {
            return new Color8Bit(PREFIX_FORE, code);
        }

        /**
         * 背景色ANSI颜色实例
         *
         * @param code 颜色代码(0-255)
         * @return 背景色ANSI颜色实例
         */
        public static Color8Bit background(int code) {
            return new Color8Bit(PREFIX_BACK, code);
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
        public Color8Bit asForeground() {
            if (PREFIX_FORE.equals(this.prefix)) {
                return this;
            }
            return Color8Bit.foreground(this.code);
        }

        /**
         * 转换为背景色
         *
         * @return 背景色
         */
        public Color8Bit asBackground() {
            if (PREFIX_BACK.equals(this.prefix)) {
                return this;
            }
            return Color8Bit.background(this.code);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Color8Bit other = (Color8Bit) obj;
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


}
