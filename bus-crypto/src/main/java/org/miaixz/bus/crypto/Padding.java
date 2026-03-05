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
package org.miaixz.bus.crypto;

/**
 * Enumeration for various padding schemes used in block ciphers. Padding is applied when the plaintext data length is
 * not a multiple of the block size, ensuring that the last block is filled to the required length.
 *
 * @author Kimi Liu
 * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher"> Cipher
 *      section in Java Cryptography Architecture Standard Algorithm Name Documentation</a>
 * @since Java 17+
 */
public enum Padding {
    /**
     * No padding is applied. The data must be an exact multiple of the block size.
     */
    NoPadding,
    /**
     * Zero-byte padding. The data is padded with zero bytes until it reaches the block length.
     */
    ZeroPadding,
    /**
     * ISO 10126-2 padding scheme for block ciphers, as described in the W3C's "XML Encryption Syntax and Processing"
     * document.
     */
    ISO10126Padding,
    /**
     * Optimal Asymmetric Encryption Padding (OAEP) scheme defined in PKCS#1.
     */
    OAEPPadding,
    /**
     * PKCS#1 padding scheme, typically used with the RSA algorithm.
     */
    PKCS1Padding,
    /**
     * PKCS#5 padding scheme, as described in RSA Laboratories, "PKCS #5: Password-Based Encryption Standard," version
     * 1.5.
     */
    PKCS5Padding,
    /**
     * PKCS#7 padding scheme, as described in RSA Laboratories, "PKCS #7: Password-Based Encryption Standard," version
     * 1.5.
     */
    PKCS7Padding,
    /**
     * SSL Protocol Version 3.0 padding scheme, as defined in section 5.2.3.2 (CBC block cipher).
     */
    SSL3Padding

}
