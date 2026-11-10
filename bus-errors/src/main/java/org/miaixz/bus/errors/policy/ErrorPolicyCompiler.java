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

import java.util.*;
import java.util.regex.Pattern;

import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.policy.PolicySnapshot.Mode;
import org.miaixz.bus.errors.policy.PolicySnapshot.Rule;

/**
 * Validates and compiles literal listener-relative error rules.
 *
 * @author Kimi Liu
 */
public final class ErrorPolicyCompiler {

    /**
     * Validates slash-terminated literal prefixes for the legacy policy.
     */
    private static final Pattern LITERAL_PREFIX = Pattern.compile("/(?:[\\p{L}\\p{M}\\p{N}._~!$&'()+,=:@-]+/)+");

    /**
     * Semantic failure types accepted by default configuration and public projections.
     */
    private static final Set<ErrorType> STANDARD_TYPES = Set.of(
            ErrorType.ROUTE_NOT_FOUND,
            ErrorType.METHOD_NOT_ALLOWED,
            ErrorType.UNSUPPORTED_MEDIA_TYPE,
            ErrorType.NOT_ACCEPTABLE,
            ErrorType.PAYLOAD_TOO_LARGE,
            ErrorType.RATE_LIMITED,
            ErrorType.GATEWAY_OVERLOADED,
            ErrorType.DEPENDENCY_UNAVAILABLE,
            ErrorType.INVALID_REQUEST,
            ErrorType.UNAUTHENTICATED,
            ErrorType.FORBIDDEN,
            ErrorType.TIMEOUT,
            ErrorType.CANCELLED,
            ErrorType.IO_FAILURE,
            ErrorType.INVALID_DATA,
            ErrorType.CONFIGURATION_FAILURE,
            ErrorType.SECURITY_FAILURE,
            ErrorType.CALLBACK_FAILURE,
            ErrorType.EXECUTION_FAILURE,
            ErrorType.OUTCOME_UNKNOWN,
            ErrorType.UNKNOWN);

    /**
     * Prevents construction of the stateless policy compiler.
     */
    private ErrorPolicyCompiler() {
    }

    /**
     * Compiles rules for one listener. Duplicate path/type pairs are rejected regardless of declaration order.
     *
     * @param rules configuration declarations
     * @return immutable snapshot with longest-prefix precedence
     */
    public static PolicySnapshot compile(List<RuleSpec> rules) {
        return compile(rules, Set.of());
    }

    /**
     * Compiles listener rules and accepts explicitly registered component error types.
     *
     * @param rules           configuration declarations
     * @param registeredTypes additional registered semantic types
     * @return immutable snapshot with longest-prefix precedence
     */
    public static PolicySnapshot compile(List<RuleSpec> rules, Set<ErrorType> registeredTypes) {
        List<Rule> compiled = new ArrayList<>();
        Set<RuleKey> seen = new HashSet<>();
        for (RuleSpec spec : rules) {
            String prefix = spec.pathPrefix();
            if (!"/".equals(prefix) && !isLiteralPrefix(prefix)) {
                throw new IllegalArgumentException("Invalid route-errors rule path-prefix: " + prefix);
            }
            if (spec.types().isEmpty()) {
                throw new IllegalArgumentException("A route-errors rule requires at least one error type");
            }
            for (ErrorType type : spec.types()) {
                if (!isStandardType(type) && !registeredTypes.contains(type)) {
                    throw new IllegalArgumentException("Unknown route-errors type: " + type.id());
                }
                if (!seen.add(new RuleKey(prefix, type))) {
                    throw new IllegalArgumentException("Duplicate route-errors rule: " + prefix + " " + type.id());
                }
            }
            List<String> segments = "/".equals(prefix) ? List.of() : List.of(prefix.substring(1).split("/"));
            compiled.add(new Rule(segments, spec.types(), spec.mode()));
        }
        compiled.sort(Comparator.comparingInt((Rule rule) -> rule.prefix().size()).reversed());
        return new PolicySnapshot(compiled);
    }

    /**
     * Checks whether a type has a standard Bus definition.
     *
     * @param type semantic failure type
     * @return whether the type is standard
     */
    public static boolean isStandardType(ErrorType type) {
        return STANDARD_TYPES.contains(type);
    }

    /**
     * Validates the legacy syntax for a literal, slash-terminated, non-root prefix.
     *
     * @param prefix configured path prefix
     * @return whether the prefix can be compiled
     */
    public static boolean isLiteralPrefix(String prefix) {
        return prefix != null && LITERAL_PREFIX.matcher(prefix).matches() && !prefix.contains("/./")
                && !prefix.contains("/../");
    }

    /**
     * One path/type rule before compilation.
     *
     * @param pathPrefix listener-relative literal prefix, or {@code /} for the listener root
     * @param types      semantic failures to select
     * @param mode       response contract
     * @author Kimi Liu
     */
    public record RuleSpec(String pathPrefix, Set<ErrorType> types, Mode mode) {

        /**
         * Validates required values and detaches the type set.
         *
         * @param pathPrefix listener-relative literal prefix, or {@code /} for the listener root
         * @param types      semantic failures to select
         * @param mode       response contract
         */
        public RuleSpec {
            Objects.requireNonNull(pathPrefix, "pathPrefix");
            types = Set.copyOf(types);
            Objects.requireNonNull(mode, "mode");
        }
    }

    /**
     * Identifies one semantic rule binding for duplicate detection.
     *
     * @param prefix literal route prefix
     * @param type   semantic error type
     * @author Kimi Liu
     */
    private record RuleKey(String prefix, ErrorType type) {
    }

}
