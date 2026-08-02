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
package org.miaixz.bus.spring.jdbc;

import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Contains the complete datasource-definition mapping and its primary routing name.
 *
 * @param primary primary routing name
 * @param sources immutable definitions keyed by routing name
 * @author Kimi Liu
 * @since Java 21+
 */
public record DataSourceMapping(String primary, Map<String, DataSourceDefinition> sources) {

    /**
     * Validates the primary route and defensively copies all definitions.
     */
    public DataSourceMapping {
        primary = StringKit.trim(primary);
        sources = sources == null ? Map.of() : Map.copyOf(sources);
        if (StringKit.isEmpty(primary) || !sources.containsKey(primary)) {
            throw new IllegalArgumentException("Primary datasource must identify a configured source");
        }
    }

}
