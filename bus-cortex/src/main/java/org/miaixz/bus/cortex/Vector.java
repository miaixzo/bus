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

import java.util.Collections;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Vector selector used by registry and configuration scans.
 * <p>
 * The selector is intentionally lightweight and can be reused across in-memory, Redis and JDBC-backed {@code CacheX}
 * implementations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Vector extends Nature {

    /**
     * API method name or logical method key.
     */
    private String method;

    /**
     * Version selector.
     */
    private String version;

    /**
     * Label selector map.
     */
    private Map<String, String> labels;

    /**
     * Instance state selector.
     */
    private String state;

    /**
     * Maximum number of results to return.
     */
    private int limit = 100;

    /**
     * Number of results to skip.
     */
    private int offset = 0;

    /**
     * Creates a new vector builder.
     *
     * @return a new builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Returns immutable label criteria.
     *
     * @return immutable labels map
     */
    public Map<String, String> labelView() {
        return labels == null ? Collections.emptyMap() : Collections.unmodifiableMap(labels);
    }

    /**
     * Fluent builder for {@link Vector}.
     */
    public static final class Builder {

        /**
         * Backing vector instance.
         */
        private final Vector vector = new Vector();

        /**
         * Sets namespace.
         *
         * @param namespace query namespace
         * @return current builder
         */
        public Builder namespace(String namespace) {
            vector.setNamespace(namespace);
            return this;
        }

        /**
         * Sets species.
         *
         * @param species query species
         * @return current builder
         */
        public Builder species(Species species) {
            vector.setSpecies(species);
            return this;
        }

        /**
         * Sets identifier.
         *
         * @param id vector identifier
         * @return current builder
         */
        public Builder id(String id) {
            vector.setId(id);
            return this;
        }

        /**
         * Sets method.
         *
         * @param method vector method
         * @return current builder
         */
        public Builder method(String method) {
            vector.setMethod(method);
            return this;
        }

        /**
         * Sets version.
         *
         * @param version vector version
         * @return current builder
         */
        public Builder version(String version) {
            vector.setVersion(version);
            return this;
        }

        /**
         * Sets labels.
         *
         * @param labels vector labels
         * @return current builder
         */
        public Builder labels(Map<String, String> labels) {
            vector.setLabels(labels);
            return this;
        }

        /**
         * Sets state vector.
         *
         * @param state instance state
         * @return current builder
         */
        public Builder state(String state) {
            vector.setState(state);
            return this;
        }

        /**
         * Sets result limit.
         *
         * @param limit max results
         * @return current builder
         */
        public Builder limit(int limit) {
            vector.setLimit(limit);
            return this;
        }

        /**
         * Sets result offset.
         *
         * @param offset skip count
         * @return current builder
         */
        public Builder offset(int offset) {
            vector.setOffset(offset);
            return this;
        }

        /**
         * Builds the vector.
         *
         * @return configured vector instance
         */
        public Vector build() {
            return vector;
        }

    }

}
