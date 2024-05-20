/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.annotation.resolve;

import org.miaixz.bus.core.annotation.Alias;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 注解代理
 * 通过代理指定注解，可以自定义调用注解的方法逻辑，如支持{@link Alias} 注解
 *
 * @param <T> 注解类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnotationProxy<T extends Annotation> implements Annotation, InvocationHandler, Serializable {

    private static final long serialVersionUID = -1L;

    private final T annotation;
    private final Class<T> type;
    private final Map<String, Object> attributes;

    /**
     * 构造
     *
     * @param annotation 注解
     */
    public AnnotationProxy(final T annotation) {
        this.annotation = annotation;
        this.type = (Class<T>) annotation.annotationType();
        this.attributes = initAttributes();
    }


    @Override
    public Class<? extends Annotation> annotationType() {
        return type;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // 注解别名
        final Alias alias = method.getAnnotation(Alias.class);
        if (null != alias) {
            final String name = alias.value();
            if (StringKit.isNotBlank(name)) {
                if (!attributes.containsKey(name)) {
                    throw new IllegalArgumentException(StringKit.format("No method for alias: [{}]", name));
                }
                return attributes.get(name);
            }
        }

        final Object value = attributes.get(method.getName());
        if (value != null) {
            return value;
        }
        return method.invoke(this, args);
    }

    /**
     * 初始化注解的属性
     * 此方法预先调用所有注解的方法，将注解方法值缓存于attributes中
     *
     * @return 属性（方法结果）映射
     */
    private Map<String, Object> initAttributes() {
        // 只缓存注解定义的方法
        final Method[] methods = MethodKit.getDeclaredMethods(this.type);
        final Map<String, Object> attributes = new HashMap<>(methods.length, 1);

        for (final Method method : methods) {
            // 跳过匿名内部类自动生成的方法
            if (method.isSynthetic()) {
                continue;
            }

            attributes.put(method.getName(), MethodKit.invoke(this.annotation, method));
        }
        return attributes;
    }

}
