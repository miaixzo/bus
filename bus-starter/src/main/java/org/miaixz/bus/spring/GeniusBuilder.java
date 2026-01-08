/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ClassKit;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * Global constant configuration for the Bus Spring Starter.
 * <p>
 * This class defines various constants used throughout the Bus Spring Starter module, including property prefixes,
 * banner configurations, and internal stage names for startup metrics. It also provides utility methods for environment
 * checks related to Spring Cloud and logging.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GeniusBuilder {

    /**
     * Bus banner ASCII art for miaixz.org.
     */
    public static final String[] BUS_BANNER = {
            " ███╗   ███╗██╗ █████╗ ██╗██╗  ██╗███████╗    ██████╗ ██████╗  ██████╗  ",
            " ████╗ ████║██║██╔══██╗██║╚██╗██╔╝╚══███╔╝   ██╔═══██╗██╔══██╗██╔════╝  ",
            " ██╔████╔██║██║███████║██║ ╚███╔╝   ███╔╝    ██║   ██║██████╔╝██║  ███╗ ",
            " ██║╚██╔╝██║██║██╔══██║██║ ██╔██╗  ███╔╝     ██║   ██║██╔══██╗██║   ██║ ",
            " ██║ ╚═╝ ██║██║██║  ██║██║██╔╝ ██╗███████╗██╗╚██████╔╝██║  ██║╚██████╔╝ ",
            " ╚═╝     ╚═╝╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝  " };

    /**
     * Bus boot banner prefix.
     */
    public static final String BUS_BOOT_BANNER = " :: Bus Boot :: ";

    /**
     * Spring Boot banner prefix.
     */
    public static final String SPRING_BOOT_BANNER = " :: Spring Boot :: ";

    /**
     * Default path for Spring context metadata.
     */
    public static final String SPRING_CONTEXT_PATH = "META-INF/spring";

    /**
     * Default profile value.
     */
    public static final String DEFAULT_PROFILE_VALUE = "default";

    /**
     * Key for Spring Parent module in module.properties.
     */
    public static final String SPRING_PARENT = "Spring-Parent";

    /**
     * Key for Module Name in module.properties.
     */
    public static final String MODULE_NAME = "Module-Name";

    /**
     * Key for Required Module in module.properties.
     */
    public static final String REQUIRE_MODULE = "Require-Module";

    /**
     * Key for Module Profile in module.properties.
     */
    public static final String MODULE_PROFILE = "Module-Profile";

    /**
     * Property source name for Bus configurations.
     */
    public static final String BUS_PROPERTY_SOURCE = "configurationProperties";

    /**
     * Property source name for high-priority Bus configurations.
     */
    public static final String BUS_HIGH_PRIORITY = "priorityConfig";

    /**
     * Property key for Bus scenes configuration.
     */
    public static final String BUS_SCENES = Keys.BUS + Symbol.DOT + "scenes";

    /**
     * Path for Bus scenes configuration files.
     */
    public static final String BUS_SCENES_PATH = Keys.BUS + File.separator + "scenes";

    /**
     * Default logging path for Bus applications.
     */
    public static final String BUS_LOGGING_PATH = File.separator + "logs";

    /**
     * Property key for logging path.
     */
    public static final String LOGGING_PATH = "logging.path";

    /**
     * Prefix for logging path properties.
     */
    public static final String LOGGING_PATH_PREFIX = "logging.path.";

    /**
     * Property key for logging level.
     */
    public static final String LOGGING_LEVEL = "logging.level";

    /**
     * Prefix for logging level properties.
     */
    public static final String LOGGING_LEVEL_PREFIX = "logging.level.";

    /**
     * Property key for console logging pattern.
     */
    public static final String LOGGING_PATTERN_CONSOLE = "logging.pattern.console";

    /**
     * Property key for file logging pattern.
     */
    public static final String LOGGING_PATTERN_FILE = "logging.pattern.file";

    /**
     * Default Spring banner file name.
     */
    public static final String SPRING_BANNER_TXT = "banner.txt";

    /**
     * Property key for Spring banner location.
     */
    public static final String SPRING_BANNER_LOCATION = "spring.banner.location";

    /**
     * Property key for Bus banner configuration.
     */
    public static final String BANNER = Keys.BUS + Symbol.DOT + "banner";

    /**
     * Property key for Bus cache configuration.
     */
    public static final String CACHE = Keys.BUS + Symbol.DOT + "cache";

    /**
     * Property key for Bus CORS configuration.
     */
    public static final String CORS = Keys.BUS + Symbol.DOT + "cors";

    /**
     * Property key for Bus Dubbo configuration.
     */
    public static final String DUBBO = Keys.BUS + Symbol.DOT + "dubbo";

    /**
     * Property key for Bus Elasticsearch configuration.
     */
    public static final String ELASTIC = Keys.BUS + Symbol.DOT + "elastic";

    /**
     * Property key for Bus Vortex gateway configuration.
     */
    public static final String VORTEX = Keys.BUS + Symbol.DOT + "vortex";

    /**
     * Property key for Bus health configuration.
     */
    public static final String HEALTH = Keys.BUS + Symbol.DOT + "health";

    /**
     * Property key for Bus i18n configuration.
     */
    public static final String I18N = Keys.BUS + Symbol.DOT + "i18n";

    /**
     * Property key for Bus image processing configuration.
     */
    public static final String IMAGE = Keys.BUS + Symbol.DOT + "image";

    /**
     * Property key for Bus limiter configuration.
     */
    public static final String LIMITER = Keys.BUS + Symbol.DOT + "limiter";

    /**
     * Property key for Bus MongoDB configuration.
     */
    public static final String MONGO = Keys.BUS + Symbol.DOT + "mongo";

    /**
     * Property key for Bus Mapper (MyBatis) configuration.
     */
    public static final String MAPPER = Keys.BUS + Symbol.DOT + "mapper";

    /**
     * Property key for Bus notify configuration.
     */
    public static final String NOTIFY = Keys.BUS + Symbol.DOT + "notify";

    /**
     * Property key for Bus authentication configuration.
     */
    public static final String AUTH = Keys.BUS + Symbol.DOT + "auth";

    /**
     * Property key for Bus office document processing configuration.
     */
    public static final String OFFICE = Keys.BUS + Symbol.DOT + "office";

    /**
     * Property key for Bus payment configuration.
     */
    public static final String PAY = Keys.BUS + Symbol.DOT + "pay";

    /**
     * Property key for Bus sensitive data handling configuration.
     */
    public static final String SENSITIVE = Keys.BUS + Symbol.DOT + "sensitive";

    /**
     * Property key for Bus socket communication configuration.
     */
    public static final String SOCKET = Keys.BUS + Symbol.DOT + "socket";

    /**
     * Property key for Bus object storage configuration.
     */
    public static final String STORAGE = Keys.BUS + Symbol.DOT + "storage";

    /**
     * Property key for Bus distributed tracing configuration.
     */
    public static final String TRACER = Keys.BUS + Symbol.DOT + "tracer";

    /**
     * Property key for Bus validation configuration.
     */
    public static final String VALIDATE = Keys.BUS + Symbol.DOT + "validate";

    /**
     * Property key for Bus request/response wrapper configuration.
     */
    public static final String WRAPPER = Keys.BUS + Symbol.DOT + "wrapper";

    /**
     * Property key for Bus ZooKeeper configuration.
     */
    public static final String ZOOKEEPER = Keys.BUS + Symbol.DOT + "zookeeper";

    /**
     * Property key for Bus temporary work directory configuration.
     */
    public static final String WORK = Keys.BUS + Symbol.DOT + "work";

    /**
     * Spring application name property key.
     */
    public static final String APP_NAME = "spring.application.name";

    /**
     * Spring data source property key.
     */
    public static final String DATASOURCE = "spring.datasource";

    /**
     * Spring Cloud bootstrap property source name.
     */
    public static final String CLOUD_BOOTSTRAP = "bootstrap";

    /**
     * Class name for Spring Cloud BootstrapConfiguration.
     */
    public static final String CLOUD_BOOTSTRAP_CONFIGURATION_CLASS = "org.springframework.cloud.bootstrap.BootstrapConfiguration";

    /**
     * Stage name for JVM startup metrics. Represents the time from JVM start to the completion of
     * {@link SpringApplicationRunListener#starting(ConfigurableBootstrapContext)}.
     */
    public static final String JVM_STARTING_STAGE = "JvmStartingStage";

    /**
     * Stage name for environment preparation metrics. Represents the time from
     * {@link SpringApplicationRunListener#starting(ConfigurableBootstrapContext)} to
     * {@link SpringApplicationRunListener#environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}.
     */
    public static final String ENVIRONMENT_PREPARE_STAGE = "EnvironmentPrepareStage";

    /**
     * Stage name for application context preparation metrics. Represents the time from
     * {@link SpringApplicationRunListener#environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}
     * to {@link SpringApplicationRunListener#contextPrepared(ConfigurableApplicationContext)}.
     */
    public static final String APPLICATION_CONTEXT_PREPARE_STAGE = "ApplicationContextPrepareStage";

    /**
     * Stage name for application context loading metrics. Represents the time from
     * {@link SpringApplicationRunListener#contextPrepared(ConfigurableApplicationContext)} to
     * {@link SpringApplicationRunListener#contextLoaded(ConfigurableApplicationContext)}.
     */
    public static final String APPLICATION_CONTEXT_LOAD_STAGE = "ApplicationContextLoadStage";

    /**
     * Stage name for application context refresh metrics. Represents the time from
     * {@link SpringApplicationRunListener#contextLoaded(ConfigurableApplicationContext)} to the completion of the
     * application context refresh event.
     */
    public static final String APPLICATION_CONTEXT_REFRESH_STAGE = "ApplicationContextRefreshStage";

    /**
     * Startup step name for Spring bean instantiation.
     */
    public static final String SPRING_BEANS_INSTANTIATE = "spring.beans.instantiate";

    /**
     * Startup step name for Spring bean smart initialization.
     */
    public static final String SPRING_BEANS_SMART_INSTANTIATE = "spring.beans.smart-initialize";

    /**
     * Startup step name for Spring context BeanDefinitionRegistry post-processing.
     */
    public static final String SPRING_CONTEXT_BEANDEF_REGISTRY_POST_PROCESSOR = "spring.context.beandef-registry.post-process";

    /**
     * Startup step name for Spring context BeanFactory post-processing.
     */
    public static final String SPRING_CONTEXT_BEAN_FACTORY_POST_PROCESSOR = "spring.context.bean-factory.post-process";

    /**
     * Startup step name for Spring bean post-processing.
     */
    public static final String SPRING_BEAN_POST_PROCESSOR = "spring.context.beans.post-process";

    /**
     * Startup step name for Spring configuration classes enhancement.
     */
    public static final String SPRING_CONFIG_CLASSES_ENHANCE = "spring.context.config-classes.enhance";

    private static boolean LOCAL_ENV = false;

    private static boolean TEST_ENV = false;

    /**
     * Checks whether the Spring Cloud Bootstrap environment is enabled.
     *
     * @param environment The current {@link Environment}.
     * @return {@code true} if Spring Cloud Bootstrap environment is enabled, {@code false} otherwise.
     */
    public static boolean isSpringCloudEnvironmentEnabled(Environment environment) {
        return ClassKit.isPresent(CLOUD_BOOTSTRAP_CONFIGURATION_CLASS, null) && bootstrapEnabled(environment);
    }

    /**
     * Checks if Spring Cloud bootstrap is enabled via properties or marker class.
     *
     * @param environment The current {@link Environment}.
     * @return {@code true} if bootstrap is enabled, {@code false} otherwise.
     */
    public static boolean bootstrapEnabled(Environment environment) {
        return environment.getProperty("spring.cloud.bootstrap.enabled", Boolean.class, false)
                || ClassKit.isPresent("org.springframework.cloud.bootstrap.marker.Marker", null);
    }

    /**
     * Checks whether Spring Cloud is present in the classpath.
     *
     * @return {@code true} if Spring Cloud is detected, {@code false} otherwise.
     */
    public static boolean isSpringCloud() {
        return ClassKit.isPresent(CLOUD_BOOTSTRAP_CONFIGURATION_CLASS, null);
    }

    /**
     * Checks whether the application is running in a Spring test environment.
     *
     * @return {@code true} if in a Spring test environment, {@code false} otherwise.
     */
    public static boolean isSpringTestEnv() {
        return TEST_ENV;
    }

    /**
     * Checks whether the application is running in a local development environment.
     *
     * @return {@code true} if in a local development environment, {@code false} otherwise.
     */
    public static boolean isLocalEnv() {
        return LOCAL_ENV;
    }

    /**
     * Initializes the {@code TEST_ENV} flag by inspecting the current stack trace. This method is typically called once
     * during application startup.
     */
    private static void initSpringTestEnv() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if ("loadContext".equals(stackTraceElement.getMethodName())
                    && "org.springframework.boot.test.context.SpringBootContextLoader"
                            .equals(stackTraceElement.getClassName())) {
                TEST_ENV = true;
                break;
            }
        }
    }

    /**
     * Checks if a given property key is related to logging configuration.
     *
     * @param key The property key to check.
     * @return {@code true} if the key is a logging configuration key, {@code false} otherwise.
     */
    public static boolean isLoggingConfig(String key) {
        return isLoggingPrefix(key) || LOGGING_PATH.equals(key) || Keys.FILE_ENCODING.equals(key);
    }

    /**
     * Checks if a given property key starts with a logging-related prefix.
     *
     * @param key The property key to check.
     * @return {@code true} if the key starts with a logging prefix, {@code false} otherwise.
     */
    public static boolean isLoggingPrefix(String key) {
        return key.startsWith(LOGGING_LEVEL_PREFIX) || key.startsWith(LOGGING_PATH_PREFIX);
    }

    /**
     * Checks if a given property key is related to logging patterns.
     *
     * @param key The property key to check.
     * @return {@code true} if the key is a logging pattern key, {@code false} otherwise.
     */
    public static boolean isLoggingPattern(String key) {
        return LOGGING_PATTERN_CONSOLE.equals(key) || LOGGING_PATTERN_FILE.equals(key);
    }

}
