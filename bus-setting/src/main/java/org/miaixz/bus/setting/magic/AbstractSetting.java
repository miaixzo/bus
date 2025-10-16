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
package org.miaixz.bus.setting.magic;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;

import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.getter.GroupedTypeGetter;
import org.miaixz.bus.core.lang.getter.TypeGetter;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

/**
 * An abstract base class for settings, providing common functionality for accessing configuration values. It implements
 * getter interfaces for retrieving typed and grouped values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSetting
        implements TypeGetter<CharSequence>, GroupedTypeGetter<CharSequence, CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852777511686L;

    /**
     * The default delimiter for array-type values.
     */
    public static final String DEFAULT_DELIMITER = Symbol.COMMA;
    /**
     * The default group name for settings.
     */
    public static final String DEFAULT_GROUP = Normal.EMPTY;

    /**
     * Default constructor.
     */
    protected AbstractSetting() {
    }

    @Override
    public Object getObject(final CharSequence key, final Object defaultValue) {
        return ObjectKit.defaultIfNull(getObjectByGroup(key, DEFAULT_GROUP), defaultValue);
    }

    /**
     * Gets a value using a lambda method reference to resolve the property name and return type.
     *
     * @param func The method reference (e.g., {@code User::getName}).
     * @param <P>  The type of the class containing the method.
     * @param <T>  The return type of the method.
     * @return The value of the property corresponding to the method name.
     */
    public <P, T> T get(final FunctionX<P, T> func) {
        final LambdaX lambdaX = LambdaKit.resolve(func);
        return get(lambdaX.getFieldName(), lambdaX.getReturnType());
    }

    /**
     * Gets a non-empty string value from a specific group.
     *
     * @param key          The key of the setting.
     * @param group        The group name.
     * @param defaultValue The default value to return if the setting is null or empty.
     * @return The setting value, or the default value.
     */
    public String getByGroupNotEmpty(final String key, final String group, final String defaultValue) {
        final String value = getStringByGroup(key, group);
        return StringKit.defaultIfEmpty(value, defaultValue);
    }

    /**
     * Gets a value as a string array from the default group, split by the default delimiter (',').
     *
     * @param key The key of the setting.
     * @return The value as a string array, or null if not found.
     */
    public String[] getStrs(final String key) {
        return getStrs(key, null);
    }

    /**
     * Gets a value as a string array from the default group, split by the default delimiter (',').
     *
     * @param key          The key of the setting.
     * @param defaultValue The default value to return if the setting is not found.
     * @return The value as a string array, or the default value.
     */
    public String[] getStrs(final CharSequence key, final String[] defaultValue) {
        String[] value = getStrsByGroup(key, null);
        return ObjectKit.defaultIfNull(value, defaultValue);
    }

    /**
     * Gets a value from a specific group as a string array, split by the default delimiter (','). For example, a
     * setting like {@code key = a,b,c} would result in {@code ["a", "b", "c"]}.
     *
     * @param key   The key of the setting.
     * @param group The group name.
     * @return The value as a string array, or null if not found.
     */
    public String[] getStrsByGroup(final CharSequence key, final CharSequence group) {
        return getStrsByGroup(key, group, DEFAULT_DELIMITER);
    }

    /**
     * Gets a value from a specific group as a string array, split by a custom delimiter.
     *
     * @param key       The key of the setting.
     * @param group     The group name.
     * @param delimiter The delimiter to split the string by.
     * @return The value as a string array, or null if not found or blank.
     */
    public String[] getStrsByGroup(final CharSequence key, final CharSequence group, final CharSequence delimiter) {
        final String value = getStringByGroup(key, group);
        if (StringKit.isBlank(value)) {
            return null;
        }
        return CharsBacker.splitToArray(value, delimiter.toString());
    }

    /**
     * Maps the settings from a specific group to an existing Java Bean object by calling its setters. Only basic type
     * conversions are supported.
     *
     * @param <T>   The type of the bean.
     * @param group The group name whose settings will be mapped.
     * @param bean  The Java Bean object to populate.
     * @return The populated Java Bean object.
     */
    public <T> T toBean(final CharSequence group, final T bean) {
        return BeanKit.fillBean(bean, new ValueProvider<>() {

            @Override
            public Object value(final String key, final Type valueType) {
                return getObjectByGroup(key, group);
            }

            @Override
            public boolean containsKey(final String key) {
                return null != getObjectByGroup(key, group);
            }
        }, CopyOptions.of());
    }

    /**
     * Maps the settings from a specific group to a new Java Bean object.
     *
     * @param <T>       The type of the bean.
     * @param group     The group name whose settings will be mapped.
     * @param beanClass The class of the Java Bean to create and populate.
     * @return The newly created and populated Java Bean object.
     */
    public <T> T toBean(final CharSequence group, final Class<T> beanClass) {
        return toBean(group, ReflectKit.newInstanceIfPossible(beanClass));
    }

    /**
     * Maps the settings from the default group to an existing Java Bean object.
     *
     * @param <T>  The type of the bean.
     * @param bean The Java Bean object to populate.
     * @return The populated Java Bean object.
     */
    public <T> T toBean(final T bean) {
        return toBean(null, bean);
    }

    /**
     * Maps the settings from the default group to a new Java Bean object.
     *
     * @param <T>       The type of the bean.
     * @param beanClass The class of the Java Bean to create and populate.
     * @return The newly created and populated Java Bean object.
     */
    public <T> T toBean(final Class<T> beanClass) {
        return toBean(null, beanClass);
    }

}
