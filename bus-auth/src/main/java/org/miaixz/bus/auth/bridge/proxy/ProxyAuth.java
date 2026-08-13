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
package org.miaixz.bus.auth.bridge.proxy;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Principal;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Defines the sole forward-auth facade and immutable contracts. It emits authorization decisions only and never owns a
 * reverse proxy, router, HTTP server, session store, or product authentication flow.
 *
 * @author Kimi Liu
 */
public final class ProxyAuth {

    /**
     * Prevents construction of the forward-auth namespace.
     */
    private ProxyAuth() {
        // No initialization required.
    }

    /**
     * Creates the forward-auth decision engine.
     *
     * @param configuration closed policy
     * @param authenticator product authenticator
     * @return decision engine
     * @throws ValidateException if an argument is null or invalid
     */
    public static Engine engine(final Config configuration, final Authenticator authenticator) {
        return new ProxyDecisionEngine(configuration, authenticator);
    }

    /**
     * Recursively snapshots a string header multimap.
     *
     * @param source      non-null header map
     * @param nullMessage validation message for a null map
     * @return immutable map containing immutable value lists
     * @throws ValidateException if the map, a header name, or a value list is null
     */
    private static Map<String, List<String>> headers(final Map<String, List<String>> source, final String nullMessage) {
        final Map<String, List<String>> checked = Assert.notNull(source, () -> new ValidateException(nullMessage));
        final java.util.LinkedHashMap<String, List<String>> snapshot = new java.util.LinkedHashMap<>();
        checked.forEach(
                (name, values) -> snapshot.put(
                        Assert.notBlank(name, () -> new ValidateException("Proxy header name must not be blank")),
                        List.copyOf(
                                Assert.notNull(
                                        values,
                                        () -> new ValidateException("Proxy header values must not be null")))));
        return Map.copyOf(snapshot);
    }

    /**
     * Forward-auth decision action.
     *
     * @author Kimi Liu
     */
    public enum Action {

        /**
         * Allows the upstream request.
         */
        ALLOW,

        /**
         * Denies the upstream request.
         */
        DENY,

        /**
         * Redirects the user agent to the registered login origin.
         */
        REDIRECT
    }

    /**
     * Forward-auth engine contract.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Engine {

        /**
         * Evaluates one request.
         *
         * @param invocation operation context
         * @param request    forward-auth request
         * @return decision stage
         */
        CompletionStage<Decision> decide(Context invocation, ForwardRequest request);
    }

    /**
     * Product authentication port.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Authenticator {

        /**
         * Authenticates one sanitized request.
         *
         * @param invocation operation context
         * @param request    sanitized request
         * @return non-null stage containing an optional authenticated principal
         */
        CompletionStage<Optional<Principal>> authenticate(Context invocation, ForwardRequest request);
    }

    /**
     * Immutable incoming request.
     *
     * @param method    request method
     * @param uri       absolute upstream URI
     * @param headers   normalized request headers
     * @param returnUri optional post-login return URI
     * @author Kimi Liu
     */
    public record ForwardRequest(Http.Method method, URI uri, Map<String, List<String>> headers, URI returnUri) {

        /**
         * Validates one request.
         *
         * @param method    method
         * @param uri       upstream URI
         * @param headers   headers
         * @param returnUri return URI
         */
        public ForwardRequest {
            method = Assert.notNull(method, () -> new ValidateException("Proxy method must not be null"));
            uri = Assert.notNull(uri, () -> new ValidateException("Proxy URI must not be null"));
            headers = ProxyAuth.headers(headers, "Proxy headers must not be null");
        }
    }

    /**
     * Immutable forward-auth decision.
     *
     * @param action   action
     * @param status   HTTP status
     * @param headers  trusted response headers
     * @param location optional validated redirect location
     * @author Kimi Liu
     */
    public record Decision(Action action, int status, Map<String, List<String>> headers, URI location) {

        /**
         * Validates one decision.
         *
         * @param action   action
         * @param status   status
         * @param headers  headers
         * @param location location
         */
        public Decision {
            action = Assert.notNull(action, () -> new ValidateException("Proxy action must not be null"));
            Assert.isTrue(
                    status >= 200 && status <= 599,
                    () -> new ValidateException("Proxy decision status is invalid"));
            headers = ProxyAuth.headers(headers, "Proxy decision headers must not be null");
            Assert.isTrue(
                    action == Action.REDIRECT ? location != null : location == null,
                    () -> new ValidateException("Proxy redirect location does not match the action"));
        }
    }

    /**
     * Immutable forward-auth policy.
     *
     * @param allowedRequestHeaders   case-insensitive incoming allow-list
     * @param identityHeaders         trusted output header to identity attribute mapping
     * @param registeredOrigins       exact HTTPS origins accepted for upstream and return URIs
     * @param loginUri                exact registered HTTPS login URI
     * @param transportPolicy         strict HTTPS transport policy
     * @param redirectUnauthenticated whether unauthenticated requests redirect instead of deny
     * @author Kimi Liu
     */
    public record Config(Set<String> allowedRequestHeaders, Map<String, String> identityHeaders,
            Set<URI> registeredOrigins, URI loginUri, TransportPolicy transportPolicy,
            boolean redirectUnauthenticated) {

        /**
         * Validates one policy.
         *
         * @param allowedRequestHeaders   request header allow-list
         * @param identityHeaders         identity header mapping
         * @param registeredOrigins       registered origins
         * @param loginUri                login URI
         * @param transportPolicy         transport policy
         * @param redirectUnauthenticated redirect flag
         */
        public Config {
            allowedRequestHeaders = Set.copyOf(
                    Assert.notNull(
                            allowedRequestHeaders,
                            () -> new ValidateException("Proxy request header allow-list must not be null")));
            identityHeaders = Map.copyOf(
                    Assert.notNull(
                            identityHeaders,
                            () -> new ValidateException("Proxy identity header mapping must not be null")));
            registeredOrigins = Set.copyOf(
                    Assert.notNull(
                            registeredOrigins,
                            () -> new ValidateException("Proxy registered origins must not be null")));
            Assert.isTrue(
                    !registeredOrigins.isEmpty() && registeredOrigins.size() <= Normal._128,
                    () -> new ValidateException("Proxy registered origin count is invalid"));
            loginUri = Assert.notNull(loginUri, () -> new ValidateException("Proxy login URI must not be null"));
            transportPolicy = Assert
                    .notNull(transportPolicy, () -> new ValidateException("Proxy transport policy must not be null"));
        }
    }

}
