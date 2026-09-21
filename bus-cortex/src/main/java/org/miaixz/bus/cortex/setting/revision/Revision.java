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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.setting.item.Item;

/**
 * Historical current-state item snapshot.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Revision extends Item {

    /**
     * Immutable operation represented by a revision.
     */
    public enum Operation {
        /**
         * The revision creates or replaces the active item content.
         */
        UPSERT,
        /**
         * The revision archives the item through a deletion tombstone.
         */
        DELETE
    }

    /**
     * Runtime pointer role held by an immutable revision.
     */
    public enum Role {
        /**
         * The revision is the stable production revision.
         */
        STABLE,
        /**
         * The revision is the active gray-release revision.
         */
        GRAY
    }

    /**
     * Governed workflow that created an immutable revision.
     */
    public enum Source {
        /**
         * The revision was created by a formal publication.
         */
        FORMAL,
        /**
         * The revision was created for a gray release.
         */
        GRAY,
        /**
         * The revision restores content from an earlier immutable revision.
         */
        ROLLBACK,
        /**
         * The revision upgrades a governed reference to newer source content.
         */
        REFERENCE_UPGRADE,
        /**
         * The revision represents an approved archive operation.
         */
        ARCHIVE
    }

    /**
     * Current item entry identifier.
     */
    private String item_id;

    /**
     * Diff summary against the previous revision when available.
     */
    private String diff;

    /**
     * Parent revision that this revision rolled back from.
     */
    private String revert;

    /**
     * Creates an empty {@code setting.revision} snapshot.
     */
    public Revision() {
        super();
        setType(Type.ITEM_REVISION.key());
    }

}
