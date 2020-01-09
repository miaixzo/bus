package org.aoju.bus.proxy.aspects;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 简单切面类,不做任何操作
 * 可以继承此类实现自己需要的方法即可
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class AspectjSimple implements Aspectj, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        return true;
    }

    /**
     * 目标方法执行后的操作
     * 如果 target.method 抛出异常且
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   参数
     * @return 是否允许返回值（接下来的操作）
     * @see Aspectj#afterException 返回true,则不会执行此操作
     * 如果
     * @see Aspectj#afterException 返回false,则无论target.method是否抛出异常,均会执行此操作
     */
    public boolean after(Object target, Method method, Object[] args) {
        return after(target, method, args, null);
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        return true;
    }

    @Override
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        return true;
    }

}
