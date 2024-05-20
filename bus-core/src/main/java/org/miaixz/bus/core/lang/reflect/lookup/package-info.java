/**
 * {@link java.lang.invoke.MethodHandles.Lookup} 创建封装，以根据不同的条件查找{@link java.lang.invoke.MethodHandles}
 *
 * <p>
 * jdk8中如果直接调用{@link java.lang.invoke.MethodHandles#lookup()}获取到的{@link java.lang.invoke.MethodHandles.Lookup}在调用findSpecial和unreflectSpecial
 * 时会出现权限不够问题，抛出"no private access for invokespecial"异常，因此针对JDK8及JDK9+分别封装lookup方法。
 * 参考：https://blog.csdn.net/u013202238/article/details/108687086
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.lang.reflect.lookup;
