package org.ukettle.basics.shiro.extend.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;

import org.ukettle.basics.shiro.extend.web.Saved2Request;


public class Authentication2Filter extends AuthenticationFilter {

	protected boolean isAccessAllowed(ServletRequest request,
			ServletResponse response, Object mappedValue) {
		Subject subject = getSubject(request, response);
		return subject.isAuthenticated();
	}

	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception {
		String backUrl = request.getParameter("backUrl");
		saveRequest(request, backUrl,
				getDefaultBackUrl(WebUtils.toHttp(request)));
		return false;
	}

	protected void saveRequest(ServletRequest request, String backUrl,
			String fallbackUrl) {
		Subject subject = SecurityUtils.getSubject();
		Session session = subject.getSession();
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		session.setAttribute("authc.fallbackUrl", fallbackUrl);
		SavedRequest savedRequest = new Saved2Request(httpRequest, backUrl);
		session.setAttribute(WebUtils.SAVED_REQUEST_KEY, savedRequest);
	}

	private String getDefaultBackUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		String domain = request.getServerName();
		int port = request.getServerPort();
		String contextPath = request.getContextPath();
		StringBuilder backUrl = new StringBuilder(scheme);
		backUrl.append("://");
		backUrl.append(domain);
		if ("http".equalsIgnoreCase(scheme) && port != 80) {
			backUrl.append(":").append(String.valueOf(port));
		} else if ("https".equalsIgnoreCase(scheme) && port != 443) {
			backUrl.append(":").append(String.valueOf(port));
		}
		backUrl.append(contextPath);
		backUrl.append(getSuccessUrl());
		return backUrl.toString();
	}
}