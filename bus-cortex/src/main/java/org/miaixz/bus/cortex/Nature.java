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
package org.miaixz.bus.cortex;

import java.io.Serial;

import org.miaixz.bus.core.basic.entity.Namespace;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Minimal common base for all Cortex domain objects.
 * <p>
 * Extends {@link Namespace} for the shared identifier, request context and namespace scope, then adds the Cortex-level
 * {@code type}. The type stores the stable numeric key of {@link Type} for persistence and business routing.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Nature extends Namespace {

    /**
     * Serialization identifier for the shared runtime base model.
     */
    @Serial
    private static final long serialVersionUID = 2852290719700L;

    /**
     * Stable numeric asset type key used for persistence and business routing.
     */
    private Integer type;

    /**
     * Creates an empty shared Cortex domain base.
     */
    protected Nature() {

    }

    /**
     * Returns whether the current entry belongs to the supplied type.
     *
     * @param type candidate type
     * @return {@code true} when the type matches
     */
    public boolean isType(Type type) {
        return type != null && getType() != null && getType().intValue() == type.key();
    }

}
