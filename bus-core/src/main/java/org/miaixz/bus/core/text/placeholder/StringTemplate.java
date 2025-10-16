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
package org.miaixz.bus.core.text.placeholder;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.placeholder.segment.AbstractSegment;
import org.miaixz.bus.core.text.placeholder.segment.LiteralSegment;
import org.miaixz.bus.core.text.placeholder.segment.StringSegment;
import org.miaixz.bus.core.text.placeholder.template.NamedPlaceholderString;
import org.miaixz.bus.core.text.placeholder.template.SinglePlaceholderString;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An abstract base class for string templates that support formatting (placeholder substitution) and parsing
 * (extracting values from a formatted string).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class StringTemplate {

    /**
     * Global default features. These are used as the initial features for any new template object. Modifying this value
     * will not affect already created template instances.
     */
    protected static int globalFeatures = Feature.of(
            Feature.FORMAT_MISSING_KEY_PRINT_WHOLE_PLACEHOLDER,
            Feature.FORMAT_NULL_VALUE_TO_STR,
            Feature.MATCH_KEEP_DEFAULT_VALUE,
            Feature.MATCH_EMPTY_VALUE_TO_NULL,
            Feature.MATCH_NULL_STR_TO_NULL);

    /**
     * Global default value handler. This is used to provide a default value for a placeholder variable when no other
     * default is specified.
     */
    protected static UnaryOperator<String> globalDefaultValueHandler;
    /**
     * The escape character, default is '\'.
     */
    protected final char escape;
    /**
     * A fixed default value to use when a placeholder's corresponding value is not found.
     */
    protected final String defaultValue;
    /**
     * The default value handler for this specific template instance.
     */
    protected final UnaryOperator<String> defaultValueHandler;
    /**
     * The raw template string.
     */
    private final String template;
    /**
     * The feature flags for this specific template instance.
     */
    private final int features;
    /**
     * A list of all segments (both literal text and placeholders) parsed from the template.
     */
    protected List<StringSegment> segments;
    /**
     * A list of all placeholder segments.
     */
    protected List<AbstractSegment> placeholderSegments;
    /**
     * The total length of all literal (fixed) text segments.
     */
    protected int fixedTextTotalLength;

    /**
     * Constructs a new {@code StringTemplate}.
     *
     * @param template            The string template.
     * @param escape              The escape character.
     * @param defaultValue        The default value for missing keys.
     * @param defaultValueHandler A handler for providing default values.
     * @param features            The feature flags.
     */
    protected StringTemplate(final String template, final char escape, final String defaultValue,
            final UnaryOperator<String> defaultValueHandler, final int features) {
        Assert.notNull(template, "String template cannot be null");
        this.template = template;
        this.escape = escape;
        this.defaultValue = defaultValue;
        this.defaultValueHandler = defaultValueHandler;
        this.features = features;
    }

    /**
     * Creates a builder for a template with single, unnamed placeholders (e.g., "{}", "?").
     *
     * @param template The string template.
     * @return a builder for a {@link SinglePlaceholderString}.
     */
    public static SinglePlaceholderString.Builder of(final String template) {
        return SinglePlaceholderString.builder(template);
    }

    /**
     * Creates a builder for a template with named placeholders (e.g., "{name}", "#{name}").
     *
     * @param template The string template.
     * @return a builder for a {@link NamedPlaceholderString}.
     */
    public static NamedPlaceholderString.Builder ofNamed(final String template) {
        return NamedPlaceholderString.builder(template);
    }

    /**
     * Sets the global default features for all new template objects.
     *
     * @param globalFeatures The global default features.
     */
    public static void setGlobalFeatures(final Feature... globalFeatures) {
        StringTemplate.globalFeatures = Feature.of(globalFeatures);
    }

    /**
     * Sets the global default value handler for all new template objects.
     *
     * @param globalDefaultValueHandler The global default value handler.
     */
    public static void setGlobalDefaultValue(final UnaryOperator<String> globalDefaultValueHandler) {
        StringTemplate.globalDefaultValueHandler = Objects.requireNonNull(globalDefaultValueHandler);
    }

    /**
     * Gets the raw template string.
     *
     * @return The template string.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Gets the feature flags for this template.
     *
     * @return The feature flags as an integer.
     */
    public int getFeatures() {
        return features;
    }

    /**
     * Checks if a given string structurally matches this template.
     *
     * @param text The string to check, which should have been generated by a format method.
     * @return {@code true} if the string matches the template structure.
     */
    public boolean isMatches(final String text) {
        if (StringKit.isEmpty(text)) {
            return false;
        }
        int startIdx = 0;
        boolean hasPlaceholder = false;
        for (final StringSegment segment : segments) {
            if (segment instanceof LiteralSegment) {
                String literalText = segment.getText();
                int findIdx = text.indexOf(literalText, startIdx);
                if (findIdx == -1) {
                    return false;
                }
                if (findIdx != startIdx && !hasPlaceholder) {
                    return false;
                }
                startIdx = findIdx + literalText.length();
                hasPlaceholder = false;
            } else {
                if (hasPlaceholder) {
                    throw new InternalException("Two adjacent placeholders cannot be reliably parsed.");
                }
                hasPlaceholder = true;
            }
        }
        return true;
    }

    /**
     * Gets a list of all placeholder variable names (e.g., "{}" -> "{}", "{name}" -> "name").
     *
     * @return A list of placeholder variable names.
     */
    public List<String> getPlaceholderVariableNames() {
        return this.placeholderSegments.stream().map(AbstractSegment::getPlaceholder).collect(Collectors.toList());
    }

    /**
     * Gets a list of the full text of all placeholders (e.g., "{}" -> "{}", "{name}" -> "{name}").
     *
     * @return A list of placeholder texts.
     */
    public List<String> getPlaceholderTexts() {
        return this.placeholderSegments.stream().map(AbstractSegment::getText).collect(Collectors.toList());
    }

    /**
     * Formats the template using raw string values provided by a key-based supplier function, without applying any
     * special feature handling.
     *
     * @param valueSupplier A function that provides a string value for a given placeholder variable name.
     * @return The formatted string.
     */
    public String formatRawByKey(final Function<String, String> valueSupplier) {
        return formatRawBySegment(segment -> valueSupplier.apply(segment.getPlaceholder()));
    }

    /**
     * Formats the template using raw string values provided by a segment-based supplier function, without applying any
     * special feature handling.
     *
     * @param valueSupplier A function that provides a string value for a given placeholder segment.
     * @return The formatted string.
     */
    public String formatRawBySegment(final Function<AbstractSegment, String> valueSupplier) {
        final List<String> values = new ArrayList<>(placeholderSegments.size());
        int totalTextLength = this.fixedTextTotalLength;

        for (final AbstractSegment segment : placeholderSegments) {
            String value = valueSupplier.apply(segment);
            if (value == null) {
                value = "null";
            }
            totalTextLength += value.length();
            values.add(value);
        }

        final StringBuilder sb = new StringBuilder(totalTextLength);
        int index = 0;
        for (final StringSegment segment : segments) {
            if (segment instanceof LiteralSegment) {
                sb.append(segment.getText());
            } else {
                sb.append(values.get(index++));
            }
        }
        return sb.toString();
    }

    /**
     * Formats the template by replacing placeholders sequentially with elements from an iterable.
     *
     * @param iterable The iterable providing the values.
     * @return The formatted string.
     */
    protected String formatSequence(final Iterable<?> iterable) {
        if (iterable == null) {
            return getTemplate();
        }
        final Iterator<?> iterator = iterable.iterator();
        return formatBySegment(segment -> iterator.hasNext() ? iterator.next() : formatMissingKey(segment));
    }

    /**
     * Formats the template by resolving values for each placeholder segment and applying feature-based logic (e.g.,
     * null handling).
     *
     * @param valueSupplier A function that provides a value for a given placeholder segment.
     * @return The formatted string.
     */
    protected String formatBySegment(final Function<AbstractSegment, ?> valueSupplier) {
        return formatRawBySegment(segment -> {
            final Object value = valueSupplier.apply(segment);
            return (value != null) ? StringKit.toString(value) : formatNullValue(segment);
        });
    }

    /**
     * Handles the case where a value for a placeholder is missing, based on the configured features.
     *
     * @param segment The placeholder segment for which a value is missing.
     * @return The string to use for the missing value.
     */
    protected String formatMissingKey(final AbstractSegment segment) {
        final int features = getFeatures();
        if (Feature.FORMAT_MISSING_KEY_PRINT_WHOLE_PLACEHOLDER.contains(features)) {
            return segment.getText();
        }
        if (Feature.FORMAT_MISSING_KEY_PRINT_DEFAULT_VALUE.contains(features)) {
            return getDefaultValue(segment);
        }
        if (Feature.FORMAT_MISSING_KEY_PRINT_NULL.contains(features)) {
            return "null";
        }
        if (Feature.FORMAT_MISSING_KEY_PRINT_EMPTY.contains(features)) {
            return "";
        }
        if (Feature.FORMAT_MISSING_KEY_PRINT_VARIABLE_NAME.contains(features)) {
            return segment.getPlaceholder();
        }
        if (Feature.FORMAT_MISSING_KEY_THROWS.contains(features)) {
            throw new InternalException("No value associated with placeholder: '{}'", segment.getPlaceholder());
        }
        throw new InternalException("No value for placeholder '{}' and no 'missing key' feature defined.",
                segment.getPlaceholder());
    }

    /**
     * Handles the case where the resolved value for a placeholder is null, based on the configured features.
     *
     * @param segment The placeholder segment.
     * @return The string to use for the null value.
     */
    protected String formatNullValue(final AbstractSegment segment) {
        final int features = getFeatures();
        if (Feature.FORMAT_NULL_VALUE_TO_STR.contains(features)) {
            return "null";
        }
        if (Feature.FORMAT_NULL_VALUE_TO_EMPTY.contains(features)) {
            return "";
        }
        if (Feature.FORMAT_NULL_VALUE_TO_WHOLE_PLACEHOLDER.contains(features)) {
            return segment.getText();
        }
        if (Feature.FORMAT_NULL_VALUE_TO_DEFAULT_VALUE.contains(features)) {
            return getDefaultValue(segment);
        }
        throw new InternalException(
                "A null value cannot be resolved for placeholder '{}'. Define a 'null value' feature.",
                segment.getPlaceholder());
    }

    /**
     * Parses a formatted string, consuming the raw string values for each placeholder.
     *
     * @param text             The string to parse.
     * @param keyValueConsumer A consumer for the placeholder variable name and its corresponding raw value.
     */
    public void matchesRawByKey(final String text, final BiConsumer<String, String> keyValueConsumer) {
        if (text == null || keyValueConsumer == null || CollKit.isEmpty(placeholderSegments)) {
            return;
        }
        matchesRawBySegment(text, (segment, value) -> keyValueConsumer.accept(segment.getPlaceholder(), value));
    }

    /**
     * Parses a formatted string, consuming the raw string values for each placeholder.
     *
     * @param text             The string to parse.
     * @param keyValueConsumer A consumer for the placeholder segment and its corresponding raw value.
     */
    public void matchesRawBySegment(final String text, final BiConsumer<AbstractSegment, String> keyValueConsumer) {
        if (text == null || keyValueConsumer == null || CollKit.isEmpty(placeholderSegments)) {
            return;
        }

        int startIdx = 0;
        AbstractSegment placeholderSegment = null;
        for (final StringSegment segment : segments) {
            if (segment instanceof LiteralSegment) {
                String literalText = segment.getText();
                int findIdx = text.indexOf(literalText, startIdx);
                if (findIdx == -1) {
                    return; // Mismatch
                }
                if (placeholderSegment != null) {
                    keyValueConsumer.accept(placeholderSegment, text.substring(startIdx, findIdx));
                } else if (findIdx != startIdx) {
                    return; // Mismatch
                }
                startIdx = findIdx + literalText.length();
                placeholderSegment = null;
            } else {
                if (placeholderSegment != null) {
                    throw new InternalException("Two adjacent placeholders cannot be reliably parsed.");
                }
                placeholderSegment = (AbstractSegment) segment;
            }
        }
        if (placeholderSegment != null) {
            keyValueConsumer.accept(placeholderSegment, text.substring(startIdx));
        }
    }

    /**
     * Parses a formatted string and returns a list of the values corresponding to each placeholder in sequence.
     *
     * @param text The string to parse, typically a result of the format methods.
     * @return A list of extracted values.
     */
    protected List<String> matchesSequence(final String text) {
        if (text == null || placeholderSegments.isEmpty() || !isMatches(text)) {
            return ListKit.zero();
        }
        final List<String> list = new ArrayList<>(placeholderSegments.size());
        matchesByKey(text, (segment, value) -> list.add(value));
        return list;
    }

    /**
     * Parses a formatted string, applying feature-based logic to the extracted values before consuming them.
     *
     * @param text             The string to parse.
     * @param keyValueConsumer A consumer for the placeholder variable and its final processed value.
     */
    public void matchesByKey(final String text, final BiConsumer<String, String> keyValueConsumer) {
        if (hasDefaultValue()) {
            matchesByKey(text, keyValueConsumer, true, this::getDefaultValue);
        } else {
            matchesByKey(text, keyValueConsumer, false, null);
        }
    }

    /**
     * Parses a formatted string, applying feature-based logic to the extracted values.
     *
     * @param text                 The string to parse.
     * @param keyValueConsumer     A consumer for the placeholder variable and its final processed value.
     * @param hasDefaultValue      Whether a default value is available.
     * @param defaultValueSupplier A supplier for the default value.
     */
    protected void matchesByKey(
            final String text,
            final BiConsumer<String, String> keyValueConsumer,
            final boolean hasDefaultValue,
            final Function<AbstractSegment, String> defaultValueSupplier) {
        if (text == null || keyValueConsumer == null || CollKit.isEmpty(placeholderSegments)) {
            return;
        }
        matchesRawBySegment(
                text,
                (segment, value) -> matchByKey(
                        keyValueConsumer,
                        segment.getPlaceholder(),
                        value,
                        hasDefaultValue,
                        () -> hasDefaultValue ? StringKit.toString(defaultValueSupplier.apply(segment)) : null));
    }

    /**
     * A helper method to process a single matched key-value pair according to the configured features.
     *
     * @param keyValueConsumer     The consumer for the final key-value pair.
     * @param key                  The placeholder variable.
     * @param value                The raw parsed value.
     * @param hasDefaultValue      Whether a default value is available.
     * @param defaultValueSupplier A supplier for the default value.
     */
    private void matchByKey(
            final BiConsumer<String, String> keyValueConsumer,
            final String key,
            final String value,
            final boolean hasDefaultValue,
            final Supplier<String> defaultValueSupplier) {
        final int features = getFeatures();

        if (hasDefaultValue && !Feature.MATCH_KEEP_DEFAULT_VALUE.contains(features)) {
            if (value.equals(defaultValueSupplier.get())) {
                if (Feature.MATCH_IGNORE_DEFAULT_VALUE.contains(features)) {
                    return;
                }
                if (Feature.MATCH_DEFAULT_VALUE_TO_NULL.contains(features)) {
                    keyValueConsumer.accept(key, null);
                    return;
                }
            }
        }

        if ("".equals(value)) {
            if (Feature.MATCH_EMPTY_VALUE_TO_NULL.contains(features)) {
                keyValueConsumer.accept(key, null);
            } else if (Feature.MATCH_EMPTY_VALUE_TO_DEFAULT_VALUE.contains(features)) {
                keyValueConsumer.accept(key, defaultValueSupplier.get());
            } else if (Feature.MATCH_IGNORE_EMPTY_VALUE.contains(features)) {
                // do nothing
            } else { // MATCH_KEEP_VALUE_EMPTY is the default
                keyValueConsumer.accept(key, value);
            }
            return;
        }

        if ("null".equals(value)) {
            if (Feature.MATCH_NULL_STR_TO_NULL.contains(features)) {
                keyValueConsumer.accept(key, null);
            } else if (Feature.MATCH_KEEP_NULL_STR.contains(features)) {
                keyValueConsumer.accept(key, value);
            } // else MATCH_IGNORE_NULL_STR, do nothing
            return;
        }

        keyValueConsumer.accept(key, value);
    }

    /**
     * Checks if any default value mechanism (fixed, handler, or global handler) is configured.
     *
     * @return {@code true} if a default value mechanism exists.
     */
    protected boolean hasDefaultValue() {
        return defaultValue != null || defaultValueHandler != null || globalDefaultValueHandler != null;
    }

    /**
     * Gets the default value for a given placeholder segment, checking instance-level, handler, and global handler
     * defaults in that order.
     *
     * @param segment The placeholder segment.
     * @return The default value.
     * @throws InternalException if no default value mechanism is configured.
     */
    protected String getDefaultValue(final AbstractSegment segment) {
        if (defaultValue != null) {
            return defaultValue;
        }
        if (defaultValueHandler != null) {
            return StringKit.toString(defaultValueHandler.apply(segment.getPlaceholder()));
        }
        if (globalDefaultValueHandler != null) {
            return StringKit.toString(globalDefaultValueHandler.apply(segment.getPlaceholder()));
        }
        throw new InternalException("No default value for placeholder: '{}'. Configure a default value mechanism.",
                segment.getPlaceholder());
    }

    /**
     * Performs common initialization tasks after the subclass constructor has finished.
     */
    protected void afterInit() {
        this.segments = new ArrayList<>(parseSegments(template));

        int literalSegmentSize = 0;
        int fixedTextLength = 0;
        for (final StringSegment segment : this.segments) {
            if (segment instanceof LiteralSegment) {
                ++literalSegmentSize;
                fixedTextLength += segment.getText().length();
            }
        }
        this.fixedTextTotalLength = fixedTextLength;

        final int placeholderSegmentsSize = segments.size() - literalSegmentSize;
        if (placeholderSegmentsSize == 0) {
            this.placeholderSegments = Collections.emptyList();
        } else {
            final List<AbstractSegment> placeholderSegments = new ArrayList<>(placeholderSegmentsSize);
            for (final StringSegment segment : segments) {
                if (segment instanceof AbstractSegment) {
                    placeholderSegments.add((AbstractSegment) segment);
                }
            }
            this.placeholderSegments = placeholderSegments;
        }
    }

    /**
     * Adds a literal text segment to the list, merging with the previous segment if it was also a literal.
     *
     * @param isLastLiteralSegment Whether the previously added segment was a literal.
     * @param list                 The list of segments.
     * @param newText              The new literal text to add.
     */
    protected void addLiteralSegment(
            final boolean isLastLiteralSegment,
            final List<StringSegment> list,
            final String newText) {
        if (newText.isEmpty()) {
            return;
        }
        if (isLastLiteralSegment) {
            final int lastIdx = list.size() - 1;
            final StringSegment lastLiteralSegment = list.get(lastIdx);
            list.set(lastIdx, new LiteralSegment(lastLiteralSegment.getText() + newText));
        } else {
            list.add(new LiteralSegment(newText));
        }
    }

    /**
     * Parses the template string into a list of segments. Must be implemented by subclasses.
     *
     * @param template The string template.
     * @return A list of {@link StringSegment}s.
     */
    protected abstract List<StringSegment> parseSegments(String template);

    /**
     * Gets the list of all segments in the template.
     *
     * @return The list of segments.
     */
    protected List<StringSegment> getSegments() {
        return segments;
    }

    /**
     * Gets the list of placeholder segments in the template.
     *
     * @return The list of placeholder segments.
     */
    protected List<AbstractSegment> getPlaceholderSegments() {
        return placeholderSegments;
    }

    /**
     * Features for controlling formatting and parsing behavior.
     */
    public enum Feature {

        /**
         * When formatting, if a value for a placeholder is missing, print the entire placeholder (e.g., "${name}").
         */
        FORMAT_MISSING_KEY_PRINT_WHOLE_PLACEHOLDER(0, 0, 6),
        /**
         * When formatting, if a value is missing, use the default value. Throws an exception if no default is
         * configured.
         */
        FORMAT_MISSING_KEY_PRINT_DEFAULT_VALUE(1, 0, 6),
        /**
         * When formatting, if a value is missing and no default is configured, print the string "null".
         */
        FORMAT_MISSING_KEY_PRINT_NULL(2, 0, 6),
        /**
         * When formatting, if a value is missing, print an empty string.
         */
        FORMAT_MISSING_KEY_PRINT_EMPTY(3, 0, 6),
        /**
         * When formatting, if a value is missing, print only the variable name (e.g., "name" from "${name}").
         */
        FORMAT_MISSING_KEY_PRINT_VARIABLE_NAME(4, 0, 6),
        /**
         * When formatting, if a value is missing, throw an exception.
         */
        FORMAT_MISSING_KEY_THROWS(5, 0, 6),

        /**
         * When formatting, if a resolved value is {@code null}, print the string "null".
         */
        FORMAT_NULL_VALUE_TO_STR(6, 6, 4),
        /**
         * When formatting, if a resolved value is {@code null}, print an empty string.
         */
        FORMAT_NULL_VALUE_TO_EMPTY(7, 6, 4),
        /**
         * When formatting, if a resolved value is {@code null}, print the entire placeholder (e.g., "${name}").
         */
        FORMAT_NULL_VALUE_TO_WHOLE_PLACEHOLDER(8, 6, 4),
        /**
         * When formatting, if a resolved value is {@code null}, use the default value.
         */
        FORMAT_NULL_VALUE_TO_DEFAULT_VALUE(9, 6, 4),

        /**
         * When parsing, if a parsed value matches the default value, keep it.
         */
        MATCH_KEEP_DEFAULT_VALUE(16, 16, 3),
        /**
         * When parsing, if a parsed value matches the default value, ignore it (do not include it in the result map).
         */
        MATCH_IGNORE_DEFAULT_VALUE(17, 16, 3),
        /**
         * When parsing, if a parsed value matches the default value, convert it to {@code null}.
         */
        MATCH_DEFAULT_VALUE_TO_NULL(18, 16, 3),

        /**
         * When parsing, if a value is an empty string, convert it to {@code null}.
         */
        MATCH_EMPTY_VALUE_TO_NULL(19, 19, 4),
        /**
         * When parsing, if a value is an empty string, convert it to the default value.
         */
        MATCH_EMPTY_VALUE_TO_DEFAULT_VALUE(20, 19, 4),
        /**
         * When parsing, if a value is an empty string, ignore it.
         */
        MATCH_IGNORE_EMPTY_VALUE(21, 19, 4),
        /**
         * When parsing, if a value is an empty string, keep it as an empty string.
         */
        MATCH_KEEP_VALUE_EMPTY(22, 19, 4),

        /**
         * When parsing, if a value is the string "null", convert it to {@code null}.
         */
        MATCH_NULL_STR_TO_NULL(23, 23, 3),
        /**
         * When parsing, if a value is the string "null", keep it as the string "null".
         */
        MATCH_KEEP_NULL_STR(24, 23, 3),
        /**
         * When parsing, if a value is the string "null", ignore it.
         */
        MATCH_IGNORE_NULL_STR(25, 23, 3);

        /**
         * The bitmask for this feature.
         */
        private final int mask;
        /**
         * The bitmask to clear other features in the same group.
         */
        private final int clearMask;

        Feature(final int bitPos, final int bitStart, final int bitLen) {
            this.mask = 1 << bitPos;
            this.clearMask = (-1 << (bitStart + bitLen)) | ((1 << bitStart) - 1);
        }

        /**
         * Combines multiple features into a single integer flag.
         *
         * @param features The features to combine.
         * @return The combined feature flag.
         */
        public static int of(final Feature... features) {
            if (features == null) {
                return 0;
            }
            int value = 0;
            for (final Feature feature : features) {
                value = feature.set(value);
            }
            return value;
        }

        /**
         * Checks if this feature is present in the given feature flag.
         *
         * @param features The feature flag to check.
         * @return {@code true} if this feature is present.
         */
        public boolean contains(final int features) {
            return (features & mask) != 0;
        }

        /**
         * Adds this feature to a feature flag, clearing any conflicting features in the same group.
         *
         * @param features The existing feature flag.
         * @return The updated feature flag.
         */
        public int set(final int features) {
            return (features & clearMask) | mask;
        }

        /**
         * Removes this feature from a feature flag.
         *
         * @param features The existing feature flag.
         * @return The updated feature flag.
         */
        public int clear(final int features) {
            return (features & ~mask);
        }
    }

    /**
     * An abstract builder for creating {@link StringTemplate} instances.
     *
     * @param <B> The type of the concrete builder subclass.
     * @param <T> The type of the concrete template subclass.
     */
    protected static abstract class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends StringTemplate> {

        /**
         * The raw template string.
         */
        protected final String template;
        /**
         * The default value for missing placeholders.
         */
        protected String defaultValue;
        /**
         * A handler for providing default values dynamically.
         */
        protected UnaryOperator<String> defaultValueHandler;
        /**
         * Whether the escape character has been explicitly set.
         */
        protected boolean escape$set;
        /**
         * The escape character.
         */
        protected char escape;
        /**
         * The feature flags for the template.
         */
        protected int features;

        /**
         * Constructs a new builder.
         *
         * @param template The string template.
         */
        protected AbstractBuilder(final String template) {
            this.template = Objects.requireNonNull(template);
            this.features = StringTemplate.globalFeatures;
        }

        /**
         * Sets the escape character.
         *
         * @param escape The escape character.
         * @return this builder instance for chaining.
         */
        public B escape(final char escape) {
            this.escape = escape;
            this.escape$set = true;
            return self();
        }

        /**
         * Sets new features, completely overwriting any existing ones.
         *
         * @param newFeatures The new features.
         * @return this builder instance for chaining.
         */
        public B features(final Feature... newFeatures) {
            this.features = Feature.of(newFeatures);
            return self();
        }

        /**
         * Adds new features to the existing set.
         *
         * @param appendFeatures The features to add.
         * @return this builder instance for chaining.
         */
        public B addFeatures(final Feature... appendFeatures) {
            if (ArrayKit.isNotEmpty(appendFeatures)) {
                for (final Feature feature : appendFeatures) {
                    this.features = feature.set(this.features);
                }
            }
            return self();
        }

        /**
         * Removes features from the existing set.
         *
         * @param removeFeatures The features to remove.
         * @return this builder instance for chaining.
         */
        public B removeFeatures(final Feature... removeFeatures) {
            if (ArrayKit.isNotEmpty(removeFeatures)) {
                for (final Feature feature : removeFeatures) {
                    this.features = feature.clear(this.features);
                }
            }
            return self();
        }

        /**
         * Sets a fixed default value for missing placeholders.
         *
         * @param defaultValue The default value.
         * @return this builder instance for chaining.
         */
        public B defaultValue(final String defaultValue) {
            this.defaultValue = Objects.requireNonNull(defaultValue);
            return self();
        }

        /**
         * Sets a handler for providing default values dynamically.
         *
         * @param defaultValueHandler A function that takes a placeholder variable name and returns a default value.
         * @return this builder instance for chaining.
         */
        public B defaultValue(final UnaryOperator<String> defaultValueHandler) {
            this.defaultValueHandler = Objects.requireNonNull(defaultValueHandler);
            return self();
        }

        /**
         * Builds the final template object.
         *
         * @return The new template instance.
         */
        public T build() {
            if (!this.escape$set) {
                this.escape = Symbol.C_BACKSLASH;
            }
            return buildInstance();
        }

        /**
         * Returns this builder instance, cast to the concrete subclass type.
         *
         * @return this builder instance.
         */
        protected abstract B self();

        /**
         * Creates and returns the final template instance.
         *
         * @return The new template instance.
         */
        protected abstract T buildInstance();
    }

}
