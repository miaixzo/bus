/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring.listener;

import java.util.*;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

/**
 * 实现{@link ApplicationListener<ApplicationEnvironmentPreparedEvent>}以适应spring cloud环境 用于将日志属性注册到spring cloud引导环境。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringCloudConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private final static MapPropertySource HIGH_PRIORITY_CONFIG = new MapPropertySource(GeniusBuilder.BUS_HIGH_PRIORITY,
            new HashMap<>());

    /**
     * 配置日志信息
     *
     * @param environment 环境资源信息
     */
    private void assemblyLogSetting(ConfigurableEnvironment environment) {
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .map(propertySource -> Arrays.asList(((EnumerablePropertySource<?>) propertySource).getPropertyNames()))
                .flatMap(Collection::stream).filter(GeniusBuilder::isLoggingConfig)
                .forEach((key) -> HIGH_PRIORITY_CONFIG.getSource().put(key, environment.getProperty(key)));
    }

    /**
     * 配置必需的属性
     */
    private void assemblyRequireProperties(ConfigurableEnvironment environment) {
        if (StringKit.hasText(environment.getProperty(GeniusBuilder.APP_NAME))) {
            HIGH_PRIORITY_CONFIG.getSource().put(GeniusBuilder.APP_NAME,
                    environment.getProperty(GeniusBuilder.APP_NAME));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // only work in spring cloud application
        if (GeniusBuilder.isSpringCloudEnvironmentEnabled(environment)) {
            if (environment.getPropertySources().contains(GeniusBuilder.CLOUD_BOOTSTRAP)) {
                // in bootstrap application context, add high priority config
                environment.getPropertySources().addLast(HIGH_PRIORITY_CONFIG);
            } else {
                // in application context, build high priority config
                SpringApplication application = event.getSpringApplication();
                StandardEnvironment bootstrapEnvironment = new StandardEnvironment();
                StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                        .filter(source -> !(source instanceof PropertySource.StubPropertySource))
                        .forEach(source -> bootstrapEnvironment.getPropertySources().addLast(source));

                List<Class<?>> sources = new ArrayList<>();
                for (Object s : application.getAllSources()) {
                    if (s instanceof Class) {
                        sources.add((Class<?>) s);
                    } else if (s instanceof String) {
                        sources.add(ClassKit.forName((String) s, null));
                    }
                }

                SpringApplication bootstrapApplication = new SpringApplicationBuilder()
                        .profiles(environment.getActiveProfiles()).bannerMode(Banner.Mode.OFF)
                        .environment(bootstrapEnvironment).sources(sources.toArray(new Class[] {}))
                        .registerShutdownHook(false).logStartupInfo(false).web(WebApplicationType.NONE).listeners()
                        .initializers().build(event.getArgs());

                ConfigurableBootstrapContext bootstrapContext = event.getBootstrapContext();
                ApplicationEnvironmentPreparedEvent bootstrapEvent = new ApplicationEnvironmentPreparedEvent(
                        bootstrapContext, bootstrapApplication, event.getArgs(), bootstrapEnvironment);

                application.getListeners().stream()
                        .filter(listener -> listener instanceof EnvironmentPostProcessorApplicationListener)
                        .forEach(listener -> ((EnvironmentPostProcessorApplicationListener) listener)
                                .onApplicationEvent(bootstrapEvent));

                assemblyLogSetting(bootstrapEnvironment);
                assemblyRequireProperties(bootstrapEnvironment);
            }
        }
    }

}
