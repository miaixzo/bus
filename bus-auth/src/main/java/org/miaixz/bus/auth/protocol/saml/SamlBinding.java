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
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Identifies one case-sensitive SAML binding URI while retaining unknown registered extensions.
 *
 * @param value exact absolute binding URI
 * @author Kimi Liu
 */
public record SamlBinding(String value) {

    /**
     * OASIS SAML 2.0 HTTP-Redirect binding URI.
     */
    public static final SamlBinding HTTP_REDIRECT = new SamlBinding(Saml.Bindings.HTTP_REDIRECT);

    /**
     * OASIS SAML 2.0 HTTP-POST binding URI.
     */
    public static final SamlBinding HTTP_POST = new SamlBinding(Saml.Bindings.HTTP_POST);

    /**
     * Validates the lexical binding value without closing the OASIS extension registry.
     *
     * @throws IllegalArgumentException if {@code value} is {@code null} or blank
     * @throws ValidateException        if {@code value} is not an absolute URI
     */
    public SamlBinding {
        value = Assert.notBlank(value, "SAML binding URI must not be blank");
        try {
            if (!new URI(value).isAbsolute()) {
                throw new ValidateException("SAML binding identifier must be an absolute URI");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("SAML binding identifier is not a valid URI", exception);
        }
    }

    /**
     * Returns whether this value is implemented by the SAML message transport runtime.
     *
     * @return {@code true} for HTTP-Redirect or HTTP-POST
     */
    public boolean supported() {
        return HTTP_REDIRECT.equals(this) || HTTP_POST.equals(this);
    }

}
