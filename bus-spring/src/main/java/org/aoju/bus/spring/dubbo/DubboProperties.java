package org.aoju.bus.spring.dubbo;

import com.alibaba.dubbo.config.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * swagger配置项
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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