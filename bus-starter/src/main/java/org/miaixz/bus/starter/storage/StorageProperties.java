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

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.cache.CacheProperties;
import org.miaixz.bus.storage.Context;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

/**
 * Configuration properties for the object storage service.
 * <p>
 * This class binds properties from the configuration file (e.g., {@code application.yml}) by default. It can also be
 * configured dynamically through setter methods (e.g., from a database).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.STORAGE)
public class StorageProperties {

    /**
     * A map of storage provider configurations, where the key is the provider {@link Registry} type and the value is
     * the {@link Context} containing the specific configuration for that provider.
     */
    private Map<Registry, Context> type;

    /**
     * Nested configuration for caching options related to the storage service.
     */
    @NestedConfigurationProperty
    private CacheProperties cache;

}
