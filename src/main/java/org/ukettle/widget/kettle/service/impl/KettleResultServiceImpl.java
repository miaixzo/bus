package org.ukettle.widget.kettle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.ukettle.basics.shiro.entity.Shiro;
import org.ukettle.engine.loader.BasicMapper;
import org.ukettle.widget.kettle.entity.KettleResult;
import org.ukettle.widget.kettle.service.KettleResultService;
import org.ukettle.www.toolkit.DateUtils;
import org.ukettle.www.toolkit.ObjectID;

@Service
public class KettleResultServiceImpl<T extends KettleResult> implements
		KettleResultService<T> {

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
			if (mapper.iKettleResultMapper.insert(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String update(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setModified(DateUtils.getTime24());
			if (mapper.iKettleResultMapper.update(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (mapper.iKettleResultMapper.delete(id) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleResultMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleResultMapper.selectByWhere(entity);
		}
		return null;
	}

}