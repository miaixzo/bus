package com.ukettle.widget.quartz.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ukettle.widget.quartz.entity.QuartzGroup;
import com.ukettle.widget.quartz.mapper.QuartzGroupMapper;
import com.ukettle.widget.quartz.service.QuartzGroupService;
import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.toolkit.ObjectID;

@Service
public class QuartzGroupServiceImpl<T extends QuartzGroup> implements QuartzGroupService<T> {

	@Autowired
	private QuartzGroupMapper<T> mapper;

	@Override
	public String insert(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setId(ObjectID.id());
			if (mapper.insert(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String update(T entity) throws DataAccessException {
		if (null != entity) {
			if (mapper.update(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (mapper.delete(id) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public T select(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.selectByWhere(entity);
		}
		return null;
	}

}