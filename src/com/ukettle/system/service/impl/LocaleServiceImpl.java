package com.ukettle.system.service.impl;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.ukettle.system.service.LocaleService;


@Service
public class LocaleServiceImpl implements LocaleResolver, LocaleService {

	private MessageSource messages;
	private Locale defaultLocale;

	public void setMessages(MessageSource msg) {
		messages = msg;
	}

	public String getMessage(String key, Locale locale) {
		return messages.getMessage(key, new Object[0], locale);
	}

	public Locale resolveLocale(HttpServletRequest request) {
		defaultLocale = request.getLocale();
		return defaultLocale;
	}

	@Override
	public void setLocale(HttpServletRequest request,
			HttpServletResponse response, Locale locale) {
		locale(request, response, locale);
	}

	public String locale(HttpServletRequest request,
			HttpServletResponse response, Locale locale) {
		defaultLocale = null;
		String language = request.getParameter("language");
		if (null != locale) {
			defaultLocale = locale;
		}
		if (null != language && null == defaultLocale) {
			if ("zh_CN".equals(language)) {
				defaultLocale = new Locale("zh", "CN");
			} else if ("en_US".equals(language)) {
				defaultLocale = new Locale("en", "US");
			} else if ("ja_JP".equals(language)) {
				defaultLocale = new Locale("ja", "JP");
			} else {
				defaultLocale = request.getLocale();
			}
		}
		request.getSession().setAttribute(
				SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
				defaultLocale);
		request.getSession().setAttribute("language", language);
		return language;
	}

}