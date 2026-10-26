/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

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
 * objects. This adapter itself uses standard Map content equality; bean proxy instances created by
 * {@link #toProxyBean(Class)} instead use identity for the exact standard Object method signatures. The backing map is
 * shared, and its null, mutability, concurrency and atomic-update policies remain in effect.
 *
 * @author Kimi Liu
 */
public class MapProxy implements Map<Object, Object>, TypeGetter<Object>, InvocationHandler, Serializable {

    @Serial
    private static final long serialVersionUID = 2535858500001L;

    /**
     * The underlying map instance that this proxy wraps.
     */
    private final Map map;

    /**
     * Constructs a new {@code MapProxy} that wraps the given map.
     *
     * @param map The map to be proxied. Must not be {@code null}.
     * @throws NullPointerException if the map is {@code null}
     */
    public MapProxy(final Map map) {
        this.map = Objects.requireNonNull(map, "map");
    }

    /**
     * Creates a {@code MapProxy} instance. If the given map is already a {@code MapProxy}, it is cast and returned;
     * otherwise, a new {@code MapProxy} is created to wrap it.
     *
     * @param map The map to be proxied.
     * @return A {@code MapProxy} instance.
     * @throws NullPointerException if the map is {@code null}
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
     * Returns the number of mappings reported by the backing map.
     *
     * @return the number of mappings
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Tests whether the backing map has no mappings.
     *
     * @return whether the backing map is empty
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Tests whether the backing map contains the specified key.
     *
     * @param key the key whose presence is to be tested
     * @return whether the key has a mapping
     */
    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    /**
     * Tests whether the backing map contains the specified value.
     *
     * @param value the value whose presence is to be tested
     * @return whether at least one mapping has the value
     */
    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns the backing value for the specified key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or null if no mapping
     */
    @Override
    public Object get(final Object key) {
        return map.get(key);
    }

    /**
     * Associates the specified key and value in the backing map.
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
     * Removes the specified key from the backing map.
     *
     * @param key the key whose mapping is to be removed
     * @return the previous value, or {@code null} if no value was associated
     */
    @Override
    public Object remove(final Object key) {
        return map.remove(key);
    }

    /**
     * Copies the supplied mappings into the shared backing map.
     *
     * @param m the source mappings
     */
    @Override
    public void putAll(final Map<?, ?> m) {
        map.putAll(m);
    }

    /**
     * Removes all mappings from the shared backing map.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns the backing map's key view, with its existing mutation and consistency semantics.
     *
     * @return the backed key set
     */
    @Override
    public Set<Object> keySet() {
        return map.keySet();
    }

    /**
     * Returns the backing map's value view, with its existing mutation and consistency semantics.
     *
     * @return the backed value collection
     */
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    /**
     * Returns the backing map's entry view without copying its mappings.
     *
     * @return the backed entry set
     */
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

    /**
     * Returns the backing map's value or the caller's unchanged default.
     *
     * @param key          the lookup key
     * @param defaultValue the fallback when the key is absent
     * @return the associated value, or the fallback when absent
     */
    @Override
    public Object getOrDefault(final Object key, final Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Delegates traversal without replacing the backing map's consistency guarantees.
     *
     * @param action the action applied to each mapping
     */
    @Override
    public void forEach(final BiConsumer<? super Object, ? super Object> action) {
        map.forEach(action);
    }

    /**
     * Delegates replacement to the backing map's implementation.
     *
     * @param function the function producing a replacement for each mapping
     */
    @Override
    public void replaceAll(final BiFunction<? super Object, ? super Object, ? extends Object> function) {
        map.replaceAll(function);
    }

    /**
     * Preserves the backing map's put-if-absent implementation and any atomicity it provides.
     *
     * @param key   the candidate key
     * @param value the candidate value
     * @return the previous value, or {@code null} if no non-null value was associated
     */
    @Override
    public Object putIfAbsent(final Object key, final Object value) {
        return map.putIfAbsent(key, value);
    }

    /**
     * Preserves the backing map's conditional removal implementation.
     *
     * @param key   the key to remove
     * @param value the expected associated value
     * @return whether a matching mapping was removed
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return map.remove(key, value);
    }

    /**
     * Delegates replacement of a present mapping.
     *
     * @param key   the key whose value is to be replaced
     * @param value the replacement value
     * @return the previous value, or {@code null} if no value was associated
     */
    @Override
    public Object replace(final Object key, final Object value) {
        return map.replace(key, value);
    }

    /**
     * Preserves the backing map's conditional replacement implementation.
     *
     * @param key      the key whose value is to be replaced
     * @param oldValue the expected current value
     * @param newValue the replacement value
     * @return whether a matching mapping was replaced
     */
    @Override
    public boolean replace(final Object key, final Object oldValue, final Object newValue) {
        return map.replace(key, oldValue, newValue);
    }

    /**
     * Preserves the backing map's absent-value computation and callback guarantees.
     *
     * @param key      the key to compute
     * @param function the function computing an absent value
     * @return the existing or computed value, or {@code null} if no value was produced
     */
    @Override
    public Object computeIfAbsent(final Object key, final Function<? super Object, ? extends Object> function) {
        return map.computeIfAbsent(key, function);
    }

    /**
     * Preserves the backing map's present-value computation implementation.
     *
     * @param key      the key to compute
     * @param function the function computing a present non-null value's replacement
     * @return the resulting value, or {@code null} if no value remains
     */
    @Override
    public Object computeIfPresent(
            final Object key,
            final BiFunction<? super Object, ? super Object, ? extends Object> function) {
        return map.computeIfPresent(key, function);
    }

    /**
     * Preserves the backing map's general computation implementation.
     *
     * @param key      the key to compute
     * @param function the function receiving the key and previous value, possibly null
     * @return the resulting value, or {@code null} if no value remains
     */
    @Override
    public Object compute(
            final Object key,
            final BiFunction<? super Object, ? super Object, ? extends Object> function) {
        return map.compute(key, function);
    }

    /**
     * Preserves the backing map's merge implementation.
     *
     * @param key      the key to merge
     * @param value    the incoming non-null value
     * @param function the function combining an existing value with the incoming value
     * @return the resulting value, or {@code null} if merging removed the mapping
     */
    @Override
    public Object merge(
            final Object key,
            final Object value,
            final BiFunction<? super Object, ? super Object, ? extends Object> function) {
        return map.merge(key, value, function);
    }

    /**
     * Compares this Map adapter by its logical mappings, independently of proxy-bean identity.
     *
     * @param object the object to compare
     * @return whether the object represents the same Map mappings
     */
    @Override
    public boolean equals(final Object object) {
        return object == this || map.equals(object);
    }

    /**
     * Returns the backing mappings' Map hash code.
     *
     * @return the backing map's content hash code
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * Returns the backing map's representation.
     *
     * @return the backing map's string representation
     */
    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Handles method invocations on the proxy instance. It translates method calls into map operations.
     * <ul>
     * <li><b>Getter methods</b> (e.g., {@code getPropertyName()}, {@code isPropertyName()}) are mapped to
     * {@code map.get("propertyName")}.</li>
     * <li><b>Setter methods</b> (e.g., {@code setPropertyName(value)}) are mapped to
     * {@code map.put("propertyName", value)}.</li>
     * </ul>
     * Standard Object signatures use proxy identity, independently of this adapter's Map content equality. Setter
     * return types must be void or able to accept the proxy; unsupported signatures are rejected before writing.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@code Method} instance for the interface method that was invoked.
     * @param args   An array of arguments passed in the method invocation.
     * @return the converted getter value, null for a void setter, the proxy for a fluent setter, or the identity-based
     *         result for a standard Object method
     * @throws UnsupportedOperationException if the invoked method is not a recognized getter, setter, or standard
     *                                       Object method.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Class<?> returnType = method.getReturnType();
        final String methodName = method.getName();
        if (Normal.EQUALS.equals(methodName) && parameterTypes.length == 1 && parameterTypes[0] == Object.class
                && returnType == boolean.class) {
            return proxy == args[0];
        }
        if (Normal.HASHCODE.equals(methodName) && parameterTypes.length == 0 && returnType == int.class) {
            return System.identityHashCode(proxy);
        }
        if (Normal.TOSTRING.equals(methodName) && parameterTypes.length == 0 && returnType == String.class) {
            return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
        }
        if (ArrayKit.isEmpty(parameterTypes)) {
            if (void.class != returnType) {
                // Handle getter methods
                String fieldName = null;
                if (methodName.startsWith("get")) {
                    fieldName = StringKit.removePreAndLowerFirst(methodName, 3);
                } else if (BooleanKit.isBoolean(returnType) && methodName.startsWith("is")) {
                    fieldName = StringKit.removePreAndLowerFirst(methodName, 2);
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
            if (methodName.startsWith("set")) {
                final String fieldName = StringKit.removePreAndLowerFirst(methodName, 3);
                if (StringKit.isNotBlank(fieldName)) {
                    if (returnType != void.class && !returnType.isInstance(proxy)) {
                        throw new UnsupportedOperationException(method.toGenericString());
                    }
                    this.put(fieldName, args[0]);
                    return returnType == void.class ? null : proxy;
                }
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
     * @throws IllegalArgumentException if the requested type cannot be implemented by a Java dynamic proxy
     */
    public <T> T toProxyBean(final Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(ClassKit.getClassLoader(), new Class<?>[] { interfaceClass }, this);
    }

}
