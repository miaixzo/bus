package com.ukettle.service.router.service;

import java.util.List;

public interface RouterService<T> {

	/**
	 * 功能描述：查询对象
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return T 返回单个对象
	 */
	public Object select(T entity);

	/**
	 * 功能描述：查询对象
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<?> selectByWhere(T entity);

	/**
	 * 功能描述：查询对象
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public void on(T entity);

	/**
	 * 功能描述：请求转发
	 * 
	 * @param entity
	 *            Object 条件
	 */
	public void forward(T entity);

	/**
	 * 功能描述：验证所有信息
	 * 
	 * @param entity
	 *            Object 条件
	 * @return Object 返回处理结果(对象)
	 */
	public boolean isValid(T entity);

}
