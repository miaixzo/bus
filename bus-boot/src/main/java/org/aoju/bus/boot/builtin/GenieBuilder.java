/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.boot.builtin;

import org.aoju.bus.Version;
import org.aoju.bus.boot.consts.BootConsts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * 用于配置一些特殊的关键属性，比如bus-boot.version等,
 * 将作为一个名为BusConfigurationProperties的属性源添加
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GenieBuilder implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        /**
         * 获取版本信息
         */
        Properties properties = getProperties();

        PropertiesPropertySource propertySource = new PropertiesPropertySource(
                BootConsts.BUS_BOOT_PROPERTIES, properties);
        environment.getPropertySources().addLast(propertySource);

        /**
         * 设置必要参数
         **/
        environment.setRequiredProperties(BootConsts.BUS_NAME);
    }

    /**
     * 获取版本信息以及banner信息
     *
     * @return properties
     */
    protected Properties getProperties() {
        Properties properties = new Properties();
        String version = getVersion();
        properties.setProperty(BootConsts.BUS_BOOT_VERSION, version);
        properties.setProperty(BootConsts.BUS_BOOT_FORMATTED_VERSION,
                version.isEmpty() ? "" : String.format(" (v%s)", version));
        return properties;
    }

    /**
     * 获取bus版本信息.
     *
     * @return version
     */
    protected String getVersion() {
        return Version.get() == null ? "" : Version.get();
    }

}
