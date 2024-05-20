/**
 * 万能类型转换器以及各种类型转换的实现类，其中Convert为转换器入口，提供各种toXXX方法和convert方法
 *
 * <p>
 * 转换器是典型的策略模式应用，通过实现{@link org.miaixz.bus.core.convert.Converter} 接口，
 * 自定义转换策略。提供了常用类型的转换策略。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.convert;
