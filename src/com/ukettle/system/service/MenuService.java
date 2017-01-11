package com.ukettle.system.service;

import java.util.List;

import com.ukettle.basics.base.service.BaseService;


public interface MenuService<T> extends BaseService<T> {

	/**
	 * 功能描述：查询下级和下级子菜单
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<?> selectById(T entity);

	/**
	 * 功能描述：查询本级和子菜单
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<?> selectByPid(T entity);

	/**
	 * 功能描述：查询微信帐号菜单
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return List<T> 返回多个对象
	 */
	public List<?> selectByUid(T entity);

}
