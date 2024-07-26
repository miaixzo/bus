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
package org.miaixz.bus.spring.annotation;

import org.miaixz.bus.core.xyz.MethodKit;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 实现了{@link InvocationHandler}在获取注释值时从环境绑定值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PlaceHolderHandler implements InvocationHandler {

    private final Annotation delegate;

    private final PlaceHolderBinder binder;

    private final Environment environment;

    public PlaceHolderHandler(Annotation delegate, PlaceHolderBinder binder, Environment environment) {
        this.delegate = delegate;
        this.binder = binder;
        this.environment = environment;
    }

    public static boolean isObjectMethod(Method method) {
        return method != null && (method.getDeclaringClass() == Object.class || MethodKit.isEqualsMethod(method)
                || MethodKit.isHashCodeMethod(method) || MethodKit.isToStringMethod(method));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(delegate, args);
        if (ret != null && !MethodKit.isObjectMethod(method) && MethodKit.isAttributeMethod(method)) {
            return resolvePlaceHolder(ret);
        }
        return ret;
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
            return binder.bind(environment, (String) origin);
        } else if (origin instanceof Annotation && !(origin instanceof WrapperAnnotation)) {
            return AnnotationWrapper.of((Annotation) origin).withBinder(binder).withEnvironment(environment)
                    .wrap((Annotation) origin);
        } else {
            return origin;
        }
    }

}
