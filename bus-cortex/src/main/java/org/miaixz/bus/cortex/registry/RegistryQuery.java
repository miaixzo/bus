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
package org.miaixz.bus.cortex.registry;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.builtin.Selector;

import lombok.Getter;
import lombok.Setter;

/**
 * Internal registry query scope converted from the public {@code Vector} compatibility entry point.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class RegistryQuery {

    /**
     * Creates an empty registry query.
     */
    public RegistryQuery() {

    }

    /**
     * Registry namespace identifier.
     */
    private String namespace_id;
    /**
     * Registry asset type.
     */
    private Type type;
    /**
     * Registry asset identifier.
     */
    private String id;
    /**
     * Application identifier used for route lookups.
     */
    private String app_id;
    /**
     * Method or route name used for route lookups.
     */
    private String method;
    /**
     * Route version used for route lookups.
     */
    private String version;
    /**
     * Required asset labels.
     */
    private Map<String, String> labels;
    /**
     * Selector expressions applied to asset labels or metadata.
     */
    private List<Selector> selectors;
    /**
     * Desired runtime state.
     */
    private String state;
    /**
     * Whether disabled entries should be included.
     */
    private boolean includeDisabled;
    /**
     * Result offset for pagination.
     */
    private int offset;
    /**
     * Maximum result size for pagination.
     */
    private int limit = 100;

}
