package org.ukettle.basics.base.service;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

/**
 * <p>
 * BaseService 接口.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 10:21:00
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface BaseService<T> extends Service {

	/**
	 * 功能描述：添加Object
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return int 返回所影响的行数
	 */
	public Object insert(T entity);

	/**
	 * 功能描述：更新Object
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return int 返回所影响的行数
	 */
	public Object update(T entity);

	/**
	 * 功能描述：删除Object
	 * 
	 * @param id
	 *            String 操作编号
	 * @return int 返回所影响的行数
	 */
	public Object delete(String id);

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
	 * @throws KettleException
	 */
	public List<?> selectByWhere(T entity);

}