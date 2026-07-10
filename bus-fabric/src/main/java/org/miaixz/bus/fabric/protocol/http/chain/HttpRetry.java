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

import java.net.URI;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;
import org.miaixz.bus.fabric.registry.policy.RetryPolicy;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP retry and redirect stage.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpRetry implements HttpStage {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Stage name.
     */
    private final String name;

    /**
     * Retry policy.
     */
    private final RetryPolicy policy;

    /**
     * HTTP authenticator.
     */
    private final HttpAuthenticator authenticator;

    /**
     * Creates a retry stage with default policy.
     */
    public HttpRetry() {
        this(RetryPolicy.defaults(), HttpAuthenticator.none());
    }

    /**
     * Creates a retry stage with an authenticator.
     *
     * @param authenticator authenticator
     */
    public HttpRetry(final HttpAuthenticator authenticator) {
        this(RetryPolicy.defaults(), authenticator);
    }

    /**
     * Creates a retry stage.
     *
     * @param policy retry policy
     */
    private HttpRetry(final RetryPolicy policy, final HttpAuthenticator authenticator) {
        this.name = "http-retry";
        this.policy = require(policy, "Retry policy");
        this.authenticator = require(authenticator, "HTTP authenticator");
    }

    /**
     * Executes retries and follow-ups.
     *
     * @param request request
     * @param chain   next chain
     * @return response
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        int attempt = 0;
        int followUps = 0;
        HttpResponse prior = null;
        while (true) {
            try {
                Logger.debug(
                        true,
                        LOG_TAG,
                        "HTTP retry attempt started: method={}, host={}, port={}, path={}, attempt={}, followUps={}",
                        current.method().value(),
                        current.url().host(),
                        current.url().port(),
                        current.url().path(),
                        attempt,
                        followUps);
                final HttpResponse response = next.proceed(current);
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP retry attempt response: method={}, host={}, port={}, path={}, code={}, attempt={}, followUps={}",
                        current.method().value(),
                        current.url().host(),
                        current.url().port(),
                        current.url().path(),
                        response.code(),
                        attempt,
                        followUps);
                final HttpRequest followUp = followUp(response);
                if (followUp == null) {
                    Logger.debug(
                            false,
                            LOG_TAG,
                            "HTTP retry completed without follow-up: code={}, attempts={}",
                            response.code(),
                            attempt);
                    return prior == null ? response : response.toBuilder().priorResponse(prior).build();
                }
                if (!followUpAllowed(response.code(), followUps)) {
                    response.close();
                    throw new ProtocolException("Too many HTTP follow-ups");
                }
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP follow-up scheduled: code={}, fromHost={}, toHost={}, sameOrigin={}, method={}, followUps={}",
                        response.code(),
                        current.url().host(),
                        followUp.url().host(),
                        sameOrigin(current.url(), followUp.url()),
                        followUp.method().value(),
                        followUps + 1);
                prior = response.toBuilder().body(HttpBody.empty()).priorResponse(prior).build();
                response.close();
                current = followUp;
                followUps++;
                attempt = 0;
            } catch (final RuntimeException e) {
                if (!recover(e, attempt)) {
                    Logger.debug(
                            false,
                            LOG_TAG,
                            "HTTP retry declined: attempt={}, exception={}",
                            attempt,
                            e.getClass().getSimpleName());
                    throw e;
                }
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP retry scheduled: attempt={}, exception={}",
                        attempt + 1,
                        e.getClass().getSimpleName());
                attempt++;
            }
        }
    }

    /**
     * Creates a follow-up request for a response.
     *
     * @param response response
     * @return follow-up request or null
     */
    public HttpRequest followUp(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        return switch (current.code()) {
            case 300, 301, 302, 303, 307, 308 -> redirect(current);
            case 401, 407 -> authenticator.authenticate(current.request(), current);
            default -> null;
        };
    }

    /**
     * Returns whether a failure can be retried.
     *
     * @param cause   cause
     * @param attempt attempt
     * @return true when recoverable
     */
    public boolean recover(final Throwable cause, final int attempt) {
        return policy.retry(require(cause, "Failure cause"), attempt);
    }

    /**
     * Returns stage name.
     *
     * @return stage name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Creates a redirect request.
     *
     * @param response response
     * @return request or null
     */
    private HttpRequest redirect(final HttpResponse response) {
        final String location = response.headers().get("Location");
        if (location == null) {
            return null;
        }
        if (StringKit.isBlank(location) || StringKit.containsAny(location, Symbol.C_CR, Symbol.C_LF)) {
            throw new ProtocolException("Invalid redirect location");
        }
        final URI uri = response.request().url().toUri().resolve(location);
        final UnoUrl url = UnoUrl.parse(uri.toString());
        final HttpRequest request = response.request();
        final HTTP.Method method = redirectMethod(response.code(), request.method());
        final boolean preserveBody = preserveBody(response.code(), request.method());
        if (preserveBody && request.body().length() > 0 && !request.body().repeatable()) {
            return null;
        }
        final HttpRequest.Builder builder = request.toBuilder().method(method).url(url)
                .headers(redirectHeaders(request.headers(), request.url(), url));
        if (preserveBody && method.supportsBody() && request.body().length() > 0) {
            builder.body(request.body());
        } else {
            builder.body(HttpBody.empty());
        }
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP redirect built: code={}, fromHost={}, toHost={}, sameOrigin={}, method={}, preserveBody={}",
                response.code(),
                request.url().host(),
                url.host(),
                sameOrigin(request.url(), url),
                method.value(),
                preserveBody);
        return builder.build();
    }

    /**
     * Returns whether another follow-up is allowed.
     *
     * @param code      response code
     * @param followUps follow-up count
     * @return true when allowed
     */
    private boolean followUpAllowed(final int code, final int followUps) {
        return switch (code) {
            case 300, 301, 302, 303, 307, 308 -> policy.redirect(code, followUps);
            case 401, 407 -> followUps < 20;
            default -> false;
        };
    }

    /**
     * Selects redirect method.
     *
     * @param code   status code
     * @param method original method
     * @return redirect method
     */
    private static HTTP.Method redirectMethod(final int code, final HTTP.Method method) {
        if (code == 307 || code == 308 || method == HTTP.Method.GET || method == HTTP.Method.HEAD) {
            return method;
        }
        return HTTP.Method.GET;
    }

    /**
     * Returns whether a redirect must preserve the original request body.
     *
     * @param code   status code
     * @param method original method
     * @return true when body is preserved
     */
    private static boolean preserveBody(final int code, final HTTP.Method method) {
        return (code == 307 || code == 308) && method.supportsBody();
    }

    /**
     * Removes request headers that must be recalculated for redirects.
     *
     * @param headers headers
     * @param from    original URL
     * @param to      redirect URL
     * @return headers
     */
    private static Headers redirectHeaders(final Headers headers, final UnoUrl from, final UnoUrl to) {
        Headers current = headers.without("Content-Length").without("Content-Type");
        if (!sameOrigin(from, to)) {
            current = current.without(HTTP.AUTHORIZATION).without(HTTP.PROXY_AUTHORIZATION).without(HTTP.COOKIE);
        }
        return current;
    }

    /**
     * Returns whether two URLs share scheme, host, and port.
     *
     * @param first  first URL
     * @param second second URL
     * @return true when same origin
     */
    private static boolean sameOrigin(final UnoUrl first, final UnoUrl second) {
        return first.address().scheme().equals(second.address().scheme())
                && first.address().host().equalsIgnoreCase(second.address().host())
                && first.address().port() == second.address().port();
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
