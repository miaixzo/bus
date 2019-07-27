package org.aoju.bus.cache.invoker;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BaseInvoker {

    Object[] getArgs();

    Object proceed() throws Throwable;

    Object proceed(Object[] args) throws Throwable;
}
