package com.ukettle.widget.kettle.spring;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ukettle.engine.loader.BasicController;
import com.ukettle.engine.loader.BasicService;
import com.ukettle.widget.kettle.entity.KettleLogs;
import com.ukettle.widget.kettle.entity.KettleResult;

@Controller
@RequestMapping(BasicController.VIEW_WIDGET + BasicController.VIEW_KETTLE
		+ BasicController.VIEW_RESULT)
public class KettleResultController extends BasicController {

	@Autowired
	private BasicService service;

	@RequestMapping(value = ACTION_DELETE, method = RequestMethod.GET)
	public String delete(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleResult entity = new KettleResult();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iKettleResultService.delete(entity.getId());
		return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_VIEW, method = RequestMethod.GET)
	public String view(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleResult entity = new KettleResult();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity = (KettleResult) service.iKettleResultService.select(entity);
		request.setAttribute("entity", entity);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_RESULT + PAGE_VIEW;
	}

	@RequestMapping(value = ACTION_LIST, method = RequestMethod.GET)
	public String kitchen(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleResult entity = new KettleResult();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		List<?> list = service.iKettleResultService.selectByWhere(entity);
		request.setAttribute(LIST, list);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_RESULT + PAGE_LIST;
	}

	@RequestMapping(value = ACTION_TEXT, method = RequestMethod.GET)
	public String logs(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		KettleLogs entity = new KettleLogs();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity = (KettleLogs) service.iKettleLogsService.select(entity);
		request.setAttribute(ENTITY, entity);
		return VIEW_WIDGET + VIEW_KETTLE + VIEW_RESULT + PAGE_TEXT;
	}

}