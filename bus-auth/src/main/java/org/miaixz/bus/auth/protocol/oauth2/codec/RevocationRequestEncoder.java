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
package org.miaixz.bus.auth.protocol.oauth2.codec;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Assert;

/**
 * Encodes the standard RFC 7009 token revocation parameters independently of client authentication and transport.
 *
 * @author Kimi Liu
 */
public class RevocationRequestEncoder implements Encoder<RevocationRequest, List<NameValue>> {

    /**
     * Creates a stateless revocation request encoder.
     */
    public RevocationRequestEncoder() {
        // No initialization required.
    }

    /**
     * Encodes token and optional token type hint in their standard form order.
     *
     * @param data validated standard revocation request
     * @return immutable ordered form parameters
     * @throws IllegalArgumentException if data is {@code null}
     */
    @Override
    public List<NameValue> encode(final RevocationRequest data) {
        Assert.notNull(data, "OAuth 2.x revocation request must not be null");
        final List<NameValue> parameters = new ArrayList<>(2);
        parameters.add(new NameValue(OAuth2.Parameters.TOKEN, data.token()));
        data.tokenTypeHint()
                .ifPresent(value -> parameters.add(new NameValue(OAuth2.Parameters.TOKEN_TYPE_HINT, value)));
        return List.copyOf(parameters);
    }

}
