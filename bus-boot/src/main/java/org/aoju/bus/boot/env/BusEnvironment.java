/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.boot.env;

import org.aoju.bus.Version;
import org.aoju.bus.boot.consts.BusConsts;
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
 * @version 3.0.9
 * @since JDK 1.8
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class BusEnvironment implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        /**
         * Get Version version properties
         */
        Properties properties = getProperties();

        PropertiesPropertySource propertySource = new PropertiesPropertySource(
                BusConsts.BUS_BOOT_PROPERTIES, properties);
        environment.getPropertySources().addLast(propertySource);

        /**
         * set required properties
         **/
        environment.setRequiredProperties(BusConsts.BUS_APP_NAME);
    }

    /**
     * Get boot Version and print it on banner
     *
     * @return properties
     */
    protected Properties getProperties() {
        Properties properties = new Properties();
        String version = getVersion();
        properties.setProperty(BusConsts.BUS_BOOT_VERSION, version);
        properties.setProperty(BusConsts.BUS_BOOT_FORMATTED_VERSION,
                version.isEmpty() ? "" : String.format(" (v%s)", version));
        return properties;
    }

    /**
     * Get Version Version string.
     *
     * @return version
     */
    protected String getVersion() {
        return Version.version() == null ? "" : Version.version();
    }

}
