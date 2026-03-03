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

import java.io.*;
import java.security.MessageDigest;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * MAC (Message Authentication Code) digest algorithm. This class is compatible with JCE's {@code javax.crypto.Mac} and
 * Bouncy Castle's {@code org.bouncycastle.crypto.Mac} objects. MAC, which stands for "Message Authentication Code",
 * primarily uses a specified algorithm to generate a message digest from a key and a message as input. Generally, a MAC
 * is used to verify messages transmitted between two parties that share a secret key. Note: This object is not
 * thread-safe after instantiation!
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mac implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852290282995L;

    /**
     * The Mac engine.
     */
    private final org.miaixz.bus.crypto.builtin.digest.mac.Mac engine;

    /**
     * Constructor.
     *
     * @param engine The MAC algorithm implementation engine.
     */
    public Mac(final org.miaixz.bus.crypto.builtin.digest.mac.Mac engine) {
        this.engine = engine;
    }

    /**
     * Gets the MAC algorithm engine.
     *
     * @return The MAC algorithm engine.
     */
    public org.miaixz.bus.crypto.builtin.digest.mac.Mac getEngine() {
        return this.engine;
    }

    /**
     * Generates a digest.
     *
     * @param data    The data to be digested.
     * @param charset The charset.
     * @return The digest.
     */
    public byte[] digest(final String data, final java.nio.charset.Charset charset) {
        return digest(ByteKit.toBytes(data, charset));
    }

    /**
     * Generates a digest.
     *
     * @param data The data to be digested.
     * @return The digest.
     */
    public byte[] digest(final String data) {
        return digest(data, Charset.UTF_8);
    }

    /**
     * Generates a digest and converts it to Base64.
     *
     * @param data      The data to be digested.
     * @param isUrlSafe Whether to use URL-safe characters.
     * @return The digest.
     */
    public String digestBase64(final String data, final boolean isUrlSafe) {
        return digestBase64(data, Charset.UTF_8, isUrlSafe);
    }

    /**
     * Generates a digest and converts it to Base64.
     *
     * @param data      The data to be digested.
     * @param charset   The charset.
     * @param isUrlSafe Whether to use URL-safe characters.
     * @return The digest.
     */
    public String digestBase64(final String data, final java.nio.charset.Charset charset, final boolean isUrlSafe) {
        final byte[] digest = digest(data, charset);
        return isUrlSafe ? Base64.encodeUrlSafe(digest) : Base64.encode(digest);
    }

    /**
     * Generates a digest and converts it to a hex string.
     *
     * @param data    The data to be digested.
     * @param charset The charset.
     * @return The digest.
     */
    public String digestHex(final String data, final java.nio.charset.Charset charset) {
        return HexKit.encodeString(digest(data, charset));
    }

    /**
     * Generates a digest.
     *
     * @param data The data to be digested.
     * @return The digest.
     */
    public String digestHex(final String data) {
        return digestHex(data, Charset.UTF_8);
    }

    /**
     * Generates a file digest using the default buffer size, see {@link Normal#_8192}.
     *
     * @param file The file to be digested.
     * @return The digest as a byte array.
     * @throws CryptoException Caused by IOException.
     */
    public byte[] digest(final File file) throws CryptoException {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return digest(in);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Generates a file digest and converts it to a hex string using the default buffer size, see {@link Normal#_8192}.
     *
     * @param file The file to be digested.
     * @return The digest.
     */
    public String digestHex(final File file) {
        return HexKit.encodeString(digest(file));
    }

    /**
     * Generates a digest.
     *
     * @param data The data in bytes.
     * @return The digest as a byte array.
     */
    public byte[] digest(final byte[] data) {
        return digest(new ByteArrayInputStream(data), -1);
    }

    /**
     * Generates a digest and converts it to a hex string.
     *
     * @param data The data to be digested.
     * @return The digest.
     */
    public String digestHex(final byte[] data) {
        return HexKit.encodeString(digest(data));
    }

    /**
     * Generates a digest using the default buffer size, see {@link Normal#_8192}.
     *
     * @param data The {@link InputStream} data stream.
     * @return The digest as a byte array.
     */
    public byte[] digest(final InputStream data) {
        return digest(data, Normal._8192);
    }

    /**
     * Generates a digest and converts it to a hex string using the default buffer size, see {@link Normal#_8192}.
     *
     * @param data The data to be digested.
     * @return The digest.
     */
    public String digestHex(final InputStream data) {
        return HexKit.encodeString(digest(data));
    }

    /**
     * Generates a digest.
     *
     * @param data         The {@link InputStream} data stream.
     * @param bufferLength The buffer length. If less than 1, {@link Normal#_8192} will be used as the default.
     * @return The digest as a byte array.
     */
    public byte[] digest(final InputStream data, final int bufferLength) {
        return this.engine.digest(data, bufferLength);
    }

    /**
     * Generates a digest and converts it to a hex string using the default buffer size, see {@link Normal#_8192}.
     *
     * @param data         The data to be digested.
     * @param bufferLength The buffer length. If less than 1, {@link Normal#_8192} will be used as the default.
     * @return The digest.
     */
    public String digestHex(final InputStream data, final int bufferLength) {
        return HexKit.encodeString(digest(data, bufferLength));
    }

    /**
     * Verifies if the generated digest matches the given digest. Simply compares each byte.
     *
     * @param digest          The generated digest.
     * @param digestToCompare The digest to compare against.
     * @return Whether they are equal.
     * @see MessageDigest#isEqual(byte[], byte[])
     */
    public boolean verify(final byte[] digest, final byte[] digestToCompare) {
        return MessageDigest.isEqual(digest, digestToCompare);
    }

    /**
     * Gets the MAC algorithm block length.
     *
     * @return The MAC algorithm block length.
     */
    public int getMacLength() {
        return this.engine.getMacLength();
    }

    /**
     * Gets the algorithm.
     *
     * @return The algorithm.
     */
    public String getAlgorithm() {
        return this.engine.getAlgorithm();
    }

}
