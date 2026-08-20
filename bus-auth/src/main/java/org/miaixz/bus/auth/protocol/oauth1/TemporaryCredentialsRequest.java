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

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an RFC 5849 temporary credentials request before client signing.
 *
 * @param parameters ordered decoded request parameters containing exactly one {@code oauth_callback}
 * @author Kimi Liu
 */
public record TemporaryCredentialsRequest(List<OAuth1Parameter> parameters) {

    /**
     * Freezes parameters and enforces temporary-credential request ownership rules.
     *
     * @throws IllegalArgumentException if the list or an entry is {@code null}
     * @throws ValidateException        if callback cardinality is not one or token/signature is pre-populated
     */
    public TemporaryCredentialsRequest {
        parameters = OAuth1Parameter.immutable(parameters);
        if (OAuth1Parameter.count(parameters, OAuth1.Parameters.CALLBACK) != 1) {
            throw new ValidateException("Temporary credentials request requires exactly one oauth_callback");
        }
        if (OAuth1Parameter.contains(parameters, OAuth1.Parameters.TOKEN)
                || OAuth1Parameter.contains(parameters, OAuth1.Parameters.SIGNATURE)) {
            throw new ValidateException(
                    "Temporary credentials request must not pre-populate oauth_token or oauth_signature");
        }
    }

}
