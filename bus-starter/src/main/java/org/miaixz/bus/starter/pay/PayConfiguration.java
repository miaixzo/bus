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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnablePay;

/**
 * Configures the integrated payment service. This class sets up the necessary beans for the payment functionality based
 * on the provided properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { PayProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.pay.Provider")
@ConditionalOnEnabled(annotation = EnablePay.class, prefix = GeniusBuilder.PAY)
public class PayConfiguration {

    /**
     * Bound pay configuration properties.
     */
    private final PayProperties properties;

    /**
     * Stores the payment-provider definitions used to construct the payment registry and service.
     *
     * @param properties bound configuration properties
     */
    public PayConfiguration(PayProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the {@link PayService} bean.
     *
     * @param complex The complex payment parameters.
     * @param cache   The payment cache instance.
     * @return A new instance of {@link PayService}.
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnBean(Complex.class)
    @ConditionalOnMissingBean(PayService.class)
    PayService payService(Complex complex, @Qualifier("payCache") CacheX<String, Object> cache) {
        return new PayService(this.properties, complex, cache);
    }

    /**
     * Creates the payment cache implementation bean.
     * <p>
     * Resolution order:
     * <ul>
     * <li>If {@code bus.pay.cache.*} configures a concrete backend type, create a pay-specific cache through
     * {@link Factory}.</li>
     * <li>If no pay-specific backend is configured, create a Context-local memory cache.</li>
     * </ul>
     *
     * @return payment cache implementation
     */
    @Bean("payCache")
    @ConditionalOnBean(Complex.class)
    @ConditionalOnMissingBean(name = "payCache")
    public CacheX<String, Object> payCache() {
        if (hasPayBackend()) {
            return new Factory().initialize(this.properties.getCache());
        }
        return new MemoryCache<>();
    }

    /**
     * Returns whether pay defines its own concrete cache backend instead of reusing the shared default cache.
     *
     * @return {@code true} when pay should initialize a dedicated backend
     */
    private boolean hasPayBackend() {
        if (this.properties.getCache() == null) {
            return false;
        }
        String type = this.properties.getCache().getType();
        return StringKit.isNotBlank(type) && !"default".equalsIgnoreCase(type.trim());
    }

}
