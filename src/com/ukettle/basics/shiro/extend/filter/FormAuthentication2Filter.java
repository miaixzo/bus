package com.ukettle.basics.shiro.extend.filter;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;

import com.ukettle.basics.shiro.extend.authc.UsernamePassword2Token;
import com.ukettle.www.toolkit.Captcha;

public class FormAuthentication2Filter extends FormAuthenticationFilter {

	protected String getCaptcha(ServletRequest request) {
		return WebUtils.getCleanParam(request, Captcha.KEY);
	}

	protected String getLocale(ServletRequest request) {
		Locale locale = request.getLocale();
		if ("en".equals(locale.getLanguage())) {
			return "en_US";
		} else if ("zh".equals(locale.getLanguage())) {
			return "zh_CN";
		}
		return null;
	}

	@Override
	protected AuthenticationToken createToken(ServletRequest request,
			ServletResponse response) {
		String username = getUsername(request);
		String password = getPassword(request);
		String captcha = getCaptcha(request);
		boolean rememberMe = isRememberMe(request);
		String locale = getLocale(request);
		String host = getHost(request);
		return new UsernamePassword2Token(username,
				null != password ? password.toCharArray() : null, rememberMe,
				host, locale, captcha);
	}

	/**
	 * 覆盖默认实现，用sendRedirect直接跳出框架，以免造成js框架重复加载js出错。
	 * 
	 * @param token
	 * @param subject
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token,
			Subject subject, ServletRequest request, ServletResponse response)
			throws Exception {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (!"XMLHttpRequest".equalsIgnoreCase(httpRequest
				.getHeader("X-Requested-With"))) {
			httpResponse.sendRedirect(httpRequest.getContextPath()
					+ this.getSuccessUrl());
		} else {
			httpRequest.getRequestDispatcher("/CN").forward(httpRequest,
					httpResponse);
		}
		return false;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) {
		try {
			// 先判断是否是登录操作
			if (isLoginSubmission(request, response)) {
				return false;
			}
		} catch (Exception e) {
		}
		return super.isAccessAllowed(request, response, mappedValue);
	}

	/**
	 * 所有请求都会经过的方法。
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception {
		onForwardUrl(request);
		if (isLoginRequest(request, response)) {
			if (isLoginSubmission(request, response)) {
				String name = getUsername(request);
				String pass = getPassword(request);
				if (null != name && !"".equals(name) && null != pass
						&& !"".equals(pass)) {
					return executeLogin(request, response);
				}
				saveRequestAndRedirectToLogin(request, response);
				return false;
			}
			return true;
		} else {
			if (!"XMLHttpRequest"
					.equalsIgnoreCase(((HttpServletRequest) request)
							.getHeader("X-Requested-With"))) {
				saveRequestAndRedirectToLogin(request, response);
			}
			return false;
		}
	}

	/**
	 * 获取重定向跳转url
	 */
	public String onForwardUrl(ServletRequest request) {
		SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
		String url = getSuccessUrl();
		if (savedRequest != null
				&& savedRequest.getMethod().equalsIgnoreCase(
						AccessControlFilter.GET_METHOD)) {
			url = savedRequest.getRequestUrl();
			request.setAttribute("backUrl", url);
		}
		return url;
	}
}