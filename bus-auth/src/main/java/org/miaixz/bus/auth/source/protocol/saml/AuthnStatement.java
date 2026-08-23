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
package org.miaixz.bus.auth.source.protocol.saml;

import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML assertion {@code AuthnStatementType}, including optional SubjectLocality attributes.
 *
 * @param authnInstant           required authentication instant
 * @param sessionIndex           optional session index
 * @param sessionNotOnOrAfter    optional exclusive session expiration
 * @param subjectLocalityAddress optional SubjectLocality Address
 * @param subjectLocalityDnsName optional SubjectLocality DNSName
 * @param authnContext           required authentication context
 * @author Kimi Liu
 */
public record AuthnStatement(Instant authnInstant, Optional<String> sessionIndex, Optional<Instant> sessionNotOnOrAfter,
        Optional<String> subjectLocalityAddress, Optional<String> subjectLocalityDnsName, AuthnContext authnContext) {

    /**
     * Validates temporal and optional string constraints.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if the session expiration does not follow authentication
     */
    public AuthnStatement {
        Assert.notNull(authnInstant, "SAML AuthnStatement AuthnInstant must not be null");
        sessionIndex = text(sessionIndex, "SAML AuthnStatement SessionIndex");
        Assert.notNull(sessionNotOnOrAfter, "SAML AuthnStatement SessionNotOnOrAfter container must not be null");
        sessionNotOnOrAfter = Optional.ofNullable(sessionNotOnOrAfter.getOrNull());
        if (sessionNotOnOrAfter.isPresent() && !authnInstant.isBefore(sessionNotOnOrAfter.getOrNull())) {
            throw new ValidateException("SAML AuthnStatement session expiration must follow AuthnInstant");
        }
        subjectLocalityAddress = text(subjectLocalityAddress, "SAML SubjectLocality Address");
        subjectLocalityDnsName = text(subjectLocalityDnsName, "SAML SubjectLocality DNSName");
        Assert.notNull(authnContext, "SAML AuthnStatement AuthnContext must not be null");
    }

    /**
     * Normalizes an optional non-empty string attribute.
     *
     * @param value optional attribute value
     * @param label safe diagnostic label
     * @return normalized optional value
     */
    private static Optional<String> text(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            Assert.notEmpty(actual, label + " must not be empty");
        }
        return Optional.ofNullable(actual);
    }

}
