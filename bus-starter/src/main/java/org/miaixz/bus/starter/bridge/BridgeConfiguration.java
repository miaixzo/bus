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
package org.miaixz.bus.starter.bridge;

import io.vertx.core.Vertx;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for the configuration center bridge.
 * <p>
 * This class enables {@link BridgeProperties}, imports the {@link BridgePropertyLoader} to load remote configurations,
 * and sets up the necessary Vert.x infrastructure for the configuration server.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Import({ BridgePropertyLoader.class })
@EnableConfigurationProperties(value = { BridgeProperties.class })
public class BridgeConfiguration {

    /**
     * Injected configuration bridge properties.
     */
    @Resource
    BridgeProperties properties;

    /**
     * Creates the core Vert.x instance.
     *
     * @return A new {@link Vertx} instance.
     */
    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    /**
     * Creates the Vert.x verticle service for the configuration bridge.
     * <p>
     * The bean's lifecycle is managed by Spring, with its {@code start} and {@code stop} methods called automatically
     * on application startup and shutdown.
     * </p>
     *
     * @return A new {@link BridgeVerticleService} instance.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public BridgeVerticleService verticle() {
        return new BridgeVerticleService(properties);
    }

}
