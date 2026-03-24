/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.limiter;

import jakarta.annotation.Resource;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.limiter.Supplier;
import org.miaixz.bus.limiter.metric.FallbackProvider;
import org.miaixz.bus.limiter.metric.MethodProvider;
import org.miaixz.bus.limiter.metric.RequestProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the rate limiting and circuit breaking framework.
 * <p>
 * This class sets up the necessary beans for the limiter functionality, including the core service, strategy providers,
 * and the annotation scanner.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { LimiterProperties.class })
public class LimiterConfiguration {

    @Resource
    private LimiterProperties properties;

    /**
     * Creates the {@link LimiterService} bean, which initializes the global limiter context.
     *
     * @return A new {@link LimiterService} instance.
     */
    @Bean
    public LimiterService limiterService() {
        return new LimiterService(this.properties);
    }

    /**
     * Creates the {@link RequestProvider} bean, which is responsible for handling request-based limiting strategies.
     * <p>
     * It also configures a custom user identifier {@link Supplier} if one is specified in the properties.
     * </p>
     *
     * @return A configured {@link RequestProvider} instance.
     */
    @Bean
    public RequestProvider requestProvider() {
        RequestProvider strategy = new RequestProvider();
        String implClassName = this.properties.getSupplier();
        // Check if a custom user identifier provider is specified.
        if (StringKit.isNotEmpty(implClassName)) {
            Supplier instance = ReflectKit.newInstance(implClassName);
            // Ensure it inherits from the correct abstract class.
            if (Supplier.class.isAssignableFrom(instance.getClass())) {
                strategy.setMarkSupplier(instance);
            }
        }
        return strategy;
    }

    /**
     * Creates the {@link FallbackProvider} bean, which handles circuit breaking (downgrade) strategies.
     *
     * @return A new {@link FallbackProvider} instance.
     */
    @Bean
    public FallbackProvider fallbackProvider() {
        return new FallbackProvider();
    }

    /**
     * Creates the {@link MethodProvider} bean, which handles hotspot method limiting strategies.
     *
     * @return A new {@link MethodProvider} instance.
     */
    @Bean
    public MethodProvider methodProvider() {
        return new MethodProvider();
    }

    /**
     * Creates the {@link LimiterScanner} bean, which is a post-processor that scans for limiter annotations and creates
     * proxies for the annotated beans.
     *
     * @return A new {@link LimiterScanner} instance.
     */
    @Bean
    public LimiterScanner scanner() {
        return new LimiterScanner();
    }

}
