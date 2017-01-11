package com.ukettle.widget.kettle.service;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

import com.ukettle.basics.base.service.BaseService;

/**
 * <p>
 * 资源库相关信息.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 20, 2013
 * @Time 10:27:31
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface KettleReposService<T> extends BaseService<T> {

	/**
	 * 功能描述：重新加载资源库信息
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回所有已初始化资源库
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object loading(T entity) throws KettleException;

	/**
	 * 功能描述：销毁当前资源库所有信息
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 已销毁的资源库名称
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object discard(T entity) throws KettleException;

	/**
	 * 功能描述：查询资源库相关信息
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 所查找的资源库信息
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object get(T entity) throws KettleException;

	/**
	 * 功能描述：获得Job或者Trans的自定义参数
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回Job、Trans相关参数
	 * @throws KettleException
	 *             返回相关异常
	 */
	public Object getParameters(T entity) throws KettleException;

	/**
	 * 功能描述：读取当前资源库信息(JOB/Trans)
	 * 
	 * @param entity
	 *            Object 参数对象
	 * @return object 返回树形资源库
	 * @throws KettleException
	 *             返回相关异常
	 */
	public List<?> getJobAndTrans(T entity) throws KettleException;
}