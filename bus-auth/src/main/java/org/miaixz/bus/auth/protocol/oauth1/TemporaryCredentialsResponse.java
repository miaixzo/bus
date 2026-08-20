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
 * Represents a successful RFC 5849 temporary credentials response while redacting credential material in diagnostics.
 *
 * @param oauthToken        temporary credential identifier
 * @param oauthTokenSecret  temporary credential shared secret
 * @param callbackConfirmed whether the server confirmed callback ownership
 * @param extensions        ordered decoded extension parameters
 * @author Kimi Liu
 */
public record TemporaryCredentialsResponse(String oauthToken, String oauthTokenSecret, boolean callbackConfirmed,
        List<OAuth1Parameter> extensions) {

    /**
     * Validates required credentials, callback confirmation, and extension separation.
     *
     * @throws IllegalArgumentException if text is blank or extensions contain {@code null}
     * @throws ValidateException        if callback was not confirmed or an extension duplicates a standard field
     */
    public TemporaryCredentialsResponse {
        Assert.notBlank(oauthToken, "Temporary credential identifier must not be blank");
        Assert.notBlank(oauthTokenSecret, "Temporary credential secret must not be blank");
        if (!callbackConfirmed) {
            throw new ValidateException("Temporary credentials response must confirm the callback");
        }
        extensions = OAuth1Parameter.immutable(extensions);
        rejectReserved(extensions);
    }

    /**
     * Rejects extension members represented by dedicated components.
     *
     * @param values extension parameters
     */
    private static void rejectReserved(final List<OAuth1Parameter> values) {
        for (OAuth1Parameter value : values) {
            if (OAuth1.Parameters.TOKEN.equals(value.name()) || OAuth1.Parameters.TOKEN_SECRET.equals(value.name())
                    || OAuth1.Parameters.CALLBACK_CONFIRMED.equals(value.name())) {
                throw new ValidateException(
                        "Temporary credentials extensions must not duplicate standard response fields");
            }
        }
    }

    /**
     * Returns a fully redacted credential representation.
     *
     * @return redacted response label
     */
    @Override
    public String toString() {
        return "TemporaryCredentialsResponse[oauthToken=[REDACTED], oauthTokenSecret=[REDACTED], callbackConfirmed=true, extensions=[REDACTED]]";
    }

}
