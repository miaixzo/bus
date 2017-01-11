package com.ukettle.www.toolkit;

import java.io.FileInputStream;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogLevel;

/**
 * 常量类
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 01:33:21
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Constant extends Const {

	/** Public */
	public static final String VERSION = "4.4";
	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final String DEFAULT_TIMEZONE = "GMT+8";
	public static final String UKETTLE = "www.properties";
	public static final String SESSION_ID = "SESSION_ID";
	
	public static final String STATUS_ENABLED = "ENABLED";
	public static final String STATUS_DISABLED = "DISABLED";
	public static final String STATUS_FINISHED = "FINISHED";
	public static final String STATUS_CLOSED = "CLOSED";

	public static final String STATUS_ERROR = "ERROR";
	public static final String STATUS_AGAIN = "AGAIN";
	public static final String STATUS_COMPLETE = "COMPLETE";
	
	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_XML = "xml";
	public static final String FORMAT_STREAM = "stream";
	public static final int VALID_TIMESTAMP = 15;

	/** Kettle */
	public static final String TYPE_JOB = "job";
	public static final String TYPE_TRANS = "transformation";
	public static final String TYPE_JOB_SUFFIX = ".kjb";
	public static final String TYPE_TRANS_SUFFIX = ".ktr";
	public static final String TYPE_TESTING = "TESTING";
	public static final String TYPE_RUNNING = "RUNNING";
	public static final String TYPE_USER_KETTLE = "KETTLE";
	public static final String TYPE_AGAIN = "AGAIN";

	public static final String STARTS_WITH_USD = "$";
	public static final String STARTS_WITH_PARAM = "-param:";
	public static final String SPLIT_PARAM = "-param:";
	public static final String SPLIT_EQUAL = "=";
	public static final String SPLIT_USD = "$";
	public static final String KETTLE_REPO = "repo";
	
	public static String KETTLE_HOME;
	public static String KETTLE_PLUGIN;
	public static String KETTLE_SCRIPT;
	public static LogLevel KETTLE_LOGLEVEL;

	/** Mail */
	public static String MAIL_USERNAME;
	public static String MAIL_PASSWORD;
	public static String MAIL_SMTP;
	public static String MAIL_PORT;
	public static String MAIL_SENDER;
	public static String UKETTLE_TYPE_EDM = "EDM";
	public static String UKETTLE_TYPE_SMS = "SMS";
	public static String UKETTLE_SENDER = "uKettle";

	/** Quartz */
	public static String EXEC_PATH;

	public static Properties props;

	static {
		props = readProperties();
		MAIL_USERNAME = props.getProperty("com.ukettle.mail.username");
		MAIL_PASSWORD = props.getProperty("com.ukettle.mail.password");
		MAIL_SMTP = props.getProperty("com.ukettle.mail.smtp");
		MAIL_PORT = props.getProperty("com.ukettle.mail.port");
		MAIL_SENDER = props.getProperty("com.ukettle.mail.sender");
		EXEC_PATH = props.getProperty("com.ukettle.quartz.exec");
		KETTLE_HOME = uKettle() + props.getProperty("com.ukettle.kettle.home");
		KETTLE_PLUGIN = KETTLE_HOME + FILE_SEPARATOR
				+ props.getProperty("com.ukettle.kettle.plugin");
		KETTLE_SCRIPT = uKettle()
				+ props.getProperty("com.ukettle.kettle.script");
		KETTLE_LOGLEVEL = logger(props
				.getProperty("com.ukettle.kettle.loglevel"));

	}

	public static String get(String key) {
		return props.getProperty(key);
	}

	public static void setProps(Properties p) {
		props = p;
	}

	public static Properties readProperties() {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(Constant.class.getResource("/")
					.getPath().replace("%20", " ")
					+ UKETTLE));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	public static LogLevel logger(String level) {
		LogLevel logLevel = null;
		if ("basic".equals(level)) {
			logLevel = LogLevel.BASIC;
		} else if ("detail".equals(level)) {
			logLevel = LogLevel.DETAILED;
		} else if ("error".equals(level)) {
			logLevel = LogLevel.ERROR;
		} else if ("debug".equals(level)) {
			logLevel = LogLevel.DEBUG;
		} else if ("minimal".equals(level)) {
			logLevel = LogLevel.MINIMAL;
		} else if ("rowlevel".equals(level)) {
			logLevel = LogLevel.ROWLEVEL;
		} else {
			logLevel = KETTLE_LOGLEVEL;
		}
		return logLevel;
	}

	private static String uKettle() {
		String classPath = Constant.class.getResource("/").getPath()
				.replace("%20", " ");
		String iQuartz = "";
		String index = "WEB-INF";
		if (classPath.indexOf("target") > 0) {
			index = "target";
		}
		// windows path
		if ("\\".equals(Constant.FILE_SEPARATOR)) {
			iQuartz = classPath.substring(1, classPath.indexOf(index));
			iQuartz = iQuartz.replace("/", "\\");
		}
		// linux path
		if ("/".equals(Constant.FILE_SEPARATOR)) {
			iQuartz = classPath.substring(0, classPath.indexOf(index));
			iQuartz = iQuartz.replace("\\", "/");
		}
		return iQuartz;
	}

}