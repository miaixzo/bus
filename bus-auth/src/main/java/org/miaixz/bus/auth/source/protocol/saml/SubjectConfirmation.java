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
 * Models the SAML assertion {@code SubjectConfirmationType}.
 *
 * @param identifier optional BaseID, NameID, or EncryptedID confirmation identifier
 * @param data       optional SubjectConfirmationData child
 * @param method     required confirmation method URI
 * @author Kimi Liu
 */
public record SubjectConfirmation(Optional<Subject.Identifier> identifier, Optional<SubjectConfirmationData> data,
        String method) {

    /**
     * Normalizes optional children and validates the required absolute method URI.
     *
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     * @throws ValidateException        if {@code method} is not an absolute URI
     */
    public SubjectConfirmation {
        Assert.notNull(identifier, "SAML SubjectConfirmation identifier container must not be null");
        identifier = Optional.ofNullable(identifier.getOrNull());
        Assert.notNull(data, "SAML SubjectConfirmationData container must not be null");
        data = Optional.ofNullable(data.getOrNull());
        method = Assert.notBlank(method, "SAML SubjectConfirmation Method must not be blank");
        try {
            if (!new URI(method).isAbsolute()) {
                throw new ValidateException("SAML SubjectConfirmation Method must be an absolute URI");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("SAML SubjectConfirmation Method is not a valid URI", exception);
        }
    }

}
