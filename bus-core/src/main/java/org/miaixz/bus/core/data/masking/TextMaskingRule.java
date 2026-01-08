/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Rich text masking rule, used to configure how to mask rich text content.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextMaskingRule {

    /**
     * The name of the rule.
     */
    private String name;

    /**
     * The matching pattern (regular expression).
     */
    private String pattern;

    /**
     * The masking type.
     */
    private EnumValue.Masking masking;

    /**
     * The replacement content.
     */
    private String replacement;

    /**
     * The number of characters to preserve on the left (for PARTIAL type).
     */
    private int preserveLeft;

    /**
     * The number of characters to preserve on the right (for PARTIAL type).
     */
    private int preserveRight;

    /**
     * The masking character.
     */
    private char maskChar = Symbol.C_STAR;

    /**
     * Whether to process the content of HTML tags.
     */
    private boolean processHtmlTags = false;

    /**
     * The HTML tags to be excluded.
     */
    private Set<String> excludeTags = new HashSet<>();

    /**
     * Only process the specified HTML tags.
     */
    private Set<String> includeTags = new HashSet<>();

    /**
     * Constructor.
     */
    public TextMaskingRule() {

    }

    /**
     * Constructor.
     *
     * @param name        The name of the rule.
     * @param pattern     The matching pattern (regular expression).
     * @param masking     The masking type.
     * @param replacement The replacement content.
     */
    public TextMaskingRule(final String name, final String pattern, final EnumValue.Masking masking,
            final String replacement) {
        this.name = name;
        this.pattern = pattern;
        this.masking = masking;
        this.replacement = replacement;
    }

    /**
     * Constructor for partial masking.
     *
     * @param name          The name of the rule.
     * @param pattern       The matching pattern (regular expression).
     * @param preserveLeft  The number of characters to preserve on the left.
     * @param preserveRight The number of characters to preserve on the right.
     * @param maskChar      The masking character.
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
     * Gets the name of the rule.
     *
     * @return The name of the rule.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the rule.
     *
     * @param name The name of the rule.
     * @return this
     */
    public TextMaskingRule setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the matching pattern (regular expression).
     *
     * @return The matching pattern (regular expression).
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the matching pattern (regular expression).
     *
     * @param pattern The matching pattern (regular expression).
     * @return this
     */
    public TextMaskingRule setPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Gets the masking type.
     *
     * @return The masking type.
     */
    public EnumValue.Masking getMasking() {
        return masking;
    }

    /**
     * Sets the masking type.
     *
     * @param masking The masking type.
     * @return this
     */
    public TextMaskingRule setMasking(final EnumValue.Masking masking) {
        this.masking = masking;
        return this;
    }

    /**
     * Gets the replacement content.
     *
     * @return The replacement content.
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * Sets the replacement content.
     *
     * @param replacement The replacement content.
     * @return this
     */
    public TextMaskingRule setReplacement(final String replacement) {
        this.replacement = replacement;
        return this;
    }

    /**
     * Gets the number of characters to preserve on the left.
     *
     * @return The number of characters to preserve on the left.
     */
    public int getPreserveLeft() {
        return preserveLeft;
    }

    /**
     * Sets the number of characters to preserve on the left.
     *
     * @param preserveLeft The number of characters to preserve on the left.
     * @return this
     */
    public TextMaskingRule setPreserveLeft(final int preserveLeft) {
        this.preserveLeft = preserveLeft;
        return this;
    }

    /**
     * Gets the number of characters to preserve on the right.
     *
     * @return The number of characters to preserve on the right.
     */
    public int getPreserveRight() {
        return preserveRight;
    }

    /**
     * Sets the number of characters to preserve on the right.
     *
     * @param preserveRight The number of characters to preserve on the right.
     * @return this
     */
    public TextMaskingRule setPreserveRight(final int preserveRight) {
        this.preserveRight = preserveRight;
        return this;
    }

    /**
     * Gets the masking character.
     *
     * @return The masking character.
     */
    public char getMaskChar() {
        return maskChar;
    }

    /**
     * Sets the masking character.
     *
     * @param maskChar The masking character.
     * @return this
     */
    public TextMaskingRule setMaskChar(final char maskChar) {
        this.maskChar = maskChar;
        return this;
    }

    /**
     * Gets whether to process the content of HTML tags.
     *
     * @return Whether to process the content of HTML tags.
     */
    public boolean isProcessHtmlTags() {
        return processHtmlTags;
    }

    /**
     * Sets whether to process the content of HTML tags.
     *
     * @param processHtmlTags Whether to process the content of HTML tags.
     * @return this
     */
    public TextMaskingRule setProcessHtmlTags(final boolean processHtmlTags) {
        this.processHtmlTags = processHtmlTags;
        return this;
    }

    /**
     * Gets the HTML tags to be excluded.
     *
     * @return The HTML tags to be excluded.
     */
    public Set<String> getExcludeTags() {
        return excludeTags;
    }

    /**
     * Sets the HTML tags to be excluded.
     *
     * @param excludeTags The HTML tags to be excluded.
     * @return this
     */
    public TextMaskingRule setExcludeTags(final Set<String> excludeTags) {
        this.excludeTags = excludeTags;
        return this;
    }

    /**
     * Adds an HTML tag to be excluded.
     *
     * @param tag The HTML tag to be excluded.
     * @return this
     */
    public TextMaskingRule addExcludeTag(final String tag) {
        this.excludeTags.add(tag.toLowerCase());
        return this;
    }

    /**
     * Gets the HTML tags to be processed exclusively.
     *
     * @return The HTML tags to be processed exclusively.
     */
    public Set<String> getIncludeTags() {
        return includeTags;
    }

    /**
     * Sets the HTML tags to be processed exclusively.
     *
     * @param includeTags The HTML tags to be processed exclusively.
     * @return this
     */
    public TextMaskingRule setIncludeTags(final Set<String> includeTags) {
        this.includeTags = includeTags;
        return this;
    }

    /**
     * Adds an HTML tag to be processed exclusively.
     *
     * @param tag The HTML tag to be processed exclusively.
     * @return this
     */
    public TextMaskingRule addIncludeTag(final String tag) {
        this.includeTags.add(tag.toLowerCase());
        return this;
    }

}
