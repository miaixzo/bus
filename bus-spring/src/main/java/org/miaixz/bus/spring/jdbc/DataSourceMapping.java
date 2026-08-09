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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Contains the complete datasource-definition mapping and its primary routing name.
 *
 * @author Kimi Liu
 */
public class DataSourceMapping {

    /**
     * Primary routing name.
     */
    private final String primary;
    /**
     * Immutable datasource definitions keyed by routing name.
     */
    private final Map<String, DataSourceDefinition> sources;

    /**
     * Creates a validated datasource mapping.
     *
     * @param primary primary routing name
     * @param sources definitions keyed by routing name
     */
    public DataSourceMapping(String primary, Map<String, DataSourceDefinition> sources) {
        this.primary = StringKit.trim(primary);
        this.sources = sources == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(sources));
        if (StringKit.isEmpty(this.primary) || !this.sources.containsKey(this.primary)) {
            throw new IllegalArgumentException("Primary datasource must identify a configured source");
        }
    }

    /**
     * Returns the primary datasource routing name.
     *
     * @return primary routing name
     */
    public String getPrimary() {
        return this.primary;
    }

    /**
     * Returns immutable datasource definitions keyed by routing name.
     *
     * @return immutable datasource definitions
     */
    public Map<String, DataSourceDefinition> getSources() {
        return this.sources;
    }

    /**
     * Compares the primary route and datasource definitions.
     *
     * @param object candidate object
     * @return {@code true} when both mapping values match
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DataSourceMapping that)) {
            return false;
        }
        return Objects.equals(this.primary, that.primary) && Objects.equals(this.sources, that.sources);
    }

    /**
     * Calculates a hash code from the primary route and datasource definitions.
     *
     * @return hash code for the mapping values
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.primary, this.sources);
    }

    /**
     * Returns diagnostic mapping text whose nested datasource definitions redact credentials.
     *
     * @return diagnostic mapping text using redacted datasource definitions
     */
    @Override
    public String toString() {
        return "DataSourceMapping[primary=" + this.primary + ", sources=" + this.sources + "]";
    }

}
