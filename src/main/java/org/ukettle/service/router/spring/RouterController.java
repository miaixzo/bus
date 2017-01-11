package org.ukettle.service.router.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.ukettle.engine.loader.BasicController;
import org.ukettle.service.router.entity.Router;

@Controller
@RequestMapping(BasicController.VIEW_ROUTER)
public class RouterController extends BasicController {

	private Router entity;

	@RequestMapping(value = ACTION_REST)
	public void rest(HttpServletRequest request, HttpServletResponse response) {
		entity = new Router(request, response);
		service.iRouterService.on(entity);
	}

	/**
	 * 获取当次请求中的所有参数信息。
	 * 
	 * @return object Object value
	 */
	protected Map<String, Object> getParams() {
		return entity.getParams();
	}

	/**
	 * *获取参数的中附带的URL或请求中的值。
	 * 
	 * @param name
	 *            Parameter name
	 * @return object Object value
	 */
	protected Object getParam(String name) {
		return entity.getParams().get(name);
	}

	/**
	 * 添加信息到该请求中<br>
	 * 开发人员可以将对象添加到 请求，<br>
	 * 所以该次请求后续处理中可以得到到它。
	 * 
	 * @param name
	 *            Parameter name
	 * @param object
	 *            Object value
	 */
	protected void setAttribute(String name, Object object) {
		entity.getRequest().setAttribute(name, object);
	}

}