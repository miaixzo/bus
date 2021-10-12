package org.aoju.bus.core.lang.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 只有一个参数的函数对象
 * 一个函数接口代表一个一个函数，用于包装一个函数为对象
 * 在JDK8之前，Java的函数并不能作为参数传递，也不能作为返回值存在
 * 此接口用于将一个函数包装成为一个对象，从而传递对象
 *
 * @param <T> 参数类型
 * @param <R> 返回值类型
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
@FunctionalInterface
public interface Fn<T, R> extends Function<T, R>, Serializable {

}
