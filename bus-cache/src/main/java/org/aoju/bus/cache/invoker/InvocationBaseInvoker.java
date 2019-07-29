package org.aoju.bus.cache.invoker;

import org.apache.commons.proxy.Invocation;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InvocationBaseInvoker implements BaseInvoker {

    private Object target;

    private Invocation invocation;

    public InvocationBaseInvoker(Object target, Invocation invocation) {
        this.target = target;
        this.invocation = invocation;
    }

    @Override
    public Object[] getArgs() {
        return invocation.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return invocation.getMethod().invoke(target, args);
    }
}
