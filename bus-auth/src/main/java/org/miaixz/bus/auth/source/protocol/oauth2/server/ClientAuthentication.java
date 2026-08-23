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
package org.miaixz.bus.auth.source.protocol.oauth2.server;

import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.source.protocol.oauth2.ClientAuthenticationMethod;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Immutable result joining one Consumer snapshot with request-scoped verified authentication facts.
 *
 * @param consumer   immutable authenticated Consumer metadata
 * @param kind       authentication result profile
 * @param method     optional registered authentication method for a standard result
 * @param federation optional verified federation facts for a federated JWT result
 * @author Kimi Liu
 */
public record ClientAuthentication(ConsumerMetadata consumer, Kind kind, Optional<ClientAuthenticationMethod> method,
        Optional<Federation> federation) {

    /**
     * Creates and validates one request-scoped authentication result.
     */
    public ClientAuthentication {
        Assert.notNull(consumer, "Authenticated consumer must not be null");
        Assert.notNull(kind, "Client authentication kind must not be null");
        Assert.notNull(method, "Client authentication method container must not be null");
        Assert.notNull(federation, "Client federation container must not be null");
        method = Optional.ofNullable(method.getOrNull());
        federation = Optional.ofNullable(federation.getOrNull());
        if (kind == Kind.STANDARD && (method.isEmpty() || federation.isPresent())) {
            throw new ValidateException("Standard client authentication requires only a registered method");
        }
        if (kind == Kind.FEDERATED_JWT && (method.isPresent() || federation.isEmpty())) {
            throw new ValidateException("Federated client authentication requires only federation facts");
        }
    }

    /**
     * Creates a standard registered-client authentication result.
     *
     * @param consumer immutable consumer snapshot used throughout the request
     * @param method   verified registered authentication method
     * @return immutable standard authentication result
     */
    public static ClientAuthentication standard(
            final ConsumerMetadata consumer,
            final ClientAuthenticationMethod method) {
        return new ClientAuthentication(consumer, Kind.STANDARD, Optional.of(method), Optional.empty());
    }

    /**
     * Creates a verified federated JWT authentication result.
     *
     * @param consumer        immutable consumer snapshot used throughout the request
     * @param issuer          verified external token issuer
     * @param externalSubject verified external subject
     * @param subject         project subject selected by the federation relation
     * @param claims          immutable verified assertion claims
     * @return immutable federated authentication result
     */
    public static ClientAuthentication federated(
            final ConsumerMetadata consumer,
            final String issuer,
            final String externalSubject,
            final Subject.Key subject,
            final JsonValue.ObjectValue claims) {
        return new ClientAuthentication(consumer, Kind.FEDERATED_JWT, Optional.empty(),
                Optional.of(new Federation(issuer, externalSubject, subject, claims)));
    }

    /**
     * Prevents verified claims from being rendered in logs.
     *
     * @return redacted client-authentication diagnostic representation
     */
    @Override
    public String toString() {
        final Federation facts = federation.getOrNull();
        return "ClientAuthentication[consumer=" + consumer.id() + ", kind=" + kind + ", method="
                + method.map(ClientAuthenticationMethod::value).orElse(null) + ", issuer="
                + (facts == null ? null : facts.issuer()) + ']';
    }

    /**
     * Authentication result profile.
     *
     * @author Kimi Liu
     */
    public enum Kind {

        /**
         * Registered Consumer authentication using a standard OAuth client method.
         */
        STANDARD,

        /**
         * Federated JWT authentication mapped to a project Subject.
         */
        FEDERATED_JWT

    }

    /**
     * Verified external issuer-to-project-subject mapping for one federated request.
     *
     * @param issuer          verified external assertion issuer
     * @param externalSubject verified external assertion subject
     * @param subject         project Subject selected by the federation relation
     * @param claims          detached verified assertion claims
     * @author Kimi Liu
     */
    public record Federation(String issuer, String externalSubject, Subject.Key subject, JsonValue.ObjectValue claims) {

        /**
         * Freezes verified non-secret claims.
         */
        public Federation {
            Assert.notBlank(issuer, "Federation issuer must not be blank");
            Assert.notBlank(externalSubject, "Federation external subject must not be blank");
            Assert.notNull(subject, "Federation project subject must not be null");
            Assert.notNull(claims, "Federation claims must not be null");
            claims = new JsonValue.ObjectValue(claims.values());
        }

    }

}
