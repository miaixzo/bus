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

import java.net.URI;
import java.net.URISyntaxException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML protocol {@code NameIDPolicyType} attributes.
 *
 * @param format          optional requested Name Identifier format URI
 * @param spNameQualifier optional service-provider qualifier
 * @param allowCreate     optional lexical-presence value of {@code AllowCreate}
 * @author Kimi Liu
 */
public record NameIDPolicy(Optional<String> format, Optional<String> spNameQualifier, Optional<Boolean> allowCreate) {

    /**
     * Normalizes optional attributes and validates the optional format URI.
     *
     * @throws IllegalArgumentException if an optional container is {@code null}
     * @throws ValidateException        if a present format is not absolute or a qualifier is empty
     */
    public NameIDPolicy {
        format = text(format, "SAML NameIDPolicy Format");
        spNameQualifier = text(spNameQualifier, "SAML NameIDPolicy SPNameQualifier");
        Assert.notNull(allowCreate, "SAML NameIDPolicy AllowCreate container must not be null");
        allowCreate = Optional.ofNullable(allowCreate.getOrNull());
        final String value = format.getOrNull();
        if (value != null) {
            try {
                if (!new URI(value).isAbsolute()) {
                    throw new ValidateException("SAML NameIDPolicy Format must be absolute");
                }
            } catch (URISyntaxException exception) {
                throw new ValidateException("SAML NameIDPolicy Format is not a valid URI", exception);
            }
        }
    }

    /**
     * Normalizes an optional non-empty XML attribute.
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
