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
import org.ukettle.widget.kettle.entity.KettleSpoon;
import org.ukettle.www.toolkit.Constant;

import net.sf.json.JSONArray;

@Controller
@RequestMapping(BasicController.VIEW_WIDGET + BasicController.VIEW_KETTLE
		+ BasicController.VIEW_SPOON)
public class KettleSpoonController extends BasicController {

	@Autowired
	private BasicService service;

	@RequestMapping(value = ACTION_EXECUTE, method = RequestMethod.GET)
	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String message = null;
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		message = (String) service.iKettleSpoonService.execute(entity);
		response.getWriter().write("Message:[ " + message + " ]");
		response.getWriter().close();
	}

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.GET)
	public String insert(HttpServletRequest request,
			HttpServletResponse response, String message) throws IOException {
		KettleRepos entity = new KettleRepos();
		entity.setStatus(Constant.STATUS_ENABLED);
		List<?> list = service.iKettleReposService.selectByWhere(entity);
		request.setAttribute(LIST, list);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + PAGE_INSERT;
	}

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.POST)
	public String insert(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		final KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity.setParams(JSONArray.fromObject(entity.getValue()).toString());
		entity.setTest(true);
		entity.setQueue(false);
		service.iKettleSpoonService.insert(entity);
		new Thread(new Runnable() {
			@Override
			public void run() {
				service.iKettleSpoonService.execute(entity);
			}
		}).start();
		return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_DELETE, method = RequestMethod.GET)
	public String delete(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		if (null != entity && null != entity.getId()) {
			service.iKettleSpoonService.delete(entity.getId());
		}
		return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.GET)
	public String updateGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity = (KettleSpoon) service.iKettleSpoonService.select(entity);
		List<?> list = service.iKettleReposService
				.selectByWhere(new KettleRepos());
		request.setAttribute(ENTITY, entity);
		request.setAttribute(LIST, list);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + PAGE_UPDATE;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.POST)
	public String update(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		final KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity.setParams(JSONArray.fromObject(entity.getValue()).toString());
		entity.setTest(true);
		entity.setQueue(false);
		service.iKettleSpoonService.update(entity);
		new Thread(new Runnable() {
			@Override
			public void run() {
				service.iKettleSpoonService.execute(entity);
			}
		}).start();
		return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_LIST, method = RequestMethod.GET)
	public String list(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		List<?> list = service.iKettleSpoonService.selectByWhere(entity);
		request.setAttribute(LIST, list);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_VIEW, method = RequestMethod.GET)
	public String view(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleSpoon entity = new KettleSpoon();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity = (KettleSpoon) service.iKettleSpoonService.select(entity);
		request.setAttribute(ENTITY, entity);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + PAGE_VIEW;
	}

	@RequestMapping(value = ACTION_TREE, method = RequestMethod.GET)
	public String treeGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_SPOON + PAGE_TREE;
	}

	@RequestMapping(value = ACTION_TREE, method = RequestMethod.POST)
	public void tree(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			Iterator<?> it = request.getParameterMap().entrySet().iterator();
			KettleRepos entity = new KettleRepos();
			while (it.hasNext()) {
				Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
				entity.setValue((String) ent.getKey(),
						((String[]) ent.getValue())[0]);
			}
			List<?> list = service.iKettleReposService.getJobAndTrans(entity);
			response.getWriter().write(JSONArray.fromObject(list).toString());
		} catch (KettleException e) {
			e.printStackTrace();
		} finally {
			response.getWriter().close();
		}
	}

	@SuppressWarnings("all")
	@RequestMapping(value = ACTION_GET, method = RequestMethod.POST)
	public void get(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			Iterator<?> it = request.getParameterMap().entrySet().iterator();
			KettleSpoon entity = new KettleSpoon();
			while (it.hasNext()) {
				Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
				entity.setValue((String) ent.getKey(),
						((String[]) ent.getValue())[0]);
			}
			Map<String, String> map = (Map<String, String>) service.iKettleReposService
					.getParameters(entity);
			response.getWriter().write(JSONArray.fromObject(map).toString());
		} catch (KettleException e) {
			e.printStackTrace();
		} finally {
			response.getWriter().close();
		}
	}
}