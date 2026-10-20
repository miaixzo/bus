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
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.setting.secret.SecretReference;
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

    /** Source discriminator for reference-only backend Secret settings. */
    public static final String SECRET_SOURCE = "SECRET";

    /**
     * Recognizes a reference-only SECRET item without resolving its backend.
     *
     * @param item item to inspect, or null
     * @return true for a trimmed, case-insensitive SECRET source
     */
    public static boolean isSecret(Item item) {
        return item != null && SECRET_SOURCE.equalsIgnoreCase(item.getSource() == null ? "" : item.getSource().trim());
    }

    /**
     * Validates persisted setting restrictions and parses its backend reference. SECRET items cannot contain inline
     * plaintext, encrypted payloads or a non-secret exposure. This method performs no backend access.
     *
     * @param item reference-only SECRET item
     * @return validated backend reference retaining the existing JSON descriptor format
     * @throws org.miaixz.bus.core.lang.exception.CryptoException if item restrictions or descriptor validation fail
     */
    public static SecretReference secretReference(Item item) {
        if (!isSecret(item) || !"SECRET".equals(item.getExposure())
                || item.getEncrypted() != null && item.getEncrypted() != 0 || item.getContent() != null
                || item.getPayload() != null || item.getSpec() == null) {
            throw new CryptoException("Invalid secret reference");
        }
        return SecretReference.parse(item.getSpec());
    }

    /**
     * Prevents utility-class instantiation.
     */
    private ItemNormalizer() {
        // No initialization required.
    }

    /**
     * Applies canonical identifiers and default values to one item.
     *
     * @param entry item with valid tenant-qualified coordinates
     * @return normalized item
     */
    public static Item normalize(Item entry) {
        Item prepared = coordinates(entry);
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
        if (prepared.getState() != null) {
            Item.State.valueOf(prepared.getState());
        }
        if (prepared.getEncrypted() == null) {
            prepared.setEncrypted(0);
        }
        if (ItemNormalizer.isSecret(prepared)) {
            ItemNormalizer.secretReference(prepared);
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
        coordinates(entry);
        return Builder.sha256(
                String.join(
                        ":",
                        segment(entry.getTenant_id()),
                        segment(entry.getScope()),
                        segment(entry.getApp_id()),
                        segment(entry.getCode()),
                        segment(entry.getKind())));
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
     * Validates and normalizes item coordinates, requiring a tenant identifier.
     *
     * @param item item coordinates
     * @return normalized item coordinates
     */
    public static Item coordinates(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item coordinates are required");
        }
        requireTenant(item.getTenant_id());
        if (item.getCode() == null || item.getCode().isBlank()) {
            throw new IllegalArgumentException("A configuration code is required");
        }
        if (item.getScope() == null || item.getScope().isBlank()) {
            item.setScope(
                    item.getApp_id() == null || item.getApp_id().isBlank() ? Item.Scope.SHARED.name()
                            : Item.Scope.APPLICATION.name());
        }
        Item.Scope scope = Item.Scope.valueOf(item.getScope());
        if (scope == Item.Scope.APPLICATION && (item.getApp_id() == null || item.getApp_id().isBlank())) {
            throw new IllegalArgumentException("Application items require an application identifier");
        }
        if (scope == Item.Scope.SHARED && item.getApp_id() != null && !item.getApp_id().isBlank()) {
            throw new IllegalArgumentException("Shared items cannot belong to an application");
        }
        if (scope == Item.Scope.SHARED) {
            item.setApp_id(null);
        }
        if (item.getKind() == null || item.getKind().isBlank()) {
            item.setKind(Item.Kind.SOURCE.name());
        }
        Item.Kind.valueOf(item.getKind());
        return item;
    }

    /**
     * Validates the global tenant identifier.
     *
     * @param tenant_id tenant identifier
     */
    public static void requireTenant(String tenant_id) {
        if (tenant_id == null || tenant_id.isBlank()) {
            throw new IllegalArgumentException("A tenant identifier is required");
        }
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

    /**
     * Encodes an identity segment without delimiter ambiguity.
     */
    private static String segment(String value) {
        return value == null ? "-" : value.length() + "#" + value;
    }

}
