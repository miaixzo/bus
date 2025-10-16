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
package org.miaixz.bus.core.text.placeholder.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.placeholder.StringTemplate;
import org.miaixz.bus.core.text.placeholder.segment.LiteralSegment;
import org.miaixz.bus.core.text.placeholder.segment.SingleSegment;
import org.miaixz.bus.core.text.placeholder.segment.StringSegment;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Single placeholder string template.
 * <p>
 * For example, "?", "{}", "$$$"
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SinglePlaceholderString extends StringTemplate {

    /**
     * The default placeholder.
     */
    public static final String DEFAULT_PLACEHOLDER = Symbol.DELIM;
    /**
     * The placeholder, defaults to {@link Symbol#DELIM}
     */
    protected String placeholder;

    /**
     * Constructs a {@code SinglePlaceholderString} with the specified template, features, placeholder, escape
     * character, default value, and default value handler.
     *
     * @param template            The string template, cannot be {@code null}.
     * @param features            The features to enable for this template.
     * @param placeholder         The placeholder string.
     * @param escape              The escape character.
     * @param defaultValue        The default value to use when a placeholder cannot be resolved.
     * @param defaultValueHandler The handler for default values.
     */
    protected SinglePlaceholderString(final String template, final int features, final String placeholder,
            final char escape, final String defaultValue, final UnaryOperator<String> defaultValueHandler) {
        super(template, escape, defaultValue, defaultValueHandler, features);

        Assert.notEmpty(placeholder);
        this.placeholder = placeholder;

        // Initialize segment list
        afterInit();
    }

    /**
     * Creates a builder for {@code SinglePlaceholderString}.
     *
     * @param template The string template, cannot be {@code null}.
     * @return A builder instance.
     */
    public static Builder builder(final String template) {
        return new Builder(template);
    }

    /**
     * Parses the template string into a list of segments.
     *
     * @param template The template string to parse.
     * @return A list of {@link StringSegment} representing the parsed template.
     */
    @Override
    protected List<StringSegment> parseSegments(final String template) {
        final int placeholderLength = placeholder.length();
        final int strPatternLength = template.length();
        // Record the position already processed
        int handledPosition = 0;
        // Placeholder position
        int delimIndex;
        // Whether the last parsed segment was a literal text, if so, it needs to be merged with the new text part
        boolean lastIsLiteralSegment = false;
        // Reused placeholder variable
        final SingleSegment singlePlaceholderSegment = SingleSegment.of(placeholder);
        List<StringSegment> segments = null;
        while (true) {
            delimIndex = template.indexOf(placeholder, handledPosition);
            if (delimIndex == -1) {
                // The entire template does not contain placeholders
                if (handledPosition == 0) {
                    return Collections.singletonList(new LiteralSegment(template));
                }
                // The remaining part of the string template no longer contains placeholders
                if (handledPosition < strPatternLength) {
                    addLiteralSegment(lastIsLiteralSegment, segments, template.substring(handledPosition));
                }
                return segments;
            } else if (segments == null) {
                segments = new ArrayList<>();
            }

            // There is an escape character
            if (delimIndex > 0 && template.charAt(delimIndex - 1) == escape) {
                // There is a double escape character
                if (delimIndex > 1 && template.charAt(delimIndex - 2) == escape) {
                    // There is another escape character before the escape character, like: "//{", the placeholder is
                    // still valid
                    addLiteralSegment(
                            lastIsLiteralSegment,
                            segments,
                            template.substring(handledPosition, delimIndex - 1));
                    segments.add(singlePlaceholderSegment);
                    lastIsLiteralSegment = false;
                    handledPosition = delimIndex + placeholderLength;
                } else {
                    // The placeholder is escaped, like: "/{", the current character is not a real placeholder, but part
                    // of a normal string
                    addLiteralSegment(
                            lastIsLiteralSegment,
                            segments,
                            template.substring(handledPosition, delimIndex - 1) + placeholder.charAt(0));
                    lastIsLiteralSegment = true;
                    handledPosition = delimIndex + 1;
                }
            } else {
                // Normal placeholder
                addLiteralSegment(lastIsLiteralSegment, segments, template.substring(handledPosition, delimIndex));
                segments.add(singlePlaceholderSegment);
                lastIsLiteralSegment = false;
                handledPosition = delimIndex + placeholderLength;
            }
        }
    }

    /**
     * Replaces placeholders with array elements in sequence.
     *
     * @param args Variable arguments.
     * @return The formatted string.
     */
    public String format(final Object... args) {
        return formatArray(args);
    }

    /**
     * Replaces placeholders with raw array elements in sequence.
     *
     * @param array A raw type array, e.g., {@code int[]}.
     * @return The formatted string.
     */
    public String formatArray(final Object array) {
        return formatArray(ArrayKit.wrap(array));
    }

    /**
     * Replaces placeholders with array elements in sequence.
     *
     * @param array The array.
     * @return The formatted string.
     */
    public String formatArray(final Object[] array) {
        if (array == null) {
            return getTemplate();
        }
        return format(Arrays.asList(array));
    }

    /**
     * Replaces placeholders with iterable elements in sequence.
     *
     * @param iterable The iterable.
     * @return The formatted string.
     */
    public String format(final Iterable<?> iterable) {
        return super.formatSequence(iterable);
    }

    /**
     * Parses the values at placeholder positions into a string array in sequence.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A string array of parameter values.
     */
    public String[] matchesToArray(final String text) {
        return matches(text).toArray(new String[0]);
    }

    /**
     * Parses the values at placeholder positions into a string list in sequence.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A string list of parameter values.
     */
    public List<String> matches(final String text) {
        return super.matchesSequence(text);
    }

    /**
     * Builder for {@link SinglePlaceholderString}.
     */
    public static class Builder extends AbstractBuilder<Builder, SinglePlaceholderString> {

        /**
         * Single placeholder.
         * <p>
         * For example: "?", "{}"
         *
         * <p>
         * Defaults to {@link SinglePlaceholderString#DEFAULT_PLACEHOLDER}
         */
        protected String placeholder;

        /**
         * Constructs a new Builder.
         *
         * @param template The template string.
         */
        protected Builder(final String template) {
            super(template);
        }

        /**
         * Sets the placeholder.
         *
         * @param placeholder The placeholder, cannot be {@code null} or an empty string.
         * @return This builder instance.
         */
        public Builder placeholder(final String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Builds a new {@link SinglePlaceholderString} instance.
         *
         * @return A new {@link SinglePlaceholderString} instance.
         */
        @Override
        protected SinglePlaceholderString buildInstance() {
            if (this.placeholder == null) {
                this.placeholder = DEFAULT_PLACEHOLDER;
            }
            return new SinglePlaceholderString(this.template, this.features, this.placeholder, this.escape,
                    this.defaultValue, this.defaultValueHandler);
        }

        /**
         * Returns this builder instance.
         *
         * @return This builder instance.
         */
        @Override
        protected Builder self() {
            return this;
        }
    }

}
