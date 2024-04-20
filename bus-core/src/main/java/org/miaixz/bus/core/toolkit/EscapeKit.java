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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.text.escape.EscapeCodeValues;
import org.miaixz.bus.core.text.translate.CharSequenceTranslator;

/**
 * 转义和反转义工具类Escape / Unescape
 * escape采用ISO Latin字符集对指定的字符串进行编码
 * Java, Java Script, HTML and XML.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EscapeKit {

    /**
     * 获取一个{@link Builder}.
     *
     * @param translator 文本转义
     * @return {@link Builder}
     */
    public static Builder builder(final CharSequenceTranslator translator) {
        return new Builder(translator);
    }

    /**
     * 使用Java字符串规则转义{@code String}中的字符
     *
     * <pre>
     * input string: He didn't say, "Stop!"
     * output string: He didn't say, \"Stop!\"
     * </pre>
     *
     * @param input 要转义值的字符串可以为空
     * @return 带转义值的字符串，{@code null}如果输入为空字符串
     */
    public static final String escapeJava(final String input) {
        return new EscapeCodeValues().ESCAPE_JAVA.translate(input);
    }

    /**
     * 使用EcmaScript字符串规则转义{@code String }中的字符.
     *
     * <pre>
     * input string: He didn't say, "Stop!"
     * output string: He didn\'t say, \"Stop!\"
     * </pre>
     *
     * @param input 要转义值的字符串可以为空
     * @return 带转义值的字符串，{@code null}如果输入为空字符串
     */
    public static final String escapeEcmaScript(final String input) {
        return new EscapeCodeValues().ESCAPE_ECMASCRIPT.translate(input);
    }

    /**
     * 使用Json字符串规则转义{@code String}中的字符
     *
     * <pre>
     * input string: He didn't say, "Stop!"
     * output string: He didn't say, \"Stop!\"
     * </pre>
     *
     * @param input 要转义值的字符串可以为空
     * @return 带转义值的字符串，{@code null}如果输入为空字符串
     */
    public static final String escapeJson(final String input) {
        return new EscapeCodeValues().ESCAPE_JSON.translate(input);
    }

    /**
     * 取消在{@code String}中发现的任何Java信息的转义.
     *
     * @param input 要取消转义的{@code String}可以为空
     * @return 新的未转义的{@code String}, {@code null}如果输入为空字符串
     */
    public static final String unescapeJava(final String input) {
        return new EscapeCodeValues().UNESCAPE_JAVA.translate(input);
    }

    /**
     * 取消在{@code String}中找到的任何EcmaScript文本
     *
     * @param input 要取消转义的{@code String }可以为空
     * @return 新的未转义的{@code String}， {@code null}如果输入为空字符串
     * @see #unescapeJava(String)
     */
    public static final String unescapeEcmaScript(final String input) {
        return new EscapeCodeValues().UNESCAPE_ECMASCRIPT.translate(input);
    }

    /**
     * 取消在{@code String}中找到的任何Json文本
     *
     * @param input 要取消转义的{@code String}可以为空
     * @return 新的未转义的{@code String}， {@code null}如果输入为空字符串
     * @see #unescapeJava(String)
     */
    public static final String unescapeJson(final String input) {
        return new EscapeCodeValues().UNESCAPE_JSON.translate(input);
    }

    /**
     * 使用HTML实体转义{@code String}中的字符
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     * @see <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO Entities</a>
     * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character Entities for ISO Latin-1</a>
     * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity references</a>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01 Character References</a>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#code-position">HTML 4.01 Code positions</a>
     */
    public static final String escapeHtml4(final String input) {
        return EscapeCodeValues.ESCAPE_HTML4.translate(input);
    }

    /**
     * 使用HTML实体转义{@code String}中的字符.
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     */
    public static final String escapeHtml3(final String input) {
        return EscapeCodeValues.ESCAPE_HTML3.translate(input);
    }

    /**
     * 将包含实体的字符串转义为包含与转义对应的实际Unicode字符的字符串。支持HTML 4.0实体
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     */
    public static final String unescapeHtml4(final String input) {
        return new EscapeCodeValues().UNESCAPE_HTML4.translate(input);
    }

    /**
     * 将包含实体的字符串转义为包含与转义对应的实际Unicode字符的字符串。支持HTML 4.0实体
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     */
    public static final String unescapeHtml3(final String input) {
        return new EscapeCodeValues().UNESCAPE_HTML3.translate(input);
    }

    /**
     * 使用XML实体转义{@code String}中的字符
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     * @see #unescapeXml(String)
     */
    public static String escapeXml10(final String input) {
        return new EscapeCodeValues().ESCAPE_XML10.translate(input);
    }

    /**
     * 使用XML实体转义{@code String}中的字符
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     * @see #unescapeXml(String)
     */
    public static String escapeXml11(final String input) {
        return new EscapeCodeValues().ESCAPE_XML11.translate(input);
    }

    /**
     * 将包含XML实体的字符串转义为包含与转义对应的实际Unicode字符的字符串
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     * @see #escapeXml10(String)
     * @see #escapeXml11(String)
     */
    public static final String unescapeXml(final String input) {
        return new EscapeCodeValues().UNESCAPE_XML.translate(input);
    }

    /**
     * 使用XSI规则转义{@code String}中的字符
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xcu/chap2.html">Shell Command Language</a>
     */
    public static final String escapeXSI(final String input) {
        return new EscapeCodeValues().ESCAPE_XSI.translate(input);
    }

    /**
     * 使用XSI规则取消对{@code String}中的字符的转义
     *
     * @param input 要转义的{@code String}可以为空
     * @return 一个新的转义{@code String}， {@code null}如果输入为空字符串
     * @see EscapeKit#escapeXSI(String)
     */
    public static final String unescapeXSI(final String input) {
        return new EscapeCodeValues().UNESCAPE_XSI.translate(input);
    }

    /**
     * 提供转义方法的方便的{@link StringBuilder}包装器
     *
     * <pre>
     * new Builder(ESCAPE_HTML4)
     *      .append("&lt;p&gt;")
     *      .escape("This is paragraph 1 and special chars like &amp; get escaped.")
     *      .append("&lt;/p&gt;&lt;p&gt;")
     *      .escape("This is paragraph 2 &amp; more...")
     *      .append("&lt;/p&gt;")
     *      .toString()
     * </pre>
     */
    public static final class Builder {

        /**
         * 要在生成器类中使用的StringBuilder.
         */
        private final StringBuilder sb;

        /**
         * 将在构建器类中使用的CharSequenceTranslator.
         */
        private final CharSequenceTranslator translator;


        private Builder(final CharSequenceTranslator translator) {
            this.sb = new StringBuilder();
            this.translator = translator;
        }

        /**
         * 根据给定的{@link CharSequenceTranslator}转义{@code input}
         *
         * @param input 要转义的字符串
         * @return {@code this}，以启用
         */
        public Builder escape(final String input) {
            sb.append(translator.translate(input));
            return this;
        }

        /**
         * 追加字符串信息.
         *
         * @param input 要追加的字符串
         * @return {@code this}，以启用
         */
        public Builder append(final String input) {
            sb.append(input);
            return this;
        }

        /**
         * 返回转义字符串
         *
         * @return 转义后的字符串
         */
        @Override
        public String toString() {
            return sb.toString();
        }
    }

}
