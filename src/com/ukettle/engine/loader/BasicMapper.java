package com.ukettle.engine.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ukettle.service.router.mapper.RouterMapper;
import com.ukettle.system.mapper.MenuMapper;
import com.ukettle.system.mapper.RestMapper;
import com.ukettle.system.mapper.RoleMapper;
import com.ukettle.system.mapper.UserMapper;
import com.ukettle.widget.kettle.mapper.KettleLogsMapper;
import com.ukettle.widget.kettle.mapper.KettleResultMapper;
import com.ukettle.widget.kettle.mapper.KettleSpoonMapper;
import com.ukettle.widget.kettle.mapper.KettleReposMapper;
import com.ukettle.widget.quartz.mapper.QuartzGroupMapper;
import com.ukettle.widget.quartz.mapper.QuartzScheduleMapper;

@Repository
public class BasicMapper {

	/** System Mapper info */
	@Autowired
	public UserMapper<Object> iUserMapper;
	@Autowired
	public RoleMapper<Object> iRoleMapper;
	@Autowired
	public MenuMapper<Object> iMenuMapper;
	@Autowired
	public RestMapper<Object> iRestMapper;

	/** Router Mapper info */
	@Autowired
	public RouterMapper<Object> iRouterMapper;

	/** Kettle Mapper info */
	@Autowired
	public KettleLogsMapper<Object> iKettleLogsMapper;
	@Autowired
	public KettleResultMapper<Object> iKettleResultMapper;
	@Autowired
	public KettleReposMapper<Object> iKettleReposMapper;
	@Autowired
	public KettleSpoonMapper<Object> iKettleSpoonMapper;
	
	/** Quartz Mapper info */
	@Autowired
	public QuartzGroupMapper<Object> iQuartzGroupMapper;
	@Autowired
	public QuartzScheduleMapper<Object> iQuartzScheduleMapper;

}