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
package org.miaixz.bus.starter.pay;

import java.util.Map;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.cache.Options;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configuration properties for the integrated payment service.
 * <p>
 * This class binds properties from the configuration file by default. It can also be configured dynamically through
 * setter methods (e.g., from a database).
 *
 * @author Kimi Liu
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.PAY)
public class PayProperties {

    /**
     * Binds immutable payment provider and cache settings from {@code bus.pay}.
     *
     * @param enabled whether payment integration is enabled
     * @param type    provider configuration grouped by payment registry type
     * @param cache   cache backend settings used by payment services
     */
    public PayProperties(@DefaultValue("false") boolean enabled, Map<Registry, Context> type, Options cache) {
        this.enabled = enabled;
        this.type = type == null ? Map.of() : Map.copyOf(type);
        this.cache = cache;
    }

    /**
     * Whether payment integration is enabled.
     */
    private final boolean enabled;

    /**
     * A map of payment provider configurations, where the key is the payment provider {@link Registry} type and the
     * value is the {@link Context} containing the specific configuration for that provider.
     */
    private final Map<Registry, Context> type;

    /**
     * Nested cache backend options for the payment module.
     * <p>
     * When present, these options initialize a pay-specific cache instance. When absent, pay falls back to the shared
     * default cache configuration or the legacy in-memory singleton.
     * </p>
     */
    @NestedConfigurationProperty
    private final Options cache;

    /**
     * @return masked diagnostic representation
     */
    @Override
    public String toString() {
        return "PayProperties[enabled=" + enabled + ", channels=" + type.size() + ", cache=***, credentials=***]";
    }

}
