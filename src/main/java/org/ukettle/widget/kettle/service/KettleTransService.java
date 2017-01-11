package org.ukettle.widget.kettle.service;

import org.pentaho.di.core.exception.KettleException;

/**
 * <p>
 * Trans执行初始化.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 22, 2013
 * @Time 10:27:31
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface KettleTransService<T> {

	/**
	 * 功能描述：执行Trans
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public void execute(T entity);

	/**
	 * 功能描述：获得初始化Trans对象
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Trans对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getInstance(T entity) throws KettleException;

	/**
	 * 功能描述：获得已创建的Trans元数据对象
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Trans元数据对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getMetaInstance(T entity) throws KettleException;

	/**
	 * 功能描述：获取当前Trans自定义参数
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Trans元数据对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object setParameters(T entity) throws KettleException;

	/**
	 * 功能描述：获取当前Trans自定义参数
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Trans参数对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getParameters(T entity) throws KettleException;

}