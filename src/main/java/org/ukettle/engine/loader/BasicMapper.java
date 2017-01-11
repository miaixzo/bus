package org.ukettle.engine.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.ukettle.service.router.mapper.RouterMapper;
import org.ukettle.system.mapper.MenuMapper;
import org.ukettle.system.mapper.RestMapper;
import org.ukettle.system.mapper.RoleMapper;
import org.ukettle.system.mapper.UserMapper;
import org.ukettle.widget.kettle.mapper.KettleLogsMapper;
import org.ukettle.widget.kettle.mapper.KettleReposMapper;
import org.ukettle.widget.kettle.mapper.KettleResultMapper;
import org.ukettle.widget.kettle.mapper.KettleSpoonMapper;
import org.ukettle.widget.quartz.mapper.QuartzGroupMapper;
import org.ukettle.widget.quartz.mapper.QuartzScheduleMapper;

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