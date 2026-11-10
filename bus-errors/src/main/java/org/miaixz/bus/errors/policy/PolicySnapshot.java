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
package org.miaixz.bus.errors.policy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.errors.ErrorType;

/**
 * Immutable route error rules for one listener, ordered by decreasing prefix length.
 *
 * @param rules   validated rules in precedence order
 * @param version process-local snapshot version
 *
 * @author Kimi Liu
 */
public record PolicySnapshot(List<Rule> rules, long version) {

    /**
     * Process-local sequence for newly compiled immutable policy snapshots.
     */
    private static final AtomicLong VERSIONS = new AtomicLong();

    /**
     * Detaches the compiled policy from the caller's list.
     *
     * @param rules   validated rules in precedence order
     * @param version process-local snapshot version
     */
    public PolicySnapshot {
        rules = List.copyOf(rules);
    }

    /**
     * Creates a distinct snapshot for a listener.
     *
     * @param rules validated rules in precedence order
     */
    public PolicySnapshot(List<Rule> rules) {
        this(rules, VERSIONS.incrementAndGet());
    }

    /**
     * Matches decoded path segments from the transport without re-splitting an encoded separator.
     *
     * @param path application-relative path segments
     * @param type verified semantic failure
     * @return the first matching rule or {@code null}
     */
    public Rule match(List<String> path, ErrorType type) {
        for (Rule rule : this.rules) {
            int size = rule.prefix().size();
            if (rule.types().contains(type) && path.size() >= size && path.subList(0, size).equals(rule.prefix())) {
                return rule;
            }
        }
        return null;
    }

    /**
     * Response contract selected by a matching rule.
     *
     * @author Kimi Liu
     */
    public enum Mode {
        /**
         * Preserve the endpoint's existing response representation.
         */
        LEGACY,
        /**
         * Use the verified HTTP status and headers with an empty body.
         */
        NATIVE_HTTP
    }

    /**
     * Compiled rule.
     *
     * @param prefix literal prefix segments, empty for the listener root
     * @param types  applicable failure types
     * @param mode   selected response contract
     * @author Kimi Liu
     */
    public record Rule(List<String> prefix, Set<ErrorType> types, Mode mode) {

        /**
         * Detaches the rule's collections.
         *
         * @param prefix literal prefix segments, empty for the listener root
         * @param types  applicable failure types
         * @param mode   selected response contract
         */
        public Rule {
            prefix = List.copyOf(prefix);
            types = Set.copyOf(types);
            java.util.Objects.requireNonNull(mode, "mode");
        }
    }

}
