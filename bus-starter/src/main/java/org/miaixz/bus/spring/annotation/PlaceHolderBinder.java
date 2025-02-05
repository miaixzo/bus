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
package org.miaixz.bus.spring.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

/**
 * 返回值信息处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PlaceHolderBinder {

    /**
     * 全局资源绑定到指定对象即属性
     *
     * @param environment 环境配置信息
     * @param targetClass 目标Class对象
     * @param prefix      资源前缀
     * @param <T>         泛型对象
     * @return the object
     */
    static <T> T bind(Environment environment, Class<T> targetClass, String prefix) {
        // 使用 Spring Boot 方式绑定
        try {
            Class<?> bindClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = bindClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = bindClass.getDeclaredMethod("bind", String.class, Class.class);
            Object bind = getMethod.invoke(null, environment);
            Object bindResult = bindMethod.invoke(bind, prefix, targetClass);
            Method resultGetMethod = bindResult.getClass().getDeclaredMethod("get");
            Method isBoundMethod = bindResult.getClass().getDeclaredMethod("isBound");
            if ((Boolean) isBoundMethod.invoke(bindResult)) {
                return (T) resultGetMethod.invoke(bindResult);
            }
            return null;
        } catch (Exception e) {
            // 使用 Spring Boot 1.x 方式绑定
            try {
                // 反射提取配置信息
                Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
                Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
                Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
                Object resolver = resolverConstructor.newInstance(environment);
                Map<String, Object> properties = (Map<String, Object>) getSubPropertiesMethod.invoke(resolver,
                        Normal.EMPTY);
                // 创建结果类
                T target = targetClass.getConstructor().newInstance();
                // 反射使用 org.springframework.boot.bind.RelaxedDataBinder
                Class<?> binderClass = Class.forName("org.springframework.boot.bind.RelaxedDataBinder");
                Constructor<?> binderConstructor = binderClass.getDeclaredConstructor(Object.class, String.class);
                Method bindMethod = binderClass.getMethod("bind", PropertyValues.class);
                // 创建 binder 并绑定数据
                Object binder = binderConstructor.newInstance(target, prefix);
                bindMethod.invoke(binder, new MutablePropertyValues(properties));
                return target;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 全局资源绑定到指定对象即属性
     *
     * @param environment 环境配置信息
     * @param string      原始值
     * @return the string
     */
    default String bind(Environment environment, String string) {
        return environment.resolvePlaceholders(string);
    }

    /**
     * 全局资源绑定到指定对象即属性
     *
     * @param string 原始值
     * @return the string
     */
    default String bind(String string) {
        return Normal.EMPTY;
    }

}
