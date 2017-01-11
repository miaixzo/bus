package org.ukettle.system.spring;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.Session;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.ukettle.basics.shiro.entity.Shiro;
import org.ukettle.engine.loader.BasicController;
import org.ukettle.www.locale.Messages;

@Controller
public class IndexController extends BasicController {

	private static Locale defaultLocale;

	@RequestMapping(value = ACTION_INDEX, method = RequestMethod.GET)
	public String index() {
		Shiro shiro = Shiro.get();
		if (null != shiro && null != shiro.getId()) {
			return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS
					+ ACTION_LIST;
		}
		Session session = Shiro.getSession();
		if (null != session) {
			session.stop();
		}
		return PAGE_INDEX;
	}

	@RequestMapping(value = ACTION_INDEX, method = RequestMethod.POST)
	public String index(
			@RequestParam(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM) String username,
			Map<String, Object> map, HttpServletRequest request,
			HttpServletResponse response) {
		defaultLocale = request.getLocale();
		Shiro shiro = Shiro.get();
		if (null != shiro && null != username
				&& shiro.getEmail().equals(username)) {
			return REDIRECT + VIEW_WIDGET + VIEW_KETTLE + VIEW_REPOS
					+ ACTION_LIST;
		}
		String message = exception(request);
		map.put("message", message);
		map.put("username", username);
		return PAGE_INDEX;
	}

	private String exception(HttpServletRequest request) {
		String error = (String) request
				.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		String message = Messages.getMessage("com.chamago.iQuartz.Shiro.Logged",
				defaultLocale);
		if (error != null) {
			if ("org.apache.shiro.authc.UnknownAccountException".equals(error)) {
				message = Messages.getMessage("org.ukettle.Shiro.Unknown",
						defaultLocale);
			} else if ("org.apache.shiro.authc.IncorrectCredentialsException"
					.equals(error)) {
				message = Messages.getMessage("org.ukettle.Shiro.Pass.Error",
						defaultLocale);
			} else if ("org.ukettle.shiro.CaptchaException".equals(error)) {
				message = Messages.getMessage("org.ukettle.Shiro.Captcha",
						defaultLocale);
			} else if ("org.apache.shiro.authc.AuthenticationException"
					.equals(error)) {
				message = Messages.getMessage("org.ukettle.Shiro.Failed",
						defaultLocale);
			} else if ("org.apache.shiro.authc.DisabledAccountException"
					.equals(error)) {
				message = Messages.getMessage("org.ukettle.Shiro.Disabled",
						defaultLocale);
			}
		}
		return message;
	}

}