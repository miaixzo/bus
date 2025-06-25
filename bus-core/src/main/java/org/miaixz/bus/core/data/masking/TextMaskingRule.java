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
package org.miaixz.bus.core.data.masking;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Symbol;

/**
 * 富文本脱敏规则，用于配置如何对富文本内容进行脱敏处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextMaskingRule {

    /**
     * 规则名称
     */
    private String name;

    /**
     * 匹配模式（正则表达式）
     */
    private String pattern;

    /**
     * 脱敏类型
     */
    private EnumValue.Masking masking;

    /**
     * 替换内容
     */
    private String replacement;

    /**
     * 保留左侧字符数（用于PARTIAL类型）
     */
    private int preserveLeft;

    /**
     * 保留右侧字符数（用于PARTIAL类型）
     */
    private int preserveRight;

    /**
     * 脱敏字符
     */
    private char maskChar = Symbol.C_STAR;

    /**
     * 是否处理HTML标签内容
     */
    private boolean processHtmlTags = false;

    /**
     * 需要排除的HTML标签
     */
    private Set<String> excludeTags = new HashSet<>();

    /**
     * 仅处理指定的HTML标签
     */
    private Set<String> includeTags = new HashSet<>();

    /**
     * 构造函数
     */
    public TextMaskingRule() {

    }

    /**
     * 构造函数
     *
     * @param name        规则名称
     * @param pattern     匹配模式（正则表达式）
     * @param masking     脱敏类型
     * @param replacement 替换内容
     */
    public TextMaskingRule(final String name, final String pattern, final EnumValue.Masking masking,
            final String replacement) {
        this.name = name;
        this.pattern = pattern;
        this.masking = masking;
        this.replacement = replacement;
    }

    /**
     * 构造函数，用于部分脱敏
     *
     * @param name          规则名称
     * @param pattern       匹配模式（正则表达式）
     * @param preserveLeft  保留左侧字符数
     * @param preserveRight 保留右侧字符数
     * @param maskChar      脱敏字符
     */
    public TextMaskingRule(final String name, final String pattern, final int preserveLeft, final int preserveRight,
            final char maskChar) {
        this.name = name;
        this.pattern = pattern;
        this.masking = EnumValue.Masking.PARTIAL;
        this.preserveLeft = preserveLeft;
        this.preserveRight = preserveRight;
        this.maskChar = maskChar;
    }

    // Getter and Setter methods

    /**
     * 获取规则名称
     *
     * @return 规则名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置规则名称
     *
     * @param name 名称
     * @return this
     */
    public TextMaskingRule setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * 获取匹配模式（正则表达式）
     *
     * @return 匹配模式（正则表达式）
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置匹配模式（正则表达式）
     *
     * @param pattern 匹配模式（正则表达式）
     * @return this
     */
    public TextMaskingRule setPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * 获取脱敏类型
     *
     * @return 脱敏类型
     */
    public EnumValue.Masking getMasking() {
        return masking;
    }

    /**
     * 设置脱敏类型
     *
     * @param masking 脱敏类型
     * @return this
     */
    public TextMaskingRule setMasking(final EnumValue.Masking masking) {
        this.masking = masking;
        return this;
    }

    /**
     * 获取替换内容
     *
     * @return 替换内容
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * 设置替换内容
     *
     * @param replacement 替换内容
     * @return this
     */
    public TextMaskingRule setReplacement(final String replacement) {
        this.replacement = replacement;
        return this;
    }

    /**
     * 获取保留左侧字符数
     *
     * @return 保留左侧字符数
     */
    public int getPreserveLeft() {
        return preserveLeft;
    }

    /**
     * 设置保留左侧字符数
     *
     * @param preserveLeft 保留左侧字符数
     * @return this
     */
    public TextMaskingRule setPreserveLeft(final int preserveLeft) {
        this.preserveLeft = preserveLeft;
        return this;
    }

    /**
     * 获取保留右侧字符数
     *
     * @return 保留右侧字符数
     */
    public int getPreserveRight() {
        return preserveRight;
    }

    /**
     * 设置保留右侧字符数
     *
     * @param preserveRight 保留右侧字符数
     * @return this
     */
    public TextMaskingRule setPreserveRight(final int preserveRight) {
        this.preserveRight = preserveRight;
        return this;
    }

    /**
     * 获取脱敏字符
     *
     * @return 脱敏字符
     */
    public char getMaskChar() {
        return maskChar;
    }

    /**
     * 设置脱敏字符
     *
     * @param maskChar 脱敏字符
     * @return this
     */
    public TextMaskingRule setMaskChar(final char maskChar) {
        this.maskChar = maskChar;
        return this;
    }

    /**
     * 获取是否处理HTML标签内容
     *
     * @return 是否处理HTML标签内容
     */
    public boolean isProcessHtmlTags() {
        return processHtmlTags;
    }

    /**
     * 设置是否处理HTML标签内容
     *
     * @param processHtmlTags 是否处理HTML标签内容
     * @return this
     */
    public TextMaskingRule setProcessHtmlTags(final boolean processHtmlTags) {
        this.processHtmlTags = processHtmlTags;
        return this;
    }

    /**
     * 获取需要排除的HTML标签
     *
     * @return 需要排除的HTML标签
     */
    public Set<String> getExcludeTags() {
        return excludeTags;
    }

    /**
     * 设置需要排除的HTML标签
     *
     * @param excludeTags 需要排除的HTML标签
     * @return this
     */
    public TextMaskingRule setExcludeTags(final Set<String> excludeTags) {
        this.excludeTags = excludeTags;
        return this;
    }

    /**
     * 添加需要排除的HTML标签
     *
     * @param tag 需要排除的HTML标签
     * @return this
     */
    public TextMaskingRule addExcludeTag(final String tag) {
        this.excludeTags.add(tag.toLowerCase());
        return this;
    }

    /**
     * 获取仅处理指定的HTML标签
     *
     * @return 仅处理指定的HTML标签
     */
    public Set<String> getIncludeTags() {
        return includeTags;
    }

    /**
     * 设置仅处理指定的HTML标签
     *
     * @param includeTags 仅处理指定的HTML标签
     * @return this
     */
    public TextMaskingRule setIncludeTags(final Set<String> includeTags) {
        this.includeTags = includeTags;
        return this;
    }

    /**
     * 添加仅处理指定的HTML标签
     *
     * @param tag 仅处理指定的HTML标签
     * @return this
     */
    public TextMaskingRule addIncludeTag(final String tag) {
        this.includeTags.add(tag.toLowerCase());
        return this;
    }

}
