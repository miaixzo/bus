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
package org.miaixz.bus.auth.metric.proxy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.ProxyAuth.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Combines header and redirect policies into deterministic allow, deny, and redirect decisions.
 */
public final class ProxyDecisionEngine implements Engine {

    /**
     * Closed configuration.
     */
    private final Config configuration;

    /**
     * Product authentication port.
     */
    private final Authenticator authenticator;

    /**
     * Header trust policy.
     */
    private final ProxyHeaderPolicy headers;

    /**
     * Origin validation policy.
     */
    private final ProxyRedirectValidator redirects;

    /**
     * Creates one decision engine.
     *
     * @param configuration configuration
     * @param authenticator authenticator
     */
    public ProxyDecisionEngine(final Config configuration, final Authenticator authenticator) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("Proxy configuration must not be null"));
        this.authenticator = Assert
                .notNull(authenticator, () -> new ValidateException("Proxy authenticator must not be null"));
        this.headers = new ProxyHeaderPolicy(configuration);
        this.redirects = new ProxyRedirectValidator(configuration);
    }

    /**
     * Evaluates one request.
     *
     * @param invocation operation context
     * @param request    request
     * @return decision stage
     */
    @Override
    public CompletionStage<Decision> decide(final Invocation invocation, final ForwardRequest request) {
        Assert.notNull(invocation, () -> new ValidateException("Proxy invocation must not be null"));
        try {
            redirects.validate(request.uri());
            if (request.returnUri() != null) {
                redirects.validate(request.returnUri());
            }
            final ForwardRequest sanitized = headers.sanitize(request);
            return authenticator.authenticate(invocation, sanitized).thenApply(identity -> {
                if (identity.authenticated()) {
                    return new Decision(Action.ALLOW, Http.Status.OK, headers.identity(identity), null);
                }
                if (configuration.redirectUnauthenticated()) {
                    return new Decision(Action.REDIRECT, Http.Status.FOUND,
                            Map.of(Http.Header.LOCATION, List.of(configuration.loginUri().toString())),
                            redirects.validate(configuration.loginUri()));
                }
                return new Decision(Action.DENY, Http.Status.UNAUTHORIZED, Map.of(), null);
            }).exceptionally(failure -> new Decision(Action.DENY, Http.Status.INTERNAL_SERVER_ERROR, Map.of(), null));
        } catch (final RuntimeException failure) {
            return CompletableFuture
                    .completedFuture(new Decision(Action.DENY, Http.Status.BAD_REQUEST, Map.of(), null));
        }
    }

}
