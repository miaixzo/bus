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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Immutable protocol consumer metadata accepted by authentication services.
 *
 * @param id                         consumer identifier
 * @param name                       display name
 * @param applicationType            web or native application type
 * @param redirectUris               ordered registered redirect URI values
 * @param postLogoutRedirectUris     ordered registered post-logout redirect URI values
 * @param grantTypes                 allowed authorization grant types
 * @param responseTypes              allowed authorization response types
 * @param scopes                     scopes the consumer may request
 * @param authenticationMethods      registered client authentication methods
 * @param clientAssertionKeyId       optional standard client assertion key identifier
 * @param subjectType                public or pairwise subject type
 * @param sectorIdentifier           optional pairwise sector identifier
 * @param idTokenEncryptionKeyId     optional ID Token encryption key identifier
 * @param idTokenEncryptionAlgorithm optional ID Token JWE key-management algorithm
 * @param idTokenEncryptionMethod    optional ID Token JWE content-encryption method
 * @param metadata                   detached non-core protocol metadata
 * @author Kimi Liu
 */
public record ConsumerMetadata(String id, String name, Optional<String> applicationType, List<String> redirectUris,
        List<String> postLogoutRedirectUris, Set<String> grantTypes, Set<String> responseTypes, Set<String> scopes,
        Set<Endpoint.Authentication> authenticationMethods, Optional<String> clientAssertionKeyId,
        Optional<String> subjectType, Optional<String> sectorIdentifier, Optional<String> idTokenEncryptionKeyId,
        Optional<String> idTokenEncryptionAlgorithm, Optional<String> idTokenEncryptionMethod,
        JsonValue.ObjectValue metadata) {

    /**
     * Creates and freezes validated consumer metadata.
     */
    public ConsumerMetadata {
        Assert.notBlank(id, "Consumer identifier must not be blank");
        Assert.notBlank(name, "Consumer name must not be blank");
        applicationType = normalized(applicationType, "Consumer application type");
        redirectUris = immutableList(redirectUris, "Consumer redirect URI");
        postLogoutRedirectUris = immutableList(postLogoutRedirectUris, "Consumer post logout redirect URI");
        grantTypes = immutableSet(grantTypes, "Consumer grant type");
        responseTypes = immutableSet(responseTypes, "Consumer response type");
        scopes = immutableSet(scopes, "Consumer scope");
        authenticationMethods = immutableSet(authenticationMethods, "Consumer authentication method");
        clientAssertionKeyId = normalized(clientAssertionKeyId, "Consumer assertion key identifier");
        subjectType = normalized(subjectType, "Consumer subject type");
        sectorIdentifier = normalized(sectorIdentifier, "Consumer sector identifier");
        idTokenEncryptionKeyId = normalized(idTokenEncryptionKeyId, "Consumer ID Token encryption key identifier");
        idTokenEncryptionAlgorithm = normalized(idTokenEncryptionAlgorithm, "Consumer ID Token encryption algorithm");
        idTokenEncryptionMethod = normalized(idTokenEncryptionMethod, "Consumer ID Token encryption method");
        Assert.notNull(metadata, "Consumer metadata must not be null");
        metadata = new JsonValue.ObjectValue(metadata.values());
    }

    /**
     * Normalizes an optional non-blank Consumer metadata value.
     *
     * @param value optional external value
     * @param label safe semantic label used in validation messages
     * @return normalized optional value
     */
    private static Optional<String> normalized(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String present = value.getOrNull();
        return present == null ? Optional.empty() : Optional.of(Assert.notBlank(present, label + " must not be blank"));
    }

    /**
     * Validates and freezes an ordered unique list of protocol text values.
     *
     * @param values values to validate and copy
     * @param label  safe semantic label used in validation messages
     * @return immutable ordered list
     */
    private static List<String> immutableList(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final List<String> copy = new ArrayList<>(values.size());
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            Assert.notBlank(value, label + " must not be blank");
            if (!unique.add(value)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
            copy.add(value);
        }
        return List.copyOf(copy);
    }

    /**
     * Validates and freezes a set of protocol text values.
     *
     * @param values values to validate and copy
     * @param label  safe semantic label used in validation messages
     * @param <T>    immutable set member type
     * @return immutable set
     */
    private static <T> Set<T> immutableSet(final Set<T> values, final String label) {
        Assert.notNull(values, label + " set must not be null");
        for (T value : values) {
            Assert.notNull(value, label + " must not be null");
            if (value instanceof String text) {
                Assert.notBlank(text, label + " must not be blank");
            }
        }
        return Set.copyOf(values);
    }

}
