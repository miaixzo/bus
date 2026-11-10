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
package org.miaixz.bus.errors.transport.http;

import java.util.List;
import java.util.Set;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import org.miaixz.bus.errors.ErrorDecision;
import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.policy.ErrorPolicyCompiler;
import org.miaixz.bus.errors.policy.PolicySnapshot;
import org.miaixz.bus.errors.policy.PolicySnapshot.Mode;

/**
 * Selects a shared Servlet or Reactive HTTP error contract.
 *
 * @author Kimi Liu
 */
public final class HttpErrorPolicy {

    /**
     * Compiled Spring patterns for listener-relative literal prefixes.
     */
    private final List<PathPattern> prefixes;
    /**
     * Verified HTTP statuses eligible for the route policy.
     */
    private final Set<Integer> statuses;
    /**
     * Optional Cache-Control override applied to native HTTP errors.
     */
    private final String cacheControl;
    /**
     * Semantic path and error type rules for this listener.
     */
    private final PolicySnapshot snapshot;
    /**
     * Whether the policy selects native responses solely from verified numeric statuses.
     */
    private final boolean statusOnly;

    /**
     * Whether the root prefix selects all paths on this listener.
     */
    private final boolean matchAll;

    /**
     * Compiles the shared listener policy from literal paths and explicit numeric statuses.
     *
     * @param prefixes     listener-relative paths, with {@code /} selecting the entire listener
     * @param statuses     nonempty HTTP error statuses from 400 through 599
     * @param cacheControl optional Cache-Control override
     * @return immutable status policy shared by the MVC and reactive adapters
     */
    public static HttpErrorPolicy forStatuses(List<String> prefixes, Set<Integer> statuses, String cacheControl) {
        return new HttpErrorPolicy(prefixes, statuses, cacheControl, new PolicySnapshot(List.of()), true);
    }

    /**
     * Identifies policies configured solely by verified HTTP status.
     *
     * @return whether the verified status policy is active
     */
    public boolean isStatusOnly() {
        return this.statusOnly;
    }

    /**
     * Checks whether a parsed path is inside the listener policy's scope.
     *
     * @param path listener-relative parsed path
     * @return whether the path matches a configured prefix
     */
    public boolean matchesPath(PathContainer path) {
        return this.matchAll || this.prefixes.stream().anyMatch(prefix -> prefix.matchStartOfPath(path) != null);
    }

    /**
     * Creates a policy with legacy status rules and optional semantic rules.
     *
     * @param prefixes     legacy literal slash-terminated prefixes
     * @param statuses     legacy 4xx statuses
     * @param cacheControl optional Cache-Control override
     * @param snapshot     compiled semantic rules for the listener
     */
    public HttpErrorPolicy(List<String> prefixes, Set<Integer> statuses, String cacheControl, PolicySnapshot snapshot) {
        this(prefixes, statuses, cacheControl, snapshot, false);
    }

    /**
     * Validates and compiles the selected route policy representation.
     *
     * @param prefixes     listener-relative literal paths
     * @param statuses     numeric HTTP error statuses
     * @param cacheControl optional Cache-Control override
     * @param snapshot     optional semantic rules and snapshot version
     * @param statusOnly   whether numeric rules may select the root and verified 5xx statuses
     */
    private HttpErrorPolicy(List<String> prefixes, Set<Integer> statuses, String cacheControl, PolicySnapshot snapshot,
            boolean statusOnly) {
        if ((prefixes.isEmpty() && snapshot.rules().isEmpty()) || prefixes.stream().anyMatch(
                prefix -> !(statusOnly && "/".equals(prefix)) && !ErrorPolicyCompiler.isLiteralPrefix(prefix))) {
            throw new IllegalArgumentException(
                    statusOnly ? "bus.errors.routing.path-prefixes requires / or literal slash-terminated paths"
                            : "route-errors.path-prefixes requires literal paths starting and ending with /");
        }
        int lastStatus = statusOnly ? 599 : 499;
        if ((!prefixes.isEmpty() && statuses.isEmpty())
                || statuses.stream().anyMatch(status -> status < 400 || status > lastStatus)) {
            throw new IllegalArgumentException(
                    statusOnly ? "bus.errors.routing.statuses requires at least one HTTP status from 400 through 599"
                            : "route-errors.statuses requires at least one 4xx HTTP status");
        }
        if (cacheControl.indexOf('\r') >= 0 || cacheControl.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(
                    statusOnly ? "Invalid bus.errors.routing.cache-control" : "Invalid route-errors.cache-control");
        }
        this.matchAll = statusOnly && prefixes.contains("/");
        this.statusOnly = statusOnly;
        this.prefixes = prefixes.stream().filter(prefix -> !"/".equals(prefix))
                .map(PathPatternParser.defaultInstance::parse).toList();
        this.statuses = Set.copyOf(statuses);
        this.cacheControl = cacheControl;
        this.snapshot = snapshot;
    }

    /**
     * Tests the listener-relative path and verified HTTP status rules.
     *
     * @param path   Spring-parsed request path
     * @param status verified HTTP status
     * @return whether the numeric route rule matches
     */
    public boolean matches(PathContainer path, int status) {
        return this.statuses.contains(status) && matchesPath(path);
    }

    /**
     * Selects a typed rule before the existing path/status rules.
     *
     * @param path        Spring-parsed request path
     * @param decision    classification with a verified status
     * @param legacyRoute whether the adapter verified an error eligible for numeric HTTP matching
     * @return selected contract, or {@code null} to leave the previous behavior in place
     */
    public Mode select(PathContainer path, ErrorDecision decision, boolean legacyRoute) {
        Mode mode = decision.error() == null ? null : semanticMode(path, decision.error().type());
        return mode != null ? mode : legacyRoute && matches(path, decision.status()) ? Mode.NATIVE_HTTP : null;
    }

    /**
     * Selects an explicitly configured semantic rule.
     *
     * @param path listener-relative parsed path
     * @param type verified semantic failure type
     * @return response mode, or {@code null} when no semantic rule matches
     */
    public Mode semanticMode(PathContainer path, ErrorType type) {
        if (this.snapshot.rules().isEmpty()) {
            return null;
        }
        List<String> segments = path.elements().stream().filter(PathContainer.PathSegment.class::isInstance)
                .map(PathContainer.PathSegment.class::cast).map(PathContainer.PathSegment::valueToMatch).toList();
        PolicySnapshot.Rule rule = this.snapshot.match(segments, type);
        return rule == null ? null : rule.mode();
    }

    /**
     * Checks whether the listener has opted into semantic route rules.
     *
     * @return whether the listener configured semantic rules
     */
    public boolean hasSemanticRules() {
        return !this.snapshot.rules().isEmpty();
    }

    /**
     * Returns the version of this listener’s immutable policy snapshot.
     *
     * @return immutable listener policy version
     */
    public long version() {
        return this.snapshot.version();
    }

    /**
     * Returns the configured Cache-Control override for native HTTP errors.
     *
     * @return configured cache policy, blank to preserve existing headers
     */
    public String cacheControl() {
        return this.cacheControl;
    }

}
