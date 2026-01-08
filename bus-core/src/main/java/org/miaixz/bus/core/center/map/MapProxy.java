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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.getter.TypeGetter;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A dynamic proxy for a {@link Map} that serves two main purposes:
 * <ol>
 * <li>Provides convenient, type-safe {@code getXXX} methods for retrieving values with default value support.</li>
 * <li>Acts as an {@link InvocationHandler} to adapt a Map to a given interface, allowing map entries to be accessed as
 * bean properties.</li>
 * </ol>
 * This allows for treating a {@code Map} like a bean, which is particularly useful for configuration or data transfer
 * objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapProxy implements Map<Object, Object>, TypeGetter<Object>, InvocationHandler, Serializable {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852275756380L;

    /**
     * The underlying map instance that this proxy wraps.
     */
    private final Map map;

    /**
     * Constructs a new {@code MapProxy} that wraps the given map.
     *
     * @param map The map to be proxied. Must not be {@code null}.
     */
    public MapProxy(final Map map) {
        this.map = map;
    }

    /**
     * Creates a {@code MapProxy} instance. If the given map is already a {@code MapProxy}, it is cast and returned;
     * otherwise, a new {@code MapProxy} is created to wrap it.
     *
     * @param map The map to be proxied.
     * @return A {@code MapProxy} instance.
     */
    public static MapProxy of(final Map<?, ?> map) {
        return (map instanceof MapProxy) ? (MapProxy) map : new MapProxy(map);
    }

    /**
     * Retrieves the value for the specified key, returning a default value if the key is not found.
     *
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not present in the map.
     * @return The value associated with the key, or the default value.
     */
    @Override
    public Object getObject(final Object key, final Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns true if this collection contains no elements.
     *
     * @return true if this collection contains no elements
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns true if this cache contains a mapping for the specified key.
     *
     * @param key the key whose presence is to be tested
     * @return true if this cache contains a mapping for the specified key
     */
    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    /**
     * Containsvalue method.
     *
     * @return the boolean value
     */
    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this cache contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or null if no mapping
     */
    @Override
    public Object get(final Object key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key in this cache.
     *
     * @param key   the key to associate the value with
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or null if there was no mapping
     */
    @Override
    public Object put(final Object key, final Object value) {
        return map.put(key, value);
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator.
     */
    @Override
    public Object remove(final Object key) {
        return map.remove(key);
    }

    /**
     * Putall method.
     */
    @Override
    public void putAll(final Map<?, ?> m) {
        map.putAll(m);
    }

    /**
     * Removes all of the elements from this collection.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Keyset method.
     *
     * @return the Set&lt;Object&gt; value
     */
    @Override
    public Set<Object> keySet() {
        return map.keySet();
    }

    /**
     * Values method.
     *
     * @return the Collection&lt;Object&gt; value
     */
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    /**
     * Entryset method.
     */
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

    /**
     * Handles method invocations on the proxy instance. It translates method calls into map operations.
     * <ul>
     * <li><b>Getter methods</b> (e.g., {@code getPropertyName()}, {@code isPropertyName()}) are mapped to
     * {@code map.get("propertyName")}.</li>
     * <li><b>Setter methods</b> (e.g., {@code setPropertyName(value)}) are mapped to
     * {@code map.put("propertyName", value)}.</li>
     * </ul>
     * It also handles standard methods like {@code hashCode()}, {@code toString()}, and {@code equals()}.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@code Method} instance for the interface method that was invoked.
     * @param args   An array of arguments passed in the method invocation.
     * @return The result of the map operation, converted to the method's return type.
     * @throws UnsupportedOperationException if the invoked method is not a recognized getter, setter, or standard
     *                                       Object method.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (ArrayKit.isEmpty(parameterTypes)) {
            final Class<?> returnType = method.getReturnType();
            if (void.class != returnType) {
                // Handle getter methods
                final String methodName = method.getName();
                String fieldName = null;
                if (methodName.startsWith("get")) {
                    fieldName = StringKit.removePreAndLowerFirst(methodName, 3);
                } else if (BooleanKit.isBoolean(returnType) && methodName.startsWith("is")) {
                    fieldName = StringKit.removePreAndLowerFirst(methodName, 2);
                } else if (Normal.HASHCODE.equals(methodName)) {
                    return this.hashCode();
                } else if (Normal.TOSTRING.equals(methodName)) {
                    return this.toString();
                }

                if (StringKit.isNotBlank(fieldName)) {
                    if (!this.containsKey(fieldName)) {
                        // Fallback to snake_case if camelCase key does not exist
                        fieldName = StringKit.toUnderlineCase(fieldName);
                    }
                    return Convert.convert(method.getGenericReturnType(), this.get(fieldName));
                }
            }
        } else if (1 == parameterTypes.length) {
            // Handle setter methods
            final String methodName = method.getName();
            if (methodName.startsWith("set")) {
                final String fieldName = StringKit.removePreAndLowerFirst(methodName, 3);
                if (StringKit.isNotBlank(fieldName)) {
                    this.put(fieldName, args[0]);
                    final Class<?> returnType = method.getReturnType();
                    // Support fluent interface by returning the proxy instance
                    if (returnType.isInstance(proxy)) {
                        return proxy;
                    }
                }
            } else if (Normal.EQUALS.equals(methodName)) {
                return this.equals(args[0]);
            }
        }

        throw new UnsupportedOperationException(method.toGenericString());
    }

    /**
     * Creates a dynamic proxy instance that implements the specified interface, backed by the wrapped map. This allows
     * the map to be treated as a bean-like object, with interface methods mapping to map operations.
     *
     * @param <T>            The type of the interface.
     * @param interfaceClass The interface class to be implemented by the proxy.
     * @return A proxy object that implements the specified interface.
     */
    public <T> T toProxyBean(final Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(ClassKit.getClassLoader(), new Class<?>[] { interfaceClass }, this);
    }

}
