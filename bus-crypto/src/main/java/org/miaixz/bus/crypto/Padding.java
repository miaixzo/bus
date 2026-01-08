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
