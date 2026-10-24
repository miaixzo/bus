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
package org.miaixz.bus.cortex.builtin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;

/**
 * Length-prefixed exact current-state, published-snapshot and application-delivery identities.
 *
 * @author Kimi Liu
 */
public class SettingGenerator implements Keying<SettingSpec> {

    /**
     * Reusable stateless default generator.
     */
    public static final SettingGenerator INSTANCE = new SettingGenerator();

    /**
     * Creates an empty SettingGenerator for construction or deserialization.
     */
    public SettingGenerator() {
        // No initialization required.
    }

    /**
     * Rejects a missing required identity component.
     *
     * @param value identity coordinate
     */
    private static void require(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Key coordinate is required");
        }
    }

    /**
     * Builds or returns the exact identity component for the current domain purpose.
     *
     * @param spec purpose-specific identity coordinates
     * @return generated identity key, or null for a null specification
     */
    @Override
    public String key(SettingSpec spec) {
        if (spec == null) {
            return null;
        }
        String identity = identity(spec);
        return switch (spec.mode()) {
            case SettingSpec.ITEM_ID, SettingSpec.SCOPE, SettingSpec.EXPORT -> identity;
            case SettingSpec.WATCH -> Builder.SETTING_PREFIX + "v2:watch:" + identity;
            case SettingSpec.ENTRY -> Builder.SETTING_PREFIX + "v2:entry:" + identity;
            case SettingSpec.REVISION -> {
                require(spec.revision_id());
                yield prefix(spec) + segment(spec.revision_id());
            }
            default -> throw new IllegalArgumentException("Unknown setting key purpose");
        };
    }

    /**
     * Returns the cache-scan prefix for the selected key purpose, when supported.
     *
     * @param spec purpose-specific identity coordinates
     * @return cache prefix, or null when the specification or purpose has no prefix
     */
    @Override
    public String prefix(SettingSpec spec) {
        if (spec == null) {
            return null;
        }
        return switch (spec.mode()) {
            case SettingSpec.ENTRY -> Builder.SETTING_PREFIX + "v2:entry:" + identity(spec);
            case SettingSpec.REVISION -> Builder.SETTING_PREFIX + "v2:revision:" + identity(spec) + Symbol.C_COLON;
            default -> null;
        };
    }

    /**
     * Builds or reads exact tenant-qualified identity without mutable ownership coordinates.
     *
     * @param spec purpose-specific identity coordinates
     * @return length-prefixed tenant and source or application coordinates
     */
    private String identity(SettingSpec spec) {
        require(spec.tenant_id());
        if (spec.mode() == SettingSpec.WATCH || spec.mode() == SettingSpec.EXPORT) {
            require(spec.app_id());
            boolean exact = spec.item_id() != null && !spec.item_id().isBlank();
            String coordinate = exact ? spec.item_id() : spec.code();
            require(coordinate);
            return segment(spec.tenant_id()) + segment(spec.app_id()) + (exact ? "id:" : "alias:")
                    + segment(coordinate);
        }
        require(spec.item_id());
        return segment(spec.tenant_id()) + segment(spec.item_id());
    }

    /**
     * Encodes an unambiguous length-prefixed identity component.
     *
     * @param value identity coordinate
     * @return coordinate preceded by its character count and a hash separator
     */
    private String segment(String value) {
        return value.length() + Symbol.HASH + value;
    }

}
