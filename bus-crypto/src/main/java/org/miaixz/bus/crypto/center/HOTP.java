/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.crypto.center;

import org.miaixz.bus.core.codec.binary.Base32;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.RandomKit;

/**
 * HMAC-based one-time passwords (HOTP) generator. Specification: <a href="https://tools.ietf.org/html/rfc4226">RFC
 * 4226</a>
 *
 * <p>
 * It is based on event synchronization, using a specific event sequence and the same seed value as input to compute a
 * consistent password through a HASH algorithm.
 * </p>
 *
 * <p>
 * Reference: https://github.com/jchambers/java-otp
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HOTP {

    /**
     * Default password length.
     */
    public static final int DEFAULT_PASSWORD_LENGTH = 6;

    /**
     * Divisors for truncation.
     */
    private static final int[] MOD_DIVISORS = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };
    /**
     * The HMAC algorithm implementation.
     */
    private final HMac mac;
    /**
     * The length of the password.
     */
    private final int passwordLength;
    /**
     * The divisor for the truncation calculation.
     */
    private final int modDivisor;
    /**
     * Buffer for the counter.
     */
    private final byte[] buffer;

    /**
     * Constructor, using default password length and default HMAC algorithm (HmacSHA1).
     *
     * @param key Shared secret, RFC 4226 requires at least 128 bits.
     */
    public HOTP(final byte[] key) {
        this(DEFAULT_PASSWORD_LENGTH, key);
    }

    /**
     * Constructor, using default HMAC algorithm (HmacSHA1).
     *
     * @param passwordLength Password length, can be 6, 7, or 8.
     * @param key            Shared secret, RFC 4226 requires at least 128 bits.
     */
    public HOTP(final int passwordLength, final byte[] key) {
        this(passwordLength, Algorithm.HMACSHA1, key);
    }

    /**
     * Constructor.
     *
     * @param passwordLength Password length, can be 6, 7, or 8.
     * @param algorithm      HMAC algorithm enum.
     * @param key            Shared secret, RFC 4226 requires at least 128 bits.
     */
    public HOTP(final int passwordLength, final Algorithm algorithm, final byte[] key) {
        if (passwordLength >= MOD_DIVISORS.length) {
            throw new IllegalArgumentException("Password length must be < " + MOD_DIVISORS.length);
        }
        this.mac = new HMac(algorithm, key);
        this.modDivisor = MOD_DIVISORS[passwordLength];
        this.passwordLength = passwordLength;
        this.buffer = new byte[8];
    }

    /**
     * Generate a Base32 representation of a secret key.
     *
     * @param numBytes The number of seed bytes to generate.
     * @return The secret key.
     */
    public static String generateSecretKey(final int numBytes) {
        return Base32.encode(RandomKit.getSHA1PRNGRandom(RandomKit.randomBytes(256)).generateSeed(numBytes));
    }

    /**
     * Generate a one-time password.
     *
     * @param counter The value of the event counter, an 8-byte integer, called the moving factor. It can be a
     *                count-based or time-based moving factor.
     * @return The integer value of the one-time password.
     */
    public synchronized int generate(final long counter) {
        // The integer value of C needs to be expressed as a binary string. For example, if an event count is 3,
        // C is "11" (omitting the preceding binary zeros).
        this.buffer[0] = (byte) ((counter & 0xff00000000000000L) >>> 56);
        this.buffer[1] = (byte) ((counter & 0x00ff000000000000L) >>> 48);
        this.buffer[2] = (byte) ((counter & 0x0000ff0000000000L) >>> 40);
        this.buffer[3] = (byte) ((counter & 0x000000ff00000000L) >>> 32);
        this.buffer[4] = (byte) ((counter & 0x00000000ff000000L) >>> 24);
        this.buffer[5] = (byte) ((counter & 0x0000000000ff0000L) >>> 16);
        this.buffer[6] = (byte) ((counter & 0x000000000000ff00L) >>> 8);
        this.buffer[7] = (byte) (counter & 0x00000000000000ffL);

        final byte[] digest = this.mac.digest(this.buffer);

        return truncate(digest);
    }

    /**
     * Gets the password length, which can be 6, 7, or 8.
     *
     * @return The password length.
     */
    public int getPasswordLength() {
        return this.passwordLength;
    }

    /**
     * Gets the HMAC algorithm.
     *
     * @return The HMAC algorithm.
     */
    public String getAlgorithm() {
        return this.mac.getAlgorithm();
    }

    /**
     * Truncate.
     *
     * @param digest The HMAC hash value.
     * @return The truncated value.
     */
    private int truncate(final byte[] digest) {
        final int offset = digest[digest.length - 1] & 0x0f;
        return ((digest[offset] & 0x7f) << 24 | (digest[offset + 1] & 0xff) << 16 | (digest[offset + 2] & 0xff) << 8
                | (digest[offset + 3] & 0xff)) % this.modDivisor;
    }

}
