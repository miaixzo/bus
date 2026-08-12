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
package org.miaixz.bus.auth.metric.radius.security;

import java.security.MessageDigest;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Creates and verifies CHAP-Password responses.
 */
public final class ChapCodec {

    /**
     * Prevents construction.
     */
    private ChapCodec() {
        // No initialization required.
    }

    /**
     * Creates one CHAP response.
     *
     * @param identifier CHAP identifier
     * @param password   clear password
     * @param challenge  challenge bytes
     * @return sixteen-byte response
     */
    public static byte[] create(final int identifier, final char[] password, final byte[] challenge) {
        Assert.isTrue(
                identifier >= Normal._0 && identifier <= 255,
                () -> new ValidateException("RADIUS CHAP identifier is invalid"));
        final byte[] secret = PapCodec.utf8(password, "RADIUS CHAP password");
        final byte[] nonce = Arrays.copyOf(
                Assert.notNull(challenge, () -> new ValidateException("RADIUS CHAP challenge must not be null")),
                challenge.length);
        try {
            final byte[] material = new byte[Normal._1 + secret.length + nonce.length];
            material[Normal._0] = (byte) identifier;
            System.arraycopy(secret, Normal._0, material, Normal._1, secret.length);
            System.arraycopy(nonce, Normal._0, material, Normal._1 + secret.length, nonce.length);
            try {
                return Builder.md5(material);
            } finally {
                Arrays.fill(material, (byte) Normal._0);
            }
        } finally {
            Arrays.fill(secret, (byte) Normal._0);
            Arrays.fill(nonce, (byte) Normal._0);
        }
    }

    /**
     * Verifies one CHAP response in constant time.
     *
     * @param identifier identifier
     * @param password   password
     * @param challenge  challenge
     * @param response   received response
     * @return whether valid
     */
    public static boolean verify(
            final int identifier,
            final char[] password,
            final byte[] challenge,
            final byte[] response) {
        return response != null && MessageDigest.isEqual(create(identifier, password, challenge), response);
    }

}
