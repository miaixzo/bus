package org.aoju.bus.starter;

import org.aoju.bus.core.utils.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

/**
 * 应用程序事件侦听器
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class BusListener implements
        ApplicationListener<ApplicationEnvironmentPreparedEvent>,
        Ordered {

    private final static MapPropertySource HIGH_PRIORITY_CONFIG = new MapPropertySource(
            BootConsts.BUS_HIGH_PRIORITY_CONFIG,
            new HashMap<>());
    private static AtomicBoolean executed = new AtomicBoolean(false);

    public static boolean filterAllLogConfig(String key) {
        return key.startsWith("logging.level.") || key.startsWith("logging.path.")
                || key.startsWith("logging.config.") || key.equals("logging.path")
                || key.equals("loggingRoot") || key.equals("file.encoding");
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        SpringApplication application = event.getSpringApplication();
        if (executed.compareAndSet(false, true)) {
            StandardEnvironment bootstrapEnvironment = new StandardEnvironment();
            StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                    .filter(source -> !(source instanceof PropertySource.StubPropertySource))
                    .forEach(source -> bootstrapEnvironment.getPropertySources().addLast(source));

            List<Class> sources = new ArrayList<>();
            for (Object s : application.getAllSources()) {
                if (s instanceof Class) {
                    sources.add((Class) s);
                } else if (s instanceof String) {
                    sources.add(ClassUtils.resolveClassName((String) s, null));
                }
            }

            SpringApplication bootstrapApplication = new SpringApplicationBuilder()
                    .profiles(environment.getActiveProfiles()).bannerMode(Banner.Mode.OFF)
                    .environment(bootstrapEnvironment).sources(sources.toArray(new Class[]{}))
                    .registerShutdownHook(false).logStartupInfo(false).web(WebApplicationType.NONE)
                    .listeners().initializers().build(event.getArgs());

            ApplicationEnvironmentPreparedEvent bootstrapEvent = new ApplicationEnvironmentPreparedEvent(
                    bootstrapApplication, event.getArgs(), bootstrapEnvironment);

            application.getListeners().stream()
                    .filter(listener -> listener instanceof ConfigFileApplicationListener)
                    .forEach(listener -> ((ConfigFileApplicationListener) listener)
                            .onApplicationEvent(bootstrapEvent));

            assemblyLogSetting(bootstrapEnvironment);
            assemblyRequireProperties(bootstrapEnvironment);
            assemblyEnvironmentMark(environment);
        } else {
            unAssemblyEnvironmentMark(environment);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    /**
     * config log settings
     */
    private void assemblyLogSetting(ConfigurableEnvironment environment) {
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .map(propertySource -> Arrays
                        .asList(((EnumerablePropertySource) propertySource).getPropertyNames()))
                .flatMap(Collection::stream).filter(BusListener::filterAllLogConfig)
                .forEach((key) -> HIGH_PRIORITY_CONFIG.getSource().put(key, environment.getProperty(key)));
    }

    /**
     * config required properties
     *
     * @param environment
     */
    private void assemblyRequireProperties(ConfigurableEnvironment environment) {
        if (StringUtils.hasText(environment.getProperty(BootConsts.BUS_NAME))) {
            HIGH_PRIORITY_CONFIG.getSource().put(BootConsts.BUS_NAME,
                    environment.getProperty(BootConsts.BUS_NAME));
        }
    }

    /**
     * Mark this environment as SOFA bootstrap environment
     *
     * @param environment
     */
    private void assemblyEnvironmentMark(ConfigurableEnvironment environment) {
        environment.getPropertySources().addFirst(
                new MapPropertySource(BootConsts.BUS_BOOTSTRAP, new HashMap<>()));
    }

    /**
     * Un-Mark this environment as SOFA bootstrap environment
     *
     * @param environment
     */
    private void unAssemblyEnvironmentMark(ConfigurableEnvironment environment) {
        environment.getPropertySources().remove(BootConsts.BUS_BOOTSTRAP);
    }

}
