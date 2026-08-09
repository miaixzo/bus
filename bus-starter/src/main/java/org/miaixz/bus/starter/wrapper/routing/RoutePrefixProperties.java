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
package org.miaixz.bus.starter.wrapper.routing;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.spring.web.routing.RoutePrefixOptions;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Spring Boot binding properties for controller route-prefix handling.
 *
 * @author Kimi Liu
 */
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.WRAPPER_ROUTE_PREFIX)
public class RoutePrefixProperties {

    /**
     * Whether route-prefix handling is enabled.
     */
    private final boolean enabled;
    /**
     * Path prefix prepended to matching controller routes.
     */
    private final String prefix;
    /**
     * Controller package roots eligible for route prefixing.
     */
    private final List<String> basePackages;
    /**
     * Whether discovered route metadata is retained for inspection.
     */
    private final boolean inStorage;

    /**
     * Creates route-prefix binding properties.
     *
     * @param enabled      whether route-prefix handling is enabled
     * @param prefix       route prefix
     * @param basePackages controller package roots
     * @param inStorage    whether discovered routes are retained
     */
    public RoutePrefixProperties(@DefaultValue(Normal.FALSE) boolean enabled, @DefaultValue(Normal.EMPTY) String prefix,
            @DefaultValue List<String> basePackages, @DefaultValue(Normal.FALSE) boolean inStorage) {
        this.enabled = enabled;
        this.prefix = prefix;
        this.basePackages = basePackages == null ? List.of() : List.copyOf(basePackages);
        this.inStorage = inStorage;
    }

    /**
     * Returns whether route-prefix handling is enabled.
     *
     * @return whether route-prefix handling is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the configured route prefix.
     *
     * @return route prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the configured controller package roots.
     *
     * @return controller package roots
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

    /**
     * Returns whether discovered routes are retained.
     *
     * @return whether discovered routes are retained
     */
    public boolean isInStorage() {
        return inStorage;
    }

    /**
     * Converts binding properties into framework-level routing options.
     *
     * @return normalized route-prefix options
     */
    public RoutePrefixOptions toOptions() {
        return new RoutePrefixOptions(prefix, basePackages, inStorage);
    }

}
