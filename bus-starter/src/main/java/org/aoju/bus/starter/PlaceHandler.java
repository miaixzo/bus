package org.aoju.bus.starter;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 拦截响应的代理
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
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
        if (!ReflectionUtils.isEqualsMethod(method) && !ReflectionUtils.isHashCodeMethod(method)
                && !ReflectionUtils.isToStringMethod(method) && isAttributeMethod(method)) {
            return resolvePlaceHolder(ret);
        }
        return ret;
    }

    private boolean isAttributeMethod(Method method) {
        return (method != null && method.getParameterTypes().length == 0 && method.getReturnType() != void.class);
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
