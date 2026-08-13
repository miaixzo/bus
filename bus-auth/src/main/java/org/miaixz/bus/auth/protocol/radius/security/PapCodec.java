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
package org.miaixz.bus.auth.protocol.radius.security;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Encodes and decodes RFC 2865 User-Password blocks while clearing temporary secret and password bytes.
 *
 * @author Kimi Liu
 */
public final class PapCodec {

    /**
     * PAP block size.
     */
    public static final int BLOCK_BYTES = Normal._16;

    /**
     * Maximum clear password bytes.
     */
    public static final int MAXIMUM_PASSWORD_BYTES = 128;

    /**
     * Prevents construction.
     */
    private PapCodec() {
        // No initialization required.
    }

    /**
     * Encrypts one password.
     *
     * @param password             clear password
     * @param secret               shared secret
     * @param requestAuthenticator request authenticator
     * @return encrypted blocks
     * @throws ValidateException if inputs, UTF-8 length, shared secret, or authenticator are invalid
     */
    public static byte[] encode(final char[] password, final char[] secret, final byte[] requestAuthenticator) {
        final byte[] clear = utf8(password, "RADIUS PAP password");
        final byte[] key = utf8(secret, "RADIUS shared secret");
        byte[] padded = null;
        try {
            Assert.isTrue(
                    clear.length <= MAXIMUM_PASSWORD_BYTES,
                    () -> new ValidateException("RADIUS PAP password exceeds its byte limit"));
            Assert.isTrue(key.length > Normal._0, () -> new ValidateException("RADIUS shared secret is empty"));
            final int size = Math
                    .max(BLOCK_BYTES, ((clear.length + BLOCK_BYTES - Normal._1) / BLOCK_BYTES) * BLOCK_BYTES);
            padded = Arrays.copyOf(clear, size);
            return transform(padded, key, requestAuthenticator, true);
        } finally {
            Arrays.fill(clear, (byte) Normal._0);
            Arrays.fill(key, (byte) Normal._0);
            if (padded != null) {
                Arrays.fill(padded, (byte) Normal._0);
            }
        }
    }

    /**
     * Decrypts one password.
     *
     * @param encrypted            encrypted blocks
     * @param secret               shared secret
     * @param requestAuthenticator request authenticator
     * @return decoded password characters
     * @throws ValidateException if inputs, encrypted length, shared secret, authenticator, or UTF-8 is invalid
     */
    public static char[] decode(final byte[] encrypted, final char[] secret, final byte[] requestAuthenticator) {
        final byte[] source = Arrays.copyOf(
                Assert.notNull(encrypted, () -> new ValidateException("RADIUS PAP value must not be null")),
                encrypted.length);
        final byte[] key = utf8(secret, "RADIUS shared secret");
        try {
            Assert.isTrue(key.length > Normal._0, () -> new ValidateException("RADIUS shared secret is empty"));
            Assert.isTrue(
                    source.length >= BLOCK_BYTES && source.length <= MAXIMUM_PASSWORD_BYTES
                            && source.length % BLOCK_BYTES == Normal._0,
                    () -> new ValidateException("RADIUS PAP encrypted length is invalid"));
            final byte[] clear = transform(source, key, requestAuthenticator, false);
            try {
                int length = clear.length;
                while (length > Normal._0 && clear[length - Normal._1] == Normal._0) {
                    length--;
                }
                try {
                    return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT)
                            .decode(ByteBuffer.wrap(clear, Normal._0, length)).toString().toCharArray();
                } catch (final CharacterCodingException failure) {
                    throw new ValidateException("RADIUS PAP password is not valid UTF-8", failure);
                }
            } finally {
                Arrays.fill(clear, (byte) Normal._0);
            }
        } finally {
            Arrays.fill(source, (byte) Normal._0);
            Arrays.fill(key, (byte) Normal._0);
        }
    }

    /**
     * Applies the chained PAP XOR transform.
     *
     * @param input         source blocks
     * @param secret        secret bytes
     * @param authenticator request authenticator
     * @param encrypt       encryption direction
     * @return transformed blocks
     * @throws ValidateException if the authenticator is not exactly sixteen bytes
     */
    static byte[] transform(
            final byte[] input,
            final byte[] secret,
            final byte[] authenticator,
            final boolean encrypt) {
        Assert.isTrue(
                authenticator != null && authenticator.length == BLOCK_BYTES,
                () -> new ValidateException("RADIUS PAP authenticator length is invalid"));
        final byte[] result = new byte[input.length];
        byte[] previous = authenticator.clone();
        for (int offset = Normal._0; offset < input.length; offset += BLOCK_BYTES) {
            final byte[] material = new byte[secret.length + previous.length];
            System.arraycopy(secret, Normal._0, material, Normal._0, secret.length);
            System.arraycopy(previous, Normal._0, material, secret.length, previous.length);
            final byte[] digest = Builder.md5(material);
            Arrays.fill(material, (byte) Normal._0);
            for (int index = Normal._0; index < BLOCK_BYTES; index++) {
                result[offset + index] = (byte) (input[offset + index] ^ digest[index]);
            }
            Arrays.fill(digest, (byte) Normal._0);
            final byte[] next = Arrays.copyOfRange(encrypt ? result : input, offset, offset + BLOCK_BYTES);
            Arrays.fill(previous, (byte) Normal._0);
            previous = next;
        }
        Arrays.fill(previous, (byte) Normal._0);
        return result;
    }

    /**
     * Encodes copied characters to UTF-8 bytes.
     *
     * @param value characters
     * @param name  name
     * @return bytes
     * @throws ValidateException if the character array is {@code null}
     */
    static byte[] utf8(final char[] value, final String name) {
        final char[] copy = Arrays
                .copyOf(Assert.notNull(value, () -> new ValidateException(name + " must not be null")), value.length);
        try {
            final ByteBuffer bytes = StandardCharsets.UTF_8.encode(CharBuffer.wrap(copy));
            final byte[] result = new byte[bytes.remaining()];
            bytes.get(result);
            return result;
        } finally {
            Arrays.fill(copy, (char) Normal._0);
        }
    }

}
