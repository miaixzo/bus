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
package org.miaixz.bus.cortex.setting.binding;

import org.miaixz.bus.core.basic.entity.Namespace;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Relation row between one setting resource and an application or profile target.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Binding extends Namespace {

    /**
     * Creates an empty setting binding.
     */
    public Binding() {

    }

    /**
     * Owning setting resource identifier. The physical column remains {@code item_id} for the existing item binding
     * table, while service-level code may treat it as the owner id.
     */
    private String item_id;
    /**
     * Binding kind. Allowed values are {@code APP} and {@code PROFILE}.
     */
    private String type;
    /**
     * Referenced target identifier.
     */
    private String ref_id;

    /**
     * Semantic alias for callers that bind non-item setting resources.
     *
     * @return owning setting resource identifier
     */
    public String getOwner_id() {
        return item_id;
    }

    /**
     * Semantic alias for callers that bind non-item setting resources.
     *
     * @param owner_id owning setting resource identifier
     */
    public void setOwner_id(String owner_id) {
        this.item_id = owner_id;
    }

}
