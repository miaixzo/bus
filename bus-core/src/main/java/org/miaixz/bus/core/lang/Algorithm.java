/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang;

/**
 * 加解密算法类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Algorithm {

    /****************************** 非对称-算法类型 *****************************/

    /**
     * RSA算法
     */
    RSA("RSA"),
    /**
     * RSA2算法
     */
    RSA2("RSA2"),
    /**
     * RSA算法，此算法用了默认补位方式为RSA/ECB/PKCS1Padding
     */
    RSA_ECB_PKCS1("RSA/ECB/PKCS1Padding"),
    /**
     * RSA算法，此算法用了默认补位方式为RSA/ECB/NoPadding
     */
    RSA_ECB("RSA/ECB/NoPadding"),
    /**
     * RSA算法，此算法用了RSA/None/NoPadding
     */
    EC("EC"),
    /**
     * ECDSA
     */
    ECDSA("ECDSA"),

    /***************************** 非对称-签名算法 *****************************/

    /**
     * RSA签名算法
     */
    NONEWITHRSA("NONEwithRSA"),
    /**
     * MD2/MD5带有RSA加密签名算法
     */
    MD2WITHRSA("MD2withRSA"),
    /**
     * MD5withRSA
     */
    MD5withRSA("MD5withRSA"),

    /**
     * 使用SHA-*和RSA的签名算法
     */
    SHA1WITHRSA("SHA1withRSA"),
    /**
     * SHA256withRSA
     */
    SHA256WITHRSA("SHA256withRSA"),
    /**
     * SHA384withRSA
     */
    SHA384WITHRSA("SHA384withRSA"),
    /**
     * SHA512withRSA
     */
    SHA512WITHRSA("SHA512withRSA"),

    /**
     * 数字签名算法
     */
    NONEWITHDSA("NONEwithDSA"),
    /**
     * 采用SHA-1签名算法的DSA
     */
    SHA1WITHDSA("SHA1withDSA"),
    /**
     * ECDSA签名算法
     */
    NONEWITHECDSA("NONEwithECDSA"),
    /**
     * SHA1withECDSA
     */
    SHA1WITHECDSA("SHA1withECDSA"),
    /**
     * SHA256withECDSA
     */
    SHA256WITHECDSA("SHA256withECDSA"),
    /**
     * SHA384withECDSA
     */
    SHA384WITHECDSA("SHA384withECDSA"),
    /**
     * SHA512withECDSA
     */
    SHA512WITHECDSA("SHA512withECDSA"),

    /**
     * SHA256WithRSA/PSS
     */
    SHA256WITHRSA_PSS("SHA256WithRSA/PSS"),
    /**
     * SHA384WithRSA/PSS
     */
    SHA384WITHRSA_PSS("SHA384WithRSA/PSS"),
    /**
     * SHA512WithRSA/PSS
     */
    SHA512WITHRSA_PSS("SHA512WithRSA/PSS"),

    /****************************** 摘要-算法类型 *****************************/

    /**
     * MD2
     */
    MD2("MD2"),
    /**
     * MD5
     */
    MD5("MD5"),
    /**
     * SHA-1
     */
    SHA1("SHA-1"),
    /**
     * SHA-256
     */
    SHA256("SHA-256"),
    /**
     * SHA-384
     */
    SHA384("SHA-384"),
    /**
     * SHA-512
     */
    SHA512("SHA-512"),
    /**
     * SHA1PRNG
     */
    SHA1PRNG("SHA1PRNG"),

    /***************************** 摘要-HMAC算法 *****************************/

    /**
     * HmacMD5
     */
    HMACMD5("HmacMD5"),
    /**
     * HmacSHA1
     */
    HMACSHA1("HmacSHA1"),
    /**
     * HmacSHA256
     */
    HMACSHA256("HmacSHA256"),
    /**
     * HmacSHA384
     */
    HMACSHA384("HmacSHA384"),
    /**
     * HmacSHA512
     */
    HMACSHA512("HmacSHA512"),
    /**
     * HmacSM3算法实现，需要BouncyCastle库支持
     */
    HMACSM3("HmacSM3"),
    /**
     * SM4 CMAC模式实现，需要BouncyCastle库支持
     */
    SM4CMAC("SM4CMAC"),

    /***************************** 对称-算法类型 *****************************/

    /**
     * 默认的AES加密方式：AES/ECB/PKCS5Padding
     */
    AES("AES"),
    /**
     * ARCFOUR
     */
    ARCFOUR("ARCFOUR"),
    /**
     * Blowfish
     */
    BLOWFISH("Blowfish"),
    /**
     * 默认的DES加密方式：DES/ECB/PKCS5Padding
     */
    DES("DES"),
    /**
     * 3DES算法，默认实现为：DESede/ECB/PKCS5Padding
     */
    DESEDE("DESede"),
    /**
     * 分组加密算法
     * RC2加密算法的执行速度是DES算法的两倍
     */
    RC2("RC2"),
    /**
     * 流加密算法，密钥长度可变
     */
    RC4("RC4"),

    /**
     * PBEWithMD5AndDES
     */
    PBEWITHMD5ANDDES("PBEWithMD5AndDES"),
    /**
     * PBEWithSHA1AndDESede
     */
    PBEWITHSHA1ANDDESEDE("PBEWithSHA1AndDESede"),
    /**
     * PBEWithSHA1AndRC2_40
     */
    PBEWITHSHA1ANDRC2_40("PBEWithSHA1AndRC2_40"),

    /******************************* 国密算法 *******************************/

    /**
     * 对称算法
     */
    SM1("SM1"),
    /**
     * 公钥密码算法
     */
    SM2("SM2"),
    /**
     * 主要用于数字签名及验证、消息认证码生成及验证、随机数生成等
     * 其安全性及效率与SHA-256相当
     */
    SM3("SM3"),
    /**
     * 迭代分组密码算法
     */
    SM4("SM4");

    private final String value;

    /**
     * 构造
     *
     * @param value 算法字符表示，区分大小写
     */
    Algorithm(String value) {
        this.value = value;
    }

    /**
     * 获取算法字符串表示，区分大小写
     *
     * @return 算法字符串表示
     */
    public String getValue() {
        return this.value;
    }

}
