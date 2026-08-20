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
package org.miaixz.bus.auth.protocol.saml;

import java.net.URI;
import java.net.URISyntaxException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the SAML 2.0 assertion {@code NameIDType} simple content and its four optional attributes.
 *
 * @param value           exact XML character content
 * @param nameQualifier   optional security or administrative domain qualifier
 * @param spNameQualifier optional service-provider qualifier
 * @param format          optional absolute Name Identifier format URI
 * @param spProvidedId    optional service-provider supplied identifier
 * @author Kimi Liu
 */
public record NameID(String value, Optional<String> nameQualifier, Optional<String> spNameQualifier,
        Optional<String> format, Optional<String> spProvidedId) {

    /**
     * Normalizes optional XML attributes and validates the optional format URI.
     *
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     * @throws ValidateException        if a present format is not absolute or a present string attribute is empty
     */
    public NameID {
        Assert.notNull(value, "SAML NameID value must not be null");
        nameQualifier = optional(nameQualifier, "NameQualifier");
        spNameQualifier = optional(spNameQualifier, "SPNameQualifier");
        format = optional(format, "Format");
        spProvidedId = optional(spProvidedId, "SPProvidedID");
        final String formatValue = format.getOrNull();
        if (formatValue != null) {
            requireAbsoluteUri(formatValue, "SAML NameID Format");
        }
    }

    /**
     * Normalizes a schema-optional non-empty string attribute.
     *
     * @param value optional attribute container
     * @param name  XML attribute name
     * @return normalized optional value
     */
    private static Optional<String> optional(final Optional<String> value, final String name) {
        Assert.notNull(value, "SAML NameID " + name + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            Assert.notEmpty(actual, "SAML NameID " + name + " must not be empty");
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Requires one XML {@code anyURI} value to be absolute at the framework boundary.
     *
     * @param value candidate URI
     * @param label safe diagnostic label
     */
    private static void requireAbsoluteUri(final String value, final String label) {
        try {
            if (!new URI(value).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

}
