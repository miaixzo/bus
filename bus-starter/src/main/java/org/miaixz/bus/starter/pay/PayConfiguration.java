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
package org.miaixz.bus.starter.pay;

import jakarta.annotation.Resource;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.cache.PayCache;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the integrated payment service. This class sets up the necessary beans for the payment
 * functionality based on the provided properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { PayProperties.class })
public class PayConfiguration {

    /**
     * Injected payment configuration properties.
     */
    @Resource
    private PayProperties properties;

    /**
     * Creates the {@link PayService} bean.
     *
     * @param complex The complex payment parameters.
     * @param cache   The cache instance.
     * @return A new instance of {@link PayService}.
     */
    @Bean
    public PayService payProviderFactory(Complex complex, CacheX cache) {
        return new PayService(this.properties, complex, cache);
    }

    /**
     * Creates a default {@link CacheX} bean if no other bean of the same type is present. This bean is only created if
     * the property `bus.pay.cache.type` is set to `default` or is missing.
     *
     * @return The default {@link CacheX} instance for payment caching.
     */
    @Bean
    @ConditionalOnMissingBean(CacheX.class)
    @ConditionalOnProperty(name = GeniusBuilder.PAY + ".cache.type", havingValue = "default", matchIfMissing = true)
    public CacheX cache() {
        return PayCache.INSTANCE;
    }

}
