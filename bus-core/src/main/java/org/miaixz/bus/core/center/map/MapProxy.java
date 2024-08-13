/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * Map代理，提供各种getXXX方法，并提供默认值支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapProxy implements Map<Object, Object>, TypeGetter<Object>, InvocationHandler, Serializable {

    private static final long serialVersionUID = -1L;

    Map map;

    /**
     * 构造
     *
     * @param map 被代理的Map
     */
    public MapProxy(final Map<?, ?> map) {
        this.map = map;
    }

    /**
     * 创建代理Map 此类对Map做一次包装，提供各种getXXX方法
     *
     * @param map 被代理的Map
     * @return {@code MapProxy}
     */
    public static MapProxy of(final Map<?, ?> map) {
        return (map instanceof MapProxy) ? (MapProxy) map : new MapProxy(map);
    }

    @Override
    public Object getObject(final Object key, final Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(final Object key) {
        return map.get(key);
    }

    @Override
    public Object put(final Object key, final Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(final Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(final Map<?, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<Object> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (ArrayKit.isEmpty(parameterTypes)) {
            final Class<?> returnType = method.getReturnType();
            if (void.class != returnType) {
                // 匹配Getter
                final String methodName = method.getName();
                String fieldName = null;
                if (methodName.startsWith("get")) {
                    // 匹配getXXX
                    fieldName = StringKit.removePreAndLowerFirst(methodName, 3);
                } else if (BooleanKit.isBoolean(returnType) && methodName.startsWith("is")) {
                    // 匹配isXXX
                    fieldName = StringKit.removePreAndLowerFirst(methodName, 2);
                } else if (Normal.HASHCODE.equals(methodName)) {
                    return this.hashCode();
                } else if (Normal.TOSTRING.equals(methodName)) {
                    return this.toString();
                }

                if (StringKit.isNotBlank(fieldName)) {
                    if (!this.containsKey(fieldName)) {
                        // 驼峰不存在转下划线尝试
                        fieldName = StringKit.toUnderlineCase(fieldName);
                    }
                    return Convert.convert(method.getGenericReturnType(), this.get(fieldName));
                }
            }

        } else if (1 == parameterTypes.length) {
            // 匹配Setter
            final String methodName = method.getName();
            if (methodName.startsWith("set")) {
                final String fieldName = StringKit.removePreAndLowerFirst(methodName, 3);
                if (StringKit.isNotBlank(fieldName)) {
                    this.put(fieldName, args[0]);
                    final Class<?> returnType = method.getReturnType();
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
     * 将Map代理为指定接口的动态代理对象
     *
     * @param <T>            代理的Bean类型
     * @param interfaceClass 接口
     * @return 代理对象
     */
    public <T> T toProxyBean(final Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(ClassKit.getClassLoader(), new Class<?>[] { interfaceClass }, this);
    }

}
