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
package org.miaixz.bus.auth.protocol.radius.packet;

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable RADIUS packet model.
 *
 * @param code          unsigned packet code
 * @param identifier    unsigned request identifier
 * @param authenticator exact sixteen-byte authenticator
 * @param attributes    ordered attributes
 * @author Kimi Liu
 */
public record RadiusPacket(int code, int identifier, byte[] authenticator, List<RadiusAttribute> attributes) {

    /**
     * Exact authenticator length.
     */
    public static final int AUTHENTICATOR_BYTES = Normal._16;

    /**
     * Fixed packet header length.
     */
    public static final int HEADER_BYTES = 20;

    /**
     * Validates and snapshots one packet.
     *
     * @param code          code
     * @param identifier    identifier
     * @param authenticator authenticator
     * @param attributes    attributes
     * @throws ValidateException if code, identifier, authenticator, or attributes are invalid
     */
    public RadiusPacket {
        Assert.isTrue(
                code > Normal._0 && code <= 255 && identifier >= Normal._0 && identifier <= 255,
                () -> new ValidateException("RADIUS packet code or identifier is invalid"));
        authenticator = Arrays.copyOf(
                Assert.notNull(authenticator, () -> new ValidateException("RADIUS authenticator must not be null")),
                authenticator.length);
        Assert.isTrue(
                authenticator.length == AUTHENTICATOR_BYTES,
                () -> new ValidateException("RADIUS authenticator length is invalid"));
        attributes = List
                .copyOf(Assert.notNull(attributes, () -> new ValidateException("RADIUS attributes must not be null")));
    }

    /**
     * Returns an independent authenticator copy.
     *
     * @return independent authenticator copy
     */
    @Override
    public byte[] authenticator() {
        return authenticator.clone();
    }

}
