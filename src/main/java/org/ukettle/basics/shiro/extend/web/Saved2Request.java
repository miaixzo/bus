package org.ukettle.basics.shiro.extend.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.util.SavedRequest;

public class Saved2Request extends SavedRequest {

	private static final long serialVersionUID = 6083702936438968056L;

	private String scheme;
	private String domain;
	private int port;
	private String contextPath;
	private String backUrl;

	public Saved2Request(HttpServletRequest request, String backUrl) {
		super(request);
		this.scheme = request.getScheme();
		this.domain = request.getServerName();
		this.port = request.getServerPort();
		this.backUrl = backUrl;
		this.contextPath = request.getContextPath();
	}

	public String getScheme() {
		return scheme;
	}

	public String getDomain() {
		return domain;
	}

	public int getPort() {
		return port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public String getRequestUrl() {
		String requestURI = getRequestURI();
		if (backUrl != null) {// 1
			if (backUrl.toLowerCase().startsWith("http://")
					|| backUrl.toLowerCase().startsWith("https://")) {
				return backUrl;
			} else if (!backUrl.startsWith(contextPath)) {// 2
				requestURI = contextPath + backUrl;
			} else {// 3
				requestURI = backUrl;
			}
		}

		StringBuilder requestUrl = new StringBuilder(scheme);// 4
		requestUrl.append("://");
		requestUrl.append(domain);// 5
		// 6
		if ("http".equalsIgnoreCase(scheme) && port != 80) {
			requestUrl.append(":").append(String.valueOf(port));
		} else if ("https".equalsIgnoreCase(scheme) && port != 443) {
			requestUrl.append(":").append(String.valueOf(port));
		}
		// 7
		requestUrl.append(requestURI);
		// 8
		if (backUrl == null && getQueryString() != null) {
			requestUrl.append("?").append(getQueryString());
		}
		return requestUrl.toString();
	}
	
}