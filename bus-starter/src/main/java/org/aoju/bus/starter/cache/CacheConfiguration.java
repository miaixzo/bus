/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.starter.cache;

import org.aoju.bus.cache.Context;
import org.aoju.bus.cache.provider.*;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存配置
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
@EnableConfigurationProperties(value = {CacheProperties.class})
public class CacheConfiguration {

    @Autowired
    CacheProperties properties;

    @Bean
    public AspectjCacheProxy cacheConfigurer() {
        String type = StringUtils.toString(this.properties.getType());
        try {
            if (!StringUtils.isEmpty(type)) {
                Object provider = ClassUtils.loadClass(type);
                Context config = Context.newConfig(this.properties.getMap());
                if (provider instanceof H2Shooting) {
                    config.setShooting(new H2Shooting(
                            this.properties.getProvider().getUrl(),
                            this.properties.getProvider().getUsername(),
                            this.properties.getProvider().getPassword()
                    ));
                } else if (provider instanceof MySQLShooting) {
                    config.setShooting(new MySQLShooting(
                            BeanUtils.beanToMap(this.properties)
                    ));
                } else if (provider instanceof SqliteShooting) {
                    config.setShooting(new SqliteShooting(
                            this.properties.getProvider().getUrl(),
                            this.properties.getProvider().getUsername(),
                            this.properties.getProvider().getPassword()
                    ));
                } else if (provider instanceof ZKShooting) {
                    config.setShooting(new ZKShooting(
                            this.properties.getProvider().getUrl()
                    ));
                } else if (provider instanceof MemoryShooting) {
                    config.setShooting(new MemoryShooting());
                }
                return new AspectjCacheProxy(config);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("can not resolve class with type: " + type);
        }
        return null;
    }

}
