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
package org.miaixz.bus.cortex.setting.secret;

import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemBindingProjection;
import org.miaixz.bus.cortex.setting.item.ItemExposure;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevision;

/**
 * Masks sensitive setting values for management views and audit-safe output.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SecretMasker {

    /**
     * Creates a secret masker.
     */
    public SecretMasker() {

    }

    /**
     * Returns a masked representation of a secret.
     *
     * @param value original value
     * @return masked value
     */
    public String mask(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    /**
     * Returns a masked copy for management responses when the entry is secret or encrypted.
     *
     * @param entry source entry
     * @return masked copy
     */
    public Item mask(Item entry) {
        if (entry == null) {
            return null;
        }
        Item copy = Item.builder().namespace_id(entry.getNamespace_id()).id(entry.getId()).type(entry.getType())
                .status(entry.getStatus()).creator(entry.getCreator()).created(entry.getCreated())
                .modifier(entry.getModifier()).modified(entry.getModified())
                .profile_ids(ItemBindingProjection.normalizedProfileIds(entry))
                .app_ids(ItemBindingProjection.normalizedAppIds(entry)).group(entry.getGroup())
                .data_id(entry.getData_id()).content(entry.getContent()).source(entry.getSource()).spec(entry.getSpec())
                .extension(entry.getExtension()).labels(entry.getLabels()).format(entry.getFormat())
                .exposure(entry.getExposure()).encrypted(entry.getEncrypted()).rule(entry.getRule())
                .checksum(entry.getChecksum()).description(entry.getDescription()).build();
        copy.setRevision(entry.getRevision());
        if (ItemNormalizer.isEncryptedFlagEnabled(entry.getEncrypted())
                || ItemExposure.SECRET.name().equals(entry.getExposure())) {
            copy.setContent(mask(entry.getContent()));
            copy.setSpec(mask(entry.getSpec()));
        }
        return copy;
    }

    /**
     * Returns a masked copy for {@code setting.item.revision} management responses.
     *
     * @param revision source revision
     * @return masked copy
     */
    public ItemRevision mask(ItemRevision revision) {
        if (revision == null) {
            return null;
        }
        ItemRevision copy = ItemRevision.builder().item_id(revision.getItem_id())
                .namespace_id(revision.getNamespace_id()).group(revision.getGroup()).data_id(revision.getData_id())
                .profile_ids(ItemBindingProjection.normalizedProfileIds(revision))
                .app_ids(ItemBindingProjection.normalizedAppIds(revision)).content(revision.getContent())
                .source(revision.getSource()).spec(revision.getSpec()).extension(revision.getExtension())
                .format(revision.getFormat()).exposure(revision.getExposure()).encrypted(revision.getEncrypted())
                .rule(revision.getRule()).checksum(revision.getChecksum()).diff(revision.getDiff())
                .revert(revision.getRevert()).status(revision.getStatus()).created(revision.getCreated())
                .modified(revision.getModified()).build();
        copy.setRevision(revision.getRevision());
        if (ItemNormalizer.isEncryptedFlagEnabled(revision.getEncrypted())
                || ItemExposure.SECRET.name().equals(revision.getExposure())) {
            copy.setContent(mask(revision.getContent()));
            copy.setSpec(mask(revision.getSpec()));
        }
        return copy;
    }

}
