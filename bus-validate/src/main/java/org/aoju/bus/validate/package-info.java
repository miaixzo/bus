/**
 * 一个校验器框架，提供注解校验方法参数和对象属性的功能，在方法运行前，拦截方法并执行参数校验，如果校验失败可以抛出自定义异常和信息；便于拓展自定义校验器
 * 开发时，参考了Hibernate-Validator 5.x，但是没有做到兼容，因为JSR-303提供的注解的方法太少，不方便拓展，所以写了这个框架。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.aoju.bus.validate;