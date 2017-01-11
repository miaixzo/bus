package com.ukettle.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ukettle.engine.loader.BasicMapper;
import com.ukettle.system.entity.Rest;
import com.ukettle.system.service.RestService;
import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.toolkit.ObjectID;


@Service
public class RestServiceImpl<T extends Rest> implements RestService<T> {

	@Autowired
	private BasicMapper mapper;

	@Override
	public Object insert(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setId(ObjectID.id());
			if (mapper.iRestMapper.insert(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object update(T entity) throws DataAccessException {
		if (null != entity) {
			if (((Integer) mapper.iRestMapper.update(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (((Integer) mapper.iRestMapper.delete(id)) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.iRestMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iRestMapper.selectByWhere(entity);
		}
		return null;
	}

}