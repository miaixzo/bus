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

import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable parsed external subject trust relation.
 *
 * @param consumerId      Consumer that accepts the federated assertion
 * @param issuer          trusted external issuer URI
 * @param externalSubject stable external subject identifier
 * @param subject         mapped project Subject key
 * @author Kimi Liu
 */
public record FederationMetadata(String consumerId, String issuer, String externalSubject, Subject.Key subject) {

    /**
     * Validates the relation without executing its trust decision.
     */
    public FederationMetadata {
        Assert.notBlank(consumerId, "Federation consumer identifier must not be blank");
        Assert.notBlank(externalSubject, "Federation external subject must not be blank");
        Assert.notNull(subject, "Federation project subject must not be null");
        try {
            final URI value = new URI(Assert.notBlank(issuer, "Federation issuer must not be blank"));
            if (!"https".equalsIgnoreCase(value.getScheme()) || value.getHost() == null || value.getQuery() != null
                    || value.getFragment() != null) {
                throw new ValidateException("Federation issuer must be an HTTPS URI without query or fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Federation issuer is invalid", cause);
        }
    }

}
