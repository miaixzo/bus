package org.aoju.bus.proxy.intercept;


import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.proxy.aspects.Aspect;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * JDK实现的动态代理切面
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
public class JavassistInterceptor implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private Object target;
    private Aspect aspect;

    /**
     * 构造
     *
     * @param target 被代理对象
     * @param aspect 切面实现
     */
    public JavassistInterceptor(Object target, Aspect aspect) {
        this.target = target;
        this.aspect = aspect;
    }

    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Object target = this.target;
        final Aspect aspect = this.aspect;
        Object result = null;

        // 开始前回调
        if (aspect.before(target, method, args)) {
            ReflectUtils.setAccessible(method);

            try {
                result = method.invoke(ClassUtils.isStatic(method) ? null : target, args);
            } catch (InvocationTargetException e) {
                // 异常回调（只捕获业务代码导致的异常,而非反射导致的异常）
                if (aspect.afterException(target, method, args, e.getTargetException())) {
                    throw e;
                }
            }
        }

        // 结束执行回调
        if (aspect.after(target, method, args, result)) {
            return result;
        }
        return null;
    }

}
