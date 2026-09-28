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
import org.miaixz.bus.cortex.setting.item.ItemExposure;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.revision.Revision;

/**
 * Masks protected values in management responses.
 *
 * @author Kimi Liu
 */
public class SecretMasker {

    /**
     * Creates a secret masker.
     */
    public SecretMasker() {
        // No initialization required.
    }

    /**
     * Masks one scalar value while retaining a small identifying prefix and suffix.
     *
     * @param value value to mask
     * @return masked value, or the original null or empty value
     */
    public String mask(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.length() <= 4 ? "****" : value.substring(0, 2) + "****" + value.substring(value.length() - 2);
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
        Item copy = Item.builder().id(entry.getId()).tenant_id(entry.getTenant_id()).space_id(entry.getSpace_id())
                .profile_id(entry.getProfile_id()).stable_id(entry.getStable_id()).gray_id(entry.getGray_id())
                .rollout_id(entry.getRollout_id()).kind(entry.getKind()).group(entry.getGroup()).code(entry.getCode())
                .fingerprint(entry.getFingerprint()).content(entry.getContent()).format(entry.getFormat())
                .editor(entry.getEditor()).source(entry.getSource()).spec(entry.getSpec()).exposure(entry.getExposure())
                .encrypted(entry.getEncrypted()).payload(entry.getPayload()).labels(entry.getLabels())
                .extension(entry.getExtension()).checksum(entry.getChecksum()).edition(entry.getEdition())
                .generation(entry.getGeneration()).description(entry.getDescription()).status(entry.getStatus())
                .creator(entry.getCreator()).created(entry.getCreated()).modifier(entry.getModifier())
                .modified(entry.getModified()).build();
        if (protectedContent(entry.getEncrypted(), entry.getExposure())) {
            copy.setContent(mask(entry.getContent()));
            copy.setPayload(mask(entry.getPayload()));
            copy.setSpec(mask(entry.getSpec()));
            copy.setExtension(mask(entry.getExtension()));
        }
        return copy;
    }

    /**
     * Returns a masked copy for {@code setting.revision} management responses.
     *
     * @param revision source revision
     * @return masked copy
     */
    public Revision mask(Revision revision) {
        if (revision == null) {
            return null;
        }
        Revision copy = Revision.builder().id(revision.getId()).tenant_id(revision.getTenant_id())
                .item_id(revision.getItem_id()).rollout_id(revision.getRollout_id()).source_id(revision.getSource_id())
                .revision(revision.getRevision()).edition(revision.getEdition()).baseline(revision.getBaseline())
                .content(revision.getContent()).format(revision.getFormat()).source(revision.getSource())
                .spec(revision.getSpec()).exposure(revision.getExposure()).encrypted(revision.getEncrypted())
                .labels(revision.getLabels()).extension(revision.getExtension()).checksum(revision.getChecksum())
                .operation(revision.getOperation()).bindings(revision.getBindings()).reference(revision.getReference())
                .rule(revision.getRule()).editing(revision.getEditing()).secrets(revision.getSecrets())
                .description(revision.getDescription()).status(revision.getStatus()).creator(revision.getCreator())
                .created(revision.getCreated()).modifier(revision.getModifier()).modified(revision.getModified())
                .build();
        if (protectedContent(revision.getEncrypted(), revision.getExposure())) {
            copy.setContent(mask(revision.getContent()));
            copy.setSpec(mask(revision.getSpec()));
            copy.setExtension(mask(revision.getExtension()));
            copy.setSecrets(mask(revision.getSecrets()));
        }
        return copy;
    }

    /**
     * Tests whether content requires management-response masking.
     *
     * @param encrypted persisted encryption flag
     * @param exposure  persisted exposure policy
     * @return {@code true} when the content is encrypted or secret
     */
    private boolean protectedContent(Integer encrypted, String exposure) {
        return ItemNormalizer.isEncryptedFlagEnabled(encrypted) || ItemExposure.SECRET.name().equals(exposure);
    }

}
