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
package org.miaixz.bus.fabric.protocol.http.chain;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConnectionException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.proxy.ProxyPolicyResolver;
import org.miaixz.bus.fabric.network.proxy.ProxySelection;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Resolves proxy policy before bridge processing and switches only after a structured, retry-safe route failure.
 * <p>
 * Application responses, authentication failures, protocol failures, target failures, and failures occurring after
 * application-data delivery bypass candidate switching and propagate to the caller unchanged.
 *
 * @author Kimi Liu
 */
public class HttpProxyRouting implements HttpStage {

    /**
     * Immutable context options consulted when a request inherits its proxy policy.
     */
    private final Options options;

    /**
     * Creates a proxy-routing stage.
     *
     * @param options non-null context options containing network and legacy proxy policies
     */
    public HttpProxyRouting(final Options options) {
        this.options = Assert.notNull(options, () -> new ValidateException("HTTP proxy options must not be null"));
    }

    /**
     * Resolves ordered route candidates and proceeds with the first successful or terminal attempt.
     *
     * @param request request whose proxy policy must be resolved
     * @param chain   remaining HTTP pipeline beginning immediately after this stage
     * @return response produced by the selected route
     * @throws ConnectionException when route establishment fails and no safe candidate remains
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final ProxySelection selection = new ProxyPolicyResolver()
                .resolve(request.proxy(), options, request.url().address());
        final int replayIndex = chain.index();
        boolean first = true;
        ConnectionException failure = null;
        for (final ProxyPlan candidate : selection.candidates()) {
            final HttpRequest routed = request.proxy() == candidate ? request
                    : request.toBuilder().proxy(candidate).build();
            try {
                final HttpChain attempt = first ? chain : chain.replayFrom(replayIndex);
                first = false;
                return attempt.proceed(routed);
            } catch (final ConnectionException e) {
                if (!e.canSwitchRoute()) {
                    throw e;
                }
                selection.connectFailed(candidate, e);
                if (failure != null && failure != e) {
                    e.addSuppressed(failure);
                }
                failure = e;
            }
        }
        throw failure;
    }

}
