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
package org.aoju.bus.starter.dubbo;

import lombok.Data;
import org.aoju.bus.spring.BusXConfig;
import org.apache.dubbo.config.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Dubbo配置项
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@ConfigurationProperties(prefix = BusXConfig.DUBBO)
public class DubboProperties {

    /**
     * 扫描dubbo接口的基本包
     */
    protected String basePackages;
    /**
     * 扫描的包以查找带注解@DubboService的类
     */
    protected String basePackageClasses;
    /**
     * 是否绑定到多个Spring bean
     */
    protected boolean multiple;

    @Bean
    @ConfigurationProperties(prefix = BusXConfig.DUBBO + ".application")
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = BusXConfig.DUBBO + ".provider")
    public ProviderConfig ProviderConfig() {
        return new ProviderConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = BusXConfig.DUBBO + ".monitor")
    public MonitorConfig monitorConfig() {
        return new MonitorConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = BusXConfig.DUBBO + ".consumer")
    public ConsumerConfig consumerConfig() {
        return new ConsumerConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = BusXConfig.DUBBO + ".registry")
    public RegistryConfig registryConfig() {
        return new RegistryConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = BusXConfig.DUBBO + ".protocol")
    public ProtocolConfig protocolConfig() {
        return new ProtocolConfig();
    }

}
