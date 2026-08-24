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

import java.util.Map;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.cache.Options;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.Registry;

/**
 * Configuration properties for the object storage service.
 * <p>
 * This class binds properties from the configuration file (e.g., {@code application.yml}) by default. It can also be
 * configured dynamically through setter methods (e.g., from a database).
 *
 * @author Kimi Liu
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.STORAGE)
public class StorageProperties {

    /**
     * Whether storage integration is enabled.
     */
    private final boolean enabled;

    /**
     * A map of storage provider configurations, where the key is the provider {@link Registry} type and the value is
     * the {@link Context} containing the specific configuration for that provider.
     */
    private final Map<Registry, Context> type;

    /**
     * Nested configuration for caching options related to the storage service.
     */
    @NestedConfigurationProperty
    private final Options cache;

    /**
     * Binds immutable storage provider and cache settings from {@code bus.storage}.
     *
     * @param enabled whether storage integration is enabled
     * @param type    provider configuration grouped by storage registry type
     * @param cache   cache backend settings used by storage services
     */
    public StorageProperties(@DefaultValue("false") boolean enabled, Map<Registry, Context> type, Options cache) {
        this.enabled = enabled;
        this.type = type == null ? Map.of() : Map.copyOf(type);
        this.cache = cache;
    }

    /**
     * @return masked diagnostic representation
     */
    @Override
    public String toString() {
        return "StorageProperties[enabled=" + enabled + ", channels=" + type.size()
                + ", endpoints=validated, buckets=validated, credentials=***, cache=***]";
    }

}
