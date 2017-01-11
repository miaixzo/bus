package com.ukettle.engine.kettle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.pentaho.di.core.JndiUtil;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.KettleLifecycleSupport;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;

import com.ukettle.www.toolkit.Constant;

/**
 * <p>
 * Kettle环境初始化.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 19, 2014
 * @Time 11:45:33
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class KettleEnvironment {

	private static Class<?> PKG = Constant.class;
	private static Boolean initialized;

	/**
	 * 初始化Kettle环境。 此方法将尝试配置 简单的JNDI， 通过简单地调用init(true)。
	 * 
	 * @see #KettleEnvironment.init(boolean)
	 * 
	 * @throws KettleException
	 *             在初始化过程中发生的任何错误都将抛出 KettleException。
	 */
	public static void init() throws KettleException {
		init(true);
	}

	/**
	 * 初始化Ketle环境。此方法执行以下 操作：
	 * 
	 * 创建一个Kettle "home" 的目录，如果它已经不存在 - 读取 在kettle.properties文件 - 初始化记录后端 - 设置
	 * 控制台日志级别调试 - 如果指定的参数，配置 简单的JNDI - 寄存器的各种原生类型和插件 插件类型 - 读取变量列表 - 初始化生命周期
	 * 启动监听程序等
	 * 
	 * @param simpleJndi
	 *            ， 真正简单的JNDI配置，否则返回false
	 * @throws KettleException
	 *             在初始化过程中发生的任何错误都将抛出 KettleException。
	 */
	public static void init(boolean simpleJndi) throws KettleException {
		if (initialized == null) {
			// 创建一个Kettle "home" 的目录
			// createKettleHome();
			// 初始化 kettle.properties 初始化其他属性等
			environmentInit();
			// 初始化日志
			CentralLogStore.init();
			// 设置控制台日志级用来调试
			LogWriter.setConsoleAppenderDebug();
			// 配置简单的JNDI 仅供我们在单机模式运行
			if (simpleJndi) {
				JndiUtil.initJNDI();
			}
			// 注册原生类型和各个所需的插件
			PluginRegistry.addPluginType(StepPluginType.getInstance());
			PluginRegistry.addPluginType(PartitionerPluginType.getInstance());
			PluginRegistry.addPluginType(JobEntryPluginType.getInstance());
			PluginRegistry.addPluginType(RepositoryPluginType.getInstance());
			PluginRegistry.addPluginType(DatabasePluginType.getInstance());
			PluginRegistry.addPluginType(LifecyclePluginType.getInstance());
			PluginRegistry.addPluginType(KettleLifecyclePluginType
					.getInstance());
			PluginRegistry.addPluginType(ImportRulePluginType.getInstance());
			PluginRegistry.init();
			// 初始化读取的变量列表。
			KettleVariablesList.init();
			// 初始化生命周期监听器
			initLifecycleListeners();
			initialized = true;
		}
	}

	/**
	 * 提醒所有生命周期的插件水壶环境。 初始化。
	 * 
	 * @throws KettleException
	 *             当一个生命周期侦听器抛出一个异常
	 */
	private static void initLifecycleListeners() throws KettleException {
		final KettleLifecycleSupport s = new KettleLifecycleSupport();
		s.onEnvironmentInit();
		// 注册关闭hook监听的调用OnExit()方法
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					s.onEnvironmentShutdown();
				} catch (Throwable t) {
					System.err.println(BaseMessages
							.getString(PKG,
									"LifecycleSupport.ErrorInvokingKettleEnvironmentShutdownListeners"));
					t.printStackTrace();
				}
			};
		});

	}

	/**
	 * 创建Kettle home,这是一个目录,其中包含一个默认的 kettle.properties 属性文件等
	 */
	public static void createKettleHome() {
		// 尝试创建目录
		String directory = Constant.getKettleDirectory();
		File dir = new File(directory);
		try {
			dir.mkdirs();
			// 创建属性文件 kettle.properties
			createDefaultKettleProperties(directory);
		} catch (Exception e) {
		}
	}

	/**
	 * 创建默认的 kettle properties 文件
	 * 
	 * @param 目录
	 *            目录
	 */
	private static void createDefaultKettleProperties(String directory) {

		String kpFile = directory + Constant.FILE_SEPARATOR + Constant.UKETTLE;
		File file = new File(kpFile);
		if (!file.exists()) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
				out.write(Constant.getKettlePropertiesFileHeader().getBytes());
			} catch (IOException e) {
				System.err
						.println(BaseMessages
								.getString(
										PKG,
										"Props.Log.Error.UnableToCreateDefaultKettleProperties.Message",
										Constant.UKETTLE, kpFile));
				System.err.println(e.getStackTrace());
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						System.err
								.println(BaseMessages
										.getString(
												PKG,
												"Props.Log.Error.UnableToCreateDefaultKettleProperties.Message",
												Constant.UKETTLE, kpFile));
						System.err.println(e.getStackTrace());
					}
				}
			}
		}
	}

	/**
	 * Checks if the Kettle environment has been initialized.
	 * 
	 * @return true if initialized, false otherwise
	 */
	/**
	 * 检查Kettle 环境初始化。
	 * 
	 * @return 返回 true or false
	 */
	public static boolean isInitialized() {
		if (initialized == null)
			return false;
		else
			return true;
	}

	/**
	 * 加载插件注册表。
	 * 
	 * @throws KettlePluginException
	 *             加载插件如果遇到任何错误返回KettlePluginException
	 */
	public void loadPluginRegistry() throws KettlePluginException {

	}

	/**
	 * 初始化环境变量。
	 * 
	 * @throws KettleException
	 *             初始化如果遇到任何错误返回KettleException
	 */
	public static void environmentInit() throws KettleException {
		if (Thread.currentThread().getContextClassLoader() == null) {
			Thread.currentThread().setContextClassLoader(
					ClassLoader.getSystemClassLoader());
		}
		Map<?, ?> prop = Constant.readProperties();
		Variables variables = new Variables();
		for (Object key : prop.keySet()) {
			String variable = (String) key;
			String value = variables.environmentSubstitute((String) prop
					.get(key));
			variables.setVariable(variable, value);
		}
		for (String variable : variables.listVariables()) {
			System.setProperty(variable, variables.getVariable(variable));
		}

		System.getProperties().put("KETTLE_HOME", Constant.KETTLE_HOME);
		System.getProperties().put("KETTLE_PLUGIN_BASE_FOLDERS",
				Constant.KETTLE_PLUGIN);
		System.getProperties().put("KETTLE_JS_HOME", Constant.KETTLE_SCRIPT);
		System.getProperties()
				.put(Constant.INTERNAL_VARIABLE_CLUSTER_SIZE, "1");
		System.getProperties().put(
				Constant.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER, "0");
		System.getProperties().put(
				Constant.INTERNAL_VARIABLE_SLAVE_SERVER_NAME,
				"slave-trans-name");
		System.getProperties().put(Constant.INTERNAL_VARIABLE_STEP_COPYNR, "0");
		System.getProperties().put(Constant.INTERNAL_VARIABLE_STEP_NAME,
				"step-name");
		System.getProperties().put(
				Constant.INTERNAL_VARIABLE_STEP_PARTITION_ID, "partition-id");
		System.getProperties().put(
				Constant.INTERNAL_VARIABLE_STEP_PARTITION_NR, "0");
		System.getProperties().put(
				Constant.INTERNAL_VARIABLE_STEP_UNIQUE_COUNT, "1");
		System.getProperties().put(
				Constant.INTERNAL_VARIABLE_STEP_UNIQUE_NUMBER, "0");
	}

}