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
package org.miaixz.bus.auth.protocol.oidc;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an OpenID Connect UserInfo request authenticated by an OAuth bearer access token.
 * <p>
 * The codec places the sensitive token in the HTTP Authorization header. This bearer-token request model does not carry
 * a DPoP proof and therefore does not expose token-type selection.
 * </p>
 *
 * @param accessToken sensitive bearer access token authorizing the UserInfo request
 * @author Kimi Liu
 */
public record UserInfoRequest(String accessToken) {

    /**
     * Creates an immutable UserInfo request.
     *
     * @throws IllegalArgumentException if {@code accessToken} is {@code null} or blank
     * @throws ValidateException        if the token contains a control character
     */
    public UserInfoRequest {
        accessToken = Assert.notBlank(accessToken, "OpenID Connect UserInfo access token must not be blank");
        for (int index = 0; index < accessToken.length(); index++) {
            if (Character.isISOControl(accessToken.charAt(index))) {
                throw new ValidateException("OpenID Connect UserInfo access token must not contain control characters");
            }
        }
    }

    /**
     * Returns a fixed diagnostic representation without the bearer token.
     *
     * @return redacted UserInfo request label
     */
    @Override
    public String toString() {
        return "UserInfoRequest[accessToken=[REDACTED]]";
    }

}
