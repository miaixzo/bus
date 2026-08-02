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
package org.miaixz.bus.starter.notify;

import java.util.Map;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.Registry;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable notification channel properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.NOTIFY)
public class NotifyProperties {

    /**
     * Whether the notify integration is enabled.
     */
    private final boolean enabled;
    /**
     * Notification channel definitions grouped by provider type.
     */
    private final Map<Registry, Context> type;

    /**
     * Binds immutable notification channel definitions and normalizes an absent provider map to an empty map.
     *
     * @param enabled whether notification integration is enabled
     * @param type    uniquely named provider configurations whose credentials are external references
     */
    public NotifyProperties(@DefaultValue("false") boolean enabled, @DefaultValue Map<Registry, Context> type) {
        this.enabled = enabled;
        this.type = type == null ? Map.of() : Map.copyOf(type);
    }

    /**
     * @return masked diagnostic representation
     */
    @Override
    public String toString() {
        return "NotifyProperties[enabled=" + enabled + ", channels=" + type.size() + ", credentials=***]";
    }

}
