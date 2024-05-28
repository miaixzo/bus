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
package org.miaixz.bus.proxy.spring;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.Provider;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Constructor;

/**
 * 基于Spring-cglib的切面代理工厂
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringCglibProvider implements Provider {

    /**
     * 创建代理对象
     * 某些对象存在非空参数构造，则需遍历查找需要的构造完成代理对象构建。
     *
     * @param <T>         代理对象类型
     * @param enhancer    {@link Enhancer}
     * @param targetClass 目标类型
     * @return 代理对象
     */
    private static <T> T create(final Enhancer enhancer, final Class<?> targetClass) {
        final Constructor<?>[] constructors = ReflectKit.getConstructors(targetClass);
        Class<?>[] parameterTypes;
        Object[] values;
        IllegalArgumentException finalException = null;
        for (final Constructor<?> constructor : constructors) {
            parameterTypes = constructor.getParameterTypes();
            values = ClassKit.getDefaultValues(parameterTypes);

            try {
                return (T) enhancer.create(parameterTypes, values);
            } catch (final IllegalArgumentException e) {
                // ignore
                finalException = e;
            }
        }
        if (null != finalException) {
            throw finalException;
        }

        throw new IllegalArgumentException("No constructor provided");
    }

    @Override
    public <T> T proxy(final T target, final Aspect aspect) {
        final Class<?> targetClass = target.getClass();

        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new SpringCglibInterceptor(target, aspect));

        return create(enhancer, targetClass);
    }

}
