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

import java.util.List;
import java.util.Map;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.cortex.builtin.Label;
import org.miaixz.bus.cortex.builtin.LabelMapper;
import org.miaixz.bus.cortex.builtin.Selector;

/**
 * Filters items by tenant, ownership scope, and application.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class ItemScope extends Tenant {

    /** Ownership scope; null permits both shared and application items. */
    private String scope;

    /** Item kind; defaults to SOURCE when unspecified. */
    private String kind;

    /**
     * Optional application identifier used to match application-bound settings.
     */
    private String app_id;

    /**
     * Optional label selector.
     */
    private Map<String, String> labels;

    /**
     * Optional advanced metadata selectors.
     */
    private List<Selector> selectors;

    /**
     * Optional request identifier propagated across control-plane operations.
     */
    private String requestId;

    /**
     * Whether deleted or tombstoned entries should be included.
     */
    private boolean includeDeleted;

    /**
     * Maximum number of entries to return.
     */
    @Default
    private int limit = 100;

    /**
     * Number of matching entries to skip.
     */
    private int offset;

    /**
     * Creates an empty setting scope.
     */
    public ItemScope() {
        this.limit = 100;
    }

    /**
     * Returns management-facing label items converted from runtime labels.
     *
     * @return label items
     */
    public List<Label> labelItems() {
        return LabelMapper.toLabels(labels);
    }

    /**
     * Sets runtime labels from management-facing label items.
     *
     * @param labelItems label items
     */
    public void labelItems(List<Label> labelItems) {
        this.labels = LabelMapper.toMap(labelItems);
    }

}
