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
package org.miaixz.bus.starter.wrapper.routing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.policy.ErrorPolicyCompiler;
import org.miaixz.bus.errors.policy.PolicySnapshot;
import org.miaixz.bus.spring.web.routing.RouteErrorPolicy;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Binds the common route error settings for the Servlet MVC and Vortex listeners.
 * <p>
 * Each listener selects its policy at startup so AOT can keep the same bean definitions across deployments.
 *
 * @param pathPrefixes common literal prefixes with leading and trailing slashes, relative to each listener
 * @param statuses     client error statuses to preserve, defaulting to {@code 404}, {@code 405}, and {@code 415}
 * @param cacheControl optional Cache-Control override for handled responses, blank by default
 * @param enabled      whether route error handling is enabled, defaulting to {@code false}
 * @param targets      listeners eligible for route error handling, defaulting to MVC
 * @param mvc          optional overrides for the MVC listener
 * @param vortex       optional overrides for the Vortex listener
 * @author Kimi Liu
 */
@ConfigurationProperties(prefix = GeniusBuilder.WRAPPER_ROUTE_ERRORS)
public record RouteErrorProperties(@DefaultValue List<String> pathPrefixes, @DefaultValue({
        "404", "405", "415" }) Set<Integer> statuses, @DefaultValue("") String cacheControl,
        @DefaultValue("false") boolean enabled, @DefaultValue("mvc") Set<Target> targets, @Nullable ListenerPaths mvc,
        @Nullable ListenerPaths vortex) {

    /**
     * Creates the bound properties for both listeners.
     *
     * @param pathPrefixes common literal prefixes with leading and trailing slashes, relative to each listener
     * @param statuses     client error statuses to preserve, defaulting to {@code 404}, {@code 405}, and {@code 415}
     * @param cacheControl optional Cache-Control override for handled responses, blank by default
     * @param enabled      whether route error handling is enabled, defaulting to {@code false}
     * @param targets      listeners eligible for route error handling, defaulting to MVC
     * @param mvc          optional overrides for the MVC listener
     * @param vortex       optional overrides for the Vortex listener
     */
    @ConstructorBinding
    public RouteErrorProperties {
        // Each active listener validates its effective path list when selecting its policy.
    }

    /**
     * Creates an enabled MVC policy using the original programmatic configuration contract.
     *
     * @param pathPrefixes literal prefixes relative to the MVC servlet mapping
     * @param statuses     HTTP client error statuses to preserve
     * @param cacheControl optional Cache-Control override for matching errors
     */
    public RouteErrorProperties(List<String> pathPrefixes, Set<Integer> statuses, String cacheControl) {
        this(pathPrefixes, statuses, cacheControl, true, Set.of(Target.MVC), null, null);
    }

    /**
     * Selects and validates the effective policy for a listener. An explicitly configured listener path list replaces
     * the common list.
     *
     * @param target listener requesting its startup policy
     * @return compiled policy for the selected listener, or empty when the listener delegates all errors
     * @throws IllegalArgumentException if an enabled configuration has no targets or the selected rules are invalid
     */
    public Optional<RouteErrorPolicy> policy(Target target) {
        if (!this.enabled) {
            return Optional.empty();
        }
        if (this.targets.isEmpty()) {
            throw new IllegalArgumentException("route-errors.targets requires at least one listener");
        }
        if (!this.targets.contains(target)) {
            return Optional.empty();
        }
        ListenerPaths overrides = target == Target.MVC ? this.mvc : this.vortex;
        List<String> prefixes = overrides == null || overrides.pathPrefixes() == null ? this.pathPrefixes
                : overrides.pathPrefixes();
        var rules = overrides == null ? List.<ErrorPolicyCompiler.RuleSpec>of()
                : overrides.rules().stream().map(RouteRule::compile).toList();
        return Optional.of(
                new RouteErrorPolicy(prefixes, this.statuses, this.cacheControl, ErrorPolicyCompiler.compile(rules)));
    }

    /**
     * HTTP listener that can apply the shared route error policy.
 *
 * @author Kimi Liu
     */
    public enum Target {

        /**
         * Servlet MVC listener.
         */
        MVC,

        /**
         * Vortex gateway listener.
         */
        VORTEX

    }

    /**
     * Optional path overrides for one listener.
     *
     * @param pathPrefixes listener-relative literal prefixes, or {@code null} to inherit the common list
     * @param rules        optional semantic path rules for this listener
 * @author Kimi Liu
     */
    public record ListenerPaths(@Nullable List<String> pathPrefixes, @DefaultValue List<RouteRule> rules) {

        /**
         * Detaches the typed rules from the binder's list.
         *
         * @param pathPrefixes listener-relative literal prefixes, or {@code null} to inherit the common list
         * @param rules        optional semantic path rules for this listener
         */
        @ConstructorBinding
        public ListenerPaths {
            rules = rules == null ? List.of() : List.copyOf(rules);
        }

        /**
         * Creates a listener override using the original constructor.
         *
         * @param pathPrefixes listener-relative path override
         */
        public ListenerPaths(@Nullable List<String> pathPrefixes) {
            this(pathPrefixes, List.of());
        }

    }

    /**
     * One semantic path rule as supplied by Spring Boot configuration.
     *
     * @param pathPrefix literal listener-relative path prefix or root
     * @param types      error type identifiers
     * @param mode       selected response representation
 * @author Kimi Liu
     */
    public record RouteRule(String pathPrefix, Set<String> types, PolicySnapshot.Mode mode) {

        /**
         * Validates required fields and detaches the bound type identifiers.
         *
         * @param pathPrefix literal listener-relative path prefix or root
         * @param types      error type identifiers
         * @param mode       selected response representation
         */
        public RouteRule {
            Objects.requireNonNull(pathPrefix, "route-errors rule path-prefix");
            types = Set.copyOf(types);
            Objects.requireNonNull(mode, "route-errors rule mode");
        }

        /**
         * Converts configured identifiers to a rule specification for policy validation.
         *
         * @return semantic rule specification for the selected listener
         */
        private ErrorPolicyCompiler.RuleSpec compile() {
            var errorTypes = this.types.stream().map(ErrorType::new).collect(Collectors.toUnmodifiableSet());
            return new ErrorPolicyCompiler.RuleSpec(this.pathPrefix, errorTypes, this.mode);
        }

    }

}
