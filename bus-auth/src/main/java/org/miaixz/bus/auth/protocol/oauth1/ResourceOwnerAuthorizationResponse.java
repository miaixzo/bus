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
 * Represents an RFC 5849 callback containing the authorized temporary credential and verification code.
 *
 * @param oauthToken    authorized temporary credential identifier
 * @param oauthVerifier resource owner verification code
 * @param extensions    ordered decoded callback extension parameters
 * @author Kimi Liu
 */
public record ResourceOwnerAuthorizationResponse(String oauthToken, String oauthVerifier,
        List<OAuth1Parameter> extensions) {

    /**
     * Validates callback fields and prevents extension duplication.
     *
     * @throws IllegalArgumentException if text is blank or extensions contain {@code null}
     * @throws ValidateException        if extensions duplicate token or verifier
     */
    public ResourceOwnerAuthorizationResponse {
        Assert.notBlank(oauthToken, "Authorized temporary credential must not be blank");
        Assert.notBlank(oauthVerifier, "OAuth verifier must not be blank");
        extensions = OAuth1Parameter.immutable(extensions);
        if (OAuth1Parameter.contains(extensions, OAuth1.Parameters.TOKEN)
                || OAuth1Parameter.contains(extensions, OAuth1.Parameters.VERIFIER)) {
            throw new ValidateException(
                    "Authorization callback extensions must not duplicate oauth_token or oauth_verifier");
        }
    }

    /**
     * Returns a fully redacted callback representation.
     *
     * @return redacted response label
     */
    @Override
    public String toString() {
        return "ResourceOwnerAuthorizationResponse[oauthToken=[REDACTED], oauthVerifier=[REDACTED], extensions=[REDACTED]]";
    }

}
