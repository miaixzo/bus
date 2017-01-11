package com.ukettle.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ukettle.basics.shiro.entity.Shiro;
import com.ukettle.engine.loader.BasicMapper;
import com.ukettle.system.entity.Menu;
import com.ukettle.system.service.MenuService;
import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.toolkit.DateUtils;
import com.ukettle.www.toolkit.ObjectID;


@Service
public class MenuServiceImpl<T extends Menu> implements MenuService<T> {

	@Autowired
	private BasicMapper mapper;

	@Override
	public Object insert(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setId(ObjectID.id());
			entity.setCreator(Shiro.get().getEmail());
			entity.setCreated(DateUtils.getTime24());
			if (((Integer) mapper.iMenuMapper.insert(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object update(T entity) throws DataAccessException {
		if (null != entity) {
			if (((Integer) mapper.iMenuMapper.update(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (((Integer) mapper.iMenuMapper.delete(id)) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iMenuMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iMenuMapper.selectByWhere(entity);
		}
		return null;
	}

	@Override
	public List<?> selectById(T entity) {
		if (null != entity) {
			entity.setUid(Shiro.get().getId());
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.iMenuMapper.selectById(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByPid(T entity) {
		if (null != entity) {
			entity.setUid(Shiro.get().getId());
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.iMenuMapper.selectByPid(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByUid(T entity) {
		if (null != entity) {
			entity.setUid(Shiro.get().getId());
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.iMenuMapper.selectByUid(entity);
		}
		return null;
	}

}