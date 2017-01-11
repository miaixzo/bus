package com.ukettle.widget.quartz.service;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * <p>
 * 用于对Job注入ApplicationContext等.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Jul 12, 2013
 * @Time 10:32:00
 * @version 1.0
 * @since JDK 1.6
 */
public class JobFactoryService extends SpringBeanJobFactory implements
		ApplicationContextAware {

	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;
	}

	/**
	 * 功能描述：创建JOB实例,并注解相关内容等
	 * 
	 * @param bundle
	 *            TriggerFiredBundle
	 * @return Object JOB实例
	 */
	@Override
	protected Object createJobInstance(TriggerFiredBundle bundle)
			throws Exception {
		Object instance = super.createJobInstance(bundle);
		context.getAutowireCapableBeanFactory().autowireBean(instance);
		return instance;
	}

}