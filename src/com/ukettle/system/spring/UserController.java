package com.ukettle.system.spring;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ukettle.engine.loader.BasicController;
import com.ukettle.system.entity.User;

@Controller
@RequestMapping(BasicController.VIEW_SYSTEM + BasicController.VIEW_USER)
public class UserController extends BasicController {

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.GET)
	public String insert() {
		return VIEW_SYSTEM + VIEW_USER + PAGE_INSERT;
	}

	@RequestMapping(value = ACTION_INSERT, method = RequestMethod.POST)
	public String insert(HttpServletRequest request,
			HttpServletResponse response) {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		User entity = new User();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iUserService.insert(entity);
		return REDIRECT + VIEW_SYSTEM + VIEW_USER + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_DELETE, method = RequestMethod.GET)
	public String delete(HttpServletRequest request,
			HttpServletResponse response) {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		User entity = new User();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iUserService.delete(entity.getId());
		return REDIRECT + VIEW_SYSTEM + VIEW_USER + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.GET)
	public String edit(HttpServletRequest request, HttpServletResponse response) {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		User entity = new User();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		entity = (User) service.iUserService.select(entity);
		request.setAttribute(ID, entity.getId());
		request.setAttribute(ENTITY, entity);
		return VIEW_SYSTEM + VIEW_USER + PAGE_UPDATE;
	}

	@RequestMapping(value = ACTION_UPDATE, method = RequestMethod.POST)
	public String update(HttpServletRequest request,
			HttpServletResponse response) {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		User entity = new User();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		service.iUserService.update(entity);
		return REDIRECT + VIEW_SYSTEM + VIEW_USER + ACTION_LIST;
	}

	@RequestMapping(value = ACTION_LIST, method = RequestMethod.GET)
	public String list(HttpServletRequest request, HttpServletResponse response) {
		Iterator<?> it = request.getParameterMap().entrySet().iterator();
		User entity = new User();
		while (it.hasNext()) {
			Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
			entity.setValue((String) ent.getKey(),
					((String[]) ent.getValue())[0]);
		}
		List<?> list = service.iUserService.selectByWhere(entity);
		request.setAttribute(LIST, list);
		return VIEW_SYSTEM + VIEW_USER + PAGE_LIST;
	}

}