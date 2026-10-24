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

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;

/**
 * Generates tenant-qualified cache, subscription, and revision keys.
 */
public class SettingGenerator implements Keying<SettingSpec> {

    /**
     * Shared stateless setting-key generator.
     */
    public static final SettingGenerator INSTANCE = new SettingGenerator();
    /**
     * Namespace segment used for item revision sequences.
     */
    public static final String SETTING_REVISION_SEQUENCE_SEGMENT = "setting:item:";

    /**
     * Creates a stateless generator using the standard configuration key layout.
     */
    public SettingGenerator() {
        // No initialization required.
    }

    /**
     * Builds a purpose-specific key from normalized, length-prefixed setting coordinates.
     *
     * @param spec key specification, or {@code null}
     * @return generated key, or {@code null} when the specification is {@code null}
     * @throws IllegalArgumentException if the purpose is unsupported, coordinates are invalid, or a historical revision
     *                                  is required but absent
     */
    @Override
    public String key(SettingSpec spec) {
        if (spec == null) {
            return null;
        }
        String identity = identity(spec);
        return switch (spec.mode()) {
            case SettingSpec.ITEM_ID, SettingSpec.SCOPE, SettingSpec.EXPORT -> identity;
            case SettingSpec.WATCH -> Builder.SETTING_PREFIX + identity;
            case SettingSpec.OVERLAY -> Builder.SETTING_PREFIX + "overlay:" + identity;
            case SettingSpec.ENTRY -> Builder.SETTING_PREFIX + "entry:" + identity;
            case SettingSpec.REVISION -> {
                if (spec.revision() == null || spec.revision().isBlank()) {
                    throw new IllegalArgumentException("A historical revision is required");
                }
                yield prefix(spec) + segment(spec.revision());
            }
            case SettingSpec.SEQUENCE -> Builder.SEQUENCE_PREFIX + SETTING_REVISION_SEQUENCE_SEGMENT + identity;
            default -> throw new IllegalArgumentException("Unknown configuration key purpose");
        };
    }

    /**
     * Builds the entry key prefix or historical revision prefix for normalized setting coordinates.
     *
     * @param spec prefix specification, or {@code null}; a revision value is not required
     * @return entry or revision prefix, or {@code null} for a null specification or any other purpose
     * @throws IllegalArgumentException if an entry or revision specification contains invalid coordinates
     */
    @Override
    public String prefix(SettingSpec spec) {
        if (spec == null) {
            return null;
        }
        return switch (spec.mode()) {
            case SettingSpec.ENTRY -> Builder.SETTING_PREFIX + "entry:" + identity(spec);
            case SettingSpec.REVISION -> Builder.SETTING_PREFIX + "revision:" + identity(spec) + ':';
            default -> null;
        };
    }

    /**
     * Length-prefixes segments to prevent collisions when values contain delimiters.
     *
     * @param spec configuration key specification with tenant and ownership coordinates
     * @return length-prefixed canonical business identity
     */
    private String identity(SettingSpec spec) {
        Item item = Item.builder().tenant_id(spec.tenant_id()).scope(spec.scope()).app_id(spec.app_id())
                .code(spec.code()).kind(spec.kind()).build();
        ItemNormalizer.coordinates(item);
        return segment(item.getTenant_id()) + ':' + segment(item.getScope()) + ':' + segment(item.getApp_id()) + ':'
                + segment(item.getCode()) + ':' + segment(item.getKind());
    }

    /**
     * Encodes a nullable value as a collision-resistant length-prefixed segment.
     *
     * @param value segment value
     * @return encoded segment
     */
    private String segment(String value) {
        return value == null ? "-" : value.length() + "#" + value;
    }

}
