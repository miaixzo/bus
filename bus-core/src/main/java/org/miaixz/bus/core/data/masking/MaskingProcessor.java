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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Rich text masking processor for masking rich text content.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MaskingProcessor {

    /**
     * The list of masking rules.
     */
    private final List<TextMaskingRule> rules = new ArrayList<>();

    /**
     * Whether to preserve HTML tags.
     */
    private boolean preserveHtmlTags = true;

    /**
     * Constructor.
     */
    public MaskingProcessor() {

    }

    /**
     * Constructor.
     *
     * @param preserveHtmlTags Whether to preserve HTML tags.
     */
    public MaskingProcessor(final boolean preserveHtmlTags) {
        this.preserveHtmlTags = preserveHtmlTags;
    }

    /**
     * Adds a masking rule.
     *
     * @param rule The masking rule.
     * @return this
     */
    public MaskingProcessor addRule(final TextMaskingRule rule) {
        this.rules.add(rule);
        return this;
    }

    /**
     * Masks the text content.
     *
     * @param text The text content.
     * @return The masked text.
     */
    public String mask(final String text) {
        if (StringKit.isBlank(text)) {
            return text;
        }

        // Special handling for HTML content
        if (preserveHtmlTags && isHtmlContent(text)) {
            return maskHtmlContent(text);
        } else {
            // Process plain text directly
            return maskPlainText(text);
        }
    }

    /**
     * Checks if the text is HTML content.
     *
     * @param text The text content.
     * @return Whether the text is HTML content.
     */
    private boolean isHtmlContent(final String text) {
        // Simple check for HTML tags
        return text.contains("<") && text.contains(">") && (text.contains("</") || text.contains("/>"));
    }

    /**
     * Masks HTML content.
     *
     * @param html The HTML content.
     * @return The masked HTML.
     */
    private String maskHtmlContent(final String html) {
        final StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        boolean inTag = false;
        String currentTag = null;

        for (int i = 0; i < html.length(); i++) {
            final char c = html.charAt(i);

            if (c == '<') {
                // Process the text content before the tag
                if (!inTag && i > lastIndex) {
                    final String textContent = html.substring(lastIndex, i);
                    result.append(processTextContentWithContext(textContent, currentTag));
                }

                inTag = true;
                lastIndex = i;

                // Try to get the current tag name
                int tagNameStart = i + 1;
                if (tagNameStart < html.length()) {
                    // Skip the slash of the closing tag
                    if (html.charAt(tagNameStart) == '/') {
                        tagNameStart++;
                    }

                    // Find the end of the tag name
                    int tagNameEnd = html.indexOf(' ', tagNameStart);
                    if (tagNameEnd == -1) {
                        tagNameEnd = html.indexOf('>', tagNameStart);
                    }

                    if (tagNameEnd > tagNameStart) {
                        currentTag = html.substring(tagNameStart, tagNameEnd).toLowerCase();
                    }
                }
            } else if (c == '>' && inTag) {
                inTag = false;
                result.append(html, lastIndex, i + 1); // Preserve the tag
                lastIndex = i + 1;
            }
        }

        // Process the last part
        if (lastIndex < html.length()) {
            if (inTag) {
                // If still inside a tag, append the rest of the string
                result.append(html.substring(lastIndex));
            } else {
                // Process the final text content
                final String textContent = html.substring(lastIndex);
                result.append(processTextContentWithContext(textContent, currentTag));
            }
        }

        return result.toString();
    }

    /**
     * Processes text content based on the context.
     *
     * @param text    The text content.
     * @param tagName The name of the current tag.
     * @return The processed text.
     */
    private String processTextContentWithContext(final String text, final String tagName) {
        if (StringKit.isBlank(text)) {
            return text;
        }

        String result = text;

        for (final TextMaskingRule rule : rules) {
            // Check if filtering by tag is needed
            if (tagName != null) {
                // If only specific tags are included and the current tag is not in the list, skip
                if (!rule.getIncludeTags().isEmpty() && !rule.getIncludeTags().contains(tagName)) {
                    continue;
                }

                // If the current tag is in the exclude list, skip
                if (rule.getExcludeTags().contains(tagName)) {
                    continue;
                }
            }

            // Apply the masking rule
            result = applyMaskingRule(result, rule);
        }

        return result;
    }

    /**
     * Masks plain text.
     *
     * @param text The text content.
     * @return The masked text.
     */
    private String maskPlainText(final String text) {
        String result = text;

        for (final TextMaskingRule rule : rules) {
            result = applyMaskingRule(result, rule);
        }

        return result;
    }

    /**
     * Applies a masking rule.
     *
     * @param text The text content.
     * @param rule The masking rule.
     * @return The masked text.
     */
    private String applyMaskingRule(final String text, final TextMaskingRule rule) {
        if (StringKit.isBlank(text) || StringKit.isBlank(rule.getPattern())) {
            return text;
        }

        final Pattern pattern = Pattern.compile(rule.getPattern());
        final Matcher matcher = pattern.matcher(text);

        final StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            final String matched = matcher.group();
            final String replacement = switch (rule.getMasking()) {
                case FULL ->
                        // Full masking: replace the entire matched content with the mask character
                        StringKit.repeat(rule.getMaskChar(), matched.length());
                case PARTIAL ->
                        // Partial masking: preserve some of the original content
                        partialMask(matched, rule.getPreserveLeft(), rule.getPreserveRight(), rule.getMaskChar());
                case REPLACE ->
                        // Replacement masking: replace with the specified text
                        rule.getReplacement();
                default -> matched;
            };

            // Handle special characters in the regular expression
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Partially masks the text, preserving some of the original content.
     *
     * @param text          The original text.
     * @param preserveLeft  The number of characters to preserve on the left.
     * @param preserveRight The number of characters to preserve on the right.
     * @param maskChar      The masking character.
     * @return The masked text.
     */
    private String partialMask(final String text, int preserveLeft, int preserveRight, final char maskChar) {
        if (StringKit.isBlank(text)) {
            return text;
        }

        final int length = text.length();

        // Adjust the number of preserved characters to ensure it does not exceed the text length
        preserveLeft = Math.min(preserveLeft, length);
        preserveRight = Math.min(preserveRight, length - preserveLeft);

        // Calculate the number of characters to be masked
        final int maskLength = length - preserveLeft - preserveRight;

        if (maskLength <= 0) {
            return text;
        }

        final StringBuilder sb = new StringBuilder(length);

        // Append the preserved characters on the left
        if (preserveLeft > 0) {
            sb.append(text, 0, preserveLeft);
        }

        // Append the masking characters
        sb.append(StringKit.repeat(maskChar, maskLength));

        // Append the preserved characters on the right
        if (preserveRight > 0) {
            sb.append(text, length - preserveRight, length);
        }

        return sb.toString();
    }

}
