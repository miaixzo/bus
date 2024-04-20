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
package org.miaixz.bus.spring;

import org.miaixz.bus.core.toolkit.ReflectKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 拦截响应的代理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PlaceHandler implements InvocationHandler {

    private final Annotation delegate;

    private final PlaceBinder binder;

    private PlaceHandler(Annotation delegate, PlaceBinder binder) {
        this.delegate = delegate;
        this.binder = binder;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(delegate, args);
        if (!ReflectKit.isEqualsMethod(method) && !ReflectKit.isHashCodeMethod(method)
                && !ReflectKit.isToStringMethod(method) && isAttributeMethod(method)) {
            return resolvePlaceHolder(ret);
        }
        return ret;
    }

    private boolean isAttributeMethod(Method method) {
        return (null != method && method.getParameterTypes().length == 0 && method.getReturnType() != void.class);
    }

    public Object resolvePlaceHolder(Object origin) {
        if (origin.getClass().isArray()) {
            int length = Array.getLength(origin);
            Object ret = Array.newInstance(origin.getClass().getComponentType(), length);
            for (int i = 0; i < length; ++i) {
                Array.set(ret, i, resolvePlaceHolder(Array.get(origin, i)));
            }
            return ret;
        } else {
            return doResolvePlaceHolder(origin);
        }
    }

    private Object doResolvePlaceHolder(Object origin) {
        if (origin instanceof String) {
            return binder.bind((String) origin);
        } else {
            return origin;
        }
    }

}
