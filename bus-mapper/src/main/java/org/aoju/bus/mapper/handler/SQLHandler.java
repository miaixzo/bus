package org.aoju.bus.mapper.handler;

/**
 * SQL 拦截处理器
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public interface SQLHandler {

    /**
     * 预处理回调方法，在方法调用前执行
     */
    default void preHandle() {

    }

    /**
     * 拦截处理程序的执行
     * 使用这种方法,每个拦截器可以对一个执行进行后处理,
     * 按执行链的相反顺序应用
     */
    default void postHandle() {

    }

    /**
     * 完成请求处理后回调
     */
    default void afterCompletion() {

    }

}
