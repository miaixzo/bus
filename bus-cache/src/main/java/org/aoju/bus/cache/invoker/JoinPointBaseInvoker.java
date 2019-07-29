package org.aoju.bus.cache.invoker;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JoinPointBaseInvoker implements BaseInvoker {

    private ProceedingJoinPoint proceedingJoinPoint;

    public JoinPointBaseInvoker(ProceedingJoinPoint proceedingJoinPoint) {
        this.proceedingJoinPoint = proceedingJoinPoint;
    }

    @Override
    public Object[] getArgs() {
        return proceedingJoinPoint.getArgs();
    }

    @Override
    public Object proceed() throws Throwable {
        return proceedingJoinPoint.proceed();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return proceedingJoinPoint.proceed(args);
    }
}
