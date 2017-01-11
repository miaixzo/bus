package org.ukettle.widget.kettle.spring;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ukettle.engine.loader.BasicController;
import org.ukettle.engine.loader.BasicService;
import org.ukettle.widget.kettle.entity.KettleRepos;
import org.ukettle.www.toolkit.Constant;

@Controller
@RequestMapping(BasicController.VIEW_WIDGET + BasicController.VIEW_KETTLE
		+ BasicController.VIEW_REPOS)
public class KettleReposController extends BasicController {

	@Autowired
	private BasicService service;

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.GET)
	public String insertGet(HttpServletRequest request,
			HttpServletResponse response) {
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_INSERT;
	}

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.POST)
	public String insert(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleRepos entity = new KettleRepos();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iKettleReposService.insert(entity);
		return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_DELETE, method = RequestMethod.GET)
	public String delete(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleRepos entity = new KettleRepos();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		if (null != entity && null != entity.getId()) {
			service.iKettleReposService.delete(entity.getId());
		}
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.GET)
	public String update(HttpServletRequest request,
			HttpServletResponse response, String message) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleRepos entity = new KettleRepos();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity.setStatus(Constant.STATUS_ENABLED);
		entity = (KettleRepos) service.iKettleReposService.select(entity);
		request.setAttribute(ENTITY, entity);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_UPDATE;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.POST)
	public String update(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleRepos entity = new KettleRepos();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iKettleReposService.update(entity);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_LIST, method = RequestMethod.GET)
	public String list(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleRepos entity = new KettleRepos();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		List<?> list = service.iKettleReposService.selectByWhere(entity);
		request.setAttribute(LIST, list);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_VIEW, method = RequestMethod.GET)
	public String view(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		KettleRepos entity = new KettleRepos();
		entity.setStatus(Constant.STATUS_ENABLED);
		entity.setId(request.getParameter("id"));
		entity = (KettleRepos) service.iKettleReposService.select(entity);
		request.setAttribute(ENTITY, entity);
		return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_VIEW;
	}

	@RequestMapping(value = ACTION_LOADING, method = RequestMethod.POST)
	public void loading(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			Iterator<?> it = request.getParameterMap().entrySet().iterator();
			KettleRepos entity = new KettleRepos();
			while (it.hasNext()) {
				Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
				entity.setValue((String) ent.getKey(),
						((String[]) ent.getValue())[0]);
			}
			service.iKettleReposService.loading(entity);
			response.getWriter().write("success...");
		} catch (Exception e) {
			response.getWriter().write("error...");
			e.printStackTrace();
		} finally {
			response.getWriter().close();
		}
	}

	@RequestMapping(value = ACTION_DISCARD, method = RequestMethod.POST)
	public String discard(HttpServletRequest request,
			HttpServletResponse response) throws IOException, KettleException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleRepos entity = new KettleRepos();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iKettleReposService.discard(entity);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS + PAGE_LIST;
	}

}