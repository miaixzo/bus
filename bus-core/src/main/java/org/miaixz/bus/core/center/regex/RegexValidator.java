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
package org.miaixz.bus.core.center.regex;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * 对象检查工具类，提供字对象的blank和empty等检查
 * <ul>
 * <li>empty定义：{@code null} or 空字对象：{@code ""}</li>
 * <li>blank定义：{@code null} or 空字对象：{@code ""} or 空格、全角空格、制表符、换行符，等不可见字符</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexValidator {

    /**
     * 给定内容是否匹配正则
     *
     * @param regex   正则
     * @param content 内容
     * @return 正则为null或者""则不检查，返回true，内容为null返回false
     */
    public static boolean isMatch(final String regex, final CharSequence content) {
        if (content == null) {
            // 提供null的字符串为不匹配
            return false;
        }

        if (StringKit.isEmpty(regex)) {
            // 正则不存在则为全匹配
            return true;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return isMatch(pattern, content);
    }

    /**
     * 给定内容是否匹配正则
     *
     * @param pattern 模式
     * @param content 内容
     * @return 正则为null或者""则不检查，返回true，内容为null返回false
     */
    public static boolean isMatch(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (content == null || pattern == null) {
            // 提供null的字符串为不匹配
            return false;
        }
        return pattern.matcher(content).matches();
    }

    /**
     * 指定内容中是否有表达式匹配的内容
     *
     * @param regex   正则表达式
     * @param content 被查找的内容
     * @return 指定内容中是否有表达式匹配的内容
     */
    public static boolean contains(final String regex, final CharSequence content) {
        if (null == regex || null == content) {
            return false;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return contains(pattern, content);
    }

    /**
     * 指定内容中是否有表达式匹配的内容
     *
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @return 指定内容中是否有表达式匹配的内容
     */
    public static boolean contains(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (null == pattern || null == content) {
            return false;
        }
        return pattern.matcher(content).find();
    }

}
