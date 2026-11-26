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
package org.miaixz.bus.spring.web.routing;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.server.PathContainer;

import org.miaixz.bus.errors.policy.PolicySnapshot;
import org.miaixz.bus.errors.transport.http.HttpErrorPolicy;

/**
 * Compatibility facade for listener-relative route error policies.
 *
 * @author Kimi Liu
 */
public final class RouteErrorPolicy {

    /**
     * Shared HTTP policy implementing legacy and semantic route matching.
     */
    private final HttpErrorPolicy delegate;

    /**
     * Creates a policy using the original literal prefixes and 4xx status contract.
     *
     * @param pathPrefixes literal slash-terminated paths relative to the listener
     * @param statuses     eligible HTTP client error statuses
     * @param cacheControl optional Cache-Control override
     */
    public RouteErrorPolicy(List<String> pathPrefixes, Set<Integer> statuses, String cacheControl) {
        this(pathPrefixes, statuses, cacheControl, new PolicySnapshot(List.of()));
    }

    /**
     * Creates a policy with compiled semantic rules for this listener.
     *
     * @param pathPrefixes original literal paths
     * @param statuses     original HTTP client error statuses
     * @param cacheControl optional Cache-Control override
     * @param snapshot     validated semantic rules
     */
    public RouteErrorPolicy(List<String> pathPrefixes, Set<Integer> statuses, String cacheControl,
            PolicySnapshot snapshot) {
        this.delegate = new HttpErrorPolicy(pathPrefixes, statuses, cacheControl, snapshot);
    }

    /**
     * Adapts a shared policy for the existing Spring MVC resolver API.
     *
     * @param policy compiled shared HTTP error policy
     * @return compatibility view of the supplied policy
     */
    public static RouteErrorPolicy fromCompiled(HttpErrorPolicy policy) {
        return new RouteErrorPolicy(policy);
    }

    /**
     * Reuses an existing immutable listener policy.
     *
     * @param policy compiled shared HTTP error policy
     */
    private RouteErrorPolicy(HttpErrorPolicy policy) {
        this.delegate = Objects.requireNonNull(policy, "policy");
    }

    /**
     * Matches the original status and path policy.
     *
     * @param path   listener-relative parsed path
     * @param status HTTP status
     * @return whether the legacy policy matches
     */
    public boolean matches(PathContainer path, int status) {
        return this.delegate.matches(path, status);
    }

    /**
     * Returns the cache policy inherited from the shared HTTP implementation.
     *
     * @return cache override, or blank to keep the existing header
     */
    public String cacheControl() {
        return this.delegate.cacheControl();
    }

    /**
     * Exposes the shared policy to the listener adapter.
     *
     * @return the compiled shared HTTP error policy
     */
    public HttpErrorPolicy compiled() {
        return this.delegate;
    }

}
