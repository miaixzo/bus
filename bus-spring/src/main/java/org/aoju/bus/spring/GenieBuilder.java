/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.spring;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.spring.banner.BusBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.StreamSupport;

/**
 * 启动监听器，初始化相关配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenieBuilder implements
        ApplicationListener<ApplicationEnvironmentPreparedEvent>,
        Ordered {

    private static final MapPropertySource HIGH_PRIORITY_CONFIG = new MapPropertySource(
            BusXBuilder.BUS_HIGH_PRIORITY_CONFIG,
            new HashMap<>());

    public static boolean filterAllLogConfig(String key) {
        return key.startsWith("logging.level.") || key.startsWith("logging.path.")
                || key.startsWith("logging.config.") || key.equals("logging.path")
                || key.equals("loggingRoot") || key.equals("file.encoding");
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        SpringApplication application = event.getSpringApplication();

        StandardEnvironment bootstrapEnvironment = new StandardEnvironment();
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(source -> !(source instanceof PropertySource.StubPropertySource))
                .forEach(source -> bootstrapEnvironment.getPropertySources().addLast(source));

        application.setBanner(new BusBanner());

        setLogging(bootstrapEnvironment);
        setRequireProperties(bootstrapEnvironment);
        setEnvironment(environment);

        AnsiOutput.setEnabled(AnsiOutput.Enabled.DETECT);
        AnsiOutput.setConsoleAvailable(true);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    /**
     * 配置日志设置
     */
    private void setLogging(ConfigurableEnvironment environment) {
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .map(propertySource -> Arrays
                        .asList(((EnumerablePropertySource) propertySource).getPropertyNames()))
                .flatMap(Collection::stream).filter(GenieBuilder::filterAllLogConfig)
                .forEach((key) -> HIGH_PRIORITY_CONFIG.getSource().put(key, environment.getProperty(key)));
    }

    /**
     * 配置所需属性
     *
     * @param environment 环境信息
     */
    private void setRequireProperties(ConfigurableEnvironment environment) {
        if (StringKit.hasText(environment.getProperty(BusXBuilder.BUS_NAME))) {
            HIGH_PRIORITY_CONFIG.getSource().put(
                    BusXBuilder.BUS_NAME,
                    environment.getProperty(BusXBuilder.BUS_NAME)
            );
        }
    }

    /**
     * 标记为引导环境
     *
     * @param environment 环境信息
     */
    private void setEnvironment(ConfigurableEnvironment environment) {
        environment.getPropertySources().addFirst(
                new MapPropertySource(BusXBuilder.BUS_BOOTSTRAP, new HashMap<>())
        );
    }

}
