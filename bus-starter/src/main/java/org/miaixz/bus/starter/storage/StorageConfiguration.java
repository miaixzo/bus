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
package org.miaixz.bus.starter.storage;

import jakarta.annotation.Resource;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.storage.cache.StorageCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration class for storage services.
 *
 * <p>
 * This class is responsible for creating and configuring the following main components:
 *
 * <ul>
 * <li>{@link StorageService} - A factory for creating various storage service providers.</li>
 * <li>{@link CacheX} - The caching implementation for storage, defaulting to {@link StorageCache}.</li>
 * </ul>
 *
 * <pre>
 * // Example configuration in application.yml
 * bus:
 * storage:
 * cache:
 * type: default  # Use the default cache
 *
 * // Example usage in code
 * &#64;Autowired
 * private StorageService storageService;
 *
 * // Get the Aliyun OSS storage provider
 * StorageProvider ossProvider = storageService.require(StorageRegistry.ALIYUN_OSS);
 * // Upload a file
 * ossProvider.upload("filePath", "fileContent");
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { StorageProperties.class })
public class StorageConfiguration {

    /**
     * Storage configuration properties, which include settings for various storage providers. This is automatically
     * injected by Spring Boot via the {@link EnableConfigurationProperties} annotation.
     */
    @Resource
    private StorageProperties properties;

    /**
     * Creates the primary {@link StorageService} bean.
     *
     * <p>
     * This method creates a {@link StorageService} instance, which acts as a factory for managing and creating various
     * storage service providers. The instance is initialized with the provided cache implementation and configuration
     * properties.
     * </p>
     *
     * @param cachex The caching implementation, used for storing file metadata and other temporary data.
     * @return A fully configured {@code StorageService} instance.
     */
    @Bean
    public StorageService storageService(CacheX cachex) {
        return new StorageService(this.properties, cachex);
    }

    /**
     * Creates the default {@link CacheX} bean for storage operations.
     *
     * <p>
     * This bean is only created if the following conditions are met:
     * </p>
     * <ul>
     * <li>No custom {@link CacheX} bean is already present in the container.</li>
     * <li>The configuration property {@code bus.storage.cache.type} is set to "default" (or is missing).</li>
     * </ul>
     *
     * @return The default storage cache implementation instance.
     */
    @Bean
    @ConditionalOnMissingBean(CacheX.class)
    @ConditionalOnProperty(name = GeniusBuilder.STORAGE + ".cache.type", havingValue = "default", matchIfMissing = true)
    public CacheX storageCache() {
        return StorageCache.INSTANCE;
    }

}
