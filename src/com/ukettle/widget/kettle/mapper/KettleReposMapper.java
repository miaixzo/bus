package com.ukettle.widget.kettle.mapper;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.ukettle.basics.base.mapper.BaseMapper;

@Repository
public interface KettleReposMapper<T extends Object> extends BaseMapper<T> {

	/**
	 * 功能描述：查询对象
	 * 
	 * @param entity
	 *            Object 查询条件
	 * @return T 返回单个对象
	 */
	public T selectById(String id) throws DataAccessException;
}
