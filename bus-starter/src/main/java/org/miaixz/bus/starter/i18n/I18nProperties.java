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
package org.miaixz.bus.starter.i18n;

import java.util.LinkedHashSet;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable internationalization resource bundle properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(GeniusBuilder.I18N)
public final class I18nProperties {

    /**
     * Whether the i18n integration is enabled.
     */
    private final boolean enabled;
    /**
     * Character encoding used when loading message bundles.
     */
    private final String defaultEncoding;
    /**
     * Ordered message bundle base names searched by Spring.
     */
    private final String[] baseNames;

    /**
     * Normalizes message bundle names and applies the default encoding used by Spring's message source.
     *
     * @param enabled         whether i18n integration is enabled
     * @param defaultEncoding resource bundle encoding
     * @param baseNames       ordered resource bundle base names
     */
    public I18nProperties(@DefaultValue("false") boolean enabled, @DefaultValue("UTF-8") String defaultEncoding,
            @DefaultValue("messages") String[] baseNames) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (baseNames != null) {
            for (String baseName : baseNames) {
                String value = baseName == null ? Normal.EMPTY : baseName.trim();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("bus.i18n.base-names must not contain blank entries");
                }
                normalized.add(value);
            }
        }
        if (normalized.isEmpty()) {
            normalized.add("messages");
        }
        this.enabled = enabled;
        this.defaultEncoding = defaultEncoding;
        this.baseNames = normalized.toArray(String[]::new);
    }

    /**
     * Returns the normalized immutable message bundle base-name list.
     *
     * @return a defensive copy of ordered bundle base names
     */
    public String[] getBaseNames() {
        return baseNames.clone();
    }

}
