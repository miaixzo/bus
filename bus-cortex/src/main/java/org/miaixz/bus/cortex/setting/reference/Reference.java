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
package org.miaixz.bus.cortex.setting.reference;

import java.io.Serial;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Current configuration-to-application ownership or consumption, including application-local usage options.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Reference extends Tenant {

    @Serial
    private static final long serialVersionUID = 2538003660000L;

    /**
     * Identifier of the source configuration item.
     */
    private String source_id;

    /**
     * Identifier of the target application.
     */
    private String target_id;

    /**
     * Optional exact published revision pin; null selects the current definition when the next rollout is frozen.
     */
    private String revision_id;

    /**
     * Relationship category stored as {@link Type#ITEM_APP} or {@link Type#ITEM_OWNER}.
     */
    private String type;

    /**
     * Reference alias within the target application.
     */
    private String alias;

    /**
     * Optimistic concurrency counter for the relationship.
     */
    private Long edition;

    /**
     * Optional literal root property under which the effective configuration is wrapped.
     */
    private String prefix;

    /**
     * Application override policy: {@code NONE}, {@code KEYS} or {@code ALL}.
     */
    private String override;

    /**
     * JSON array of strict JSON Pointers that the application may override.
     */
    @Lob
    private String keys;

    /**
     * Protected override object for this relationship; sensitive values must not be stored as plaintext.
     */
    @Lob
    private String overlay;

    /**
     * Creates an empty configuration-to-application relationship.
     */
    public Reference() {
        // No initialization required.
    }

    /**
     * Derives the selection strategy from the presence of an exact published revision pin.
     *
     * @return strategy for selecting a fixed revision or the current definition
     */
    public Strategy followPolicy() {
        return revision_id == null ? Strategy.LATEST : Strategy.FIXED;
    }

    /**
     * Revision selection strategies derived from the relationship rather than persisted separately.
     *
     * @author Kimi Liu
     */
    public enum Strategy {
        /**
         * Selects the current definition when the next rollout is frozen.
         */
        LATEST,
        /**
         * Selects the exact published revision when the next rollout is frozen.
         */
        FIXED

    }

    /**
     * Current ownership and consumption relationship categories.
     *
     * @author Kimi Liu
     */
    public enum Type {
        /**
         * Application consumption of a shared configuration item.
         */
        ITEM_APP,
        /**
         * Exclusive application ownership of a configuration item.
         */
        ITEM_OWNER

    }

}
