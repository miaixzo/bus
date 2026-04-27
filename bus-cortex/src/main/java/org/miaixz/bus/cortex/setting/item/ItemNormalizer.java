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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.Type;

/**
 * Canonical normalization policy for setting entries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ItemNormalizer {

    /**
     * Built-in source used for inline setting content.
     */
    public static final String INLINE_SOURCE = "INLINE";

    /**
     * Creates the item normalization utility holder.
     */
    private ItemNormalizer() {

    }

    /**
     * Applies canonical defaults and derived identifiers to one setting entry before persistence or resolution.
     *
     * @param entry raw setting entry
     * @return normalized setting entry
     */
    public static Item normalize(Item entry) {
        Item prepared = entry == null ? new Item() : entry;
        ItemBindingProjection.normalizeProfileIdsInto(prepared, ItemBindingProjection.normalizedProfileIds(prepared));
        ItemBindingProjection.normalizeAppIdsInto(prepared, ItemBindingProjection.normalizedAppIds(prepared));
        if (prepared.getExtension() != null && !(prepared.getExtension() instanceof LinkedHashMap<?, ?>)) {
            ItemBindingProjection.copyExtensionInto(prepared, new LinkedHashMap<>(prepared.getExtension()));
        }
        prepared.setNamespace_id(CortexIdentity.namespace(prepared.getNamespace_id()));
        prepared.setType(Type.ITEM.key());
        prepared.setId(ItemKeys.itemId(prepared.getNamespace_id(), prepared.getGroup(), prepared.getData_id()));
        if (prepared.getSource() == null) {
            prepared.setSource(INLINE_SOURCE);
        }
        if (prepared.getFormat() == null) {
            prepared.setFormat(ItemFormat.TEXT.name());
        }
        if (prepared.getExposure() == null) {
            prepared.setExposure(ItemExposure.INTERNAL.name());
        }
        if (prepared.getEncrypted() == null) {
            prepared.setEncrypted(0);
        }
        if (prepared.getStatus() == null) {
            prepared.setStatus(1);
        }
        if (prepared.getChecksum() == null || prepared.getChecksum().isBlank()) {
            prepared.setChecksum(checksum(prepared));
        }
        return prepared;
    }

    /**
     * Computes a stable checksum for one setting entry.
     *
     * @param entry setting entry
     * @return checksum text
     */
    public static String checksum(Item entry) {
        if (entry == null) {
            return "0";
        }
        Map<String, Object> extension = entry.getExtension();
        return Integer.toHexString(
                Objects.hash(
                        entry.getNamespace_id(),
                        entry.getGroup(),
                        entry.getData_id(),
                        ItemBindingProjection.normalizedAppIds(entry),
                        ItemBindingProjection.normalizedProfileIds(entry),
                        entry.getContent(),
                        entry.getSource(),
                        entry.getSpec(),
                        extension == null ? null : new LinkedHashMap<>(extension),
                        entry.getFormat(),
                        entry.getExposure(),
                        entry.getEncrypted(),
                        entry.getRule()));
    }

    /**
     * Returns whether the encrypted flag represents encrypted content.
     *
     * @param encrypted encrypted flag
     * @return {@code true} when the flag is enabled
     */
    public static boolean isEncryptedFlagEnabled(Integer encrypted) {
        return encrypted != null && encrypted.intValue() == 1;
    }

}
