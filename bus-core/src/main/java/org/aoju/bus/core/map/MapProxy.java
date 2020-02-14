/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.map;

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.getter.OptNullObject;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.BooleanUtils;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Map代理,提供各种getXXX方法,并提供默认值支持
 *
 * @author Kimi Liu
 * @version 5.5.9
 * @since JDK 1.8+
 */
public class MapProxy implements Map<Object, Object>, OptNullObject<Object>, InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private Map map;

    /**
     * 构造
     *
     * @param map 被代理的Map
     */
    public MapProxy(Map<?, ?> map) {
        this.map = map;
    }

    /**
     * 创建代理Map
     * 此类对Map做一次包装,提供各种getXXX方法
     *
     * @param map 被代理的Map
     * @return {@link MapProxy}
     */
    public static MapProxy create(Map<?, ?> map) {
        return (map instanceof MapProxy) ? (MapProxy) map : new MapProxy(map);
    }

    public Object getObj(Object key, Object defaultValue) {
        final Object value = map.get(key);
        return null != value ? value : defaultValue;
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
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<?, ?> m) {
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
    public Object invoke(Object proxy, Method method, Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (ArrayUtils.isEmpty(parameterTypes)) {
            final Class<?> returnType = method.getReturnType();
            if (null != returnType && void.class != returnType) {
                // 匹配Getter
                final String methodName = method.getName();
                String fieldName = null;
                if (methodName.startsWith("get")) {
                    // 匹配getXXX
                    fieldName = StringUtils.removePreAndLowerFirst(methodName, 3);
                } else if (BooleanUtils.isBoolean(returnType) && methodName.startsWith("is")) {
                    // 匹配isXXX
                    fieldName = StringUtils.removePreAndLowerFirst(methodName, 2);
                } else if ("hashCode".equals(methodName)) {
                    return this.hashCode();
                } else if ("toString".equals(methodName)) {
                    return this.toString();
                }

                if (StringUtils.isNotBlank(fieldName)) {
                    if (false == this.containsKey(fieldName)) {
                        // 驼峰不存在转下划线尝试
                        fieldName = StringUtils.toUnderlineCase(fieldName);
                    }
                    return Convert.convert(method.getGenericReturnType(), this.get(fieldName));
                }
            }

        } else if (1 == parameterTypes.length) {
            // 匹配Setter
            final String methodName = method.getName();
            if (methodName.startsWith("set")) {
                final String fieldName = StringUtils.removePreAndLowerFirst(methodName, 3);
                if (StringUtils.isNotBlank(fieldName)) {
                    this.put(fieldName, args[0]);
                }
            } else if ("equals".equals(methodName)) {
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
    public <T> T toProxyBean(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(ClassUtils.getClassLoader(), new Class<?>[]{interfaceClass}, this);
    }

}
