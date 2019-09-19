/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.consts;

/**
 * @author Kimi Liu
 * @version 3.5.1
 * @since JDK 1.8
 */
public class ModeType {

    /**
     * 默认的AES加密方式：AES/CBC/PKCS5Padding
     */
    public static final String AES = "AES";
    public static final String ARCFOUR = "ARCFOUR";
    public static final String Blowfish = "Blowfish";
    /**
     * 默认的DES加密方式：DES/ECB/PKCS5Padding
     */
    public static final String DES = "DES";
    /**
     * 3DES算法，默认实现为：DESede/CBC/PKCS5Padding
     */
    public static final String DESede = "DESede";
    public static final String RC2 = "RC2";
    /**
     * RSA算法
     */
    public static final String RSA = "RSA";
    /**
     * RSA算法，此算法用了默认补位方式为RSA/ECB/PKCS1Padding
     */
    public static final String RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";
    /**
     * RSA算法，此算法用了RSA/None/NoPadding
     */
    public static final String RSA_None = "RSA/None/NoPadding";
    /**
     * EC算法
     */
    public static final String EC = "EC";
    public static final String PBEWithMD5AndDES = "PBEWithMD5AndDES";
    public static final String PBEWithSHA1AndDESede = "PBEWithSHA1AndDESede";
    public static final String PBEWithSHA1AndRC2_40 = "PBEWithSHA1AndRC2_40";
    // The RSA signature algorithm
    public static final String NONEwithRSA = "NONEwithRSA";
    // The MD2/MD5 with RSA Encryption signature algorithm
    public static final String MD2withRSA = "MD2withRSA";
    public static final String MD5withRSA = "MD5withRSA";
    // The signature algorithm with SHA-* and the RSA
    public static final String SHA1withRSA = "SHA1withRSA";
    public static final String SHA256withRSA = "SHA256withRSA";
    public static final String SHA384withRSA = "SHA384withRSA";
    public static final String SHA512withRSA = "SHA512withRSA";
    // The Digital Signature Algorithm
    public static final String NONEwithDSA = "NONEwithDSA";
    // The DSA with SHA-1 signature algorithm
    public static final String SHA1withDSA = "SHA1withDSA";
    // The ECDSA signature algorithms
    public static final String NONEwithECDSA = "NONEwithECDSA";
    public static final String SHA1withECDSA = "SHA1withECDSA";
    public static final String SHA256withECDSA = "SHA256withECDSA";
    public static final String SHA384withECDSA = "SHA384withECDSA";
    public static final String SHA512withECDSA = "SHA512withECDSA";
    public static final String HmacMD5 = "HmacMD5";
    public static final String HmacSHA1 = "HmacSHA1";
    public static final String HmacSHA256 = "HmacSHA256";
    public static final String HmacSHA384 = "HmacSHA384";
    public static final String HmacSHA512 = "HmacSHA512";
    public static final String MD2 = "MD2";
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA384 = "SHA-384";
    public static final String SHA512 = "SHA-512";
    public static final String SHAPRNG = "SHA1PRNG";

}
