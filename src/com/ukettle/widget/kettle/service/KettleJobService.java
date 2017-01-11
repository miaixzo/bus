package com.ukettle.widget.kettle.service;

import org.pentaho.di.core.exception.KettleException;

/**
 * <p>
 * Job执行初始化.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 20, 2013
 * @Time 10:27:31
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface KettleJobService<T> {

	/**
	 * 功能描述：执行Job(队列等待)
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public void execute(T entity);
	
	/**
	 * 功能描述：执行Job(非队列,直接执行)
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public void executeJob(T entity);

	/**
	 * 功能描述：获得初始化Job对象
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Job对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getInstance(T entity) throws KettleException;

	/**
	 * 功能描述：获得已创建的Job元数据对象
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Job元数据对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getMetaInstance(T entity) throws KettleException;

	/**
	 * 功能描述：获取当前Job自定义参数
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Job元数据对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object setParameters(T entity) throws KettleException;

	/**
	 * 功能描述：获取当前Job自定义参数
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Job参数对象
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getParameters(T entity) throws KettleException;
}