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

import java.util.*;
import java.util.function.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.placeholder.StringTemplate;
import org.miaixz.bus.core.text.placeholder.segment.*;
import org.miaixz.bus.core.xyz.*;

/**
 * String template with named or indexed placeholders.
 * <p>
 * For example, "{1}", "{name}", "#{data}"
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamedPlaceholderString extends StringTemplate {

    /**
     * Default prefix for placeholders.
     */
    public static final String DEFAULT_PREFIX = Symbol.BRACE_LEFT;
    /**
     * Default suffix for placeholders.
     */
    public static final String DEFAULT_SUFFIX = Symbol.BRACE_RIGHT;
    /**
     * Placeholder prefix, defaults to {@link #DEFAULT_PREFIX}
     */
    protected String prefix;
    /**
     * Placeholder suffix, defaults to {@link #DEFAULT_SUFFIX}
     */
    protected String suffix;
    /**
     * In indexed placeholders, the maximum index value.
     */
    protected int indexedSegmentMaxIdx = 0;

    /**
     * Constructs a {@code NamedPlaceholderString} with the specified template, features, prefix, suffix, escape
     * character, default value, and default value handler.
     *
     * @param template            The string template, cannot be {@code null}.
     * @param features            The features to enable for this template.
     * @param prefix              The prefix for placeholders.
     * @param suffix              The suffix for placeholders.
     * @param escape              The escape character.
     * @param defaultValue        The default value to use when a placeholder cannot be resolved.
     * @param defaultValueHandler The handler for default values.
     */
    protected NamedPlaceholderString(final String template, final int features, final String prefix,
            final String suffix, final char escape, final String defaultValue,
            final UnaryOperator<String> defaultValueHandler) {
        super(template, escape, defaultValue, defaultValueHandler, features);

        Assert.notEmpty(prefix);
        Assert.notEmpty(suffix);
        this.prefix = prefix;
        this.suffix = suffix;

        // Some initialization subsequent operations
        afterInit();

        // Record the maximum index value for indexed placeholders
        if (!placeholderSegments.isEmpty()) {
            for (final AbstractSegment segment : placeholderSegments) {
                if (segment instanceof IndexedSegment) {
                    this.indexedSegmentMaxIdx = Math
                            .max(this.indexedSegmentMaxIdx, ((IndexedSegment) segment).getIndex());
                }
            }
        }
    }

    /**
     * Creates a builder for {@code NamedPlaceholderString}.
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
     * @throws InternalException if a placeholder's opening delimiter is not matched by a closing delimiter.
     */
    @Override
    protected List<StringSegment> parseSegments(final String template) {
        // Find the first prefix symbol
        int openCursor = template.indexOf(prefix);
        // No placeholders
        if (openCursor == -1) {
            return Collections.singletonList(new LiteralSegment(template));
        }

        final int openLength = prefix.length();
        final int closeLength = suffix.length();
        final List<StringSegment> segments = new ArrayList<>();
        int closeCursor = 0;
        // Start matching
        final char[] src = template.toCharArray();
        final StringBuilder expression = new StringBuilder(16);
        boolean hasDoubleEscape = false;
        // Placeholder variable name
        String variableName;
        // Complete placeholder
        String wholePlaceholder;
        // Whether the last parsed segment was a literal text, if so, it needs to be merged with the new text part
        boolean isLastLiteralSegment = false;
        while (openCursor > -1) {
            // Whether the opening symbol is escaped, if so, skip and find the next opening symbol
            if (openCursor > 0 && src[openCursor - 1] == escape) {
                // There is a double escape character, another escape character before the escape character, like:
                // "\\{", the placeholder is still valid
                if (openCursor > 1 && src[openCursor - 2] == escape) {
                    hasDoubleEscape = true;
                } else {
                    // The opening symbol is escaped, skip, find the next opening symbol
                    addLiteralSegment(
                            isLastLiteralSegment,
                            segments,
                            template.substring(closeCursor, openCursor - 1) + prefix);
                    isLastLiteralSegment = true;
                    closeCursor = openCursor + openLength;
                    openCursor = template.indexOf(prefix, closeCursor);
                    continue;
                }
            }

            // No double escape character
            if (!hasDoubleEscape) {
                if (closeCursor < openCursor) {
                    // Completely record the string between the opening symbol of the current placeholder and the
                    // closing symbol of the previous placeholder
                    addLiteralSegment(isLastLiteralSegment, segments, template.substring(closeCursor, openCursor));
                }
            } else {
                // There is a double escape character, only one escape character can be kept
                hasDoubleEscape = false;
                addLiteralSegment(isLastLiteralSegment, segments, template.substring(closeCursor, openCursor - 1));
            }

            // Reset the closing cursor to the beginning of the current placeholder
            closeCursor = openCursor + openLength;

            // Find the index of the closing symbol
            int end = template.indexOf(suffix, closeCursor);
            while (end > -1) {
                // The closing symbol is escaped, find the next closing symbol
                if (end > closeCursor && src[end - 1] == escape) {
                    // Double escape character, keep one escape character, and found the closing character
                    if (end > 1 && src[end - 2] == escape) {
                        expression.append(src, closeCursor, end - closeCursor - 1);
                        break;
                    } else {
                        expression.append(src, closeCursor, end - closeCursor - 1).append(suffix);
                        closeCursor = end + closeLength;
                        end = template.indexOf(suffix, closeCursor);
                    }
                }
                // Found the closing symbol
                else {
                    expression.append(src, closeCursor, end - closeCursor);
                    break;
                }
            }

            // If the closing symbol is not found, it means the match is abnormal
            if (end == -1) {
                throw new InternalException(
                        "The opening delimiter at character index {} in \"{}\" has no corresponding closing delimiter",
                        openCursor, template);
            }
            // If the closing symbol is found, the string between the start and end symbols is the placeholder variable
            else {
                // Placeholder variable name
                variableName = expression.toString();
                expression.setLength(0);
                // Complete placeholder
                wholePlaceholder = expression.append(prefix).append(variableName).append(suffix).toString();
                expression.setLength(0);
                // If it is an integer, treat it as an index
                if (MathKit.isInteger(variableName)) {
                    segments.add(new IndexedSegment(variableName, wholePlaceholder));
                } else {
                    // Treat as variable name
                    segments.add(new NamedSegment(variableName, wholePlaceholder));
                }
                isLastLiteralSegment = false;
                // Complete the processing and matching of the current placeholder, find the next one
                closeCursor = end + closeLength;
            }

            // Find the next opening symbol
            openCursor = template.indexOf(prefix, closeCursor);
        }

        // If there are unprocessed strings after matching, append them directly to the expression
        if (closeCursor < src.length) {
            addLiteralSegment(isLastLiteralSegment, segments, template.substring(closeCursor));
        }
        return segments;
    }

    /**
     * Replaces placeholders with array elements in sequence.
     *
     * @param args Variable arguments.
     * @return The formatted string.
     */
    public String formatSequence(final Object... args) {
        return formatArraySequence(args);
    }

    /**
     * Replaces placeholders with raw array elements in sequence.
     *
     * @param array A raw type array, e.g., {@code int[]}.
     * @return The formatted string.
     */
    public String formatArraySequence(final Object array) {
        return formatArraySequence(ArrayKit.wrap(array));
    }

    /**
     * Replaces placeholders with array elements in sequence.
     *
     * @param array The array.
     * @return The formatted string.
     */
    public String formatArraySequence(final Object[] array) {
        if (array == null) {
            return getTemplate();
        }
        return formatSequence(Arrays.asList(array));
    }

    /**
     * Replaces placeholders with iterator elements in sequence.
     *
     * @param iterable The iterable.
     * @return The formatted string.
     */
    @Override
    public String formatSequence(final Iterable<?> iterable) {
        return super.formatSequence(iterable);
    }

    /**
     * Replaces placeholders with array elements by index.
     *
     * @param args Variable arguments.
     * @return The formatted string.
     */
    public String formatIndexed(final Object... args) {
        return formatArrayIndexed(args);
    }

    /**
     * Replaces placeholders with raw array elements by index.
     *
     * @param array A raw type array.
     * @return The formatted string.
     */
    public String formatArrayIndexed(final Object array) {
        return formatArrayIndexed(ArrayKit.wrap(array));
    }

    /**
     * Replaces placeholders with array elements by index.
     *
     * @param array The array.
     * @return The formatted string.
     */
    public String formatArrayIndexed(final Object[] array) {
        if (array == null) {
            return getTemplate();
        }
        return formatIndexed(Arrays.asList(array));
    }

    /**
     * Replaces placeholders with collection elements by index.
     *
     * @param collection The collection elements.
     * @return The formatted string.
     */
    public String formatIndexed(final Collection<?> collection) {
        return formatIndexed(collection, null);
    }

    /**
     * Replaces placeholders with collection elements by index.
     *
     * @param collection          The collection elements.
     * @param missingIndexHandler A handler for when an index is not found in the collection, returns a substitute value
     *                            based on the index.
     * @return The formatted string.
     */
    public String formatIndexed(final Collection<?> collection, final IntFunction<String> missingIndexHandler) {
        if (collection == null) {
            return getTemplate();
        }

        final int size = collection.size();
        final boolean isList = collection instanceof List;
        return formatBySegment(segment -> {
            int index = ((IndexedSegment) segment).getIndex();
            if (index < 0) {
                index += size;
            }
            if (index >= 0 && index < size) {
                if (isList) {
                    return ((List<?>) collection).get(index);
                }
                return CollKit.get(collection, index);
            }
            // Index out of bounds, meaning the placeholder has no corresponding value, try to get a substitute value
            else if (missingIndexHandler != null) {
                return missingIndexHandler.apply(index);
            } else {
                return formatMissingKey(segment);
            }
        });
    }

    /**
     * Replaces placeholders by querying values from a Bean or Map using the placeholder variable name.
     *
     * @param beanOrMap The Bean or Map instance.
     * @return The formatted string.
     */
    public String format(final Object beanOrMap) {
        if (beanOrMap == null) {
            return getTemplate();
        }
        if (beanOrMap instanceof Map) {
            return format((Map<String, ?>) beanOrMap);
        }
        return format(fieldName -> BeanKit.getProperty(beanOrMap, fieldName));
    }

    /**
     * Replaces placeholders by querying values from a Map using the placeholder variable name.
     *
     * @param map The map.
     * @return The formatted string.
     */
    public String format(final Map<String, ?> map) {
        if (null == map) {
            return getTemplate();
        }
        return format(map::get, map::containsKey);
    }

    /**
     * Replaces placeholders by querying values from a value supplier using the placeholder variable name.
     *
     * @param valueSupplier A function that returns a value based on the placeholder variable name.
     * @return The formatted string.
     */
    public String format(final Function<String, ?> valueSupplier) {
        if (valueSupplier == null) {
            return getTemplate();
        }
        return formatBySegment(segment -> valueSupplier.apply(segment.getPlaceholder()));
    }

    /**
     * Replaces placeholders by querying values from a value supplier using the placeholder variable name.
     *
     * @param valueSupplier A function that returns a value based on the placeholder variable name.
     * @param containsKey   A predicate to check if the placeholder variable name exists, e.g.,
     *                      {@code map.containsKey(data)}.
     * @return The formatted string.
     */
    public String format(final Function<String, ?> valueSupplier, final Predicate<String> containsKey) {
        if (valueSupplier == null || containsKey == null) {
            return getTemplate();
        }

        return formatBySegment(segment -> {
            final String placeholder = segment.getPlaceholder();
            if (containsKey.test(placeholder)) {
                return valueSupplier.apply(placeholder);
            }
            return formatMissingKey(segment);
        });
    }

    /**
     * Parses the values at placeholder positions into a string array in sequence.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A string array.
     */
    public String[] matchesSequenceToArray(final String text) {
        return matchesSequence(text).toArray(new String[0]);
    }

    /**
     * Parses the values at placeholder positions into a string list in sequence.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A string list.
     */
    @Override
    public List<String> matchesSequence(final String text) {
        return super.matchesSequence(text);
    }

    /**
     * Parses the values at placeholder positions into a string array by placeholder index.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A string array.
     * @see #matchesIndexed(String, IntFunction)
     */
    public String[] matchesIndexedToArray(final String text) {
        return matchesIndexed(text, null).toArray(new String[0]);
    }

    /**
     * Parses the values at placeholder positions into a string array by placeholder index.
     *
     * @param text                The string to be parsed, typically the return value of a formatting method.
     * @param missingIndexHandler A function that returns a default value based on the index when the index position is
     *                            not found in the collection. This parameter can be {@code null}, and is only effective
     *                            with the {@link Feature#MATCH_EMPTY_VALUE_TO_DEFAULT_VALUE} strategy.
     * @return A string array.
     * @see #matchesIndexed(String, IntFunction)
     */
    public String[] matchesIndexedToArray(final String text, final IntFunction<String> missingIndexHandler) {
        return matchesIndexed(text, missingIndexHandler).toArray(new String[0]);
    }

    /**
     * Parses the values at placeholder positions into a string list by placeholder index.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A string list.
     * @see #matchesIndexed(String, IntFunction)
     */
    public List<String> matchesIndexed(final String text) {
        return matchesIndexed(text, null);
    }

    /**
     * Parses the values at placeholder positions into a string list by placeholder index.
     * <p>
     * For example, if the template is {@literal "This is between {1} and {2}"}, and the formatted result is
     * {@literal "This is between 666 and 999"}, since the maximum index is 2, the parsing result will always have 3
     * elements, and the result will be {@code [null, "666", "999"]}.
     *
     * @param text                The string to be parsed, typically the return value of a formatting method.
     * @param missingIndexHandler A function that returns a default value based on the index when the index position is
     *                            not found in the collection. This parameter can be {@code null}, and is only effective
     *                            with the {@link Feature#MATCH_EMPTY_VALUE_TO_DEFAULT_VALUE} strategy.
     * @return A string list.
     */
    public List<String> matchesIndexed(final String text, final IntFunction<String> missingIndexHandler) {
        if (text == null || placeholderSegments.isEmpty() || !isMatches(text)) {
            return ListKit.zero();
        }

        final List<String> params = new ArrayList<>(this.indexedSegmentMaxIdx + 1);
        // Fill all positions with null values
        ListKit.setOrPadding(params, this.indexedSegmentMaxIdx, null, null);
        matchesIndexed(text, params::set, missingIndexHandler);
        return params;
    }

    /**
     * Extracts result values based on the index and the value at the indexed placeholder position.
     * <p>
     * For example, if the template is {@literal "This is between {1} and {2}"}, and the formatted result is
     * {@literal "This is between 666 and 999"}, since the maximum index is 2, the parsing result will always have 3
     * elements, and the result will be {@code [null, "666", "999"]}.
     *
     * @param text                The string to be parsed, typically the return value of a formatting method.
     * @param idxValueConsumer    A consumer that processes the index and the value at the indexed placeholder position,
     *                            e.g., {@code (idx, value) -> list.set(idx, value)}.
     * @param missingIndexHandler A function that returns a default value based on the index when the index position is
     *                            not found in the collection. This parameter can be {@code null}, and is only effective
     *                            with the {@link Feature#MATCH_EMPTY_VALUE_TO_DEFAULT_VALUE} strategy.
     */
    public void matchesIndexed(
            final String text,
            final BiConsumer<Integer, String> idxValueConsumer,
            final IntFunction<String> missingIndexHandler) {
        if (text == null || CollKit.isEmpty(placeholderSegments) || !isMatches(text)) {
            return;
        }

        if (missingIndexHandler == null) {
            matchesByKey(text, (key, value) -> idxValueConsumer.accept(Integer.parseInt(key), value));
        } else {
            matchesByKey(text, (key, value) -> idxValueConsumer.accept(Integer.parseInt(key), value), true, segment -> {
                if ((segment instanceof IndexedSegment)) {
                    return missingIndexHandler.apply(((IndexedSegment) segment).getIndex());
                }
                return getDefaultValue(segment);
            });
        }
    }

    /**
     * Constructs a {@link Map} based on placeholder variables and their corresponding parsed values.
     *
     * @param text The string to be parsed, typically the return value of a formatting method.
     * @return A {@link Map}.
     */
    public Map<String, String> matches(final String text) {
        return matches(text, HashMap::new);
    }

    /**
     * Constructs a map or bean instance based on placeholder variables and their corresponding parsed values.
     *
     * @param text              The string to be parsed, typically the return value of a formatting method.
     * @param beanOrMapSupplier A supplier that provides a bean or map, e.g., {@code HashMap::new}.
     * @param <T>               The type of the returned object.
     * @return A map or bean instance.
     */
    public <T> T matches(final String text, final Supplier<T> beanOrMapSupplier) {
        Assert.notNull(beanOrMapSupplier, "beanOrMapSupplier cannot be null");
        final T object = beanOrMapSupplier.get();
        if (text == null || object == null || placeholderSegments.isEmpty() || !isMatches(text)) {
            return object;
        }

        if (object instanceof Map) {
            final Map<String, String> map = (Map<String, String>) object;
            matchesByKey(text, map::put);
        } else if (BeanKit.isWritableBean(object.getClass())) {
            matchesByKey(text, (key, value) -> BeanKit.setProperty(object, key, value));
        }
        return object;
    }

    /**
     * Builder for {@link NamedPlaceholderString}.
     */
    public static class Builder extends AbstractBuilder<Builder, NamedPlaceholderString> {

        /**
         * Placeholder prefix, defaults to {@link NamedPlaceholderString#DEFAULT_PREFIX}, cannot be an empty string.
         */
        protected String prefix;
        /**
         * Placeholder suffix, defaults to {@link NamedPlaceholderString#DEFAULT_SUFFIX}, cannot be an empty string.
         */
        protected String suffix;

        /**
         * Constructs a new Builder.
         *
         * @param template The template string.
         */
        protected Builder(final String template) {
            super(template);
        }

        /**
         * Sets the placeholder prefix.
         *
         * @param prefix The placeholder prefix, cannot be an empty string.
         * @return This builder instance.
         */
        public Builder prefix(final String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Sets the placeholder suffix.
         *
         * @param suffix The placeholder suffix, cannot be an empty string.
         * @return This builder instance.
         */
        public Builder suffix(final String suffix) {
            this.suffix = suffix;
            return this;
        }

        /**
         * Builds a new {@link NamedPlaceholderString} instance.
         *
         * @return A new {@link NamedPlaceholderString} instance.
         */
        @Override
        protected NamedPlaceholderString buildInstance() {
            if (this.prefix == null) {
                this.prefix = DEFAULT_PREFIX;
            }
            if (this.suffix == null) {
                this.suffix = DEFAULT_SUFFIX;
            }
            return new NamedPlaceholderString(this.template, this.features, this.prefix, this.suffix, this.escape,
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
