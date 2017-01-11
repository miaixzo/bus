package org.ukettle.engine.loader;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.ukettle.basics.base.spring.Controller;
import org.ukettle.basics.page.PageContext;

public abstract class BasicController extends Controller {

	@Autowired
	public BasicService service;

	/** Public Info */
	public static final String REDIRECT = "redirect:";
	public static final String FORWARD = "forward:";
	public static final String MESSAGE = "Message";
	public static final String ID = "Id";
	public static final String ENTITY = "Entity";
	public static final String LIST = "List";
	public static final String PAGE = "Page";
	public static final String SEPARATOR = "/";

	/** Public View Info */
	public static final String VIEW_USER = SEPARATOR + "User";
	public static final String VIEW_ROLE = SEPARATOR + "Role";
	public static final String VIEW_MENU = SEPARATOR + "Menu";
	public static final String VIEW_FILE = SEPARATOR + "File";
	public static final String VIEW_RULE = SEPARATOR + "Rule";
	public static final String VIEW_IMAGE = SEPARATOR + "Image";
	public static final String VIEW_MEDIA = SEPARATOR + "Media";
	public static final String VIEW_SPOON = SEPARATOR + "Spoon";
	public static final String VIEW_REPOS = SEPARATOR + "Repos";
	public static final String ACTION_TEXT = SEPARATOR + "Text";
	public static final String VIEW_SYSTEM = SEPARATOR + "System";
	public static final String VIEW_CENTER = SEPARATOR + "Center";
	public static final String VIEW_WIDGET = SEPARATOR + "Widget";
	public static final String VIEW_KETTLE = SEPARATOR + "Kettle";
	public static final String VIEW_RESULT = SEPARATOR + "Result";
	public static final String VIEW_PLUGIN = SEPARATOR + "Plugin";
	public static final String VIEW_ROUTER = SEPARATOR + "router";
	public static final String VIEW_LOCALE = SEPARATOR + "Locale";
	public static final String VIEW_QUARTZ = SEPARATOR + "Quartz";
	public static final String VIEW_DETAIL = SEPARATOR + "Detail";
	public static final String VIEW_SERVICE = SEPARATOR + "Service";

	/** Public Action Info */
	public static final String ACTION_CN = SEPARATOR + "CN";
	public static final String ACTION_EN = SEPARATOR + "EN";
	public static final String ACTION_LIST = SEPARATOR + "List";
	public static final String ACTION_SHOW = SEPARATOR + "Show";
	public static final String ACTION_VIEW = SEPARATOR + "View";
	public static final String ACTION_SYNC = SEPARATOR + "Sync";
	public static final String ACTION_TREE = SEPARATOR + "Tree";
	public static final String ACTION_INDEX = SEPARATOR + "Index";
	public static final String ACTION_PAUSE = SEPARATOR + "Pause";
	public static final String ACTION_INSERT = SEPARATOR + "Insert";
	public static final String ACTION_DELETE = SEPARATOR + "Delete";
	public static final String ACTION_UPDATE = SEPARATOR + "Update";
	public static final String ACTION_SEARCH = SEPARATOR + "Search";
	public static final String ACTION_EXPORT = SEPARATOR + "Export";
	public static final String ACTION_IMPORT = SEPARATOR + "Import";
	public static final String ACTION_DETAIL = SEPARATOR + "Detail";
	public static final String ACTION_RESUME = SEPARATOR + "Resume";
	public static final String ACTION_UPLOAD = SEPARATOR + "Upload";
	
	public static final String ACTION_EXECUTE = SEPARATOR + "Execute";
	public static final String ACTION_LOADING = SEPARATOR + "Loading";
	public static final String ACTION_STATUS = SEPARATOR + "Status";
	public static final String ACTION_DISCARD = SEPARATOR + "Discard";
	public static final String ACTION_CAPTCHA = SEPARATOR + "Captcha";
	public static final String ACTION_ENABLED = SEPARATOR + "Enabled";
	public static final String ACTION_DISABLED = SEPARATOR + "Disabled";
	public static final String ACTION_DOWNLOAD = SEPARATOR + "Download";
	public static final String ACTION_EXISTING = SEPARATOR + "Existing";
	
	public static final String ACTION_REST = SEPARATOR + "rest";
	public static final String ACTION_GET = SEPARATOR + "get";

	/** Public Page Info */
	public static final String PAGE_LIST = SEPARATOR + "list";
	public static final String PAGE_SHOW = SEPARATOR + "show";
	public static final String PAGE_VIEW = SEPARATOR + "view";
	public static final String PAGE_TEXT = SEPARATOR + "text";
	public static final String PAGE_TREE = SEPARATOR + "tree";
	public static final String PAGE_INDEX = SEPARATOR + "index";
	public static final String PAGE_INSERT = SEPARATOR + "insert";
	public static final String PAGE_DELETE = SEPARATOR + "delete";
	public static final String PAGE_UPDATE = SEPARATOR + "update";
	public static final String PAGE_SEARCH = SEPARATOR + "search";
	public static final String PAGE_SELECT = SEPARATOR + "select";
	public static final String PAGE_EXPORT = SEPARATOR + "export";
	public static final String PAGE_IMPORT = SEPARATOR + "import";
	public static final String PAGE_RECEIVE = SEPARATOR + "receive";
	public static final String PAGE_SETTING = SEPARATOR + "setting";

	/**
	 * PageSize(页大小)
	 */
	public int getSize(HttpServletRequest request) {
		String size = request.getParameter("size");
		return null != size ? Integer.parseInt(size) : PageContext
				.getPageContext().getSize();
	}

	/**
	 * Limit(当前页)
	 */
	public int getLimit(HttpServletRequest request) {
		String limit = request.getParameter("limit");
		return null != limit ? Integer.parseInt(limit) : PageContext
				.getPageContext().getLimit();
	}

}