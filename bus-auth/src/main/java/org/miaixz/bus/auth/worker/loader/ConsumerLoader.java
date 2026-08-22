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
package org.miaixz.bus.auth.worker.loader;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Loader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Loads one externally managed protocol consumer record.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ConsumerLoader extends Loader<ConsumerLoader.Request, ConsumerLoader.Record> {

    /**
     * Identifies one consumer requested within an exact Source registration.
     *
     * @param registration exact Source registration requesting the data
     * @param consumerId   exact external consumer identifier
     * @author Kimi Liu
     */
    record Request(Blueprint.SourceEntry registration, String consumerId) {

        /**
         * Validates one complete consumer-loading request.
         */
        public Request {
            Assert.notNull(registration, "Consumer registration must not be null");
            Assert.notBlank(consumerId, "Consumer identifier must not be blank");
        }

    }

    /**
     * Project-adapted consumer data that has not yet been parsed by bus-auth.
     *
     * @param sourceId                   exact Source identifier that owns the returned data
     * @param id                         exact external consumer identifier
     * @param name                       display name used by protocol-facing consent information
     * @param applicationType            registered application category
     * @param redirectUris               registered exact redirect URI values
     * @param postLogoutRedirectUris     registered exact post-logout redirect URI values
     * @param grantTypes                 registered OAuth grant types
     * @param responseTypes              registered OAuth response types
     * @param scopes                     registered scope-token set
     * @param authenticationMethods      registered token-endpoint authentication methods
     * @param clientAssertionKeyId       optional key identifier for private-key JWT authentication
     * @param subjectType                registered OpenID subject type
     * @param sectorIdentifier           optional pairwise subject sector identifier
     * @param idTokenEncryptionKeyId     optional ID Token encryption key identifier
     * @param idTokenEncryptionAlgorithm optional ID Token JWE key-management algorithm
     * @param idTokenEncryptionMethod    optional ID Token JWE content-encryption method
     * @param metadata                   protocol-specific non-secret registration metadata
     * @author Kimi Liu
     */
    record Record(String sourceId, String id, String name, String applicationType, List<String> redirectUris,
            List<String> postLogoutRedirectUris, Set<String> grantTypes, Set<String> responseTypes, Set<String> scopes,
            Set<String> authenticationMethods, Optional<String> clientAssertionKeyId, String subjectType,
            Optional<String> sectorIdentifier, Optional<String> idTokenEncryptionKeyId,
            Optional<JwaAlgorithm> idTokenEncryptionAlgorithm, Optional<JwaAlgorithm> idTokenEncryptionMethod,
            JsonValue.ObjectValue metadata) {

    }

}
