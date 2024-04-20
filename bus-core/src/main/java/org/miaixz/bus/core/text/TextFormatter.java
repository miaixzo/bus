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
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.Map;

/**
 * 字符串格式化工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextFormatter {

    /**
     * 格式化字符串
     * 此方法只是简单将占位符 {} 按照顺序替换为参数
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可
     * 例：
     * 通常使用：format("this is {} for {}", "a", "b") - this is a for b
     * 转义{}： format("this is \\{} for {}", "a", "b") - this is \{} for a
     * 转义\： format("this is \\\\{} for {}", "a", "b") - this is \a for b
     *
     * @param template 字符串模板
     * @param args     参数列表
     * @return 结果
     */
    public static String format(String template, Object... args) {
        return format(template, "{}", args);
    }

    /**
     * 格式化字符串
     * 此方法只是简单将指定占位符 按照顺序替换为参数
     * 如果想输出占位符使用 \\转义即可，如果想输出占位符之前的 \ 使用双转义符 \\\\ 即可
     * 例：
     * 通常使用：format("this is {} for {}", "{}", "a", "b") - this is a for b
     * 转义{}： format("this is \\{} for {}", "{}", "a", "b") - this is {} for a
     * 转义\： format("this is \\\\{} for {}", "{}", "a", "b") - this is \a for b
     *
     * @param template    字符串模板
     * @param placeHolder 占位符，例如{}
     * @param args        参数列表
     * @return 结果
     */
    public static String format(String template, String placeHolder, Object... args) {
        if (StringKit.isBlank(template) || StringKit.isBlank(placeHolder) || ArrayKit.isEmpty(args)) {
            return template;
        }
        final int templateLength = template.length();
        final int placeHolderLength = placeHolder.length();

        // 初始化定义好的长度以获得更好的性能
        final StringBuilder text = new StringBuilder(templateLength + 50);

        // 记录已经处理到的位置
        int handledPosition = 0;
        // 占位符所在位置
        int delimIndex;
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            delimIndex = template.indexOf(placeHolder, handledPosition);
            // 剩余部分无占位符
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    // 不带占位符的模板直接返回
                    return template;
                }
                // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
                text.append(template, handledPosition, templateLength);
                return text.toString();
            }

            // 转义符
            if (delimIndex > 0 && template.charAt(delimIndex - 1) == Symbol.C_BACKSLASH) {
                // 双转义符
                if (delimIndex > 1 && template.charAt(delimIndex - 2) == Symbol.C_BACKSLASH) {
                    // 转义符之前还有一个转义符，占位符依旧有效
                    text.append(template, handledPosition, delimIndex - 1);
                    text.append(StringKit.toString(args[argIndex]));
                    handledPosition = delimIndex + placeHolderLength;
                } else {
                    // 占位符被转义
                    argIndex--;
                    text.append(template, handledPosition, delimIndex - 1);
                    text.append(placeHolder.charAt(0));
                    handledPosition = delimIndex + 1;
                }
            } else {// 正常占位符
                text.append(template, handledPosition, delimIndex);
                text.append(StringKit.toString(args[argIndex]));
                handledPosition = delimIndex + placeHolderLength;
            }
        }

        // 加入最后一个占位符后所有的字符
        text.append(template, handledPosition, template.length());

        return text.toString();
    }

    /**
     * 格式化文本
     *
     * @param template   文本模板，被替换的部分用 {key} 表示
     * @param map        参数值对
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     */
    public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template.toString();
        }

        String text = template.toString();
        String value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            value = StringKit.toString(entry.getValue());
            if (null == value && ignoreNull) {
                continue;
            }
            text = StringKit.replace(text, Symbol.BRACE_LEFT + entry.getKey() + Symbol.BRACE_RIGHT, value);
        }
        return text;
    }

}
