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
package org.miaixz.bus.cortex.registry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.cortex.Assets;

/**
 * Existing-asset lookup result keyed by registry route identity.
 *
 * @param existingByRoute           existing assets keyed by route
 * @param missingRoutes             routes known to be missing
 * @param authoritativeByRoute      whether route lookup is authoritative for missing routes
 * @param skipMethodVersionFallback whether method/version fallback should be skipped
 * @param warnings                  lookup warnings
 * @author Kimi Liu
 * @since Java 21+
 */
public record RegistryBatchLookup(Map<RegistryRouteKey, Assets> existingByRoute, Set<RegistryRouteKey> missingRoutes,
        boolean authoritativeByRoute, boolean skipMethodVersionFallback, List<String> warnings) {

    /**
     * Normalizes null collections to immutable empty collections.
     */
    public RegistryBatchLookup {
        existingByRoute = existingByRoute == null ? Map.of() : Map.copyOf(existingByRoute);
        missingRoutes = missingRoutes == null ? Set.of() : Set.copyOf(missingRoutes);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    /**
     * Returns an empty lookup result.
     *
     * @return empty result
     */
    public static RegistryBatchLookup empty() {
        return new RegistryBatchLookup(Map.of(), Set.of(), false, false, List.of());
    }

}
