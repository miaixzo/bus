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
package org.miaixz.bus.auth.protocol.oauth1;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents the RFC 5849 resource owner authorization redirect input for one temporary credential.
 *
 * @param oauthToken temporary credential identifier
 * @param parameters ordered decoded authorization endpoint extension parameters
 * @author Kimi Liu
 */
public record ResourceOwnerAuthorizationRequest(String oauthToken, List<OAuth1Parameter> parameters) {

    /**
     * Validates token ownership and freezes non-duplicating extension parameters.
     *
     * @throws IllegalArgumentException if token is blank or the list contains {@code null}
     * @throws ValidateException        if parameters repeat the dedicated token field
     */
    public ResourceOwnerAuthorizationRequest {
        Assert.notBlank(oauthToken, "Resource owner authorization temporary credential must not be blank");
        parameters = OAuth1Parameter.immutable(parameters);
        if (OAuth1Parameter.contains(parameters, OAuth1.Parameters.TOKEN)) {
            throw new ValidateException("Resource owner authorization parameters must not duplicate oauth_token");
        }
    }

    /**
     * Returns a redacted redirect-input representation.
     *
     * @return redacted request label
     */
    @Override
    public String toString() {
        return "ResourceOwnerAuthorizationRequest[oauthToken=[REDACTED], parameters=[REDACTED]]";
    }

}
