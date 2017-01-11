package org.ukettle.engine.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukettle.service.router.service.RouterService;
import org.ukettle.system.service.*;
import org.ukettle.widget.kettle.service.*;
import org.ukettle.widget.quartz.service.QuartzGroupService;
import org.ukettle.widget.quartz.service.QuartzScheduleService;

@Service
public class BasicService {

	/** System Service info */
	@Autowired
	public LocaleService iLocaleService;
	@Autowired
	public UserService<Object> iUserService;
	@Autowired
	public RoleService<Object> iRoleService;
	@Autowired
	public MenuService<Object> iMenuService;
	@Autowired
	public RestService<Object> iRestService;

	/** Router Service info */
	@Autowired
	public RouterService<Object> iRouterService;

	/** Kettle Service info */
	@Autowired
	public KettleLogsService<Object> iKettleLogsService;
	@Autowired
	public KettleResultService<Object> iKettleResultService;
	@Autowired
	public KettleReposService<Object> iKettleReposService;
	@Autowired
	public KettleJobService<Object> iKettleJobService;
	@Autowired
	public KettleTransService<Object> iKettleTransService;
	@Autowired
	public KettleSpoonService<Object> iKettleSpoonService;
	
	/** Quartz Service info */
	@Autowired
	public QuartzGroupService<Object> iQuartzGroupService;
	@Autowired
	public QuartzScheduleService<Object> iQuartzScheduleService;
	
}