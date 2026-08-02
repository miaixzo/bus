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
package org.miaixz.bus.starter.json;

import java.util.Locale;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable JSON provider selection properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.JSON)
public final class JsonProperties {

    /**
     * Whether the json integration is enabled.
     */
    private final boolean enabled;
    /**
     * JSON provider selected for the current application context.
     */
    private final Provider provider;

    /**
     * Creates JSON properties.
     *
     * @param enabled  whether JSON integration is enabled
     * @param provider requested provider
     */
    public JsonProperties(@DefaultValue("false") boolean enabled, @DefaultValue("AUTO") Provider provider) {
        this.enabled = enabled;
        this.provider = provider;
    }

    /**
     * Supported JSON provider selections.
     */
    public enum Provider {

        /**
         * Require exactly one available provider.
         */
        AUTO,
        /**
         * Fastjson provider.
         */
        FASTJSON,
        /**
         * Gson provider.
         */
        GSON,
        /**
         * Jackson provider.
         */
        JACKSON;

        /**
         * Returns the canonical key used to select this JSON Provider.
         *
         * @return canonical provider name consumed by the JSON factory
         */
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}
