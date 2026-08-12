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
package org.miaixz.bus.auth.metric.oidc;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.OIDC.UserInfoResponse;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.auth.metric.shared.validation.UriValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Implements provider-side and relying-party-side UserInfo operations. Provider resolution uses the injected subject
 * resolver. Remote retrieval uses the injected HTTPS transport, strict JSON parsing, and exact subject comparison with
 * the already validated ID Token subject.
 *
 * @author Kimi Liu
 */
public final class UserInfoService {

    /**
     * Subject claim name.
     */
    private static final String SUBJECT = "sub";

    /**
     * Authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one UserInfo service.
     *
     * @param runtime authentication runtime
     */
    public UserInfoService(final Runtime runtime) {
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
    }

    /**
     * Resolves provider-side claims for one exact authenticated subject.
     *
     * @param invocation operation context
     * @param subject    exact authenticated subject
     * @return stage containing released UserInfo
     */
    public CompletionStage<UserInfoResponse> resolve(final Invocation invocation, final String subject) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final String expected = Assert.notBlank(subject, "UserInfo subject must be not blank!");
        final CompletionStage<Optional<Subject>> resolved = Assert
                .notNull(runtime.subjects().resolve(context, expected), "Subject resolver stage must be not null!");
        return resolved.thenApply(optional -> {
            if (optional == null || optional.isEmpty() || !expected.equals(optional.get().id())) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final LinkedHashMap<String, Object> claims = new LinkedHashMap<>(optional.get().attributes());
            claims.put(SUBJECT, expected);
            return new UserInfoResponse(expected, claims);
        });
    }

    /**
     * Fetches remote UserInfo and enforces exact ID Token subject equality.
     *
     * @param invocation  operation context
     * @param endpoint    validated UserInfo endpoint
     * @param accessToken bearer access token
     * @param subject     exact ID Token subject
     * @param policy      strict HTTPS transport policy
     * @return stage containing subject-bound UserInfo
     */
    public CompletionStage<UserInfoResponse> fetch(
            final Invocation invocation,
            final URI endpoint,
            final String accessToken,
            final String subject,
            final TransportPolicy policy) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final String token = Assert.notBlank(accessToken, "UserInfo access token must be not blank!");
        final String expected = Assert.notBlank(subject, "ID Token subject must be not blank!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        final URI target = UriValidator.transport(UriValidator.https(endpoint), transportPolicy);
        final Request request = new Request(Http.Method.GET, target,
                Map.of(
                        Http.Header.ACCEPT,
                        List.of(MediaType.APPLICATION_JSON),
                        Http.Header.AUTHORIZATION,
                        List.of(Http.Auth.BEARER_PREFIX + token)),
                Map.of(), Normal.EMPTY, new byte[0]);
        final CompletionStage<Response> exchanged = Assert.notNull(
                runtime.transports().protocol().exchange(context, request, transportPolicy),
                "Protocol transport stage must be not null!");
        return exchanged.thenApply(response -> decode(response, expected));
    }

    /**
     * Decodes a successful JSON response and binds its subject.
     *
     * @param response transport response
     * @param expected exact ID Token subject
     * @return subject-bound UserInfo
     */
    private UserInfoResponse decode(final Response response, final String expected) {
        final Response source = Assert.notNull(response, "UserInfo response must be not null!");
        final List<String> contentTypes = source.headers().get(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT));
        if (source.status() != Http.Status.OK || contentTypes == null || contentTypes.size() != Normal._1
                || !contentTypes.get(Normal._0).toLowerCase(Locale.ROOT).startsWith(MediaType.APPLICATION_JSON)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Object decoded = new StrictJsonReader(runtime.json(), runtime.limits()).read(source.body(), Map.class);
        if (!(decoded instanceof Map<?, ?> values)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Object subject = values.get(SUBJECT);
        if (!(subject instanceof String actual) || !expected.equals(actual)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (!(name instanceof String member)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            claims.put(member, value);
        });
        return new UserInfoResponse(expected, claims);
    }

}
