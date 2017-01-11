package org.ukettle.system.mapper;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.ukettle.basics.base.mapper.BaseMapper;


@Repository
public interface MenuMapper<T extends Object> extends BaseMapper<T> {

	/**
	 * 功能描述：查询下级和下级子菜单
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<T> selectById(T entity) throws DataAccessException;
	
	/**
	 * 功能描述：查询本级和子菜单
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<T> selectByPid(T entity) throws DataAccessException;

	/**
	 * 功能描述：查询微信帐号菜单
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<T> selectByUid(T entity) throws DataAccessException;
}
