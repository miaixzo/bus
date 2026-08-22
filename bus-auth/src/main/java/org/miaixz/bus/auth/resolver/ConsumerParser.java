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
package org.miaixz.bus.auth.resolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.protocol.oauth2.ClientAuthenticationMethod;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oidc.SubjectType;
import org.miaixz.bus.auth.worker.loader.ConsumerLoader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Pure parser for externally loaded consumer records.
 *
 * @author Kimi Liu
 */
public class ConsumerParser {

    /**
     * Creates a stateless consumer-metadata parser.
     */
    public ConsumerParser() {
        // No initialization required.
    }

    /**
     * Validates and freezes exact absolute redirect URI lexical values.
     */
    private static List<String> uris(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        for (String value : values) {
            try {
                final URI uri = new URI(Assert.notBlank(value, label + " must not be blank"));
                if (!uri.isAbsolute() || uri.getFragment() != null) {
                    throw new ValidateException(label + " must be absolute and must not contain a fragment");
                }
            } catch (URISyntaxException cause) {
                throw new ValidateException(label + " is invalid", cause);
            }
        }
        return List.copyOf(values);
    }

    /**
     * Validates Source and consumer ownership without performing data access.
     *
     * @param registration exact Source registration that requested the data
     * @param expectedId   exact requested consumer identifier
     * @param record       project-loaded consumer record
     * @return validated immutable consumer metadata
     */
    public ConsumerMetadata parse(
            final Blueprint.SourceEntry registration,
            final String expectedId,
            final ConsumerLoader.Record record) {
        final String sourceId = Assert.notNull(registration, "Consumer Source registration must not be null").resource()
                .getId();
        final String expected = Assert.notBlank(expectedId, "Expected consumer identifier must not be blank");
        final ConsumerLoader.Record loaded = Assert.notNull(record, "Loaded consumer record must not be null");
        if (!sourceId.equals(loaded.sourceId())) {
            throw new ValidateException("Loaded consumer does not belong to the requested Source");
        }
        if (!expected.equals(loaded.id())) {
            throw new ValidateException("Loaded consumer identifier does not match the requested identifier");
        }
        final ConsumerMetadata.ApplicationType applicationType;
        try {
            applicationType = ConsumerMetadata.ApplicationType.valueOf(
                    Assert.notBlank(loaded.applicationType(), "Consumer application type must not be blank")
                            .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Consumer application type is unsupported", cause);
        }
        final List<String> redirectUris = uris(loaded.redirectUris(), "Consumer redirect URI");
        final List<String> postLogoutRedirectUris = uris(
                loaded.postLogoutRedirectUris(),
                "Consumer post logout redirect URI");
        final Set<GrantType> grantTypes = loaded.grantTypes().stream().map(GrantType::new)
                .collect(Collectors.toUnmodifiableSet());
        final Set<ClientAuthenticationMethod> authenticationMethods = loaded.authenticationMethods().stream()
                .map(ClientAuthenticationMethod::new).collect(Collectors.toUnmodifiableSet());
        if (!Set.of("code").containsAll(loaded.responseTypes())) {
            throw new ValidateException("Consumer response type is unsupported");
        }
        return new ConsumerMetadata(loaded.id(), loaded.name(), applicationType, redirectUris, postLogoutRedirectUris,
                grantTypes, loaded.responseTypes(), loaded.scopes(), authenticationMethods,
                loaded.clientAssertionKeyId(), new SubjectType(loaded.subjectType()), loaded.sectorIdentifier(),
                loaded.idTokenEncryptionKeyId(), loaded.idTokenEncryptionAlgorithm().map(algorithm -> algorithm.name()),
                loaded.idTokenEncryptionMethod().map(method -> method.name()), loaded.metadata());
    }

}
