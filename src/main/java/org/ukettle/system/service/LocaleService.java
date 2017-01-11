package org.ukettle.system.service;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;

public interface LocaleService {

	public void setMessages(MessageSource messages);

	public String getMessage(String key, Locale locale);

	public Locale resolveLocale(HttpServletRequest request);

	public String locale(HttpServletRequest request,
			HttpServletResponse response, Locale locale);
}