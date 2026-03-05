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
