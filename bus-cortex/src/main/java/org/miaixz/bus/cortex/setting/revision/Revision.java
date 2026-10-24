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
package org.miaixz.bus.cortex.setting.revision;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.crypto.Builder;

/**
 * Persisted configuration snapshot whose stored content is append-only.
 * The entity remains mutable for serialization and construction; publication is represented by item pointers and a frozen rollout.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Revision extends Tenant {

    /**
     * Snapshot vocabulary version; null denotes an immutable legacy snapshot.
     */
    private Integer protocol;
    /**
     * Frozen configuration category.
     */
    private Integer type;
    /**
     * Frozen external source descriptor.
     */
    @Lob
    private String descriptor;
    /**
     * Frozen whole-content JSON Schema.
     */
    @Lob
    private String schema;

    /**
     * Owning item identifier.
     */
    private String item_id;
    /**
     * Rollout that created the revision.
     */
    private String rollout_id;
    /**
     * Source revision used for derivation or rollback.
     */
    private String source_id;
    /**
     * Monotonic item revision number.
     */
    private String revision;
    /**
     * Item edition captured by the snapshot.
     */
    private Long edition;
    /**
     * Item generation captured by the snapshot.
     */
    private Long generation;
    /**
     * Immutable content.
     */
    @Lob
    private String content;
    /**
     * Immutable content format.
     */
    private String format;
    /**
     * Legacy editing-mode text retained for historical snapshots and checksums.
     */
    private String editor;
    /**
     * Immutable structured editor payload.
     */
    @Lob
    private String payload;
    /**
     * Immutable content source.
     */
    private String source;
    /**
     * Immutable source descriptor.
     */
    @Lob
    private String spec;
    /**
     * Immutable exposure policy.
     */
    private String exposure;
    /**
     * Immutable encryption flag.
     */
    private Integer encrypted;
    /**
     * Immutable labels text.
     */
    @Lob
    private String labels;
    /**
     * Immutable extension text.
     */
    @Lob
    private String extension;
    /**
     * Snapshot checksum.
     */
    private String checksum;
    /**
     * Snapshot operation.
     */
    private String operation;
    /**
     * Frozen reference manifest with exact revision identifiers, never resolved from live relationships on rollback.
     */
    @Lob
    private String reference;
    /**
     * Protected-value snapshot stored as text.
     */
    @Lob
    private String secrets;
    /**
     * Optional snapshot description.
     */
    private String description;

    /**
     * Creates an empty snapshot entity for construction or deserialization; persistence must enforce append-only storage.
     */
    public Revision() {
        // No initialization required.
    }

    /**
     * Checks whether a snapshot uses protocol {@code 2}; a null protocol denotes the legacy layout.
     *
     * @param revision non-null snapshot to inspect
     * @return {@code true} for protocol 2, otherwise {@code false} for a legacy snapshot
     * @throws IllegalArgumentException if a non-null protocol is unsupported
     */
    public static boolean current(Revision revision) {
        if (revision.getProtocol() != null && !Integer.valueOf(2).equals(revision.getProtocol())) {
            throw new IllegalArgumentException("Unsupported configuration snapshot protocol");
        }
        return Integer.valueOf(2).equals(revision.getProtocol());
    }

    /**
     * Resolves the configuration category using the snapshot protocol. Legacy SECRET source or exposure maps to the protected category.
     *
     * @param revision non-null snapshot to inspect
     * @return stable configuration category code
     * @throws IllegalArgumentException if the protocol or current category is unsupported
     */
    public static Integer category(Revision revision) {
        return current(revision) ? Item.Type.of(revision.getType()).key()
                : "SECRET".equals(revision.getExposure()) || "SECRET".equals(revision.getSource())
                        ? Item.Type.SECRET.key()
                        : Item.Type.CONFIG_MAP.key();
    }

    /**
     * Returns the source descriptor from the current or legacy snapshot layout.
     *
     * @param revision non-null snapshot to inspect
     * @return current descriptor, or legacy spec for a non-INLINE source; otherwise {@code null}
     * @throws IllegalArgumentException if the snapshot protocol is unsupported
     */
    public static String descriptor(Revision revision) {
        return current(revision) ? revision.getDescriptor()
                : "INLINE".equals(revision.getSource()) || revision.getSource() == null ? null : revision.getSpec();
    }

    /**
     * Returns the schema from the current or legacy snapshot layout.
     *
     * @param revision non-null snapshot to inspect
     * @return current schema, or legacy spec for an INLINE or unspecified source; otherwise {@code null}
     * @throws IllegalArgumentException if the snapshot protocol is unsupported
     */
    public static String schema(Revision revision) {
        return current(revision) ? revision.getSchema()
                : "INLINE".equals(revision.getSource()) || revision.getSource() == null ? revision.getSpec() : null;
    }

    /**
     * Reconstructs item content and metadata from a snapshot, resolving legacy category and spec semantics.
     *
     * @param revision non-null immutable storage snapshot
     * @return a new mutable item containing the snapshot content
     * @throws IllegalArgumentException if the snapshot protocol or current category is unsupported
     */
    public static Item content(Revision revision) {
        return Item.builder().tenant_id(revision.getTenant_id()).id(revision.getItem_id()).type(category(revision))
                .content(revision.getContent()).format(revision.getFormat()).source(revision.getSource())
                .descriptor(descriptor(revision)).schema(schema(revision)).encrypted(revision.getEncrypted())
                .labels(revision.getLabels()).extension(revision.getExtension()).checksum(revision.getChecksum())
                .edition(revision.getEdition()).generation(revision.getGeneration())
                .description(revision.getDescription()).build();
    }

    /**
     * Computes the protocol-specific content checksum. Current snapshots use the item normalizer; legacy snapshots retain their historical field order and separator.
     *
     * @param revision non-null snapshot to checksum
     * @return SHA-256 checksum of the protocol-specific semantic content
     * @throws IllegalArgumentException if the snapshot protocol or current category is unsupported
     */
    public static String checksum(Revision revision) {
        if (current(revision))
            return ItemNormalizer.checksum(content(revision));
        return Builder.sha256(
                String.join(
                        "\u001f",
                        text(revision.getContent()),
                        text(revision.getPayload()),
                        text(revision.getFormat()),
                        text(revision.getEditor()),
                        text(revision.getSource()),
                        text(revision.getSpec()),
                        text(revision.getExposure()),
                        text(revision.getEncrypted()),
                        text(revision.getLabels()),
                        text(revision.getExtension())));
    }

    /**
     * Converts a legacy checksum component to text without introducing the literal string null.
     *
     * @param value legacy component, possibly null
     * @return empty string for null, otherwise the string representation
     */
    private static String text(Object value) {
        return value == null ? Normal.EMPTY : String.valueOf(value);
    }

    /**
     * Supported immutable snapshot operations.
     *
     * @author Kimi Liu
     */
    public enum Operation {
        /**
         * Creates or replaces the current item state.
         */
        UPSERT,
        /**
         * Archives the current item state.
         */
        DELETE
    }

}
