/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.cache.provider.*;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * 缓存配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { CacheProperties.class })
public class CacheConfiguration {

    @Resource
    CacheProperties properties;

    @Bean
    public AspectjCacheProxy cacheConfigurer() {
        try {
            if (StringKit.isNotEmpty(this.properties.getType())) {
                Object provider = ClassKit.loadClass(this.properties.getType());
                Context config = Context.newConfig(this.properties.getMap());
                if (provider instanceof H2Hitting) {
                    config.setHitting(new H2Hitting(this.properties.getProvider().getUrl(),
                            this.properties.getProvider().getUsername(), this.properties.getProvider().getPassword()));
                } else if (provider instanceof MySQLHitting) {
                    config.setHitting(new MySQLHitting(BeanKit.beanToMap(this.properties)));
                } else if (provider instanceof SqliteHitting) {
                    config.setHitting(new SqliteHitting(this.properties.getProvider().getUrl(),
                            this.properties.getProvider().getUsername(), this.properties.getProvider().getPassword()));
                } else if (provider instanceof ZookeeperHitting) {
                    config.setHitting(new ZookeeperHitting(this.properties.getProvider().getUrl()));
                } else if (provider instanceof MemoryHitting) {
                    config.setHitting(new MemoryHitting());
                }
                return new AspectjCacheProxy(config);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("can not resolve class with type: " + this.properties.getType());
        }
        return null;
    }

}
