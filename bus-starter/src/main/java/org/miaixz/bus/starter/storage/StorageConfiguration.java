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
package org.miaixz.bus.starter.storage;

import jakarta.annotation.Resource;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.storage.cache.StorageCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * StorageProvider ossProvider = storageService.require(Registry.ALIYUN_OSS);
 * // Upload a file
 * ossProvider.upload("filePath", "fileContent");
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
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
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService storageService(@Qualifier("storageCache") CacheX<String, Object> cachex) {
        return new StorageService(this.properties, cachex);
    }

    /**
     * Creates the storage cache implementation bean.
     * <p>
     * Resolution order:
     * <ul>
     * <li>If {@code bus.storage.cache.*} configures a concrete backend type, create a storage-specific cache through
     * {@link Factory}.</li>
     * <li>If {@code bus.storage.cache.type=default} or no storage-specific backend is configured, reuse the shared
     * {@code defaultCache} bean when available.</li>
     * <li>If neither storage nor global cache is configured, fall back to {@link StorageCache#INSTANCE}.</li>
     * </ul>
     *
     * @param factory              shared cache factory
     * @param defaultCacheProvider shared default cache provider
     * @return storage cache implementation
     *
     */
    @Bean("storageCache")
    @ConditionalOnMissingBean(name = "storageCache")
    public CacheX<String, Object> storageCache(
            Factory factory,
            @Qualifier("defaultCache") ObjectProvider<CacheX<String, Object>> defaultCacheProvider) {
        if (hasStorageBackend()) {
            return factory.initialize(this.properties.getCache());
        }
        CacheX<String, Object> defaultCache = defaultCacheProvider.getIfAvailable();
        return defaultCache != null ? defaultCache : StorageCache.INSTANCE;
    }

    /**
     * Returns whether storage defines its own concrete cache backend instead of reusing the shared default cache.
     *
     * @return {@code true} when storage should initialize a dedicated backend
     */
    private boolean hasStorageBackend() {
        if (this.properties.getCache() == null) {
            return false;
        }
        String type = this.properties.getCache().getType();
        return StringKit.isNotBlank(type) && !"default".equalsIgnoreCase(type.trim());
    }

}
