package org.aoju.bus.proxy.intercept;


import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.proxy.aspects.Aspectj;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * JDK实现的动态代理切面
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class JavassistInterceptor implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private Object target;
    private Aspectj aspectj;

    /**
     * 构造
     *
     * @param target  被代理对象
     * @param aspectj 切面实现
     */
    public JavassistInterceptor(Object target, Aspectj aspectj) {
        this.target = target;
        this.aspectj = aspectj;
    }

    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Object target = this.target;
        final Aspectj aspectj = this.aspectj;
        Object result = null;

        // 开始前回调
        if (aspectj.before(target, method, args)) {
            ReflectUtils.setAccessible(method);

            try {
                result = method.invoke(ClassUtils.isStatic(method) ? null : target, args);
            } catch (InvocationTargetException e) {
                // 异常回调（只捕获业务代码导致的异常,而非反射导致的异常）
                if (aspectj.afterException(target, method, args, e.getTargetException())) {
                    throw e;
                }
            }
        }

        // 结束执行回调
        if (aspectj.after(target, method, args, result)) {
            return result;
        }
        return null;
    }

}
