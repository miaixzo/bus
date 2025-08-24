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
package org.miaixz.bus.starter.storage;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.storage.cache.StorageCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * 存储服务自动配置类，用于配置存储相关的Bean。
 *
 * 该类负责创建并配置以下主要组件：
 * <ul>
 * <li>{@link StorageProviderService} - 存储服务提供者工厂，用于创建各种存储服务</li>
 * <li>{@link CacheX} - 存储缓存实现，默认使用{@link StorageCache}作为缓存实现</li>
 * </ul>
 * 
 * <pre>
 * // 在application.yml中配置
 * bus:
 *   storage:
 *     cache:
 *       type: default  # 使用默认缓存
 *
 * // 在代码中直接注入使用
 * &#64;Autowired
 * private StorageProviderService storageProviderService;
 *
 * // 获取阿里云OSS存储服务
 * Provider ossProvider = storageProviderService.require(StorageRegistry.ALIYUN_OSS);
 * // 上传文件
 * ossProvider.upload("文件路径", "文件内容");
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { StorageProperties.class })
public class StorageConfiguration {

    /**
     * 存储配置属性，包含各种存储组件的配置信息。 通过{@link EnableConfigurationProperties}注解自动注入。
     */
    @Resource
    StorageProperties properties;

    /**
     * 创建存储服务提供者工厂Bean。
     *
     * <p>
     * 该方法创建一个{@link StorageProviderService}实例，用于管理和创建各种存储服务提供者。 该实例会使用传入的缓存实现和配置属性来初始化。
     * </p>
     *
     * @param CacheX 缓存实现，用于存储文件元数据等临时数据
     * @return 配置好的存储服务提供者工厂实例
     */
    @Bean
    public StorageProviderService storageProviderFactory(CacheX CacheX) {
        return new StorageProviderService(this.properties, CacheX);
    }

    /**
     * 创建默认的存储缓存实现Bean。 当满足以下条件时，该方法会创建一个默认的缓存实现：
     * <ul>
     * <li>容器中不存在自定义的{@link CacheX} Bean</li>
     * <li>配置属性中缓存类型设置为"default"（默认值）</li>
     * </ul>
     *
     * @return 默认的存储缓存实现实例
     */
    @Bean
    @ConditionalOnMissingBean(CacheX.class)
    @ConditionalOnProperty(name = GeniusBuilder.STORAGE + ".cache.type", havingValue = "default", matchIfMissing = true)
    public CacheX storageCache() {
        return StorageCache.INSTANCE;
    }

}