package com.ukettle.widget.kettle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ukettle.basics.shiro.entity.Shiro;
import com.ukettle.engine.loader.BasicMapper;
import com.ukettle.widget.kettle.entity.KettleLogs;
import com.ukettle.widget.kettle.service.KettleLogsService;
import com.ukettle.www.toolkit.DateUtils;
import com.ukettle.www.toolkit.ObjectID;

@Service
public class KettleLogsServiceImpl<T extends KettleLogs> implements
		KettleLogsService<T> {

	@Autowired
	private BasicMapper mapper;

	@Override
	public String insert(T entity) throws DataAccessException {
		if (null != entity) {
			String id = Shiro.get().getId();
			entity.setId(ObjectID.id());
			entity.setCreator(id);
			entity.setModifier(id);
			entity.setCreated(DateUtils.getTime24());
			entity.setModified(DateUtils.getTime24());
			if (mapper.iKettleLogsMapper.insert(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String update(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setModified(DateUtils.getTime24());
			if (mapper.iKettleLogsMapper.update(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (mapper.iKettleLogsMapper.delete(id) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleLogsMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleLogsMapper.selectByWhere(entity);
		}
		return null;
	}

}