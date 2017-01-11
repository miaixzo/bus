package org.ukettle.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.ukettle.basics.shiro.entity.Shiro;
import org.ukettle.engine.loader.BasicMapper;
import org.ukettle.system.entity.User;
import org.ukettle.system.service.UserService;
import org.ukettle.www.toolkit.DateUtils;
import org.ukettle.www.toolkit.Encode;
import org.ukettle.www.toolkit.ObjectID;
import org.ukettle.www.toolkit.Random;


@Service
public class UserServiceImpl<T extends User> implements UserService<T> {

	@Autowired
	private BasicMapper mapper;

	@Override
	public Object insert(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setId(ObjectID.id());
			entrypt(entity);
			entity.setCreator(Shiro.get().getEmail());
			entity.setCreated(DateUtils.getTime24());
			if (((Integer) mapper.iUserMapper.insert(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object update(T entity) throws DataAccessException {
		if (null != entity) {
			entrypt(entity);
			if (((Integer) mapper.iUserMapper.update(entity)) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public Object delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (((Integer) mapper.iUserMapper.delete(id)) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iUserMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iUserMapper.selectByWhere(entity);
		}
		return null;
	}

	private void entrypt(T entity) {
		byte[] random = Random.generate(Random.CODE_SIZE);
		entity.setRandom(Encode.encodeHex(random));
		byte[] password = Random.sha1(entity.getPassword().getBytes(), random,
				Random.ITERATION);
		entity.setPassword(Encode.encodeHex(password));
	}

}