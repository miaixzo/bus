package com.ukettle.widget.quartz.spring;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ukettle.engine.loader.BasicController;
import com.ukettle.engine.loader.BasicService;
import com.ukettle.widget.kettle.entity.KettleSpoon;
import com.ukettle.widget.quartz.entity.QuartzGroup;
import com.ukettle.widget.quartz.entity.QuartzSchedule;
import com.ukettle.www.toolkit.Constant;

@Controller
@RequestMapping(BasicController.VIEW_WIDGET + BasicController.VIEW_QUARTZ)
public class QuartzController extends BasicController {

	private Map<String, List<String>> jobClassesMap = new HashMap<String, List<String>>();

	@Autowired
	private BasicService service;

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.GET)
	public String insert(HttpServletRequest request,
			HttpServletResponse response, Object object) {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity.setParams(JSONArray.fromObject(entity.getValue()).toString());
		request.setAttribute(ENTITY, entity);
		List<?> list = service.iQuartzGroupService
				.selectByWhere(new QuartzGroup());
		request.setAttribute(LIST, list);
		return VIEW_WIDGET + VIEW_QUARTZ + PAGE_INSERT;
	}

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.POST)
	public String insert(HttpServletRequest request,
			HttpServletResponse response) throws SchedulerException,
			ParseException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		QuartzSchedule entity = new QuartzSchedule();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		if (!service.iQuartzScheduleService.exists(entity)) {
			service.iQuartzScheduleService.insert(entity);
		}
		return REDIRECT + VIEW_WIDGET + VIEW_QUARTZ + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_DELETE, method = RequestMethod.GET)
	public String delete(HttpServletRequest request,
			HttpServletResponse response) throws SchedulerException {
		String jobKeys = request.getParameter("id");
		if (null != jobKeys) {
			service.iQuartzScheduleService.delete(jobKeys);
		}
		return REDIRECT + VIEW_WIDGET + VIEW_QUARTZ + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.GET)
	public String edit(HttpServletRequest request, HttpServletResponse response)
			throws SchedulerException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		QuartzSchedule entity = new QuartzSchedule();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		List<?> list = service.iQuartzGroupService
				.selectByWhere(new QuartzGroup());
		request.setAttribute(LIST, list);
		Map<?, ?> map = (Map<?, ?>) service.iQuartzScheduleService
				.select(entity);
		request.setAttribute(ID, entity.getId());
		request.setAttribute(ENTITY, map);
		return VIEW_WIDGET + VIEW_QUARTZ + PAGE_UPDATE;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.POST)
	public String update(HttpServletRequest request,
			HttpServletResponse response) throws SchedulerException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		QuartzSchedule entity = new QuartzSchedule();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		if (!service.iQuartzScheduleService.exists(entity)) {
			service.iQuartzScheduleService.update(entity);
		}
		return REDIRECT + VIEW_WIDGET + VIEW_QUARTZ + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_VIEW, method = RequestMethod.GET)
	public String view(HttpServletRequest request, HttpServletResponse response) {
		try {
			Iterator<?> it = request.getParameterMap().entrySet().iterator();
			QuartzSchedule entity = new QuartzSchedule();
			while (it.hasNext()) {
				Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
				entity.setValue((String) ent.getKey(),
						((String[]) ent.getValue())[0]);
			}
			List<?> list = service.iQuartzGroupService
					.selectByWhere(new QuartzGroup());
			request.setAttribute(LIST, list);
			Map<?, ?> map = (Map<?, ?>) service.iQuartzScheduleService
					.select(entity);
			request.setAttribute(ENTITY, map);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return VIEW_WIDGET + VIEW_QUARTZ + PAGE_VIEW;
	}

	@RequestMapping(value = ACTION_LIST)
	public String quartz(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			Iterator<?> it = request.getParameterMap().entrySet().iterator();
			QuartzSchedule entity = new QuartzSchedule();
			while (it.hasNext()) {
				Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
				entity.setValue((String) ent.getKey(),
						((String[]) ent.getValue())[0]);
			}
			request.setAttribute("group", entity.getGroup());
			request.setAttribute("title", entity.getName());
			Map<?, ?> map = (Map<?, ?>) service.iQuartzScheduleService
					.selectByWhere(entity);
			List<?> list = service.iQuartzGroupService
					.selectByWhere(new QuartzGroup());
			request.setAttribute(LIST, list);
			request.setAttribute(ENTITY, map);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return VIEW_WIDGET + VIEW_QUARTZ + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_EXISTING, method = RequestMethod.POST)
	public void exist(HttpServletRequest request, HttpServletResponse response)
			throws SchedulerException, Exception {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		QuartzSchedule entity = new QuartzSchedule();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		String exist = "0";
		if (service.iQuartzScheduleService.exists(entity)) {
			exist = "1";
		}
		response.getWriter().write("" + exist);
		response.getWriter().close();
	}

	@RequestMapping(value = ACTION_EXECUTE)
	public String execute(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String jobKey = request.getParameter("id");
			if (null != jobKey) {
				service.iQuartzScheduleService.execute(jobKey);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return VIEW_WIDGET + VIEW_QUARTZ + PAGE_LIST;

	}

	@RequestMapping(value = ACTION_PAUSE)
	public String pause(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			String jobKeys = request.getParameter("id");
			if (null != jobKeys) {
				service.iQuartzScheduleService.pause(jobKeys);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return REDIRECT + VIEW_WIDGET + VIEW_QUARTZ + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_RESUME)
	public String resume(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String jobKeys = request.getParameter("id");
			if (null != jobKeys) {
				service.iQuartzScheduleService.resume(jobKeys);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return REDIRECT + VIEW_WIDGET + VIEW_QUARTZ + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_TREE, method = RequestMethod.GET)
	public String eTree(HttpServletRequest request, HttpServletResponse response) {
		URL base = getClass().getClassLoader().getResource("");
		String basePath = base.getFile();
		String jobPackage = basePath
				+ Constant.EXEC_PATH.replaceAll("\\.", "/");
		listClazz(jobPackage);
		request.setAttribute(ENTITY, jobClassesMap);
		return VIEW_WIDGET + VIEW_QUARTZ + PAGE_TREE;
	}

	private void listClazz(String jobPackage) {
		List<String> classesList = new ArrayList<String>();
		File packageFolder = new File(jobPackage);
		File[] jobClassFiles = packageFolder.listFiles();
		if (jobClassFiles != null && jobClassFiles.length > 0) {
			for (File jobClassFile : jobClassFiles) {
				if (jobClassFile.isDirectory()) {
					try {
						listClazz(jobClassFile.getCanonicalPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (jobClassFile.getName().matches(".*\\.class")) {
					String[] clazz = jobClassFile.getName().split("\\.");
					classesList.add(clazz[0]);
				}
			}
		}
		if (classesList.size() > 0) {
			jobClassesMap.put(Constant.EXEC_PATH.replace("\\", "."),
					classesList);
		}
	}

}