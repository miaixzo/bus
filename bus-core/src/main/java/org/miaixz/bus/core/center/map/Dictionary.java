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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.lang.reflect.Type;
import java.util.*;

import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.bean.path.BeanPath;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CloneException;
import org.miaixz.bus.core.lang.getter.TypeGetter;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.LambdaKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * A flexible and powerful dictionary class that extends {@link CustomKeyMap} and implements {@link TypeGetter}. It
 * serves as a versatile data carrier, offering type-safe getters, seamless bean conversion, and support for
 * case-insensitive keys.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dictionary extends CustomKeyMap<String, Object> implements TypeGetter<String> {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852273032017L;
    /**
     * Flag indicating whether keys in this dictionary are case-insensitive.
     */
    protected boolean caseInsensitive;

    /**
     * Constructs an empty, case-sensitive {@code Dictionary}.
     */
    public Dictionary() {
        this(false);
    }

    /**
     * Constructs an empty {@code Dictionary} with a specified case-insensitivity setting.
     *
     * @param caseInsensitive If {@code true}, keys will be treated as case-insensitive (stored as lowercase).
     */
    public Dictionary(final boolean caseInsensitive) {
        this(Normal._16, caseInsensitive);
    }

    /**
     * Constructs an empty, case-sensitive {@code Dictionary} with a specified initial capacity.
     *
     * @param initialCapacity The initial capacity of the map.
     */
    public Dictionary(final int initialCapacity) {
        this(initialCapacity, false);
    }

    /**
     * Constructs an empty {@code Dictionary} with a specified initial capacity and case-insensitivity.
     *
     * @param initialCapacity The initial capacity of the map.
     * @param caseInsensitive If {@code true}, keys will be treated as case-insensitive.
     */
    public Dictionary(final int initialCapacity, final boolean caseInsensitive) {
        this(initialCapacity, Normal.DEFAULT_LOAD_FACTOR, caseInsensitive);
    }

    /**
     * Constructs an empty, case-sensitive {@code Dictionary} with a specified capacity and load factor.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     */
    public Dictionary(final int initialCapacity, final float loadFactor) {
        this(initialCapacity, loadFactor, false);
    }

    /**
     * Constructs an empty {@code Dictionary} with a specified capacity, load factor, and case-insensitivity.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @param caseInsensitive If {@code true}, keys will be treated as case-insensitive.
     */
    public Dictionary(final int initialCapacity, final float loadFactor, final boolean caseInsensitive) {
        super(new LinkedHashMap<>(initialCapacity, loadFactor));
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Constructs a {@code Dictionary} from an existing {@link Map}.
     *
     * @param m The map to initialize the dictionary with. If {@code null}, an empty map is created.
     */
    public Dictionary(final Map<String, Object> m) {
        super((null == m) ? new HashMap<>() : m);
    }

    /**
     * Creates a new, empty {@code Dictionary} instance.
     *
     * @return A new {@code Dictionary}.
     */
    public static Dictionary of() {
        return new Dictionary();
    }

    /**
     * Creates a {@code Dictionary} from a Java Bean, using its properties as key-value pairs.
     *
     * @param <T>  The type of the bean.
     * @param bean The bean to convert.
     * @return A new {@code Dictionary} populated with the bean's properties.
     */
    public static <T> Dictionary parse(final T bean) {
        return of().parseBean(bean);
    }

    /**
     * Creates a {@code Dictionary} from an array of {@link Map.Entry} objects.
     *
     * @param pairs An array of key-value entries.
     * @return A new {@code Dictionary} populated with the given entries.
     */
    @SafeVarargs
    public static Dictionary ofEntries(final Map.Entry<String, Object>... pairs) {
        final Dictionary dictionary = of();
        for (final Map.Entry<String, Object> pair : pairs) {
            dictionary.put(pair.getKey(), pair.getValue());
        }
        return dictionary;
    }

    /**
     * Creates a {@code Dictionary} from an array of alternating keys and values. Keys are converted to strings.
     * <p>
     * Example: {@code Dictionary.ofKvs("name", "John", "age", 30)}
     *
     * @param keysAndValues An array of alternating keys and values.
     * @return A new {@code Dictionary} populated with the key-value pairs.
     */
    public static Dictionary ofKvs(final Object... keysAndValues) {
        final Dictionary dictionary = of();
        String key = null;
        for (int i = 0; i < keysAndValues.length; i++) {
            if ((i & 1) == 0) { // Even index is key
                key = Convert.toString(keysAndValues[i]);
            } else { // Odd index is value
                dictionary.put(key, keysAndValues[i]);
            }
        }
        return dictionary;
    }

    /**
     * Populates a Java Bean with the values from this dictionary.
     *
     * @param <T>  The type of the bean.
     * @param bean The bean instance to populate.
     * @return The populated bean instance.
     */
    public <T> T toBean(final T bean) {
        return BeanKit.fillBeanWithMap(this, bean, CopyOptions.of());
    }

    /**
     * Populates a Java Bean with the values from this dictionary, ignoring case when matching keys to properties.
     *
     * @param <T>  The type of the bean.
     * @param bean The bean instance to populate.
     * @return The populated bean instance.
     */
    public <T> T toBeanIgnoreCase(final T bean) {
        return BeanKit.fillBeanWithMap(this, bean, CopyOptions.of().setIgnoreCase(true));
    }

    /**
     * Converts this dictionary into a new instance of the specified bean class.
     *
     * @param <T>   The type of the bean.
     * @param clazz The class of the bean to create.
     * @return A new bean instance populated with values from this dictionary.
     */
    public <T> T toBean(final Class<T> clazz) {
        return BeanKit.toBean(this, clazz);
    }

    /**
     * Converts this dictionary into a new instance of the specified bean class, ignoring case.
     *
     * @param <T>   The type of the bean.
     * @param clazz The class of the bean to create.
     * @return A new bean instance populated with values from this dictionary.
     */
    public <T> T toBeanIgnoreCase(final Class<T> clazz) {
        return BeanKit.toBean(this, clazz, CopyOptions.of().setIgnoreCase(true));
    }

    /**
     * Populates this dictionary with the properties of a given bean.
     *
     * @param <T>  The type of the bean.
     * @param bean The bean to parse. Must not be {@code null}.
     * @return This {@code Dictionary} instance for method chaining.
     */
    public <T> Dictionary parseBean(final T bean) {
        Assert.notNull(bean, "Bean must not be null");
        this.putAll(BeanKit.toBeanMap(bean));
        return this;
    }

    /**
     * Populates this dictionary with bean properties, with options for key casing and null handling.
     *
     * @param <T>               The type of the bean.
     * @param bean              The bean to parse. Must not be {@code null}.
     * @param isToUnderlineCase If {@code true}, property names are converted to snake_case.
     * @param ignoreNullValue   If {@code true}, properties with {@code null} values are skipped.
     * @return This {@code Dictionary} instance for method chaining.
     */
    public <T> Dictionary parseBean(final T bean, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        Assert.notNull(bean, "Bean must not be null");
        this.putAll(BeanKit.beanToMap(bean, isToUnderlineCase, ignoreNullValue));
        return this;
    }

    /**
     * Removes entries from this dictionary that have the same key-value pair as the given dictionary. This is useful
     * for finding the differences between two data sets.
     *
     * @param <T>          The type of the dictionary to compare against.
     * @param dict         The dictionary to compare with.
     * @param withoutNames Field names to exclude from this comparison (i.e., they will be kept even if equal).
     */
    public <T extends Dictionary> void removeEqual(final T dict, final String... withoutNames) {
        final HashSet<String> withoutSet = SetKit.of(withoutNames);
        for (final Map.Entry<String, Object> entry : dict.entrySet()) {
            if (withoutSet.contains(entry.getKey())) {
                continue;
            }
            final Object value = this.get(entry.getKey());
            if (Objects.equals(value, entry.getValue())) {
                this.remove(entry.getKey());
            }
        }
    }

    /**
     * Creates a new dictionary containing only the entries with the specified keys.
     *
     * @param keys The keys to retain.
     * @return A new {@code Dictionary} with only the filtered entries.
     */
    public Dictionary filterNew(final String... keys) {
        final Dictionary result = new Dictionary(keys.length, 1);
        for (final String key : keys) {
            if (this.containsKey(key)) {
                result.put(key, this.get(key));
            }
        }
        return result;
    }

    /**
     * Creates a new dictionary excluding the entries with the specified keys.
     *
     * @param keys The keys to remove.
     * @return A new {@code Dictionary} without the specified entries.
     */
    public Dictionary removeNew(final String... keys) {
        return MapKit.removeAny(this.clone(), keys);
    }

    /**
     * Sets a key-value pair in the dictionary.
     *
     * @param attr  The attribute (key) to set.
     * @param value The value to associate with the attribute.
     * @return This {@code Dictionary} instance for method chaining.
     */
    public Dictionary set(final String attr, final Object value) {
        this.put(attr, value);
        return this;
    }

    /**
     * Sets a key-value pair only if both the key and value are not {@code null}.
     *
     * @param attr  The attribute (key) to set.
     * @param value The value to associate with the attribute.
     * @return This {@code Dictionary} instance for method chaining.
     */
    public Dictionary setIgnoreNull(final String attr, final Object value) {
        if (null != attr && null != value) {
            set(attr, value);
        }
        return this;
    }

    /**
     * Retrieves the value for the specified key, returning a default value if the key is not found.
     *
     * @param key          The key whose value is to be returned.
     * @param defaultValue The default value to return if the key is not present.
     * @return The value associated with the key, or the default value.
     */
    @Override
    public Object getObject(final String key, final Object defaultValue) {
        return getOrDefault(key, defaultValue);
    }

    /**
     * Retrieves a value using a Lambda method reference as the key.
     * <p>
     * Example: {@code String name = dict.get(User::getName);}
     *
     * @param <P>  The type of the class containing the method.
     * @param <T>  The return type of the method.
     * @param func A method reference (e.g., {@code User::getName}).
     * @return The value corresponding to the property name derived from the method reference.
     */
    public <P, T> T get(final FunctionX<P, T> func) {
        final LambdaX lambdaX = LambdaKit.resolve(func);
        return get(lambdaX.getFieldName(), lambdaX.getReturnType());
    }

    /**
     * Retrieves a value and casts it to the specified type.
     *
     * @param <T>  The target type.
     * @param attr The attribute (key) of the value to retrieve.
     * @return The value, cast to type {@code T}.
     */
    public <T> T getBean(final String attr) {
        return (T) get(attr);
    }

    /**
     * Retrieves a nested value using a bean path expression (e.g., "user.address.city").
     *
     * @param <T>        The target type.
     * @param expression The bean path expression.
     * @return The value at the specified path.
     * @see BeanPath#getValue(Object)
     */
    public <T> T getByPath(final String expression) {
        return (T) BeanPath.of(expression).getValue(this);
    }

    /**
     * Retrieves and converts a nested value using a bean path expression.
     *
     * @param <T>        The target type.
     * @param expression The bean path expression.
     * @param resultType The type to convert the result to.
     * @return The converted value at the specified path.
     * @see BeanPath#getValue(Object)
     */
    public <T> T getByPath(final String expression, final Type resultType) {
        return Convert.convert(resultType, getByPath(expression));
    }

    /**
     * Creates and returns a shallow copy of this {@code Dictionary} instance.
     *
     * @return A clone of this instance.
     * @throws CloneException if cloning is not supported.
     */
    @Override
    public Dictionary clone() {
        try {
            return (Dictionary) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

    /**
     * Customizes the key before it is used. If case-insensitivity is enabled, the key is converted to lowercase.
     *
     * @param key The original key.
     * @return The transformed key.
     */
    @Override
    protected String customKey(Object key) {
        if (this.caseInsensitive && null != key) {
            key = key.toString().toLowerCase();
        }
        return (String) key;
    }

    /**
     * Sets multiple key-value pairs using an array of getter method references. The key is derived from the method
     * name, and the value is obtained by invoking the method.
     * <p>
     * Example: {@code dict.setFields(user::getName, user::getAge);}
     * 
     *
     * @param fields An array of getter method references (e.g., {@code user::getNickname}).
     * @return This {@code Dictionary} instance for method chaining.
     */
    public Dictionary setFields(final SupplierX<?>... fields) {
        Arrays.stream(fields).forEach(f -> set(LambdaKit.getFieldName(f), f.get()));
        return this;
    }

}
