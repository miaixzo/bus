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
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML assertion {@code AudienceRestrictionType}.
 *
 * @param audiences one or more audience URI values
 * @author Kimi Liu
 */
public record AudienceRestriction(List<String> audiences) {

    /**
     * Requires a non-empty immutable list of absolute audience URIs.
     *
     * @throws IllegalArgumentException if the list or an item is {@code null}
     * @throws ValidateException        if the list is empty or an audience is not an absolute URI
     */
    public AudienceRestriction {
        Assert.notNull(audiences, "SAML AudienceRestriction audience list must not be null");
        if (audiences.isEmpty()) {
            throw new ValidateException("SAML AudienceRestriction requires at least one Audience");
        }
        for (String audience : audiences) {
            final String value = Assert.notBlank(audience, "SAML Audience must not be blank");
            try {
                if (!new URI(value).isAbsolute()) {
                    throw new ValidateException("SAML Audience must be an absolute URI");
                }
            } catch (URISyntaxException exception) {
                throw new ValidateException("SAML Audience is not a valid URI", exception);
            }
        }
        audiences = List.copyOf(audiences);
    }

}
