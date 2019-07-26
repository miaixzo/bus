package org.aoju.bus.core.lang;

/**
 * 函数对象<br>
 * 接口灵感来自于<a href="http://actframework.org/">ActFramework</a><br>
 * 一个函数接口代表一个一个函数，用于包装一个函数为对象<br>
 * 在JDK8之前，Java的函数并不能作为参数传递，也不能作为返回值存在，此接口用于将一个函数包装成为一个对象，从而传递对象
 *
 * @param <P> 参数类型
 * @param <R> 返回值类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Func<P, R> {
    /**
     * 执行函数
     *
     * @param parameters 参数列表
     * @return 函数执行结果
     */
    R call(P... parameters);
}
