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
import java.util.List;
import java.util.Map;

import lombok.experimental.SuperBuilder;
import org.miaixz.bus.cortex.builtin.Label;
import org.miaixz.bus.cortex.builtin.LabelMapper;
import org.miaixz.bus.cortex.builtin.Selector;

import lombok.Getter;
import lombok.Setter;

/**
 * Shared cross-domain selector used by registry and setting scans.
 *
 * <p>
 * {@code Vector} is intentionally limited to common filtering concerns such as namespace, type, id, method/version,
 * labels/selectors, instance state, and pagination. Setting-only, admin-only, delivery-only, or debug semantics should
 * live in domain-specific scope types instead of expanding this model further.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Vector extends Nature {

    /**
     * Creates an empty shared query vector.
     */
    public Vector() {

    }

    /**
     * Application identifier bound to the queried asset or setting scope.
     */
    private String app_id;

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
     * Advanced metadata selectors used together with exact labels.
     */
    private List<Selector> selectors;

    /**
     * Instance state selector.
     */
    private String state;
    /**
     * Optional logical purpose such as query, watch, rebuild or export.
     */
    private String purpose;
    /**
     * Whether the caller expects watch semantics instead of one-off query semantics.
     */
    private boolean watch;
    /**
     * Whether the caller expects a durable refresh or rebuild instead of cache-only lookup.
     */
    private boolean refresh;
    /**
     * Optional request identifier propagated across control-plane operations.
     */
    private String requestId;
    /**
     * Whether disabled or tombstoned records should be included.
     */
    private boolean includeDisabled;

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
     * Returns immutable advanced selector criteria.
     *
     * @return immutable selector list
     */
    public List<Selector> selectorView() {
        return selectors == null ? List.of() : List.copyOf(selectors);
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

    /**
     * Fluent builder for {@link Vector}.
     */
    public static final class Builder {

        /**
         * Backing vector instance.
         */
        private final Vector vector = new Vector();

        /**
         * Creates a new fluent vector builder.
         */
        public Builder() {
        }

        /**
         * Sets namespace.
         *
         * @param namespace_id query namespace
         * @return current builder
         */
        public Builder namespace_id(String namespace_id) {
            vector.setNamespace_id(namespace_id);
            return this;
        }

        /**
         * Sets type.
         *
         * @param type query type key
         * @return current builder
         */
        public Builder type(Integer type) {
            vector.setType(type);
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
         * Sets application identifier.
         *
         * @param app_id query application identifier
         * @return current builder
         */
        public Builder app_id(String app_id) {
            vector.setApp_id(app_id);
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
         * Sets advanced metadata selectors.
         *
         * @param selectors selector expressions
         * @return current builder
         */
        public Builder selectors(List<Selector> selectors) {
            vector.setSelectors(selectors);
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
         * Sets vector purpose.
         *
         * @param purpose logical purpose
         * @return current builder
         */
        public Builder purpose(String purpose) {
            vector.setPurpose(purpose);
            return this;
        }

        /**
         * Sets watch mode.
         *
         * @param watch watch mode flag
         * @return current builder
         */
        public Builder watch(boolean watch) {
            vector.setWatch(watch);
            return this;
        }

        /**
         * Sets refresh mode.
         *
         * @param refresh refresh mode flag
         * @return current builder
         */
        public Builder refresh(boolean refresh) {
            vector.setRefresh(refresh);
            return this;
        }

        /**
         * Sets request identifier.
         *
         * @param requestId request identifier
         * @return current builder
         */
        public Builder requestId(String requestId) {
            vector.setRequestId(requestId);
            return this;
        }

        /**
         * Sets whether disabled entries should be included.
         *
         * @param includeDisabled include-disabled flag
         * @return current builder
         */
        public Builder includeDisabled(boolean includeDisabled) {
            vector.setIncludeDisabled(includeDisabled);
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
