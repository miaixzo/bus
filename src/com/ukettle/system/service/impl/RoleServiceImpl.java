package com.ukettle.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ukettle.engine.loader.BasicMapper;
import com.ukettle.system.entity.Role;
import com.ukettle.system.service.RoleService;
import com.ukettle.www.toolkit.ObjectID;


@Service
public class RoleServiceImpl<T extends Role> implements RoleService<T> {

	@Autowired
	private BasicMapper mapper;

	@Override
	public Object insert(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setId(ObjectID.id());
			if (((Integer) mapper.iRoleMapper.insert(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object update(T entity) throws DataAccessException {
		if (null != entity) {
			if (((Integer) mapper.iRoleMapper.update(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (((Integer) mapper.iRoleMapper.delete(id)) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iRoleMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iRoleMapper.selectByWhere(entity);
		}
		return null;
	}

}