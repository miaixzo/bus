package org.ukettle.www.toolkit.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 功能描述：Spring上下文信息
 * 
 * @author Kimi Liu
 * @Date Mar 10, 2014
 * @Time 21:20:25
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public final class SpringUtils implements BeanFactoryPostProcessor {

	 // Spring应用上下文环境
	private static ConfigurableListableBeanFactory factory;

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		factory = beanFactory;
	}

	/**
	 * 获取对象
	 * 
	 * @param name
	 * @return Object 一个以所给名字注册的bean的实例
	 * @throws org.springframework.beans.BeansException
	 * 
	 */
	@SuppressWarnings("all")
	public static <T> T getBean(String name) throws BeansException {
		return (T) factory.getBean(name);
	}

	/**
	 * 获取类型为requiredType的对象
	 * 
	 * @param clz
	 * @return
	 * @throws org.springframework.beans.BeansException
	 * 
	 */
	public static <T> T getBean(Class<T> clazz) throws BeansException {
		return (T) factory.getBean(clazz);
	}

	/**
	 * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
	 * 
	 * @param name
	 * @return boolean
	 */
	public static boolean containsBean(String name) {
		return factory.containsBean(name);
	}

	/**
	 * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
	 * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
	 * 
	 * @param name
	 * @return boolean
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 
	 */
	public static boolean isSingleton(String name)
			throws NoSuchBeanDefinitionException {
		return factory.isSingleton(name);
	}

	/**
	 * @param name
	 * @return Class 注册对象的类型
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 
	 */
	public static Class<?> getType(String name)
			throws NoSuchBeanDefinitionException {
		return factory.getType(name);
	}

	/**
	 * 如果给定的bean名字在bean定义中有别名，则返回这些别名
	 * 
	 * @param name
	 * @return
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 
	 */
	public static String[] getAliases(String name)
			throws NoSuchBeanDefinitionException {
		return factory.getAliases(name);
	}

}