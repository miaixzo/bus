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
package org.miaixz.bus.cortex.setting.curator;

import java.util.Map;

import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.secret.SecretContext;

/**
 * Strategy interface used to resolve setting values from different sources.
 *
 * @author Kimi Liu
 */
public interface ItemSourceAdapter {

    /**
     * Returns the supported source.
     *
     * @return supported source
     */
    String source();

    /**
     * Returns whether the adapter can handle the supplied entry.
     *
     * @param entry setting entry
     * @return {@code true} when the adapter should handle the entry
     */
    default boolean supports(Item entry) {
        return entry != null && source().equalsIgnoreCase(entry.getSource());
    }

    /**
     * Validates whether the source metadata is sufficient for resolution.
     *
     * @param entry setting entry
     * @return {@code true} when the entry is valid for this adapter
     */
    default boolean validate(Item entry) {
        return supports(entry);
    }

    /**
     * Resolves the effective string value for a setting entry.
     *
     * @param entry setting entry
     * @return resolved value
     */
    String resolve(Item entry);

    /**
     * Resolves with trusted context. Ordinary sources delegate to the existing overload; SECRET sources must override
     * this method explicitly and are rejected by default.
     *
     * @param entry   setting entry or already selected snapshot; null is handled as documented
     * @param context authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                reference
     * @return resolved string, configured fallback where applicable, or null when no value is available
     * @throws IllegalStateException if a SECRET source has not explicitly implemented protected resolution
     */
    default String resolve(Item entry, SecretContext context) {
        if (ItemNormalizer.isSecret(entry)) {
            throw new IllegalStateException("SECRET requires the protected source adapter");
        }
        return resolve(entry);
    }

    /**
     * Returns a preview value without mutating external state.
     *
     * @param entry setting entry
     * @return preview value
     */
    default String preview(Item entry) {
        return resolve(entry);
    }

    /**
     * Returns adapter capability hints.
     *
     * @return capability map
     */
    default Map<String, Object> capabilities() {
        return Map.of("source", source());
    }

}
