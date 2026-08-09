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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Framework-level route-prefix options independent of Spring Boot configuration binding.
 *
 * @author Kimi Liu
 */
public class RoutePrefixOptions {

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
     * Creates normalized route-prefix options.
     *
     * @param prefix       route prefix
     * @param basePackages base packages
     * @param inStorage    whether discovered routes are retained
     */
    public RoutePrefixOptions(String prefix, List<String> basePackages, boolean inStorage) {
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
        this.prefix = normalizedPrefix;
        this.basePackages = List.copyOf(normalizedPackages);
        this.inStorage = inStorage;
    }

    /**
     * Returns the normalized route prefix.
     *
     * @return route prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the ordered unique base packages.
     *
     * @return base packages
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

    /**
     * Returns whether discovered routes are retained.
     *
     * @return whether route metadata is retained
     */
    public boolean isInStorage() {
        return inStorage;
    }

}
