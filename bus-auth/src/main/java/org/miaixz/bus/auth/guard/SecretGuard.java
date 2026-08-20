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
package org.miaixz.bus.auth.guard;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Validates and compares short-lived secret character material without creating immutable secret strings.
 * <p>
 * Length limits apply to the strict UTF-8 representation used at external protocol boundaries. Temporary byte arrays
 * exist only for the duration of validation or constant-time comparison and are overwritten before the method returns.
 * The caller remains responsible for closing its SecretLease or invoking {@link #clear(char[])} on owned material.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SecretGuard {

    /**
     * Creates a stateless secret-material guard.
     */
    public SecretGuard() {
        // No initialization required.
    }

    /**
     * Strictly encodes secret characters as an exactly sized UTF-8 byte array.
     *
     * @param secret secret character material
     * @return exact encoded bytes owned by the caller
     * @throws ValidateException if malformed Unicode input cannot be encoded
     */
    private static byte[] encode(final char[] secret) {
        try {
            final ByteBuffer buffer = Charset.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(secret));
            final byte[] encoded = new byte[buffer.remaining()];
            buffer.get(encoded);
            if (buffer.hasArray()) {
                Arrays.fill(buffer.array(), (byte) 0);
            }
            return encoded;
        } catch (CharacterCodingException cause) {
            throw new ValidateException("Secret material is not valid UTF-8 input", cause);
        }
    }

    /**
     * Validates strict UTF-8 encoding and inclusive encoded length limits.
     *
     * @param secret       short-lived secret character material
     * @param minimumBytes minimum permitted UTF-8 byte length, inclusive
     * @param maximumBytes maximum permitted UTF-8 byte length, inclusive
     * @throws IllegalArgumentException if {@code secret} is {@code null}
     * @throws ValidateException        if bounds are invalid, encoding fails, or the byte length is outside the bounds
     */
    public void validate(final char[] secret, final int minimumBytes, final int maximumBytes) {
        Assert.notNull(secret, "Secret material must not be null");
        if (minimumBytes < 0 || maximumBytes < minimumBytes) {
            throw new ValidateException("Secret byte-length bounds are invalid");
        }
        final byte[] encoded = encode(secret);
        try {
            if (encoded.length < minimumBytes || encoded.length > maximumBytes) {
                throw new ValidateException("Secret UTF-8 byte length is outside the permitted bounds");
            }
        } finally {
            Arrays.fill(encoded, (byte) 0);
        }
    }

    /**
     * Compares two strict UTF-8 secret encodings using the JCA constant-time digest comparison primitive.
     *
     * @param expected  expected secret material
     * @param presented presented secret material
     * @return {@code true} when the encoded values are equal
     * @throws IllegalArgumentException if either array is {@code null}
     * @throws ValidateException        if either array contains malformed Unicode input
     */
    public boolean matches(final char[] expected, final char[] presented) {
        Assert.notNull(expected, "Expected secret material must not be null");
        Assert.notNull(presented, "Presented secret material must not be null");
        final byte[] expectedBytes = encode(expected);
        byte[] presentedBytes = null;
        try {
            presentedBytes = encode(presented);
            return MessageDigest.isEqual(expectedBytes, presentedBytes);
        } finally {
            Arrays.fill(expectedBytes, (byte) 0);
            if (presentedBytes != null) {
                Arrays.fill(presentedBytes, (byte) 0);
            }
        }
    }

    /**
     * Overwrites caller-owned secret character material in place.
     *
     * @param secret secret material to erase
     * @throws IllegalArgumentException if {@code secret} is {@code null}
     */
    public void clear(final char[] secret) {
        Assert.notNull(secret, "Secret material must not be null");
        Arrays.fill(secret, '\0');
    }

}
