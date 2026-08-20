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
 * Represents successful RFC 5849 token credentials while suppressing credential material from diagnostics.
 *
 * @param oauthToken       token credential identifier
 * @param oauthTokenSecret token credential shared secret
 * @param extensions       ordered decoded extension parameters
 * @author Kimi Liu
 */
public record TokenCredentialsResponse(String oauthToken, String oauthTokenSecret, List<OAuth1Parameter> extensions) {

    /**
     * Token credential secret response parameter name.
     */
    private static final String TOKEN_SECRET = "oauth_token_secret";

    /**
     * Validates required credentials and extension separation.
     *
     * @throws IllegalArgumentException if text is blank or extensions contain {@code null}
     * @throws ValidateException        if extensions duplicate credential fields
     */
    public TokenCredentialsResponse {
        Assert.notBlank(oauthToken, "Token credential identifier must not be blank");
        Assert.notBlank(oauthTokenSecret, "Token credential secret must not be blank");
        extensions = OAuth1Parameter.immutable(extensions);
        if (OAuth1Parameter.contains(extensions, OAuth1.Parameters.TOKEN)
                || OAuth1Parameter.contains(extensions, TOKEN_SECRET)) {
            throw new ValidateException("Token credentials extensions must not duplicate credential fields");
        }
    }

    /**
     * Returns a fully redacted token credential representation.
     *
     * @return redacted response label
     */
    @Override
    public String toString() {
        return "TokenCredentialsResponse[oauthToken=[REDACTED], oauthTokenSecret=[REDACTED], extensions=[REDACTED]]";
    }

}
