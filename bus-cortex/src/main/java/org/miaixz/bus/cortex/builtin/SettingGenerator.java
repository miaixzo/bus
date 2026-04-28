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

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;

/**
 * Built-in setting-side {@link Keying} implementation.
 * <p>
 * This implementation centralizes all default setting key semantics, including:
 * </p>
 * <ul>
 * <li>logical item identifiers</li>
 * <li>profile scopes</li>
 * <li>watch and overlay cache keys</li>
 * <li>current-state entry keys and entry scan prefixes</li>
 * <li>revision keys, revision prefixes, and revision-sequence keys</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SettingGenerator implements Keying<SettingSpec> {

    /**
     * Shared default instance.
     */
    public static final SettingGenerator INSTANCE = new SettingGenerator();

    /**
     * Sequence stream segment used for setting item revisions.
     */
    public static final String SETTING_REVISION_SEQUENCE_SEGMENT = "setting:item:";

    /**
     * Creates the default setting key generator.
     */
    public SettingGenerator() {

    }

    /**
     * Builds the strongest key represented by the supplied setting specification.
     *
     * @param spec setting key specification
     * @return primary setting key or {@code null}
     */
    @Override
    public String key(SettingSpec spec) {
        if (spec == null) {
            return null;
        }
        return switch (spec.mode()) {
            case SettingSpec.ITEM_ID -> itemId(spec);
            case SettingSpec.PROFILE_SCOPE -> buildScope(spec);
            case SettingSpec.WATCH -> Builder.SETTING_PREFIX + buildScope(spec);
            case SettingSpec.OVERLAY -> Builder.SETTING_PREFIX + "overlay:" + buildScope(spec);
            case SettingSpec.EXPORT -> buildScope(spec);
            case SettingSpec.ENTRY -> Builder.SETTING_PREFIX + "entry:" + buildScope(spec);
            case SettingSpec.REVISION -> prefix(spec) + spec.revisionToken();
            case SettingSpec.SEQUENCE -> Builder.SEQUENCE_PREFIX + SETTING_REVISION_SEQUENCE_SEGMENT + itemId(spec);
            default -> null;
        };
    }

    /**
     * Builds the scan prefix for setting modes that support range-style lookup.
     *
     * @param spec setting key specification
     * @return setting scan prefix or {@code null} when the mode has no prefix form
     */
    @Override
    public String prefix(SettingSpec spec) {
        if (spec == null) {
            return null;
        }
        return switch (spec.mode()) {
            case SettingSpec.ENTRY -> spec.groupToken() == null && spec.dataIdToken() == null
                    ? Builder.SETTING_PREFIX + "entry:" + CortexIdentity.namespace(spec.namespace()) + ":"
                    : Builder.SETTING_PREFIX + "entry:" + itemId(spec);
            case SettingSpec.REVISION -> Builder.SETTING_PREFIX + "revision:" + buildScope(spec) + ":";
            default -> null;
        };
    }

    /**
     * Builds the logical item identifier portion shared by multiple setting keys.
     *
     * @param spec setting key specification
     * @return item identifier
     */
    private String itemId(SettingSpec spec) {
        return CortexIdentity.namespace(spec.namespace()) + ":" + StringKit.emptyIfNull(spec.groupToken()) + ":"
                + StringKit.emptyIfNull(spec.dataIdToken());
    }

    /**
     * Builds the logical profile scope used by watch, overlay, entry, export, and revision keys.
     *
     * @param spec setting key specification
     * @return profile scope identifier
     */
    private String buildScope(SettingSpec spec) {
        String itemId = itemId(spec);
        String profile = spec.profileToken();
        return profile == null ? itemId : itemId + ":" + profile;
    }

}
