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
package org.miaixz.bus.core.bean.copier;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.bean.desc.BeanDesc;
import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.LambdaKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Options for configuring property copy operations.
 * <p>
 * This class allows for customization of how properties are copied, including:
 * <ul>
 * <li>Restricting the copy to properties of a specific class or interface.</li>
 * <li>Ignoring null values from the source.</li>
 * <li>Ignoring specific properties by name.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CopyOptions implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852291736328L;

    /**
     * The class or interface to restrict the copy to. Only properties declared in this class or its
     * superclasses/interfaces will be copied.
     */
    protected Class<?> editable;
    /**
     * Whether to ignore null values from the source object. If true, null values are not copied.
     */
    protected boolean ignoreNullValue;
    /**
     * Whether to ignore errors that occur during the copy process (e.g., type conversion errors).
     */
    protected boolean ignoreError;
    /**
     * Whether to ignore case when matching property names.
     */
    protected boolean ignoreCase;
    /**
     * An editor for field names and values, allowing for custom transformation rules (e.g., converting camelCase to
     * snake_case).
     */
    protected UnaryOperator<MutableEntry<Object, Object>> fieldEditor;
    /**
     * Whether to ignore fields marked with the {@code transient} keyword or {@code @Transient} annotation.
     */
    protected boolean transientSupport = true;
    /**
     * Whether to override the destination's existing values. If false, a value is only copied if the corresponding
     * property in the destination is null.
     */
    protected boolean override = true;
    /**
     * Whether to automatically convert property names to camelCase for matching. This helps resolve mismatches between
     * map keys (e.g., snake_case) and bean property names (camelCase).
     */
    protected boolean autoTransCamelCase = true;
    /**
     * A custom type converter. If not set, the global {@link Convert} utility is used.
     */
    protected Converter converter = (type, value) -> Convert.convertWithCheck(type, value, null, ignoreError);
    /**
     * The custom {@link BeanDesc} class to use for bean analysis.
     */
    protected Class<BeanDesc> beanDescClass;
    /**
     * A filter to determine which properties should be copied.
     */
    private BiPredicate<Field, Object> propertiesFilter;

    /**
     * Default constructor.
     */
    public CopyOptions() {

    }

    /**
     * Constructs new CopyOptions.
     *
     * @param editable         The class or interface to restrict properties to.
     * @param ignoreNullValue  Whether to ignore null values.
     * @param ignoreProperties An array of property names to ignore.
     */
    public CopyOptions(final Class<?> editable, final boolean ignoreNullValue, final String... ignoreProperties) {
        this.propertiesFilter = (f, v) -> true;
        this.editable = editable;
        this.ignoreNullValue = ignoreNullValue;
        this.setIgnoreProperties(ignoreProperties);
    }

    /**
     * Creates new default {@code CopyOptions}.
     *
     * @return A new {@code CopyOptions} instance.
     */
    public static CopyOptions of() {
        return new CopyOptions();
    }

    /**
     * Creates new {@code CopyOptions} with the specified settings.
     *
     * @param editable         The class or interface to restrict properties to.
     * @param ignoreNullValue  Whether to ignore null values.
     * @param ignoreProperties An array of property names to ignore.
     * @return A new {@code CopyOptions} instance.
     */
    public static CopyOptions of(
            final Class<?> editable,
            final boolean ignoreNullValue,
            final String... ignoreProperties) {
        return new CopyOptions(editable, ignoreNullValue, ignoreProperties);
    }

    /**
     * Sets the class or interface to restrict the copy to.
     *
     * @param editable The class or interface.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setEditable(final Class<?> editable) {
        this.editable = editable;
        return this;
    }

    /**
     * Sets whether to ignore null values from the source.
     *
     * @param ignoreNullValue If true, null values are not copied.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setIgnoreNullValue(final boolean ignoreNullValue) {
        this.ignoreNullValue = ignoreNullValue;
        return this;
    }

    /**
     * Sets a filter to determine which properties should be copied.
     *
     * @param propertiesFilter The property filter predicate.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setPropertiesFilter(final BiPredicate<Field, Object> propertiesFilter) {
        this.propertiesFilter = propertiesFilter;
        return this;
    }

    /**
     * Sets a list of property names to ignore during the copy.
     *
     * @param ignoreProperties The names of the properties to ignore.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setIgnoreProperties(final String... ignoreProperties) {
        return setPropertiesFilter((field, o) -> {
            if (ignoreCase) {
                return !ArrayKit.containsIgnoreCase(ignoreProperties, field.getName());
            }
            return !ArrayKit.contains(ignoreProperties, field.getName());
        });
    }

    /**
     * Sets a list of property names to ignore, specified via lambda method references.
     *
     * @param <P>   The type of the class containing the method.
     * @param <R>   The return type of the method.
     * @param funcs The lambda method references (e.g., {@code User::getName}).
     * @return This {@code CopyOptions} instance for chaining.
     */
    @SafeVarargs
    public final <P, R> CopyOptions setIgnoreProperties(final FunctionX<P, R>... funcs) {
        final Set<String> ignoreProperties = ArrayKit.mapToSet(funcs, LambdaKit::getFieldName);
        return setPropertiesFilter((field, o) -> !ignoreProperties.contains(field.getName()));
    }

    /**
     * Sets whether to ignore errors that occur during property setting.
     *
     * @param ignoreError If true, errors are ignored.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setIgnoreError(final boolean ignoreError) {
        this.ignoreError = ignoreError;
        return this;
    }

    /**
     * A convenience method to enable ignoring errors.
     *
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions ignoreError() {
        return setIgnoreError(true);
    }

    /**
     * Sets whether to ignore case when matching property names.
     *
     * @param ignoreCase If true, property names are matched case-insensitively.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setIgnoreCase(final boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    /**
     * A convenience method to enable case-insensitive property matching.
     *
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions ignoreCase() {
        return setIgnoreCase(true);
    }

    /**
     * Sets a mapping between source and destination property names.
     *
     * @param fieldMapping A map where keys are source property names and values are destination property names.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setFieldMapping(final Map<?, ?> fieldMapping) {
        return setFieldEditor(entry -> {
            final Object key = entry.getKey();
            final Object keyMapped = fieldMapping.get(key);
            entry.setKey(null == keyMapped ? key : keyMapped);
            return entry;
        });
    }

    /**
     * Sets a field editor to customize property name and value transformations.
     *
     * @param editor A function that edits a mutable entry of (key, value).
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setFieldEditor(final UnaryOperator<MutableEntry<Object, Object>> editor) {
        this.fieldEditor = editor;
        return this;
    }

    /**
     * Edits a field's key and value using the configured editor.
     *
     * @param key   The field name.
     * @param value The field value.
     * @return The edited entry.
     */
    protected MutableEntry<Object, Object> editField(final Object key, final Object value) {
        final MutableEntry<Object, Object> entry = new MutableEntry<>(key, value);
        return (null != this.fieldEditor) ? this.fieldEditor.apply(entry) : entry;
    }

    /**
     * Sets whether to support the {@code transient} keyword and {@code @Transient} annotation.
     *
     * @param transientSupport If true, transient fields will be ignored.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setTransientSupport(final boolean transientSupport) {
        this.transientSupport = transientSupport;
        return this;
    }

    /**
     * Sets whether to override existing values in the destination object.
     *
     * @param override If false, values are only copied if the destination property is null.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setOverride(final boolean override) {
        this.override = override;
        return this;
    }

    /**
     * Sets whether to automatically convert non-camelCase property names to camelCase for matching.
     *
     * @param autoTransCamelCase If true, enables automatic conversion.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setAutoTransCamelCase(final boolean autoTransCamelCase) {
        this.autoTransCamelCase = autoTransCamelCase;
        return this;
    }

    /**
     * Sets a custom type converter.
     *
     * @param converter The custom converter.
     * @return This {@code CopyOptions} instance for chaining.
     */
    public CopyOptions setConverter(final Converter converter) {
        this.converter = converter;
        return this;
    }

    /**
     * Converts a field value to the target type using the configured converter.
     *
     * @param targetType The target type to convert to.
     * @param fieldValue The original field value.
     * @return The converted field value.
     */
    protected Object convertField(final Type targetType, final Object fieldValue) {
        return (null != this.converter) ? this.converter.convert(targetType, fieldValue) : fieldValue;
    }

    /**
     * Tests if a property should be copied based on the configured filter.
     *
     * @param field The field being considered.
     * @param value The value of the field.
     * @return {@code true} if the property should be copied, {@code false} otherwise.
     */
    protected boolean testPropertyFilter(final Field field, final Object value) {
        return null == this.propertiesFilter || this.propertiesFilter.test(field, value);
    }

    /**
     * Finds the corresponding property descriptor in the target bean for a given source key. This method attempts a
     * direct match, and if that fails and auto-camelCase is enabled, it will try matching a camelCase version of the
     * key.
     *
     * @param targetPropDescMap A map of property descriptors for the target bean.
     * @param sourceKey         The source key or field name.
     * @return The matching {@link PropDesc}, or null if no match is found.
     */
    protected PropDesc findPropDesc(final Map<String, PropDesc> targetPropDescMap, final String sourceKey) {
        PropDesc propDesc = targetPropDescMap.get(sourceKey);
        if (null == propDesc && this.autoTransCamelCase) {
            final String camelCaseKey = StringKit.toCamelCase(sourceKey);
            if (!StringKit.equals(sourceKey, camelCaseKey)) {
                propDesc = targetPropDescMap.get(camelCaseKey);
            }
        }
        return propDesc;
    }

}
