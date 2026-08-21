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
 * Models a recursively nestable SAML protocol {@code StatusCodeType}.
 *
 * @param value  required absolute status-code URI
 * @param nested optional second-level status code
 * @author Kimi Liu
 */
public record StatusCode(String value, Optional<StatusCode> nested) {

    /**
     * SAML top-level success status.
     */
    public static final String SUCCESS = Saml.Statuses.SUCCESS;

    /**
     * SAML top-level requester error status.
     */
    public static final String REQUESTER = Saml.Statuses.REQUESTER;

    /**
     * SAML top-level responder error status.
     */
    public static final String RESPONDER = Saml.Statuses.RESPONDER;

    /**
     * SAML top-level version mismatch status.
     */
    public static final String VERSION_MISMATCH = Saml.Statuses.VERSION_MISMATCH;

    /**
     * Validates the required status URI and normalizes the optional recursive child.
     *
     * @throws IllegalArgumentException if a component is {@code null} or the value is blank
     * @throws ValidateException        if the status-code value is not an absolute URI
     */
    public StatusCode {
        value = Assert.notBlank(value, "SAML StatusCode Value must not be blank");
        Assert.notNull(nested, "SAML nested StatusCode container must not be null");
        nested = Optional.ofNullable(nested.getOrNull());
        try {
            if (!new URI(value).isAbsolute()) {
                throw new ValidateException("SAML StatusCode Value must be an absolute URI");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("SAML StatusCode Value is not a valid URI", exception);
        }
    }

}
