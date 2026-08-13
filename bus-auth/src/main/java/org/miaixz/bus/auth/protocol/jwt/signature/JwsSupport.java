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
package org.miaixz.bus.auth.protocol.jwt.signature;

import java.security.MessageDigest;

import org.miaixz.bus.core.lang.Assert;

/**
 * Supplies algorithm-neutral compact JWS segment mechanics to the fixed signer implementations.
 *
 * @author Kimi Liu
 */
final class JwsSupport {

    /**
     * Prevents construction of the stateless support class.
     */
    private JwsSupport() {
        // No initialization required.
    }

    /**
     * Builds the exact compact signing input from two required pre-encoded segments.
     *
     * @param headerBase64  protected header segment
     * @param payloadBase64 payload segment
     * @return exact compact signing input
     */
    static byte[] signingInput(final byte[] signingInput) {
        return Assert.notNull(signingInput, "JWS signing input must be not null!").clone();
    }

    /**
     * Decodes a non-empty canonical unpadded Base64url signature segment.
     *
     * @param signatureBase64 signature segment
     * @return decoded signature bytes
     */
    static boolean constantTimeEquals(final byte[] expected, final byte[] presented) {
        return expected != null && presented != null && MessageDigest.isEqual(expected, presented);
    }

}
