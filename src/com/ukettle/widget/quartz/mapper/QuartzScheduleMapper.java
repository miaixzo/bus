package com.ukettle.widget.quartz.mapper;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.ukettle.basics.base.mapper.BaseMapper;

@Repository
public interface QuartzScheduleMapper<T extends Object> extends BaseMapper<T> {

	public T exists(T entity) throws DataAccessException;
}
