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
package org.aoju.bus.spring.dubbo;

import com.alibaba.dubbo.config.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * swagger配置项
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties(prefix = "spring.dubbo")
public class DubboProperties {

    protected String basePackage;
    protected String basePackageClass;
    protected boolean multiple;

    @Bean
    @ConfigurationProperties(prefix = "spring.dubbo.application")
    public ApplicationConfig applicationConfig() {
        return new ApplicationConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.dubbo.provider")
    public ProviderConfig ProviderConfig() {
        return new ProviderConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.dubbo.monitor")
    public MonitorConfig monitorConfig() {
        return new MonitorConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.dubbo.consumer")
    public ConsumerConfig consumerConfig() {
        return new ConsumerConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.dubbo.registry")
    public RegistryConfig registryConfig() {
        return new RegistryConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.dubbo.protocol")
    public ProtocolConfig protocolConfig() {
        return new ProtocolConfig();
    }

}
