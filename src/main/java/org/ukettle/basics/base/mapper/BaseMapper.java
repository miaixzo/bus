package org.ukettle.basics.base.mapper;

import java.util.List;

import org.springframework.dao.DataAccessException;


/**
 * <p>
 * BaseMapper 接口.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 10:27:31
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public interface BaseMapper<T> extends Mapper {

	/**
	 * 功能描述：添加Object
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return int 返回所影响的行数
	 */
	public int insert(T entity) throws DataAccessException;

	/**
	 * 功能描述：更新Object
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return int 返回所影响的行数
	 */
	public int update(T entity) throws DataAccessException;

	/**
	 * 功能描述：删除Object
	 * 
	 * @param id
	 *            String 操作编号
	 * @return int 返回所影响的行数
	 */
	public int delete(String id) throws DataAccessException;

	/**
	 * 功能描述：查询对象
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return T 返回单个对象
	 */
	public T select(T entity) throws DataAccessException;

	/**
	 * 功能描述：查询对象
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<?> selectByWhere(T entity) throws DataAccessException;

}