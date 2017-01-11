package org.ukettle.service.router.entity;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ukettle.system.entity.Rest;
import org.ukettle.www.https.HTTPD;
import org.ukettle.www.https.HTTPM;
import org.ukettle.www.serialize.Base64Serializer;
import org.ukettle.www.serialize.JsonSerializer;
import org.ukettle.www.serialize.XmlSerializer;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.StringUtils;

public class Router extends Rest {

	private static final long serialVersionUID = 7296930124937196873L;

	public Message message;
	public Object clazz;
	public String query;
	public String httpMethod;

	public File file;
	public String attachment;
	public String contentType;

	public Map<String, Object> params;
	public HttpServletRequest request;
	public HttpServletResponse response;

	public Router() {
	}

	public Router(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.init();
	}

	@SuppressWarnings("all")
	public void init() {
		params = new HashMap<String, Object>();
		params.put(Constant.FORMAT_XML, new XmlSerializer());
		params.put(Constant.FORMAT_JSON, new JsonSerializer());
		params.put(Constant.FORMAT_STREAM, new Base64Serializer());
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			params.put(name, request.getParameter(name));
		}
		Enumeration<String> attributeNames = request.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			params.put(name, request.getAttribute(name));
		}
		initParams();
	}

	public void initParams() {
		id = (String) params.get("id");
		nonce = (String) params.get("nonce");
		token = (String) params.get("token");
		signature = (String) params.get("signature");
		timestamp = (String) params.get("timestamp");
		echostr = (String) params.get("echostr");
		format = (String) params.get("format");
		method = (String) params.get("method");
		secret = (String) params.get("secret");
		remote = getRemote(request);
		query = request.getQueryString();
		if (null != format) {
			clazz = params.get(format);
			if (null == clazz) {
				clazz = params.get(Constant.FORMAT_XML);
			}
		} else {
			format = Constant.FORMAT_XML;
			clazz = params.get(format);
		}
		HTTPM methods = HTTPM.valueOf(request.getMethod());
		if (methods == HTTPM.POST) {
			httpMethod = HTTPD.POST;
		} else if (methods == HTTPM.GET) {
			httpMethod = HTTPD.GET;
		}
	}

	/**
	 * 返回远程地址，包括协议。例如： 192.168.12.10<br>
	 * 检查是否有转发，并x_forwarded_for的情况下，<br>
	 * 有 客户端和服务器之间的代理。
	 * 
	 * @return string url value
	 */
	private String getRemote(HttpServletRequest request) {
		if (null == request)
			return "";
		String ip = request.getHeader("X-Requested-For");
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Forwarded-For");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			if ("127.0.0.1".equals(ip) || "localhost".equals(ip.toLowerCase())) {
				try {
					// 根据网卡取本机配置的IP
					InetAddress inet = InetAddress.getLocalHost();
					ip = inet.getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ip != null && ip.length() > 15) {
			if (ip.indexOf(",") > 0) {
				ip = ip.substring(0, ip.indexOf(","));
			}
		}
		return ip;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Object getClazz() {
		return clazz;
	}

	public void setClazz(Object clazz) {
		this.clazz = clazz;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

}