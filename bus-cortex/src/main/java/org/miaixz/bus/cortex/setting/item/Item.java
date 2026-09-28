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

import jakarta.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Space;

/**
 * Current state of one setting item.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Item extends Space {

    /**
     * Profile that owns the item.
     */
    private String profile_id;
    /**
     * Current stable revision identifier.
     */
    private String stable_id;
    /**
     * Current gray revision identifier.
     */
    private String gray_id;
    /**
     * Active gray rollout identifier.
     */
    private String rollout_id;
    /**
     * Persisted item kind.
     */
    private String kind;
    /**
     * Logical setting group.
     */
    @Column(name = "\"group\"")
    private String group;
    /**
     * Stable setting code.
     */
    private String code;
    /**
     * Digest of immutable item coordinates.
     */
    private String fingerprint;
    /**
     * Editable source content.
     */
    private String content;
    /**
     * Content format.
     */
    private String format;
    /**
     * Editing mode.
     */
    private String editor;
    /**
     * Content source type.
     */
    private String source;
    /**
     * External source descriptor.
     */
    private String spec;
    /**
     * Delivery exposure policy.
     */
    private String exposure;
    /**
     * Encrypted-content flag.
     */
    private Integer encrypted;
    /**
     * Structured editor payload stored as text.
     */
    private String payload;
    /**
     * Labels stored as text.
     */
    private String labels;
    /**
     * Source extension stored as text.
     */
    private String extension;
    /**
     * Semantic content checksum.
     */
    private String checksum;
    /**
     * Optimistic editing edition.
     */
    private Long edition;
    /**
     * Runtime delivery generation.
     */
    private Long generation;
    /**
     * Optional item description.
     */
    private String description;

    /**
     * Creates an empty setting item.
     */
    public Item() {
        // No initialization required.
    }

    /**
     * Supported item purposes.
     *
     * @author Kimi Liu
     */
    public enum Kind {
        /** User-authored source configuration. */
        SOURCE,
        /** Materialized effective configuration. */
        EFFECTIVE
    }

    /**
     * Supported content editing modes.
     *
     * @author Kimi Liu
     */
    public enum Editor {
        /** Source-text editing mode. */
        SOURCE,
        /** Structured key-value editing mode. */
        KEY_VALUE
    }

    /**
     * User-facing item states.
     *
     * @author Kimi Liu
     */
    public enum State {
        /** Item has never been published. */
        UNPUBLISHED,
        /** Item matches its stable published revision. */
        PUBLISHED,
        /** Item contains unpublished changes. */
        CHANGED,
        /** Item has an active gray revision. */
        GRAY,
        /** Item is archived. */
        ARCHIVED
    }

    /**
     * Supported protected-value mutations.
     *
     * @author Kimi Liu
     */
    public enum SecretAction {
        /** Keeps the existing protected value. */
        KEEP,
        /** Replaces the existing protected value. */
        REPLACE,
        /** Deletes the existing protected value. */
        DELETE
    }

    /**
     * Supported validation depths.
     *
     * @author Kimi Liu
     */
    public enum ValidationLevel {
        /** Performs syntax validation only. */
        SYNTAX,
        /** Performs syntax and business validation. */
        FULL
    }

}
