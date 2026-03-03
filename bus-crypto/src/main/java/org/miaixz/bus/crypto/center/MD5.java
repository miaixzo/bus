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
package org.miaixz.bus.crypto.center;

import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.builtin.digest.Digester;
import org.miaixz.bus.crypto.builtin.digest.DigesterFactory;

/**
 * MD5 (Message-Digest Algorithm 5) implementation. MD5 is a widely used cryptographic hash function that produces a
 * 128-bit (16-byte) hash value.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MD5 extends Digester {

    @Serial
    private static final long serialVersionUID = 2852290106196L;

    /**
     * The {@link DigesterFactory} for MD5, using the JDK's default provider for better initial performance. MD5
     * algorithm does not use the Bouncy Castle library by default in this implementation.
     */
    private static final DigesterFactory FACTORY = DigesterFactory.ofJdk(Algorithm.MD5.getValue());

    /**
     * Constructs an MD5 digester.
     */
    public MD5() {
        super(FACTORY.createMessageDigester());
    }

    /**
     * Constructs an MD5 digester with the specified salt.
     *
     * @param salt The salt value as a byte array.
     */
    public MD5(final byte[] salt) {
        this(salt, 0, 1);
    }

    /**
     * Constructs an MD5 digester with the specified salt and digest count.
     *
     * @param salt        The salt value as a byte array.
     * @param digestCount The number of times to apply the digest algorithm. If less than or equal to 1, it defaults to
     *                    1.
     */
    public MD5(final byte[] salt, final int digestCount) {
        this(salt, 0, digestCount);
    }

    /**
     * Constructs an MD5 digester with the specified salt, salt position, and digest count.
     *
     * @param salt         The salt value as a byte array.
     * @param saltPosition The index at which the salt string is placed in the data. Defaults to 0.
     * @param digestCount  The number of times to apply the digest algorithm. If less than or equal to 1, it defaults to
     *                     1.
     */
    public MD5(final byte[] salt, final int saltPosition, final int digestCount) {
        this();
        this.salt = salt;
        this.saltPosition = saltPosition;
        this.digestCount = digestCount;
    }

    /**
     * Creates a new MD5 instance.
     *
     * @return A new {@link MD5} instance.
     */
    public static MD5 of() {
        return new MD5();
    }

    /**
     * Generates a 16-bit MD5 digest for the given data and returns it as a hexadecimal string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The 16-bit MD5 digest as a hexadecimal string.
     */
    public String digestHex16(final String data, final Charset charset) {
        return Builder.md5HexTo16(digestHex(data, charset));
    }

    /**
     * Generates a 16-bit MD5 digest for the given data (UTF-8 encoded) and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The 16-bit MD5 digest as a hexadecimal string.
     */
    public String digestHex16(final String data) {
        return Builder.md5HexTo16(digestHex(data));
    }

    /**
     * Generates a 16-bit MD5 digest for the data from the given input stream and returns it as a hexadecimal string.
     *
     * @param data The input stream containing the data to be digested.
     * @return The 16-bit MD5 digest as a hexadecimal string.
     */
    public String digestHex16(final InputStream data) {
        return Builder.md5HexTo16(digestHex(data));
    }

    /**
     * Generates a 16-bit MD5 digest for the given file and returns it as a hexadecimal string.
     *
     * @param data The file to be digested.
     * @return The 16-bit MD5 digest as a hexadecimal string.
     */
    public String digestHex16(final File data) {
        return Builder.md5HexTo16(digestHex(data));
    }

    /**
     * Generates a 16-bit MD5 digest for the given byte array data and returns it as a hexadecimal string.
     *
     * @param data The byte array data to be digested.
     * @return The 16-bit MD5 digest as a hexadecimal string.
     */
    public String digestHex16(final byte[] data) {
        return Builder.md5HexTo16(digestHex(data));
    }

}
