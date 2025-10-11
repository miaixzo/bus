/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.cache;

import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Metrics;
import org.miaixz.bus.cache.support.metrics.*;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * Auto-configuration for caching.
 * <p>
 * This class enables {@link CacheProperties} and sets up the necessary beans for AOP-based caching. It creates the
 * {@link AspectjCacheProxy} which intercepts method calls to provide caching behavior.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { CacheProperties.class })
public class CacheConfiguration {

    /**
     * Injected cache configuration properties.
     */
    @Resource
    CacheProperties properties;

    /**
     * Creates the {@link AspectjCacheProxy} bean, which is the core of the caching aspect.
     * <p>
     * This method configures the caching context, including the metrics provider ({@link Metrics}), based on the
     * {@code bus.cache.type} property. It supports various backends like H2, MySQL, Zookeeper, and in-memory.
     * </p>
     *
     * @return The configured {@link AspectjCacheProxy} instance.
     * @throws IllegalArgumentException if the specified cache type class cannot be resolved.
     */
    @Bean
    public AspectjCacheProxy cacheConfigurer() {
        try {
            if (StringKit.isNotEmpty(this.properties.getType())) {
                Object provider = ClassKit.loadClass(this.properties.getType());
                Context config = Context.newConfig(this.properties.getMap());

                Metrics metrics = null;
                if (provider instanceof H2Metrics) {
                    metrics = new H2Metrics(this.properties.getProvider().getUrl(),
                            this.properties.getProvider().getUsername(), this.properties.getProvider().getPassword());
                } else if (provider instanceof MySQLMetrics) {
                    metrics = new MySQLMetrics(BeanKit.beanToMap(this.properties));
                } else if (provider instanceof SqliteMetrics) {
                    metrics = new SqliteMetrics(this.properties.getProvider().getUrl(),
                            this.properties.getProvider().getUsername(), this.properties.getProvider().getPassword());
                } else if (provider instanceof ZookeeperMetrics) {
                    metrics = new ZookeeperMetrics(this.properties.getProvider().getUrl());
                } else if (provider instanceof MemoryMetrics) {
                    metrics = new MemoryMetrics();
                }
                config.setMetrics(metrics);
                return new AspectjCacheProxy(config);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot resolve class with type: " + this.properties.getType(), e);
        }
        return null; // Return null if no cache type is specified, effectively disabling the aspect.
    }

}
