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
package org.miaixz.bus.cortex.setting.item;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.crypto.Builder;

/**
 * Canonical business normalization for setting items.
 *
 * @author Kimi Liu
 */
public final class ItemNormalizer {

    /**
     * Source name used for content stored directly on an item.
     */
    public static final String INLINE_SOURCE = "INLINE";

    /**
     * Prevents utility-class instantiation.
     */
    private ItemNormalizer() {
        // No initialization required.
    }

    /**
     * Applies canonical identifiers and default values to one item.
     *
     * @param entry item to normalize, or {@code null} to create one
     * @return normalized item
     */
    public static Item normalize(Item entry) {
        Item prepared = entry == null ? new Item() : entry;
        prepared.setSpace_id(CortexIdentity.space(prepared.getSpace_id()));
        if (StringKit.isEmpty(prepared.getId())) {
            prepared.setId(ID.objectId());
        }
        if (StringKit.isEmpty(prepared.getKind())) {
            prepared.setKind(Item.Kind.SOURCE.name());
        }
        if (StringKit.isEmpty(prepared.getSource())) {
            prepared.setSource(INLINE_SOURCE);
        }
        if (StringKit.isEmpty(prepared.getFormat())) {
            prepared.setFormat(ItemFormat.TEXT.name());
        }
        if (StringKit.isEmpty(prepared.getEditor())) {
            prepared.setEditor(Item.Editor.SOURCE.name());
        }
        if (StringKit.isEmpty(prepared.getExposure())) {
            prepared.setExposure(ItemExposure.INTERNAL.name());
        }
        if (prepared.getEncrypted() == null) {
            prepared.setEncrypted(0);
        }
        if (prepared.getEdition() == null) {
            prepared.setEdition(0L);
        }
        if (prepared.getGeneration() == null) {
            prepared.setGeneration(0L);
        }
        if (StringKit.isEmpty(prepared.getFingerprint())) {
            prepared.setFingerprint(fingerprint(prepared));
        }
        if (StringKit.isEmpty(prepared.getChecksum())) {
            prepared.setChecksum(checksum(prepared));
        }
        return prepared;
    }

    /**
     * Calculates the immutable-coordinate fingerprint of one item.
     *
     * @param entry item whose coordinates are hashed
     * @return SHA-256 fingerprint
     */
    public static String fingerprint(Item entry) {
        return Builder.sha256(
                String.join(
                        "\u001f",
                        value(entry.getTenant_id()),
                        value(entry.getSpace_id()),
                        value(entry.getProfile_id()),
                        value(entry.getGroup()),
                        value(entry.getCode()),
                        value(entry.getKind())));
    }

    /**
     * Calculates the semantic-content checksum of one item.
     *
     * @param entry item whose content is hashed
     * @return SHA-256 checksum
     */
    public static String checksum(Item entry) {
        if (entry == null) {
            return Builder.sha256("");
        }
        return Builder.sha256(
                String.join(
                        "\u001f",
                        value(entry.getContent()),
                        value(entry.getPayload()),
                        value(entry.getFormat()),
                        value(entry.getEditor()),
                        value(entry.getSource()),
                        value(entry.getSpec()),
                        value(entry.getExposure()),
                        value(entry.getEncrypted()),
                        value(entry.getLabels()),
                        value(entry.getExtension())));
    }

    /**
     * Tests whether the persisted encryption flag is enabled.
     *
     * @param encrypted persisted encryption flag
     * @return {@code true} when the flag is exactly {@code 1}
     */
    public static boolean isEncryptedFlagEnabled(Integer encrypted) {
        return encrypted != null && encrypted.intValue() == 1;
    }

    /**
     * Converts a nullable value to its canonical hash input.
     *
     * @param value source value
     * @return empty text for {@code null}, otherwise the string value
     */
    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
