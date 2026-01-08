/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.crypto.builtin.digest;

import java.io.*;
import java.security.MessageDigest;
import java.security.Provider;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Holder;

/**
 * Abstract base class for digest algorithms. This class provides common functionality for computing message digests,
 * including support for salting and repeated hashing.
 * <p>
 * Note: Instances of this object are not thread-safe after instantiation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Digester extends SimpleWrapper<MessageDigest> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852288992512L;

    /**
     * The salt value used in the digest process.
     */
    protected byte[] salt;
    /**
     * The position at which the salt is inserted into the data before digestion. Defaults to 0 (beginning).
     */
    protected int saltPosition;
    /**
     * The number of times the digest algorithm is applied. If less than or equal to 1, it defaults to 1.
     */
    protected int digestCount;

    /**
     * Constructs a Digester with the specified algorithm.
     *
     * @param algorithm The {@link Algorithm} enumeration.
     */
    public Digester(final Algorithm algorithm) {
        this(algorithm.getValue());
    }

    /**
     * Constructs a Digester with the specified algorithm name.
     *
     * @param algorithm The algorithm name (e.g., "MD5", "SHA-256").
     */
    public Digester(final String algorithm) {
        this(algorithm, null);
    }

    /**
     * Constructs a Digester with the specified algorithm and security provider.
     *
     * @param algorithm The {@link Algorithm} enumeration.
     * @param provider  The {@link Provider} to use. If {@code null}, {@link Holder} is used to find one.
     */
    public Digester(final Algorithm algorithm, final Provider provider) {
        this(algorithm.getValue(), provider);
    }

    /**
     * Constructs a Digester with the specified algorithm name and security provider.
     *
     * @param algorithm The algorithm name.
     * @param provider  The {@link Provider} to use. If {@code null}, {@link Holder} is used to find one.
     */
    public Digester(final String algorithm, final Provider provider) {
        this(Builder.createMessageDigest(algorithm, provider));
    }

    /**
     * Constructs a Digester with an existing {@link MessageDigest} instance.
     *
     * @param messageDigest The {@link MessageDigest} instance.
     */
    public Digester(final MessageDigest messageDigest) {
        super(messageDigest);
    }

    /**
     * Sets the salt content for the digester.
     *
     * @param salt The salt value as a byte array.
     * @return This Digester instance.
     */
    public Digester setSalt(final byte[] salt) {
        this.salt = salt;
        return this;
    }

    /**
     * Sets the position at which the salt is inserted into the data. This is only effective if a salt is present. The
     * salt position refers to the index within the data byte array where the salt is inserted. For example:
     * 
     * <pre>
     * data: 0123456
     * </pre>
     * 
     * If {@code saltPosition = 2}, the salt is inserted between '1' and '2', resulting in:
     * 
     * <pre>
     * data: 01[salt]23456
     * </pre>
     *
     * @param saltPosition The position to insert the salt.
     * @return This Digester instance.
     */
    public Digester setSaltPosition(final int saltPosition) {
        this.saltPosition = saltPosition;
        return this;
    }

    /**
     * Sets the number of times the digest value should be computed (repeated hashing).
     *
     * @param digestCount The number of digest computations.
     * @return This Digester instance.
     */
    public Digester setDigestCount(final int digestCount) {
        this.digestCount = digestCount;
        return this;
    }

    /**
     * Resets the underlying {@link MessageDigest} to its initial state.
     *
     * @return This Digester instance.
     */
    public Digester reset() {
        this.raw.reset();
        return this;
    }

    /**
     * Generates a message digest for the given string data using the specified charset.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The message digest as a byte array.
     */
    public byte[] digest(final String data, final java.nio.charset.Charset charset) {
        return digest(ByteKit.toBytes(data, charset));
    }

    /**
     * Generates a message digest for the given string data using UTF-8 encoding.
     *
     * @param data The string data to be digested.
     * @return The message digest as a byte array.
     */
    public byte[] digest(final String data) {
        return digest(data, Charset.UTF_8);
    }

    /**
     * Generates a message digest for the given string data using the specified charset and returns it as a hexadecimal
     * string.
     *
     * @param data    The string data to be digested.
     * @param charset The character set to use for encoding the string.
     * @return The hexadecimal string representation of the message digest.
     */
    public String digestHex(final String data, final java.nio.charset.Charset charset) {
        return HexKit.encodeString(digest(data, charset));
    }

    /**
     * Generates a message digest for the given string data using UTF-8 encoding and returns it as a hexadecimal string.
     *
     * @param data The string data to be digested.
     * @return The hexadecimal string representation of the message digest.
     */
    public String digestHex(final String data) {
        return digestHex(data, Charset.UTF_8);
    }

    /**
     * Generates a message digest for the given file. Uses a default buffer size of {@link Normal#_8192}.
     *
     * @param file The file to be digested.
     * @return The message digest as a byte array.
     * @throws CryptoException if an I/O error occurs during file reading.
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
     * Generates a message digest for the given file and returns it as a hexadecimal string. Uses a default buffer size
     * of {@link Normal#_8192}.
     *
     * @param file The file to be digested.
     * @return The hexadecimal string representation of the message digest.
     * @throws CryptoException if an I/O error occurs during file reading.
     */
    public String digestHex(final File file) {
        return HexKit.encodeString(digest(file));
    }

    /**
     * Generates a message digest for the given byte array data, considering salt and repeated hashing.
     *
     * @param data The byte array data to be digested.
     * @return The message digest as a byte array.
     */
    public byte[] digest(final byte[] data) {
        final byte[] result;
        if (this.saltPosition <= 0) {
            // Salt at the beginning, automatically ignore empty salt
            result = doDigest(this.salt, data);
        } else if (this.saltPosition >= data.length) {
            // Salt at the end, automatically ignore empty salt
            result = doDigest(data, this.salt);
        } else if (ArrayKit.isNotEmpty(this.salt)) {
            final MessageDigest digest = this.raw;
            // Salt in the middle
            digest.update(data, 0, this.saltPosition);
            digest.update(this.salt);
            digest.update(data, this.saltPosition, data.length - this.saltPosition);
            result = digest.digest();
        } else {
            // No salt
            result = doDigest(data);
        }

        return resetAndRepeatDigest(result);
    }

    /**
     * Generates a message digest for the given byte array data and returns it as a hexadecimal string.
     *
     * @param data The byte array data to be digested.
     * @return The hexadecimal string representation of the message digest.
     */
    public String digestHex(final byte[] data) {
        return HexKit.encodeString(digest(data));
    }

    /**
     * Generates a message digest for the data from the given input stream. Uses a default buffer size of
     * {@link Normal#_8192}.
     *
     * @param data The {@link InputStream} containing the data to be digested.
     * @return The message digest as a byte array.
     * @throws InternalException if an I/O error occurs during stream reading.
     */
    public byte[] digest(final InputStream data) {
        return digest(data, Normal._8192);
    }

    /**
     * Generates a message digest for the data from the given input stream and returns it as a hexadecimal string. Uses
     * a default buffer size of {@link Normal#_8192}.
     *
     * @param data The {@link InputStream} containing the data to be digested.
     * @return The hexadecimal string representation of the message digest.
     * @throws InternalException if an I/O error occurs during stream reading.
     */
    public String digestHex(final InputStream data) {
        return HexKit.encodeString(digest(data));
    }

    /**
     * Generates a message digest for the data from the given input stream with a specified buffer length.
     *
     * @param data         The {@link InputStream} containing the data to be digested.
     * @param bufferLength The buffer length to use for reading the stream. If less than 1, {@link Normal#_8192} is used
     *                     as default.
     * @return The message digest as a byte array.
     * @throws InternalException if an I/O error occurs during stream reading.
     */
    public byte[] digest(final InputStream data, int bufferLength) throws InternalException {
        if (bufferLength < 1) {
            bufferLength = Normal._8192;
        }

        final byte[] result;
        try {
            if (ArrayKit.isEmpty(this.salt)) {
                result = digestWithoutSalt(data, bufferLength);
            } else {
                result = digestWithSalt(data, bufferLength);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return resetAndRepeatDigest(result);
    }

    /**
     * Generates a message digest for the data from the given input stream with a specified buffer length and returns it
     * as a hexadecimal string.
     *
     * @param data         The {@link InputStream} containing the data to be digested.
     * @param bufferLength The buffer length to use for reading the stream. If less than 1, {@link Normal#_8192} is used
     *                     as default.
     * @return The hexadecimal string representation of the message digest.
     * @throws InternalException if an I/O error occurs during stream reading.
     */
    public String digestHex(final InputStream data, final int bufferLength) {
        return HexKit.encodeString(digest(data, bufferLength));
    }

    /**
     * Retrieves the length of the digest in bytes.
     *
     * @return The digest length in bytes, or 0 if this method is not supported by the underlying {@link MessageDigest}.
     */
    public int getDigestLength() {
        return this.raw.getDigestLength();
    }

    /**
     * Generates a message digest for the data from the given input stream without using salt.
     *
     * @param data         The {@link InputStream} containing the data to be digested.
     * @param bufferLength The buffer length to use for reading the stream.
     * @return The message digest as a byte array.
     * @throws IOException if an I/O error occurs during stream reading.
     */
    private byte[] digestWithoutSalt(final InputStream data, final int bufferLength) throws IOException {
        final MessageDigest digest = this.raw;
        final byte[] buffer = new byte[bufferLength];
        int read;
        while ((read = data.read(buffer, 0, bufferLength)) > -1) {
            digest.update(buffer, 0, read);
        }
        return digest.digest();
    }

    /**
     * Generates a message digest for the data from the given input stream with salt. The salt is inserted at the
     * {@link #saltPosition}.
     *
     * @param data         The {@link InputStream} containing the data to be digested.
     * @param bufferLength The buffer length to use for reading the stream.
     * @return The message digest as a byte array.
     * @throws IOException if an I/O error occurs during stream reading.
     */
    private byte[] digestWithSalt(final InputStream data, final int bufferLength) throws IOException {
        final MessageDigest digest = this.raw;
        if (this.saltPosition <= 0) {
            // Salt at the beginning
            digest.update(this.salt);
        }

        final byte[] buffer = new byte[bufferLength];
        int total = 0;
        int read;
        while ((read = data.read(buffer, 0, bufferLength)) > -1) {
            total += read;
            if (this.saltPosition > 0 && total >= this.saltPosition) {
                if (total != this.saltPosition) {
                    digest.update(buffer, 0, total - this.saltPosition);
                }
                // Salt in the middle
                digest.update(this.salt);
                digest.update(buffer, total - this.saltPosition, read);
            } else {
                digest.update(buffer, 0, read);
            }
        }

        if (total < this.saltPosition) {
            // Salt at the end
            digest.update(this.salt);
        }

        return digest.digest();
    }

    /**
     * Performs the actual digest operation on one or more byte arrays.
     *
     * @param datas One or more byte arrays to be digested.
     * @return The message digest as a byte array.
     */
    private byte[] doDigest(final byte[]... datas) {
        final MessageDigest digest = this.raw;
        for (final byte[] data : datas) {
            if (null != data) {
                digest.update(data);
            }
        }
        return digest.digest();
    }

    /**
     * Resets the digester and repeats the digest operation {@link #digestCount} times. The digester is reset before
     * each repeated digest calculation.
     *
     * @param digestData The data that has been digested once.
     * @return The final message digest after repeated hashing.
     */
    private byte[] resetAndRepeatDigest(byte[] digestData) {
        final int digestCount = Math.max(1, this.digestCount);
        reset();
        for (int i = 0; i < digestCount - 1; i++) {
            digestData = doDigest(digestData);
            reset();
        }
        return digestData;
    }

}
