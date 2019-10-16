/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.spring.cache;

import org.aoju.bus.cache.Aspectj;
import org.aoju.bus.cache.Context;
import org.aoju.bus.cache.provider.*;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存配置
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
@EnableConfigurationProperties(value = {CacheProperties.class})
public class CacheConfiguration {

    @Autowired
    CacheProperties properties;

    @Autowired
    Aspectj cacheAspect = cacheConfigurer();

    @Bean
    public Aspectj cacheConfigurer() {
        String type = StringUtils.toString(this.properties.getType());
        try {
            if (!StringUtils.isEmpty(type)) {
                Object provider = ClassUtils.loadClass(type);
                Context config = Context.newConfig(this.properties.getMap());
                if (provider instanceof H2Provider) {
                    // config.setProvider(new H2Provider());
                } else if (provider instanceof MySQLProvider) {
                    //config.setProvider(new MySQLProvider());
                } else if (provider instanceof SqliteProvider) {
                    //config.setProvider(new SqliteProvider());
                } else if (provider instanceof ZKProvider) {
                    //config.setProvider(new ZKProvider());
                } else if (provider instanceof MemoryProvider) {
                    //config.setProvider(new MemoryProvider());
                }
                this.cacheAspect = new Aspectj(config);
                return this.cacheAspect;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("can not resolve class with type: " + type);
        }
        return null;
    }

}
