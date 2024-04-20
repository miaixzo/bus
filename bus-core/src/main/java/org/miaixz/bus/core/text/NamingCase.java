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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.CharsKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 命名规则封装，主要是针对驼峰风格命名、连接符命名等的封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamingCase {

    /**
     * 将驼峰式命名的字符串转换为下划线方式，又称SnakeCase、underScoreCase
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串
     * 规则为：
     * <ul>
     *     <li>单字之间以下划线隔开</li>
     *     <li>每个单字的首字母亦用小写字母</li>
     * </ul>
     * 例如：
     *
     * <pre>
     * HelloWorld - hello_world
     * Hello_World - hello_world
     * HelloWorld_test - hello_world_test
     * </pre>
     *
     * @param text 转换前的驼峰式命名的字符串，也可以为下划线形式
     * @return 转换后下划线方式命名的字符串
     */
    public static String toUnderlineCase(CharSequence text) {
        return toSymbolCase(text, Symbol.C_UNDERLINE);
    }

    /**
     * 将驼峰式命名的字符串转换为短横连接方式
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串
     * 规则为：
     * <ul>
     *     <li>单字之间横线线隔开</li>
     *     <li>每个单字的首字母亦用小写字母</li>
     * </ul>
     * 例如：
     *
     * <pre>
     * HelloWorld - hello-world
     * Hello_World - hello-world
     * HelloWorld_test - hello-world-test
     * </pre>
     *
     * @param text 转换前的驼峰式命名的字符串，也可以为下划线形式
     * @return 转换后下划线方式命名的字符串
     */
    public static String toKebabCase(CharSequence text) {
        return toSymbolCase(text, Symbol.C_MINUS);
    }

    /**
     * 将驼峰式命名的字符串转换为使用符号连接方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串
     *
     * @param text   转换前的驼峰式命名的字符串，也可以为符号连接形式
     * @param symbol 原字符串中的连接符连接符
     * @return 转换后符号连接方式命名的字符串
     */
    public static String toSymbolCase(CharSequence text, char symbol) {
        if (text == null) {
            return null;
        }

        final int length = text.length();
        final TextBuilder sb = new TextBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            c = text.charAt(i);
            if (Character.isUpperCase(c)) {
                final Character preChar = (i > 0) ? text.charAt(i - 1) : null;
                final Character nextChar = (i < text.length() - 1) ? text.charAt(i + 1) : null;

                if (null != preChar) {
                    if (symbol == preChar) {
                        // 前一个为分隔符
                        if (null == nextChar || Character.isLowerCase(nextChar)) {
                            //普通首字母大写，如_Abb -> _abb
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如_AB -> _AB
                    } else if (Character.isLowerCase(preChar)) {
                        // 前一个为小写
                        sb.append(symbol);
                        if (null == nextChar
                                || Character.isLowerCase(nextChar)
                                || CharsKit.isNumber(nextChar)) {
                            // 普通首字母大写，如aBcc -> a_bcc
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如aBC -> a_BC
                    } else {
                        // 前一个为大写
                        if (null != nextChar && Character.isLowerCase(nextChar)) {
                            // 普通首字母大写，如ABcc -> A_bcc
                            sb.append(symbol);
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如ABC -> ABC
                    }
                } else {
                    // 首字母，需要根据后一个判断是否转为小写
                    if (null == nextChar || Character.isLowerCase(nextChar)) {
                        // 普通首字母大写，如Abc -> abc
                        c = Character.toLowerCase(c);
                    }
                    // 后一个为大写，按照专有名词对待，如ABC -> ABC
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 将下划线方式命名的字符串转换为帕斯卡式
     * 规则为：
     * <ul>
     *     <li>单字之间不以空格或任何连接符断开</li>
     *     <li>第一个单字首字母采用大写字母</li>
     *     <li>后续单字的首字母亦用大写字母</li>
     * </ul>
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串
     * 例如：hello_world - HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toPascalCase(CharSequence name) {
        return StringKit.upperFirst(toCamelCase(name));
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
     * 规则为：
     * <ul>
     *     <li>单字之间不以空格或任何连接符断开</li>
     *     <li>第一个单字首字母采用小写字母</li>
     *     <li>后续单字的首字母亦用大写字母</li>
     * </ul>
     * 例如：hello_world=》helloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toCamelCase(CharSequence name) {
        return toCamelCase(name, Symbol.C_UNDERLINE);
    }

    /**
     * 将连接符方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
     *
     * @param name   转换前的自定义方式命名的字符串
     * @param symbol 连接符
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toCamelCase(CharSequence name, char symbol) {
        if (null == name) {
            return null;
        }

        final String value = name.toString();
        if (StringKit.contains(value, symbol)) {
            final int length = value.length();
            final StringBuilder sb = new StringBuilder(length);
            boolean upperCase = false;
            for (int i = 0; i < length; i++) {
                char c = value.charAt(i);

                if (c == symbol) {
                    upperCase = true;
                } else if (upperCase) {
                    sb.append(Character.toUpperCase(c));
                    upperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
            return sb.toString();
        } else {
            return value;
        }
    }

}
