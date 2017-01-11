package org.ukettle.widget.kettle.service;

import org.ukettle.basics.base.service.BaseService;

public interface KettleSpoonService<T> extends BaseService<T> {

	/**
	 * 功能描述：执行JOB或者Trans
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 非法参数等信息
	 */
	public Object execute(T entity);

}