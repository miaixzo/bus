package com.ukettle.system.entity;

import com.ukettle.basics.base.entity.BaseEntity;

public class Rest extends BaseEntity {

	private static final long serialVersionUID = -321173140738173917L;

	public String secret;
	public String session;
	public String format;
	public String method;

	public String timestamp;
	public String signature;
	public String token;
	public String url;
	public String version;
	public String nonce;
	public String echostr;
	public String remote;

	public void setValue(String key, String value) {
		super.setValue(key, value);
		if ("secret".equals(key)) {
			this.setSecret((String) value);
		} else if ("session".equals(key)) {
			this.setSession((String) value);
		} else if ("format".equals(key)) {
			this.setFormat((String) value);
		} else if ("method".equals(key)) {
			this.setMethod((String) value);
		} else if ("timestamp".equals(key)) {
			this.setTimestamp((String) value);
		} else if ("token".equals(key)) {
			this.setToken((String) value);
		} else if ("url".equals(key)) {
			this.setUrl((String) value);
		} else if ("version".equals(key)) {
			this.setVersion((String) value);
		}
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getEchostr() {
		return echostr;
	}

	public void setEchostr(String echostr) {
		this.echostr = echostr;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

}