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
package org.miaixz.bus.spring.web.routing;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Immutable opt-in controller route-prefix properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Validated
@ConfigurationProperties(prefix = "bus.wrapper.route-prefix")
public class RoutePrefixProperties {

    /**
     * Whether the route prefix integration is enabled.
     */
    private final boolean enabled;
    /**
     * Normalized path prefix prepended to matching controller routes.
     */
    private final String prefix;
    /**
     * Ordered controller package roots eligible for route prefixing.
     */
    private final List<String> basePackages;
    /**
     * Whether discovered route metadata is retained for later inspection.
     */
    private final boolean inStorage;

    /**
     * Creates route-prefix properties by normalizing the prefix and removing duplicate package roots in encounter
     * order.
     *
     * @param enabled      whether the feature is enabled
     * @param prefix       route prefix
     * @param basePackages base packages
     * @param inStorage    in storage
     */
    public RoutePrefixProperties(@DefaultValue(Normal.FALSE) boolean enabled, @DefaultValue(Normal.EMPTY) String prefix,
            @DefaultValue List<String> basePackages, @DefaultValue(Normal.FALSE) boolean inStorage) {
        String normalizedPrefix = prefix == null ? Normal.EMPTY : prefix.trim();
        if (!normalizedPrefix.isEmpty()
                && (!normalizedPrefix.startsWith(Symbol.SLASH) || normalizedPrefix.endsWith(Symbol.SLASH))) {
            throw new IllegalArgumentException("Route prefix must start with '/' and must not end with '/'");
        }
        LinkedHashSet<String> normalizedPackages = new LinkedHashSet<>();
        if (basePackages != null) {
            for (String basePackage : basePackages) {
                String value = basePackage == null ? Normal.EMPTY : basePackage.trim();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Route base packages must not contain blank entries");
                }
                normalizedPackages.add(value);
            }
        }
        this.enabled = enabled;
        this.prefix = normalizedPrefix;
        this.basePackages = List.copyOf(normalizedPackages);
        this.inStorage = inStorage;
    }

    /**
     * Returns whether route prefixing is enabled.
     *
     * @return whether route prefixing is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the normalized route prefix.
     *
     * @return normalized route prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the ordered unique base packages.
     *
     * @return ordered unique base packages
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

    /**
     * Returns whether discovered routes are stored.
     *
     * @return whether discovered routes are stored
     */
    public boolean isInStorage() {
        return inStorage;
    }

}
