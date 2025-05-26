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
package org.miaixz.bus.goalie.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * 终端版本注解，用于指定客户端终端类型、版本号和比较操作符，用于 Spring MVC 请求匹配。 常与 ClientVersion 注解配合使用，定义终端和版本的匹配规则。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TerminalVersion {

    /**
     * 终端类型数组，表示支持的客户端终端类型（如 1, 2 表示不同设备类型）。
     *
     * @return 终端类型数组，默认为空数组
     */
    int[] terminals() default {};

    /**
     * 版本比较操作符，定义版本号的比较方式（如大于、等于）。
     *
     * @return 比较操作符，默认为 NIL（无操作）
     */
    Version op() default Version.NIL;

    /**
     * 版本号，指定客户端版本号（如 "1.0"）。
     *
     * @return 版本号字符串，默认为空字符串
     */
    String version() default Normal.EMPTY;

    /**
     * 版本比较操作符枚举，定义支持的比较操作类型
     */
    enum Version {

        /**
         * 无操作
         */
        NIL(Normal.EMPTY),

        /**
         * 小于
         */
        LT(Symbol.LT),

        /**
         * 大于
         */
        GT(Symbol.GT),

        /**
         * 大于或等于
         */
        GTE(Symbol.GT + Symbol.EQUAL),

        /**
         * 小于或等于
         */
        LTE(Symbol.LE + Symbol.EQUAL),

        /**
         * 小于或等于
         */
        LE(Symbol.LE),

        /**
         * 大于或等于
         */
        GE(Symbol.GE),

        /**
         * 不等于
         */
        NE(Symbol.NOT + Symbol.EQUAL),

        /**
         * 等于
         */
        EQ(Symbol.EQUAL + Symbol.EQUAL);

        /**
         * 操作符的字符串表示
         */
        private final String code;

        /**
         * 构造器，初始化操作符代码
         *
         * @param code 操作符字符串
         */
        Version(String code) {
            this.code = code;
        }

        /**
         * 解析字符串为操作符
         *
         * @param code 操作符字符串
         * @return 匹配的操作符，若无匹配返回 null
         */
        public static Version parse(String code) {
            for (Version operator : Version.values()) {
                if (operator.getCode().equalsIgnoreCase(code)) {
                    return operator;
                }
            }
            return null;
        }

        /**
         * 获取操作符的字符串表示
         *
         * @return 操作符字符串
         */
        public String getCode() {
            return code;
        }

    }

}