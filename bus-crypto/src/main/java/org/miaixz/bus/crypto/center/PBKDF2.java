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

    /**
     * The algorithm to use for PBKDF2.
     */
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
