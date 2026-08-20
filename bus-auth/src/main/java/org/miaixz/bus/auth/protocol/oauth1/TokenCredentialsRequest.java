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
 * Represents an RFC 5849 token credentials request before the client adds its signature parameters.
 *
 * @param oauthToken    authorized temporary credential identifier
 * @param oauthVerifier resource owner verification code
 * @param parameters    ordered decoded extension parameters
 * @author Kimi Liu
 */
public record TokenCredentialsRequest(String oauthToken, String oauthVerifier, List<OAuth1Parameter> parameters) {

    /**
     * Validates dedicated fields and rejects pre-populated owned signature parameters.
     *
     * @throws IllegalArgumentException if text is blank or parameters contain {@code null}
     * @throws ValidateException        if parameters duplicate token/verifier or pre-populate signature
     */
    public TokenCredentialsRequest {
        Assert.notBlank(oauthToken, "Token credentials temporary credential must not be blank");
        Assert.notBlank(oauthVerifier, "Token credentials verifier must not be blank");
        parameters = OAuth1Parameter.immutable(parameters);
        if (OAuth1Parameter.contains(parameters, OAuth1.Parameters.TOKEN)
                || OAuth1Parameter.contains(parameters, OAuth1.Parameters.VERIFIER)
                || OAuth1Parameter.contains(parameters, OAuth1.Parameters.SIGNATURE)) {
            throw new ValidateException("Token credentials parameters must not duplicate owned OAuth fields");
        }
    }

    /**
     * Returns a fully redacted credential-exchange representation.
     *
     * @return redacted request label
     */
    @Override
    public String toString() {
        return "TokenCredentialsRequest[oauthToken=[REDACTED], oauthVerifier=[REDACTED], parameters=[REDACTED]]";
    }

}
