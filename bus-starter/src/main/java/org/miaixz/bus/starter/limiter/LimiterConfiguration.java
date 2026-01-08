/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
