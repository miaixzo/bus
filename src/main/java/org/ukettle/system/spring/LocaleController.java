package org.ukettle.system.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.ukettle.engine.loader.BasicController;
import org.ukettle.www.locale.Messages;

@Controller
@RequestMapping(value = BasicController.VIEW_LOCALE)
public class LocaleController extends BasicController {

	/**
	 * 如果language符合中文、英语等，那么就改变，否则使用浏览器默认的语言
	 * 
	 * @param language
	 *            选择的语言
	 * @param rurl
	 *            跳转的url
	 * @return 跳转的url
	 */
	@RequestMapping(method = RequestMethod.POST)
	public void language(HttpServletRequest request,
			HttpServletResponse response) {
		Messages.locale(request, response, null);
	}

}