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

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.crypto.Keeper;

/**
 * PBKDF2 (Password-Based Key Derivation Function 2) applies a pseudorandom function to derive a key. In simple terms,
 * PBKDF2 is the repeated calculation of a salted hash. Reference:
 * https://blog.csdn.net/huoji555/article/details/83659687
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PBKDF2 {

    private String algorithm = Algorithm.PBKDF2WITHHMACSHA1.getValue();

    /**
     * The length of the generated key.
     */
    private int keyLength = 512;

    /**
     * The number of iterations.
     */
    private int iterationCount = 1000;

    /**
     * Constructor with default algorithm PBKDF2WithHmacSHA1, key length 512, and 1000 iterations.
     */
    public PBKDF2() {

    }

    /**
     * Constructor.
     *
     * @param algorithm      The algorithm, generally PBKDF2WithXXX.
     * @param keyLength      The length of the key to generate, default is 512.
     * @param iterationCount The number of iterations, default is 1000.
     */
    public PBKDF2(final String algorithm, final int keyLength, final int iterationCount) {
        this.algorithm = algorithm;
        this.keyLength = keyLength;
        this.iterationCount = iterationCount;
    }

    /**
     * Encrypts a password.
     *
     * @param password The password.
     * @param salt     The salt.
     * @return The encrypted password.
     */
    public byte[] encrypt(final char[] password, final byte[] salt) {
        final PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        final SecretKey secretKey = Keeper.generateKey(algorithm, pbeKeySpec);
        return secretKey.getEncoded();
    }

    /**
     * Encrypts a password and returns it as a hex string.
     *
     * @param password The password.
     * @param salt     The salt.
     * @return The encrypted password as a hex string.
     */
    public String encryptHex(final char[] password, final byte[] salt) {
        return HexKit.encodeString(encrypt(password, salt));
    }

}
