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
package org.miaixz.bus.core;

import java.lang.reflect.Field;
import java.util.*;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.io.resource.PropertySource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Ignore;
import org.miaixz.bus.core.lang.annotation.Values;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.JdkProxy;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.RequiredArgsConstructor;

/**
 * A utility for binding properties from a {@link PropertySource} to an object or class. It can also resolve
 * placeholders in string values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@RequiredArgsConstructor
public class Binder {

    /**
     * The default placeholder prefix: "${".
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = Symbol.DOLLAR + Symbol.BRACE_LEFT;

    /**
     * The default placeholder suffix: "}".
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = Symbol.BRACE_RIGHT;

    /**
     * The default value separator: ":".
     */
    public static final String DEFAULT_VALUE_SEPARATOR = Symbol.COLON;

    /**
     * A default instance of the binder with standard configurations.
     */
    public static final Binder DEFAULT_HELPER;

    /**
     * A map of simple prefixes for handling nested placeholders.
     */
    private static final Map<String, String> SIMPLE_PREFIXES = new HashMap<>(4);

    static {
        SIMPLE_PREFIXES.put(Symbol.BRACE_RIGHT, Symbol.BRACE_LEFT);
        SIMPLE_PREFIXES.put(Symbol.BRACKET_RIGHT, Symbol.BRACKET_LEFT);
        SIMPLE_PREFIXES.put(Symbol.PARENTHESE_RIGHT, Symbol.PARENTHESE_LEFT);

        DEFAULT_HELPER = new Binder(DEFAULT_PLACEHOLDER_PREFIX, DEFAULT_PLACEHOLDER_SUFFIX, DEFAULT_VALUE_SEPARATOR,
                true);
    }

    /**
     * The prefix for placeholders.
     */
    private final String placeholderPrefix;
    /**
     * The suffix for placeholders.
     */
    private final String placeholderSuffix;
    /**
     * The simple prefix for nested placeholders.
     */
    private final String simplePrefix;
    /**
     * The separator for default values in placeholders.
     */
    private final String valueSeparator;
    /**
     * A flag to indicate whether to ignore unresolvable placeholders.
     */
    private final boolean ignoreUnresolvablePlaceholders;
    /**
     * The source of properties.
     */
    private PropertySource source;

    /**
     * Constructs a new binder with the specified placeholder prefix and suffix.
     *
     * @param placeholderPrefix The prefix for placeholders (e.g., "${").
     * @param placeholderSuffix The suffix for placeholders (e.g., "}").
     */
    public Binder(String placeholderPrefix, String placeholderSuffix) {
        this(placeholderPrefix, placeholderSuffix, null, true);
    }

    /**
     * Constructs a new binder with detailed configuration.
     *
     * @param placeholderPrefix              The prefix for placeholders.
     * @param placeholderSuffix              The suffix for placeholders.
     * @param valueSeparator                 The separator for default values.
     * @param ignoreUnresolvablePlaceholders {@code true} to ignore unresolvable placeholders, {@code false} to throw an
     *                                       exception.
     */
    public Binder(String placeholderPrefix, String placeholderSuffix, String valueSeparator,
            boolean ignoreUnresolvablePlaceholders) {

        Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
        Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = SIMPLE_PREFIXES.get(this.placeholderSuffix);
        if (null != simplePrefixForSuffix && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        } else {
            this.simplePrefix = this.placeholderPrefix;
        }
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    /**
     * Checks if a substring matches the text at a given index.
     *
     * @param text      The text to check.
     * @param index     The starting index.
     * @param substring The substring to match.
     * @return {@code true} if it matches, {@code false} otherwise.
     */
    private static boolean substringMatch(CharSequence text, int index, CharSequence substring) {
        if (index + substring.length() > text.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (text.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Binds properties to a new instance of the specified class.
     *
     * @param clazz The class to instantiate and bind.
     * @param <T>   The type of the object.
     * @return The newly created and bound object.
     */
    public <T> T bind(Class<T> clazz) {
        return bind(clazz, Symbol.DOT);
    }

    /**
     * Binds properties with a specific prefix to a new instance of the specified class.
     *
     * @param clazz  The class to instantiate and bind.
     * @param prefix The prefix for the properties to bind.
     * @param <T>    The type of the object.
     * @return The newly created and bound object.
     */
    public <T> T bind(Class<T> clazz, String prefix) {
        T object;
        try {
            object = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InternalException(e);
        }

        Class<?> actualClass = JdkProxy.getCglibActualClass(clazz);
        boolean b = (null == prefix || Symbol.DOT.equals(prefix)) && actualClass.isAnnotationPresent(Values.class);
        if (b) {
            prefix = actualClass.getAnnotation(Values.class).value();
        }
        return clazz.cast(bind(object, prefix));
    }

    /**
     * Binds properties with a specific prefix to an existing object instance.
     *
     * @param object The object instance to bind properties to.
     * @param prefix The prefix for the properties to bind.
     * @param <T>    The type of the object.
     * @return The bound object.
     */
    public <T> T bind(T object, String prefix) {
        if (!StringKit.hasText(prefix) || Symbol.DOT.equals(prefix)) {
            prefix = null;
        }
        for (Field field : FieldKit.getFields(object.getClass())) {
            bindField(object, field, prefix);
        }
        return object;
    }

    /**
     * Binds a property to a specific field of an object.
     *
     * @param object The target object.
     * @param field  The field to bind.
     * @param prefix The property prefix.
     */
    private void bindField(Object object, Field field, String prefix) {
        if (field.isAnnotationPresent(Ignore.class)) {
            return;
        }
        String key = field.getName();
        boolean wrap = false;
        if (field.isAnnotationPresent(Values.class)) {
            key = field.getAnnotation(Values.class).value();
            wrap = true;
        } else if (null != prefix) {
            key = prefix + Symbol.DOT + key;
        }
        Object value = getProperty(key, field.getType(), wrap);
        if (null != value) {
            FieldKit.setFieldValue(object, field, value);
            return;
        }
        if (!(null == field.getType().getClassLoader()) && source.containPrefix(key + Symbol.DOT)) {
            value = FieldKit.getFieldValue(object, field);
            if (null == value) {
                value = bind(field.getType(), key);
                FieldKit.setFieldValue(object, field, value);
            } else {
                bind(value, key);
            }
        }
    }

    /**
     * Gets a property from the source and converts it to the specified type.
     *
     * @param key  The property key.
     * @param type The target type.
     * @param wrap Whether the property is wrapped.
     * @return The converted property value, or {@code null} if not found.
     */
    private Object getProperty(String key, Class<?> type, boolean wrap) {
        String value;
        if (wrap) {
            value = source.getPlaceholderProperty(key);
        } else {
            value = source.getProperty(key);
        }
        if (null == value || value.getClass() == type) {
            return value;
        }
        return Convert.convert(type, value);
    }

    /**
     * Replaces all placeholders of the form {@code ${name}} in the given value.
     *
     * @param value      The string to resolve.
     * @param properties The properties to use for replacement.
     * @return The resolved string.
     * @throws IllegalArgumentException if a circular placeholder reference is detected.
     */
    public String replacePlaceholders(String value, final Properties properties) {
        Assert.notNull(properties, "'properties' must not be null");
        Assert.notNull(value, "'value' must not be null");
        return parseStringValue(value, properties, new HashSet<>());
    }

    /**
     * Parses the given string value, replacing placeholders with their corresponding values from the properties.
     *
     * @param value               The string to parse.
     * @param properties          The properties to use for replacement.
     * @param visitedPlaceholders A set of already visited placeholders to detect circular references.
     * @return The parsed string with placeholders replaced.
     * @throws IllegalArgumentException if a circular placeholder reference is detected.
     */
    protected String parseStringValue(String value, Properties properties, Set<String> visitedPlaceholders) {
        StringBuilder result = new StringBuilder(value);
        int startIndex = value.indexOf(this.placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex != -1) {
                String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException(
                            "Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
                }
                // Recursively parse the placeholder.
                placeholder = parseStringValue(placeholder, properties, visitedPlaceholders);
                // Get the property value without placeholders.
                String propVal = properties.getProperty(placeholder);
                propVal = getCommonVal(properties, placeholder, propVal);
                if (null != propVal) {
                    // Recursively parse expressions within expressions.
                    propVal = parseStringValue(propVal, properties, visitedPlaceholders);
                    result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                    startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                } else if (this.ignoreUnresolvablePlaceholders) {
                    // Continue parsing.
                    startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                } else {
                    throw new IllegalArgumentException(
                            String.format("Could not resolve placeholder '%s' in value '%s'", placeholder, value));
                }
                visitedPlaceholders.remove(originalPlaceholder);
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    /**
     * Gets a common value, handling default values in placeholders.
     *
     * @param properties  The properties.
     * @param placeholder The placeholder.
     * @param propVal     The property value.
     * @return The resolved property value.
     */
    private String getCommonVal(Properties properties, String placeholder, String propVal) {
        if (null == propVal && null != this.valueSeparator) {
            int separatorIndex = placeholder.indexOf(this.valueSeparator);
            if (separatorIndex != -1) {
                String actualPlaceholder = placeholder.substring(0, separatorIndex);
                String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                propVal = properties.getProperty(actualPlaceholder);
                if (null == propVal) {
                    propVal = defaultValue;
                }
            }
        }
        return propVal;
    }

    /**
     * Finds the end index of a placeholder, handling nested placeholders.
     *
     * @param buf        The buffer to search in.
     * @param startIndex The starting index.
     * @return The end index, or -1 if not found.
     */
    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            } else if (substringMatch(buf, index, this.simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + this.simplePrefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }

}
