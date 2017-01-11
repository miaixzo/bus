package org.ukettle.basics.shiro.extend.authc;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * extends UsernamePasswordToken
 */
public class UsernamePassword2Token extends UsernamePasswordToken {

	private static final long serialVersionUID = 1L;
	private String captcha;
	private String locale;

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public UsernamePassword2Token() {
		super();
	}

	public UsernamePassword2Token(String username, char[] password,
			boolean rememberMe, String host, String locale, String captcha) {
		super(username, password, rememberMe, host);
		this.locale = locale;
		this.captcha = captcha;
	}

}