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
package org.miaixz.bus.crypto;

/**
 * 补码方式 补码方式是在分组密码中，当明文长度不是分组长度的整数倍时，需要在最后一个分组中填充一些数据使其凑满一个分组的长度。
 *
 * @author Kimi Liu
 * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher">
 *      Cipher章节</a>
 * @since Java 17+
 */
public enum Padding {
    /**
     * 无补码
     */
    NoPadding,
    /**
     * 0补码，即不满block长度时使用0填充
     */
    ZeroPadding,
    /**
     * This padding for block ciphers is described in 5.2 Block Encryption Algorithms in the W3C's "XML Encryption
     * Syntax and Processing" document.
     */
    ISO10126Padding,
    /**
     * Optimal Asymmetric Encryption Padding scheme defined in PKCS1
     */
    OAEPPadding,
    /**
     * The padding scheme described in PKCS #1, used with the RSA algorithm
     */
    PKCS1Padding,
    /**
     * The padding scheme described in RSA Laboratories, "PKCS #5: Password-Based Encryption Standard," version 1.5,
     * November 1993.
     */
    PKCS5Padding,
    /**
     * The padding scheme described in RSA Laboratories, "PKCS #7: Password-Based Encryption Standard," version 1.5,
     * November 1993.
     */
    PKCS7Padding,
    /**
     * The padding scheme defined in the SSL Protocol Version 3.0, November 18, 1996, section 5.2.3.2 (CBC block cipher)
     */
    SSL3Padding
}
