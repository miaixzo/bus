package org.miaixz.bus.starter.limiter;

import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.limiter.Supplier;
import org.miaixz.bus.limiter.metric.FallbackProvider;
import org.miaixz.bus.limiter.metric.MethodProvider;
import org.miaixz.bus.limiter.metric.RequestProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 限流/降级配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(LimiterProperties.class)
public class LimiterConfiguration {

    @Bean
    public LimiterService limiterService(LimiterProperties properties) {
        return new LimiterService(properties);
    }

    @Bean
    public RequestProvider requestProvider(LimiterProperties properties) {
        RequestProvider strategy = new RequestProvider();
        String implClassName = properties.getSupplier();
        // 是否指定用户标识提供者
        if (StringKit.isNotEmpty(implClassName)) {
            Supplier instance = ReflectKit.newInstance(implClassName);
            // 判断是否继承抽象类
            if (Supplier.class.isAssignableFrom(instance.getClass())) {
                strategy.setMarkSupplier(instance);
            }
        }
        return strategy;
    }

    @Bean
    public FallbackProvider fallbackProvider() {
        return new FallbackProvider();
    }

    @Bean
    public MethodProvider methodProvider() {
        return new MethodProvider();
    }

    @Bean
    public LimiterScanner scanner() {
        return new LimiterScanner();
    }

}
