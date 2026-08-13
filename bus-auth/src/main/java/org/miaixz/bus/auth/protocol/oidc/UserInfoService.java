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
package org.miaixz.bus.auth.protocol.oidc;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.codec.http.HttpValues;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.codec.json.StrictJsonReader;
import org.miaixz.bus.auth.guard.UriValidator;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.protocol.oidc.OIDC.UserInfoResponse;
import org.miaixz.bus.auth.resolver.SubjectResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;

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
     * JSON provider used only through the strict bounded response reader.
     */
    private final JsonProvider json;

    /**
     * Strict decoding limits.
     */
    private final Limits limits;

    /**
     * Creates one UserInfo service.
     *
     * @param json   JSON provider used for strict response decoding
     * @param limits immutable decoding limits
     * @throws ValidateException if either collaborator is {@code null}
     */
    public UserInfoService(final JsonProvider json, final Limits limits) {
        this.json = Assert.notNull(json, () -> new ValidateException("JSON provider must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("Limits must not be null"));
    }

    /**
     * Creates the fixed invalid-request failure.
     *
     * @return new OAuth invalid-request protocol failure
     */
    private static RuntimeException invalidRequest() {
        return new ProtocolException(ProtocolError.INVALID_REQUEST);
    }

    /**
     * Resolves provider-side claims for one exact authenticated subject.
     *
     * @param invocation operation context
     * @param subject    exact authenticated subject
     * @param subjects   non-null tenant-aware subject resolver
     * @return stage containing released UserInfo
     * @throws IllegalArgumentException if a required input or resolver stage is {@code null}
     */
    public CompletionStage<UserInfoResponse> resolve(
            final Context invocation,
            final String subject,
            final SubjectResolver subjects) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final String expected = Assert.notBlank(subject, "UserInfo subject must be not blank!");
        final SubjectResolver resolver = Assert.notNull(subjects, "Subject resolver must be not null!");
        final CompletionStage<Optional<Subject>> resolved = Assert
                .notNull(resolver.resolve(context, expected), "Subject resolver stage must be not null!");
        return resolved.thenApply(optional -> {
            if (optional == null || optional.isEmpty() || !expected.equals(optional.get().id())) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final LinkedHashMap<String, Object> claims = new LinkedHashMap<>(optional.get().claims().snapshot());
            claims.put(SUBJECT, expected);
            return new UserInfoResponse(expected, claims);
        });
    }

    /**
     * Fetches remote UserInfo and enforces exact ID Token subject equality.
     *
     * @param invocation  operation context
     * @param fabric      caller-owned Fabric context used for the remote request
     * @param endpoint    validated UserInfo endpoint
     * @param accessToken bearer access token
     * @param subject     exact ID Token subject
     * @param policy      strict HTTPS transport policy
     * @return stage containing subject-bound UserInfo
     * @throws IllegalArgumentException if a required input is {@code null} or blank
     * @throws ProtocolException        if the endpoint violates the HTTPS transport policy
     */
    public CompletionStage<UserInfoResponse> fetch(
            final Context invocation,
            final org.miaixz.bus.fabric.Context fabric,
            final URI endpoint,
            final String accessToken,
            final String subject,
            final TransportPolicy policy) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        Assert.notNull(fabric, "Fabric context must be not null!");
        final String token = Assert.notBlank(accessToken, "UserInfo access token must be not blank!");
        final String expected = Assert.notBlank(subject, "ID Token subject must be not blank!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        final URI target;
        try {
            target = UriValidator.transport(UriValidator.https(endpoint), transportPolicy.addressPolicy());
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
        return java.util.concurrent.CompletableFuture.supplyAsync(
                () -> HttpX.builder(fabric).get(target.toASCIIString())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + token)
                        .addressPolicy(transportPolicy.addressPolicy()).timeout(transportPolicy.timeout()).build()
                        .execute())
                .thenApply(response -> decode(response, expected));
    }

    /**
     * Decodes a successful JSON response and binds its subject.
     *
     * @param response transport response
     * @param expected exact ID Token subject
     * @return subject-bound UserInfo
     * @throws ProtocolException if status, media type, bounds, JSON, member names, or subject binding is invalid
     */
    private UserInfoResponse decode(final HttpResponse response, final String expected) {
        try (HttpResponse source = Assert.notNull(response, "UserInfo response must be not null!")) {
            if (source.code() != Http.Status.OK
                    || !HttpValues.json(source.headers().asMap(), UserInfoService::invalidRequest)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final Object decoded = new StrictJsonReader(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                    .read(source.bytes(limits.maxJsonBytes()), Map.class);
            if (!(decoded instanceof Map<?, ?> values)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final String actual = JsonValues
                    .requiredText(values, SUBJECT, limits.maxParameterBytes(), UserInfoService::invalidRequest);
            if (!expected.equals(actual)) {
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

}
