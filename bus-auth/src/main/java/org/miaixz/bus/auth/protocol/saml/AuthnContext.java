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
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML assertion {@code AuthnContextType} and its declaration choice.
 *
 * @param classReference            optional {@code AuthnContextClassRef} URI
 * @param declaration               optional complete {@code AuthnContextDecl} element bytes
 * @param declarationReference      optional {@code AuthnContextDeclRef} URI
 * @param authenticatingAuthorities ordered authority URI values
 * @author Kimi Liu
 */
public record AuthnContext(Optional<String> classReference, Optional<byte[]> declaration,
        Optional<String> declarationReference, List<String> authenticatingAuthorities) {

    /**
     * Enforces the schema declaration choice and defensively owns declaration XML.
     *
     * @throws IllegalArgumentException if a component, item, or optional container is {@code null}
     * @throws ValidateException        if the CHOICE or a URI/XML value is invalid
     */
    public AuthnContext {
        classReference = uri(classReference, "SAML AuthnContextClassRef");
        Assert.notNull(declaration, "SAML AuthnContextDecl container must not be null");
        final byte[] xml = declaration.getOrNull();
        if (xml != null && xml.length == 0) {
            throw new ValidateException("SAML AuthnContextDecl XML must not be empty");
        }
        declaration = Optional.ofNullable(xml == null ? null : xml.clone());
        declarationReference = uri(declarationReference, "SAML AuthnContextDeclRef");
        if (declaration.isPresent() && declarationReference.isPresent()) {
            throw new ValidateException("SAML AuthnContext cannot contain both declaration forms");
        }
        if (classReference.isEmpty() && declaration.isEmpty() && declarationReference.isEmpty()) {
            throw new ValidateException("SAML AuthnContext requires a class or declaration reference");
        }
        Assert.notNull(authenticatingAuthorities, "SAML AuthenticatingAuthority list must not be null");
        for (String authority : authenticatingAuthorities) {
            absolute(authority, "SAML AuthenticatingAuthority");
        }
        authenticatingAuthorities = List.copyOf(authenticatingAuthorities);
    }

    /**
     * Normalizes an optional absolute URI.
     *
     * @param value optional URI value
     * @param label safe diagnostic label
     * @return normalized optional URI
     */
    private static Optional<String> uri(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String actual = value.getOrNull();
        if (actual != null) {
            absolute(actual, label);
        }
        return Optional.ofNullable(actual);
    }

    /**
     * Requires one non-empty absolute URI.
     *
     * @param value candidate URI
     * @param label safe diagnostic label
     */
    private static void absolute(final String value, final String label) {
        final String actual = Assert.notBlank(value, label + " must not be blank");
        try {
            if (!new URI(actual).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Returns a defensive copy of optional declaration XML.
     *
     * @return optional copied declaration element
     */
    @Override
    public Optional<byte[]> declaration() {
        final byte[] value = declaration.getOrNull();
        return Optional.ofNullable(value == null ? null : value.clone());
    }

}
