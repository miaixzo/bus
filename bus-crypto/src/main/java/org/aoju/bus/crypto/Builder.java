/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.crypto;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.instance.Instances;
import org.aoju.bus.core.lang.*;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.crypto.asymmetric.RSA;
import org.aoju.bus.crypto.asymmetric.SM2;
import org.aoju.bus.crypto.asymmetric.Sign;
import org.aoju.bus.crypto.digest.*;
import org.aoju.bus.crypto.digest.mac.BCHMacEngine;
import org.aoju.bus.crypto.digest.mac.MacEngine;
import org.aoju.bus.crypto.symmetric.*;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.System;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.*;
import java.util.Map;

/**
 * 加密解密模块，实现了对JDK中加密解密算法的封装
 * 安全相关工具／密钥工具类
 * 加密分为三种：
 * 1、对称加密(symmetric)，例如：AES、DES等
 * 2、非对称加密(asymmetric)，例如：RSA、DSA等
 * 3、摘要加密(digest)，例如：MD5、SHA-1、SHA-256、HMAC等
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public final class Builder {

    /**
     * Java密钥库(Java Key Store，JKS)KEY_STORE
     */
    public static final String KEY_TYPE_JKS = "JKS";

    /**
     * PKCS12是公钥加密标准，它规定了可包含所有私钥、公钥和证书。其以二进制格式存储，也称为 PFX 文件
     */
    public static final String KEY_TYPE_PKCS12 = "pkcs12";
    /**
     * Certification类型：X.509
     */
    public static final String CERT_TYPE_X509 = "X.509";

    /**
     * 默认密钥字节数
     *
     * <pre>
     * RSA/DSA
     * Default Keysize 1024
     * Keysize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * </pre>
     */
    public static final int DEFAULT_KEY_SIZE = 1024;
    /**
     * SM2默认曲线
     *
     * <pre>
     * Default SM2 curve
     * </pre>
     */
    public static final String SM2_DEFAULT_CURVE = "sm2p256v1";
    public static final ECDomainParameters SM2_DOMAIN_PARAMS = toDomainParams(GMNamedCurves.getByName(SM2_DEFAULT_CURVE));
    private final static int RS_LEN = 32;

    /**
     * 数据加密
     *
     * @param algorithm 加密算法
     * @param key       密钥, 字符串使用,分割
     *                  格式: 私钥,公钥,类型
     * @param content   需要加密的内容
     * @return 加密结果
     */
    public static byte[] encrypt(String algorithm, String key, byte[] content) {
        final Provider provider = Registry.require(algorithm);
        return provider.encrypt(key, content);
    }

    /**
     * 数据加密
     *
     * @param algorithm 解密算法
     * @param key       密钥, 字符串使用,分割
     *                  格式: 私钥,公钥,类型
     * @param content   需要加密的内容
     * @param charset   字符集
     * @return 加密结果
     */
    public static String encrypt(String algorithm, String key, String content, java.nio.charset.Charset charset) {
        return HexKit.encodeHexStr(encrypt(algorithm, key, content.getBytes(charset)));
    }

    /**
     * 数据加密
     *
     * @param algorithm   加密算法
     * @param key         密钥, 字符串使用,分割
     *                    格式: 私钥,公钥,类型
     * @param inputStream 需要加密的内容
     * @return 加密结果
     */
    public static InputStream encrypt(String algorithm, String key, InputStream inputStream) {
        final Provider provider = Registry.require(algorithm);
        return new ByteArrayInputStream(provider.encrypt(key, IoKit.readBytes(inputStream)));
    }

    /**
     * 数据解密
     *
     * @param algorithm 加密算法
     * @param key       密钥, 字符串使用,分割
     *                  格式: 私钥,公钥,类型
     * @param content   需要解密的内容
     * @return 解密结果
     */
    public static byte[] decrypt(String algorithm, String key, byte[] content) {
        return Registry.require(algorithm).decrypt(key, content);
    }

    /**
     * 数据解密
     *
     * @param algorithm 解密算法
     * @param key       密钥, 字符串使用,分割
     *                  格式: 私钥,公钥,类型
     * @param content   需要解密的内容
     * @param charset   字符集
     * @return 解密结果
     */
    public static String decrypt(String algorithm, String key, String content, java.nio.charset.Charset charset) {
        return new String(decrypt(algorithm, key, HexKit.decodeHex(content)), charset);
    }

    /**
     * 数据解密
     *
     * @param algorithm   解密算法
     * @param key         密钥, 字符串使用,分割
     *                    格式: 私钥,公钥,类型
     * @param inputStream 需要解密的内容
     * @return 解密结果
     */
    public static InputStream decrypt(String algorithm, String key, InputStream inputStream) {
        return new ByteArrayInputStream(Registry.require(algorithm).decrypt(key, IoKit.readBytes(inputStream)));
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法，支持PBE算法
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm) {
        return generateKey(algorithm, -1);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, int keySize) {
        return generateKey(algorithm, keySize, null);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法
     * @param key       密钥，如果为{@code null} 自动生成随机密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, byte[] key) {
        Assert.notBlank(algorithm, "Algorithm is blank!");
        SecretKey secretKey;
        if (algorithm.startsWith("PBE")) {
            // PBE密钥
            secretKey = generatePBEKey(algorithm, (null == key) ? null : StringKit.toString(key).toCharArray());
        } else if (algorithm.startsWith(Algorithm.DES)) {
            // DES密钥
            secretKey = generateDESKey(algorithm, key);
        } else {
            // 其它算法密钥
            secretKey = (null == key) ? generateKey(algorithm) : new SecretKeySpec(key, algorithm);
        }
        return secretKey;
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成<br>
     * 当指定keySize&lt;0时，AES默认长度为128，其它算法不指定。
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度，&lt;0表示不设定密钥长度，即使用默认长度
     * @param random    随机数生成器，null表示默认
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, int keySize, SecureRandom random) {
        algorithm = getMainAlgorithm(algorithm);

        final KeyGenerator keyGenerator = getKeyGenerator(algorithm);
        if (keySize <= 0 && Algorithm.AES.equals(algorithm)) {
            // 对于AES的密钥，除非指定，否则强制使用128位
            keySize = 128;
        }

        if (keySize > 0) {
            if (null == random) {
                keyGenerator.init(keySize);
            } else {
                keyGenerator.init(keySize, random);
            }
        }
        return keyGenerator.generateKey();
    }

    /**
     * 生成 {@link SecretKey}
     *
     * @param algorithm DES算法，包括DES、DESede等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateDESKey(String algorithm, byte[] key) {
        if (StringKit.isBlank(algorithm) || false == algorithm.startsWith(Algorithm.DES)) {
            throw new InstrumentException("Algorithm [{}] is not a DES algorithm!");
        }

        SecretKey secretKey;
        if (null == key) {
            secretKey = generateKey(algorithm);
        } else {
            KeySpec keySpec;
            try {
                if (algorithm.startsWith(Algorithm.DESede)) {
                    // DESede兼容
                    keySpec = new DESedeKeySpec(key);
                } else {
                    keySpec = new DESKeySpec(key);
                }
            } catch (InvalidKeyException e) {
                throw new InstrumentException(e);
            }
            secretKey = generateKey(algorithm, keySpec);
        }
        return secretKey;
    }

    /**
     * 生成PBE {@link SecretKey}
     *
     * @param algorithm PBE算法，包括：PBEWithMD5AndDES、PBEWithSHA1AndDESede、PBEWithSHA1AndRC2_40等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generatePBEKey(String algorithm, char[] key) {
        if (StringKit.isBlank(algorithm) || false == algorithm.startsWith("PBE")) {
            throw new InstrumentException("Algorithm [{}] is not a PBE algorithm!");
        }

        if (null == key) {
            key = RandomKit.randomString(32).toCharArray();
        }
        PBEKeySpec keySpec = new PBEKeySpec(key);
        return generateKey(algorithm, keySpec);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, KeySpec keySpec) {
        final SecretKeyFactory keyFactory = getSecretKeyFactory(algorithm);
        try {
            return keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 生成RSA私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param key 密钥，必须为DER编码存储
     * @return RSA私钥 {@link PrivateKey}
     */
    public static PrivateKey generateRSAPrivateKey(byte[] key) {
        return generatePrivateKey(Algorithm.RSA, key);
    }

    /**
     * 生成私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param algorithm 算法
     * @param key       密钥，必须为DER编码存储
     * @return 私钥 {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(String algorithm, byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePrivateKey(algorithm, new PKCS8EncodedKeySpec(key));
    }

    /**
     * 生成私钥，仅用于非对称加密
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return 私钥 {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(String algorithm, KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        algorithm = getAlgorithmAfterWith(algorithm);
        try {
            return getKeyFactory(algorithm).generatePrivate(keySpec);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 生成私钥，仅用于非对称加密
     *
     * @param keyStore {@link KeyStore}
     * @param alias    别名
     * @param password 密码
     * @return 私钥 {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(KeyStore keyStore, String alias, char[] password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 生成公钥，仅用于非对称加密
     * 采用X509证书规范
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param algorithm 算法
     * @param key       密钥，必须为DER编码存储
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generatePublicKey(String algorithm, byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePublicKey(algorithm, new X509EncodedKeySpec(key));
    }

    /**
     * 生成公钥，仅用于非对称加密
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generatePublicKey(String algorithm, KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        algorithm = getAlgorithmAfterWith(algorithm);
        try {
            return getKeyFactory(algorithm).generatePublic(keySpec);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 生成RSA公钥，仅用于非对称加密
     * 采用X509证书规范
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param key 密钥，必须为DER编码存储
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generateRSAPublicKey(byte[] key) {
        return generatePublicKey(Algorithm.RSA, key);
    }

    /**
     * 生成用于非对称加密的公钥和私钥，仅用于非对称加密
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm) {
        return generateKeyPair(algorithm, DEFAULT_KEY_SIZE);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模(modulus )长度
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize) {
        return generateKeyPair(algorithm, keySize, null);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模(modulus )长度
     * @param seed      种子
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize, byte[] seed) {
        // SM2算法需要单独定义其曲线生成
        if (Algorithm.SM2.equalsIgnoreCase(algorithm)) {
            final ECGenParameterSpec sm2p256v1 = new ECGenParameterSpec(SM2_DEFAULT_CURVE);
            return generateKeyPair(algorithm, keySize, seed, sm2p256v1);
        }
        return generateKeyPair(algorithm, keySize, seed, (AlgorithmParameterSpec[]) null);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @param params    {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, AlgorithmParameterSpec params) {
        return generateKeyPair(algorithm, null, params);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @param param     {@link AlgorithmParameterSpec}
     * @param seed      种子
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, byte[] seed, AlgorithmParameterSpec param) {
        return generateKeyPair(algorithm, DEFAULT_KEY_SIZE, seed, param);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * <p>
     * 对于非对称加密算法，密钥长度有严格限制，具体如下：
     *
     * <p>
     * <b>RSA：</b>
     * <pre>
     * RS256、PS256：2048 bits
     * RS384、PS384：3072 bits
     * RS512、RS512：4096 bits
     * </pre>
     *
     * <p>
     * <b>EC(Elliptic Curve)：</b>
     * <pre>
     * EC256：256 bits
     * EC384：384 bits
     * EC512：512 bits
     * </pre>
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模(modulus )长度(单位bit)
     * @param seed      种子
     * @param params    {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize, byte[] seed, AlgorithmParameterSpec... params) {
        return generateKeyPair(algorithm, keySize, RandomKit.getSecureRandom(seed), params);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * <p>
     * 对于非对称加密算法，密钥长度有严格限制，具体如下：
     *
     * <p>
     * <b>RSA：</b>
     * <pre>
     * RS256、PS256：2048 bits
     * RS384、PS384：3072 bits
     * RS512、RS512：4096 bits
     * </pre>
     *
     * <p>
     * <b>EC(Elliptic Curve)：</b>
     * <pre>
     * EC256：256 bits
     * EC384：384 bits
     * EC512：512 bits
     * </pre>
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模(modulus )长度(单位bit)
     * @param random    {@link SecureRandom} 对象，创建时可选传入seed
     * @param params    {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize, SecureRandom random, AlgorithmParameterSpec... params) {
        algorithm = getAlgorithmAfterWith(algorithm);
        final KeyPairGenerator keyPairGen = getKeyPairGenerator(algorithm);

        // 密钥模(modulus )长度初始化定义
        if (keySize > 0) {
            // key长度适配修正
            if (Algorithm.EC.equalsIgnoreCase(algorithm) && keySize > 256) {
                // 对于EC(EllipticCurve)算法，密钥长度有限制，在此使用默认256
                keySize = 256;
            }
            if (null != random) {
                keyPairGen.initialize(keySize, random);
            } else {
                keyPairGen.initialize(keySize);
            }
        }

        // 自定义初始化参数
        if (ArrayKit.isNotEmpty(params)) {
            for (AlgorithmParameterSpec param : params) {
                if (null == param) {
                    continue;
                }
                try {
                    if (null != random) {
                        keyPairGen.initialize(param, random);
                    } else {
                        keyPairGen.initialize(param);
                    }
                } catch (InvalidAlgorithmParameterException e) {
                    throw new InstrumentException(e);
                }
            }
        }
        return keyPairGen.generateKeyPair();
    }

    /**
     * 获取用于密钥生成的算法
     * 获取XXXwithXXX算法的后半部分算法，如果为ECDSA或SM2，返回算法为EC
     *
     * @param algorithm XXXwithXXX算法
     * @return 算法
     */
    public static String getAlgorithmAfterWith(String algorithm) {
        Assert.notNull(algorithm, "algorithm must be not null !");
        int indexOfWith = StringKit.lastIndexOfIgnoreCase(algorithm, "with");
        if (indexOfWith > 0) {
            algorithm = StringKit.subSuf(algorithm, indexOfWith + "with".length());
        }
        if (Algorithm.ECDSA.equalsIgnoreCase(algorithm)
                || Algorithm.SM2.equalsIgnoreCase(algorithm)) {
            algorithm = Algorithm.EC;
        }
        return algorithm;
    }

    /**
     * 生成算法，格式为XXXwithXXX
     *
     * @param asymmetric 非对称算法
     * @param digest     摘要算法
     * @return 算法
     */
    public static String generateAlgorithm(String asymmetric, Algorithm digest) {
        return StringKit.format("{}with{}", (null == digest) ? "NONE" : digest, asymmetric);
    }

    /**
     * 生成签名对象，仅用于非对称加密
     *
     * @param asymmetric {@link Algorithm} 非对称加密算法
     * @param digest     {@link Algorithm} 摘要算法
     * @return {@link Signature}
     */
    public static Signature generateSignature(String asymmetric, Algorithm digest) {
        try {
            return Signature.getInstance(generateAlgorithm(asymmetric, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readJKSKeyStore(InputStream in, char[] password) {
        return readKeyStore(KEY_TYPE_JKS, in, password);
    }

    /**
     * 读取KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @param password 密码，null表示无密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(String type, InputStream in, char[] password) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(type);
            keyStore.load(in, password);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        return keyStore;
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in       {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     */
    public static Certificate readX509Certificate(InputStream in, char[] password, String alias) {
        return readCertificate(CERT_TYPE_X509, in, password, alias);
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @return {@link KeyStore}
     */
    public static Certificate readX509Certificate(InputStream in) {
        return readCertificate(CERT_TYPE_X509, in);
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型，例如X.509
     * @param in       {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     */
    public static Certificate readCertificate(String type, InputStream in, char[] password, String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type 类型，例如X.509
     * @param in   {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @return {@link Certificate}
     */
    public static Certificate readCertificate(String type, InputStream in) {
        try {
            return getCertificateFactory(type).generateCertificate(in);
        } catch (CertificateException e) {
            throw new InstrumentException(e);
        }
    }


    /**
     * 获取{@link KeyGenerator}
     *
     * @param algorithm 对称加密算法
     * @return {@link KeyGenerator}
     */
    public static KeyGenerator getKeyGenerator(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        KeyGenerator generator;
        try {
            generator = (null == provider)
                    ? KeyGenerator.getInstance(getMainAlgorithm(algorithm))
                    : KeyGenerator.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }
        return generator;
    }


    /**
     * 获取{@link KeyFactory}
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyFactory}
     */
    public static KeyFactory getKeyFactory(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        KeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? KeyFactory.getInstance(getMainAlgorithm(algorithm))
                    : KeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }
        return keyFactory;
    }

    /**
     * 获取{@link KeyPairGenerator}
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyPairGenerator}
     */
    public static KeyPairGenerator getKeyPairGenerator(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = (null == provider)
                    ? KeyPairGenerator.getInstance(getMainAlgorithm(algorithm))
                    : KeyPairGenerator.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }
        return keyPairGen;
    }

    /**
     * 获取{@link SecretKeyFactory}
     *
     * @param algorithm 对称加密算法
     * @return {@link KeyFactory}
     */
    public static SecretKeyFactory getSecretKeyFactory(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        SecretKeyFactory keyFactory;
        try {
            keyFactory = (null == provider)
                    ? SecretKeyFactory.getInstance(getMainAlgorithm(algorithm))
                    : SecretKeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }
        return keyFactory;
    }

    /**
     * 获取主体算法名，例如RSA/ECB/PKCS1Padding的主体算法是RSA
     *
     * @param algorithm XXXwithXXX算法
     * @return 主体算法名
     */
    public static String getMainAlgorithm(String algorithm) {
        final int slashIndex = algorithm.indexOf(Symbol.SLASH);
        if (slashIndex > 0) {
            return algorithm.substring(0, slashIndex);
        }
        return algorithm;
    }

    /**
     * 获得 Certification
     *
     * @param keyStore {@link KeyStore}
     * @param alias    别名
     * @return {@link Certificate}
     */
    public static Certificate getCertificate(KeyStore keyStore, String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取{@link CertificateFactory}
     *
     * @param type 类型，例如X.509
     * @return {@link KeyPairGenerator}
     */
    public static CertificateFactory getCertificateFactory(String type) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        CertificateFactory factory;
        try {
            factory = (null == provider) ? CertificateFactory.getInstance(type) : CertificateFactory.getInstance(type, provider);
        } catch (CertificateException e) {
            throw new InstrumentException(e);
        }
        return factory;
    }

    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param keyFile  证书文件
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readJKSKeyStore(File keyFile, char[] password) {
        return readKeyStore(KEY_TYPE_JKS, keyFile, password);
    }


    /**
     * 读取PKCS12 KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param keyFile  证书文件
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readPKCS12KeyStore(File keyFile, char[] password) {
        return readKeyStore(KEY_TYPE_PKCS12, keyFile, password);
    }

    /**
     * 读取PKCS12 KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readPKCS12KeyStore(InputStream in, char[] password) {
        return readKeyStore(KEY_TYPE_PKCS12, in, password);
    }

    /**
     * 读取KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型
     * @param keyFile  证书文件
     * @param password 密码，null表示无密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(String type, File keyFile, char[] password) {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(keyFile);
            return readKeyStore(type, in, password);
        } finally {
            IoKit.close(in);
        }
    }


    /**
     * 从KeyStore中获取私钥公钥
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyPair}
     */
    public static KeyPair getKeyPair(String type, InputStream in, char[] password, String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        return getKeyPair(keyStore, password, alias);
    }

    /**
     * 从KeyStore中获取私钥公钥
     *
     * @param keyStore {@link KeyStore}
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyPair}
     */
    public static KeyPair getKeyPair(KeyStore keyStore, char[] password, String alias) {
        PublicKey publicKey;
        PrivateKey privateKey;
        try {
            publicKey = keyStore.getCertificate(alias).getPublicKey();
            privateKey = (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        return new KeyPair(publicKey, privateKey);
    }


    /**
     * 读取X.509 Certification文件中的公钥
     * Certification为证书文件
     * see: https://www.cnblogs.com/yinliang/p/10115519.html
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @return {@link KeyStore}
     */
    public static PublicKey readPublicKeyFromCert(InputStream in) {
        final Certificate certificate = readX509Certificate(in);
        if (null != certificate) {
            return certificate.getPublicKey();
        }
        return null;
    }

    /**
     * AES加密，生成随机KEY。注意解密时必须使用相同 {@link AES}对象或者使用相同KEY
     * 例：
     *
     * <pre>
     * AES加密：aes().encrypt(data)
     * AES解密：aes().decrypt(data)
     * </pre>
     *
     * @return {@link AES}
     */
    public static AES aes() {
        return new AES();
    }

    /**
     * AES加密
     * 例：
     *
     * <pre>
     * AES加密：aes(key).encrypt(data)
     * AES解密：aes(key).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link Symmetric}
     */
    public static AES aes(byte[] key) {
        return new AES(key);
    }

    /**
     * DES加密，生成随机KEY。注意解密时必须使用相同 {@link DES}对象或者使用相同KEY
     * 例：
     *
     * <pre>
     * DES加密：des().encrypt(data)
     * DES解密：des().decrypt(data)
     * </pre>
     *
     * @return {@link DES}
     */
    public static DES des() {
        return new DES();
    }

    /**
     * DES加密
     * 例：
     *
     * <pre>
     * DES加密：des(key).encrypt(data)
     * DES解密：des(key).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link DES}
     */
    public static DES des(byte[] key) {
        return new DES(key);
    }

    /**
     * DESede加密(又名3DES、TripleDES)，生成随机KEY。注意解密时必须使用相同 {@link DESede}对象或者使用相同KEY
     * Java中默认实现为：DESede/ECB/PKCS5Padding
     * 例：
     *
     * <pre>
     * DESede加密：desede().encrypt(data)
     * DESede解密：desede().decrypt(data)
     * </pre>
     *
     * @return {@link DESede}
     */
    public static DESede desede() {
        return new DESede();
    }

    /**
     * DESede加密(又名3DES、TripleDES)
     * Java中默认实现为：DESede/ECB/PKCS5Padding
     * 例：
     *
     * <pre>
     * DESede加密：desede(key).encrypt(data)
     * DESede解密：desede(key).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link DESede}
     */
    public static DESede desede(byte[] key) {
        return new DESede(key);
    }

    /**
     * MD5加密
     * 例：
     *
     * <pre>
     * MD5加密：md5().digest(data)
     * MD5加密并转为16进制字符串：md5().digestHex(data)
     * </pre>
     *
     * @return {@link Digester}
     */
    public static MD5 md5() {
        return new MD5();
    }

    /**
     * 计算32位MD5摘要值
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] md5(byte[] data) {
        return new MD5().digest(data);
    }

    /**
     * 计算32位MD5摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要
     */
    public static byte[] md5(String data, String charset) {
        return new MD5().digest(data, charset);
    }

    /**
     * 计算32位MD5摘要值，使用UTF-8编码
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] md5(String data) {
        return md5(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算32位MD5摘要值
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] md5(InputStream data) {
        return new MD5().digest(data);
    }

    /**
     * 计算32位MD5摘要值
     *
     * @param file 被摘要文件
     * @return MD5摘要
     */
    public static byte[] md5(File file) {
        return new MD5().digest(file);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(byte[] data) {
        return new MD5().digestHex(data);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(String data, String charset) {
        return new MD5().digestHex(data, charset);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(String data, java.nio.charset.Charset charset) {
        return new MD5().digestHex(data, charset);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(String data) {
        return md5Hex(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(InputStream data) {
        return new MD5().digestHex(data);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(File file) {
        return new MD5().digestHex(file);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(byte[] data) {
        return new MD5().digestHex16(data);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(String data, java.nio.charset.Charset charset) {
        return new MD5().digestHex16(data, charset);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(String data) {
        return md5Hex16(data, Charset.UTF_8);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(InputStream data) {
        return new MD5().digestHex16(data);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(File file) {
        return new MD5().digestHex16(file);
    }

    /**
     * 32位MD5转16位MD5
     *
     * @param md5Hex 32位MD5
     * @return 16位MD5
     */
    public static String md5HexTo16(String md5Hex) {
        return md5Hex.substring(8, 24);
    }

    /**
     * SHA1加密
     * 例：
     * SHA1加密：sha1().digest(data)
     * SHA1加密并转为16进制字符串：sha1().digestHex(data)
     *
     * @return {@link Digester}
     */
    public static Digester sha1() {
        return new Digester(Algorithm.SHA1);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data 被摘要数据
     * @return SHA-1摘要
     */
    public static byte[] sha1(byte[] data) {
        return new Digester(Algorithm.SHA1).digest(data);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-1摘要
     */
    public static byte[] sha1(String data, String charset) {
        return new Digester(Algorithm.SHA1).digest(data, charset);
    }

    /**
     * 计算sha1摘要值，使用UTF-8编码
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] sha1(String data) {
        return sha1(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data 被摘要数据
     * @return SHA-1摘要
     */
    public static byte[] sha1(InputStream data) {
        return new Digester(Algorithm.SHA1).digest(data);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param file 被摘要文件
     * @return SHA-1摘要
     */
    public static byte[] sha1(File file) {
        return new Digester(Algorithm.SHA1).digest(file);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(byte[] data) {
        return new Digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(String data, String charset) {
        return new Digester(Algorithm.SHA1).digestHex(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(String data) {
        return sha1Hex(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(InputStream data) {
        return new Digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(File file) {
        return new Digester(Algorithm.SHA1).digestHex(file);
    }

    /**
     * SHA256加密
     * 例：
     * SHA256加密：sha256().digest(data)
     * SHA256加密并转为16进制字符串：sha256().digestHex(data)
     *
     * @return {@link Digester}
     */
    public static Digester sha256() {
        return new Digester(Algorithm.SHA256);
    }

    /**
     * SHA256加密，生成16进制SHA256字符串
     *
     * @param data 数据
     * @return SHA256字符串
     */
    public static String sha256(String data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * SHA256加密，生成16进制SHA256字符串
     *
     * @param data 数据
     * @return SHA1字符串
     */
    public static String sha256(InputStream data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * SHA256加密文件，生成16进制SHA256字符串
     *
     * @param dataFile 被加密文件
     * @return SHA256字符串
     */
    public static String sha256(File dataFile) {
        return new Digester(Algorithm.SHA256).digestHex(dataFile);
    }


    /**
     * 计算SHA-256摘要值
     *
     * @param data 被摘要数据
     * @return SHA-256摘要
     */
    public static byte[] sha256(byte[] data) {
        return new Digester(Algorithm.SHA256).digest(data);
    }

    /**
     * 计算SHA-256摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-256摘要
     */
    public static byte[] sha256(String data, String charset) {
        return new Digester(Algorithm.SHA256).digest(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(byte[] data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(String data, String charset) {
        return new Digester(Algorithm.SHA256).digestHex(data, charset);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(String data) {
        return sha256Hex(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(InputStream data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(File file) {
        return new Digester(Algorithm.SHA256).digestHex(file);
    }


    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(String algorithm, String key) {
        return new HMac(algorithm, StringKit.bytes(key));
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(String algorithm, byte[] key) {
        return new HMac(algorithm, key);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥{@link SecretKey}，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(String algorithm, SecretKey key) {
        return new HMac(algorithm, key);
    }

    /**
     * HmacMD5加密器
     * 例：
     * HmacMD5加密：hmacMd5(key).digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5(key).digestHex(data)
     *
     * @param key 加密密钥,如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(String key) {
        return hmacMd5(StringKit.bytes(key));
    }

    /**
     * HmacMD5加密器
     * 例：
     * HmacMD5加密：hmacMd5(key).digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5(key).digestHex(data)
     *
     * @param key 加密密钥,如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(byte[] key) {
        return new HMac(Algorithm.HmacMD5, key);
    }

    /**
     * HmacMD5加密器,生成随机KEY
     * 例：
     * HmacMD5加密：hmacMd5().digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacMd5() {
        return new HMac(Algorithm.HmacMD5);
    }

    /**
     * 新建摘要器
     *
     * @param algorithm 签名算法
     * @return Digester
     */
    public static Digester digester(String algorithm) {
        return new Digester(algorithm);
    }

    /**
     * 生成Bcrypt加密后的密文
     *
     * @param password 明文密码
     * @return 加密后的密文
     */
    public static String hashpw(String password) {
        return new BCrypt().hashpw(password);
    }

    /**
     * 验证密码是否与Bcrypt加密后的密文匹配
     *
     * @param password 明文密码
     * @param hashed   hash值(加密后的值)
     * @return 是否匹配
     */
    public static boolean checkpw(String password, String hashed) {
        return new BCrypt().checkpw(password, hashed);
    }

    /**
     * HmacSHA1加密器
     * 例：
     * HmacSHA1加密：hmacSha1(key).digest(data)
     * HmacSHA1加密并转为16进制字符串：hmacSha1(key).digestHex(data)
     *
     * @param key 加密密钥,如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha1(String key) {
        return hmacSha1(StringKit.bytes(key));
    }

    /**
     * HmacSHA1加密器
     * 例：
     * HmacSHA1加密：hmacSha1(key).digest(data)
     * HmacSHA1加密并转为16进制字符串：hmacSha1(key).digestHex(data)
     *
     * @param key 加密密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha1(byte[] key) {
        return new HMac(Algorithm.HmacSHA1, key);
    }

    /**
     * HmacSHA1加密器，生成随机KEY
     * 例：
     * HmacSHA1加密：hmacSha1().digest(data)
     * HmacSHA1加密并转为16进制字符串：hmacSha1().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacSha1() {
        return new HMac(Algorithm.HmacSHA1);
    }

    /**
     * 创建RSA算法对象
     * 生成新的私钥公钥对
     *
     * @return {@link RSA}
     */
    public static RSA rsa() {
        return new RSA();
    }

    /**
     * 创建RSA算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥Base64
     * @param publicKey  公钥Base64
     * @return {@link RSA}
     */
    public static RSA rsa(String privateKey, String publicKey) {
        return new RSA(privateKey, publicKey);
    }

    /**
     * 创建RSA算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link RSA}
     */
    public static RSA rsa(byte[] privateKey, byte[] publicKey) {
        return new RSA(privateKey, publicKey);
    }

    /**
     * 创建签名算法对象
     * 生成新的私钥公钥对
     *
     * @param algorithm 签名算法
     * @return {@link Sign}
     */
    public static Sign sign(String algorithm) {
        return new Sign(algorithm);
    }

    /**
     * 创建签名算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  签名算法
     * @param privateKey 私钥Base64
     * @param publicKey  公钥Base64
     * @return {@link Sign}
     */
    public static Sign sign(String algorithm, String privateKey, String publicKey) {
        return new Sign(algorithm, privateKey, publicKey);
    }

    /**
     * 创建Sign算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法枚举
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link Sign}
     */
    public static Sign sign(String algorithm, byte[] privateKey, byte[] publicKey) {
        return new Sign(algorithm, privateKey, publicKey);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param crypto 对称加密算法
     * @param params 参数
     * @param other  其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParams(Symmetric crypto, Map<?, ?> params, String... other) {
        return signParams(crypto, params, Normal.EMPTY, Normal.EMPTY, true, other);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     *
     * @param crypto            对称加密算法
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param other             其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParams(Symmetric crypto, Map<?, ?> params, String separator,
                                    String keyValueSeparator, boolean isIgnoreNull, String... other) {
        return crypto.encryptHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, other));
    }

    /**
     * 对参数做md5签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params 参数
     * @param other  其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParamsMd5(Map<?, ?> params, String... other) {
        return signParams(Algorithm.MD5, params, other);
    }

    /**
     * 对参数做Sha1签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params 参数
     * @param other  其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParamsSha1(Map<?, ?> params, String... other) {
        return signParams(Algorithm.SHA1, params, other);
    }

    /**
     * 对参数做Sha256签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params 参数
     * @param other  其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParamsSha256(Map<?, ?> params, String... other) {
        return signParams(Algorithm.SHA256, params, other);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param algorithm   摘要算法
     * @param params      参数
     * @param otherParams 其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParams(String algorithm, Map<?, ?> params, String... otherParams) {
        return signParams(algorithm, params, Normal.EMPTY, Normal.EMPTY, true, otherParams);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串,然后根据提供的签名算法生成签名字符串
     *
     * @param algorithm         摘要算法
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParams(String algorithm, Map<?, ?> params, String separator,
                                    String keyValueSeparator, boolean isIgnoreNull, String... otherParams) {
        return new Digester(algorithm).digestHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, otherParams));
    }

    /**
     * 增加加密解密的算法提供者,默认优先使用,例如：
     *
     * <pre>
     * addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
     * </pre>
     *
     * @param provider 算法提供者
     */
    public static void addProvider(java.security.Provider provider) {
        Security.insertProviderAt(provider, 0);
    }

    /**
     * 解码字符串密钥，可支持的编码如下：
     *
     * <pre>
     * 1. Hex(16进制)编码
     * 1. Base64编码
     * </pre>
     *
     * @param key 被解码的密钥字符串
     * @return 密钥
     */
    public static byte[] decode(String key) {
        return Validator.isHex(key) ? HexKit.decodeHex(key) : Base64.decode(key);
    }

    /**
     * 创建{@link Cipher}
     *
     * @param algorithm 算法
     * @return {@link Cipher}
     */
    public static Cipher createCipher(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        Cipher cipher;
        try {
            cipher = (null == provider) ? Cipher.getInstance(algorithm) : Cipher.getInstance(algorithm, provider);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }

        return cipher;
    }

    /**
     * 创建{@link MessageDigest}
     *
     * @param algorithm 算法
     * @return {@link MessageDigest}
     */
    public static MessageDigest createMessageDigest(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        MessageDigest messageDigest;
        try {
            messageDigest = (null == provider) ? MessageDigest.getInstance(algorithm) : MessageDigest.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }

        return messageDigest;
    }

    /**
     * 创建{@link Mac}
     *
     * @param algorithm 算法
     * @return {@link Mac}
     */
    public static Mac createMac(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        Mac mac;
        try {
            mac = (null == provider) ? Mac.getInstance(algorithm) : Mac.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }

        return mac;
    }

    /**
     * RC4算法
     *
     * @param key 密钥
     * @return {@link RC4}
     */
    public static RC4 rc4(String key) {
        return new RC4(key);
    }

    /**
     * 强制关闭Bouncy Castle库的使用,全局有效
     */
    public static void disableBouncyCastle() {
        Holder.setUseBouncyCastle(false);
    }

    /**
     * 只获取私钥里的d，32字节
     *
     * @param privateKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
     * @return 压缩得到的X
     */
    public static byte[] encodeECPrivateKey(PrivateKey privateKey) {
        return ((BCECPrivateKey) privateKey).getD().toByteArray();
    }

    /**
     * 编码压缩EC公钥(基于BouncyCastle)
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @return 压缩得到的X
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey) {
        return ((BCECPublicKey) publicKey).getQ().getEncoded(true);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,(基于BouncyCastle)
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param encode    压缩公钥
     * @param curveName EC曲线名
     * @return 公钥
     */
    public static PublicKey decodeECPoint(String encode, String curveName) {
        return decodeECPoint(Builder.decode(encode), curveName);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,(基于BouncyCastle)
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param encodeByte 压缩公钥
     * @param curveName  EC曲线名，例如{@link Builder#SM2_DEFAULT_CURVE}
     * @return 公钥
     */
    public static PublicKey decodeECPoint(byte[] encodeByte, String curveName) {
        final ECNamedCurveParameterSpec namedSpec = ECNamedCurveTable.getParameterSpec(curveName);
        final ECCurve curve = namedSpec.getCurve();
        final EllipticCurve ecCurve = new EllipticCurve(//
                new ECFieldFp(curve.getField().getCharacteristic()), //
                curve.getA().toBigInteger(), //
                curve.getB().toBigInteger());
        // 根据X恢复点Y
        final ECPoint point = ECPointUtil.decodePoint(ecCurve, encodeByte);

        // 根据曲线恢复公钥格式
        ECParameterSpec ecSpec = new ECNamedCurveSpec(curveName, curve, namedSpec.getG(), namedSpec.getN());

        return generatePublicKey("EC", new ECPublicKeySpec(point, ecSpec));
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param parameterSpec ECParameterSpec
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(org.bouncycastle.jce.spec.ECParameterSpec parameterSpec) {
        return new ECDomainParameters(
                parameterSpec.getCurve(),
                parameterSpec.getG(),
                parameterSpec.getN(),
                parameterSpec.getH());
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param curveName Curve名称
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(String curveName) {
        return toDomainParams(ECUtil.getNamedCurveByName(curveName));
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param x9ECParameters {@link X9ECParameters}
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(X9ECParameters x9ECParameters) {
        return new ECDomainParameters(
                x9ECParameters.getCurve(),
                x9ECParameters.getG(),
                x9ECParameters.getN(),
                x9ECParameters.getH()
        );
    }

    /**
     * 密钥转换为AsymmetricKeyParameter
     *
     * @param key PrivateKey或者PublicKey
     * @return ECPrivateKeyParameters或者ECPublicKeyParameters
     */
    public static AsymmetricKeyParameter toParams(Key key) {
        if (key instanceof PrivateKey) {
            return toPrivateParams((PrivateKey) key);
        } else if (key instanceof PublicKey) {
            return toPublicParams((PublicKey) key);
        }

        return null;
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param dHex 私钥d值16进制字符串
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2Params(String dHex) {
        return toSm2PrivateParams(dHex);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param dHex             私钥d值16进制字符串
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toParams(String dHex, ECDomainParameters domainParameters) {
        return toPrivateParams(dHex, domainParameters);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2Params(byte[] d) {
        return toSm2PrivateParams(d);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toParams(byte[] d, ECDomainParameters domainParameters) {
        return toPrivateParams(d, domainParameters);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2Params(BigInteger d) {
        return toParams(d, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toParams(BigInteger d, ECDomainParameters domainParameters) {
        return toPrivateParams(d, domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param x                公钥X
     * @param y                公钥Y
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toParams(BigInteger x, BigInteger y, ECDomainParameters domainParameters) {
        return toPublicParams(x, y, domainParameters);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param xHex 公钥X
     * @param yHex 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2Params(String xHex, String yHex) {
        return toParams(xHex, yHex, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param xHex             公钥X
     * @param yHex             公钥Y
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toParams(String xHex, String yHex, ECDomainParameters domainParameters) {
        return toParams(HexKit.decodeHex(xHex), HexKit.decodeHex(yHex), domainParameters);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param xBytes 公钥X
     * @param yBytes 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2Params(byte[] xBytes, byte[] yBytes) {
        return toParams(xBytes, yBytes, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param xBytes           公钥X
     * @param yBytes           公钥Y
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toParams(byte[] xBytes, byte[] yBytes, ECDomainParameters domainParameters) {
        return toPublicParams(xBytes, yBytes, domainParameters);
    }

    /**
     * 公钥转换为 {@link ECPublicKeyParameters}
     *
     * @param publicKey 公钥，传入null返回null
     * @return {@link ECPublicKeyParameters}或null
     */
    public static ECPublicKeyParameters toParams(PublicKey publicKey) {
        return toPublicParams(publicKey);
    }

    /**
     * 私钥转换为 {@link ECPrivateKeyParameters}
     *
     * @param privateKey 私钥，传入null返回null
     * @return {@link ECPrivateKeyParameters}或null
     */
    public static ECPrivateKeyParameters toParams(PrivateKey privateKey) {
        return toPrivateParams(privateKey);
    }

    /**
     * 读取PEM格式的私钥
     *
     * @param pemStream pem流
     * @return {@link PrivateKey}
     */
    public static PrivateKey readPrivateKey(InputStream pemStream) {
        return (PrivateKey) readPemKey(pemStream);
    }

    /**
     * 读取PEM格式的公钥
     *
     * @param pemStream pem流
     * @return {@link PublicKey}
     */
    public static PublicKey readPublicKey(InputStream pemStream) {
        return (PublicKey) readPemKey(pemStream);
    }

    /**
     * 从pem文件中读取公钥或私钥
     * 根据类型返回{@link PublicKey} 或者 {@link PrivateKey}
     *
     * @param keyStream pem流
     * @return {@link Key}，null表示无法识别的密钥类型
     */
    public static Key readPemKey(InputStream keyStream) {
        final PemObject object = readPemObject(keyStream);
        final String type = object.getType();
        if (StringKit.isNotBlank(type)) {
            if (type.endsWith("PRIVATE KEY")) {
                return generateRSAPrivateKey(object.getContent());
            } else if (type.endsWith("PUBLIC KEY")) {
                return generateRSAPublicKey(object.getContent());
            } else if (type.endsWith("CERTIFICATE")) {
                return readPublicKeyFromCert(IoKit.toStream(object.getContent()));
            }
        }
        return null;
    }

    /**
     * 从pem文件中读取公钥或私钥
     *
     * @param keyStream pem流
     * @return 密钥bytes
     */
    public static byte[] readKeyBytes(InputStream keyStream) {
        PemObject pemObject = readPemObject(keyStream);
        if (null != pemObject) {
            return pemObject.getContent();
        }
        return null;
    }

    /**
     * 从pem流中读取公钥或私钥
     *
     * @param keyStream pem流
     * @return 密钥bytes
     */
    public static byte[] readPem(InputStream keyStream) {
        PemObject pemObject = readPemObject(keyStream);
        if (null != pemObject) {
            return pemObject.getContent();
        }
        return null;
    }

    /**
     * 读取pem文件中的信息，包括类型、头信息和密钥内容
     *
     * @param keyStream pem流
     * @return {@link PemObject}
     */
    public static PemObject readPemObject(InputStream keyStream) {
        return readPemObject(IoKit.getReader(keyStream, Charset.UTF_8));
    }

    /**
     * 读取pem文件中的信息，包括类型、头信息和密钥内容
     *
     * @param reader pem Reader
     * @return {@link PemObject}
     */
    public static PemObject readPemObject(Reader reader) {
        PemReader pemReader = null;
        try {
            pemReader = new PemReader(reader);
            return pemReader.readPemObject();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoKit.close(pemReader);
        }
    }

    /**
     * 写出pem密钥(私钥、公钥、证书)
     *
     * @param type      密钥类型(私钥、公钥、证书)
     * @param content   密钥内容
     * @param keyStream pem流
     */
    public static void writePemObject(String type, byte[] content, OutputStream keyStream) {
        writePemObject(new PemObject(type, content), keyStream);
    }

    /**
     * 写出pem密钥(私钥、公钥、证书)
     *
     * @param pemObject pem对象，包括密钥和密钥类型等信息
     * @param keyStream pem流
     */
    public static void writePemObject(PemObjectGenerator pemObject, OutputStream keyStream) {
        PemWriter writer = null;
        try {
            writer = new PemWriter(IoKit.getWriter(keyStream, Charset.UTF_8));
            writer.writeObject(pemObject);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoKit.close(writer);
        }
    }

    /**
     * 创建SM2算法对象
     * 生成新的私钥公钥对
     *
     * @return {@link SM2}
     */
    public static SM2 sm2() {
        return new SM2();
    }

    /**
     * 创建SM2算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     * @return {@link SM2}
     */
    public static SM2 sm2(String privateKey, String publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * 创建SM2算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link SM2}
     */
    public static SM2 sm2(byte[] privateKey, byte[] publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * SM3加密
     * 例：
     * SM3加密：sm3().digest(data)
     * SM3加密并转为16进制字符串：sm3().digestHex(data)
     *
     * @return {@link SM3}
     */
    public static SM3 sm3() {
        return new SM3();
    }

    /**
     * SM3加密，生成16进制SM3字符串
     *
     * @param data 数据
     * @return SM3字符串
     */
    public static String sm3(String data) {
        return sm3().digestHex(data);
    }

    /**
     * SM3加密，生成16进制SM3字符串
     *
     * @param data 数据
     * @return SM3字符串
     */
    public static String sm3(InputStream data) {
        return sm3().digestHex(data);
    }

    /**
     * SM3加密文件，生成16进制SM3字符串
     *
     * @param dataFile 被加密文件
     * @return SM3字符串
     */
    public static String sm3(File dataFile) {
        return sm3().digestHex(dataFile);
    }

    /**
     * SM4加密,生成随机KEY 注意解密时必须使用相同 {@link Symmetric}对象或者使用相同KEY
     * 例：
     *
     * <pre>
     * SM4加密：sm4().encrypt(data)
     * SM4解密：sm4().decrypt(data)
     * </pre>
     *
     * @return {@link Symmetric}
     */
    public static SM4 sm4() {
        return new SM4();
    }

    /**
     * SM4加密
     * 例：
     *
     * <pre>
     * SM4加密：sm4(key).encrypt(data)
     * SM4解密：sm4(key).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link Symmetric}
     */
    public static Symmetric sm4(byte[] key) {
        return new SM4(key);
    }

    /**
     * bc加解密使用旧标c1||c2||c3,此方法在加密后调用,将结果转化为c1||c3||c2
     *
     * @param c1c2c3             加密后的bytes,顺序为C1C2C3
     * @param ecDomainParameters {@link ECDomainParameters}
     * @return 加密后的bytes, 顺序为C1C3C2
     */
    public static byte[] changeC1C2C3ToC1C3C2(byte[] c1c2c3, ECDomainParameters ecDomainParameters) {
        // sm2p256v1的这个固定65 可看GMNamedCurves、ECCurve代码
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c2c3.length];
        System.arraycopy(c1c2c3, 0, result, 0, c1Len); // c1
        System.arraycopy(c1c2c3, c1c2c3.length - c3Len, result, c1Len, c3Len); // c3
        System.arraycopy(c1c2c3, c1Len, result, c1Len + c3Len, c1c2c3.length - c1Len - c3Len); // c2
        return result;
    }

    /**
     * bc加解密使用旧标c1||c3||c2,此方法在解密前调用,将密文转化为c1||c2||c3再去解密
     *
     * @param c1c3c2             加密后的bytes,顺序为C1C3C2
     * @param ecDomainParameters {@link ECDomainParameters}
     * @return c1c2c3 加密后的bytes，顺序为C1C2C3
     */
    public static byte[] changeC1C3C2ToC1C2C3(byte[] c1c3c2, ECDomainParameters ecDomainParameters) {
        // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c3c2.length];
        System.arraycopy(c1c3c2, 0, result, 0, c1Len); // c1: 0->65
        System.arraycopy(c1c3c2, c1Len + c3Len, result, c1Len, c1c3c2.length - c1Len - c3Len); // c2
        System.arraycopy(c1c3c2, c1Len, result, c1c3c2.length - c3Len, c3Len); // c3
        return result;
    }

    /**
     * BC的SM3withSM2签名得到的结果的rs是asn1格式的,这个方法转化成直接拼接r||s
     *
     * @param rsDer rs in asn1 format
     * @return sign result in plain byte array
     */
    public static byte[] rsAsn1ToPlain(byte[] rsDer) {
        ASN1Sequence seq = ASN1Sequence.getInstance(rsDer);
        byte[] r = bigIntToFixedLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(0)).getValue());
        byte[] s = bigIntToFixedLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(1)).getValue());
        byte[] result = new byte[RS_LEN * 2];
        System.arraycopy(r, 0, result, 0, r.length);
        System.arraycopy(s, 0, result, RS_LEN, s.length);
        return result;
    }

    /**
     * BC的SM3withSM2验签需要的rs是asn1格式的,
     * 这个方法将直接拼接r||s的字节数组转化成asn1格式
     *
     * @param sign in plain byte array
     * @return rs result in asn1 format
     */
    public static byte[] rsPlainToAsn1(byte[] sign) {
        if (sign.length != RS_LEN * 2) {
            throw new InstrumentException("err rs. ");
        }
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, RS_LEN));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, RS_LEN, RS_LEN * 2));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        try {
            return new DERSequence(v).getEncoded("DER");
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 创建HmacSM3算法的{@link MacEngine}
     *
     * @param key 密钥
     * @return {@link MacEngine}
     */
    public static MacEngine createHmacSm3Engine(byte[] key) {
        return new BCHMacEngine(new SM3Digest(), key);
    }

    /**
     * HmacSM3算法实现
     *
     * @param key 密钥
     * @return {@link HMac} 对象,调用digestXXX即可
     */
    public static HMac hmacSm3(byte[] key) {
        return new HMac(Algorithm.HmacSM3, key);
    }

    /**
     * 转换为 ECPublicKeyParameters
     *
     * @param q 公钥Q值
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(byte[] q) {
        return toPublicParams(q, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPublicKeyParameters
     *
     * @param q 公钥Q值
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(String q) {
        return toPublicParams(q, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param x 公钥X
     * @param y 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(String x, String y) {
        return toPublicParams(x, y, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param xBytes 公钥X
     * @param yBytes 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(byte[] xBytes, byte[] yBytes) {
        return toPublicParams(xBytes, yBytes, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param x                公钥X
     * @param y                公钥Y
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(String x, String y, ECDomainParameters domainParameters) {
        return toPublicParams(decode(x), decode(y), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param xBytes           公钥X
     * @param yBytes           公钥Y
     * @param domainParameters ECDomainParameters曲线参数
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(byte[] xBytes, byte[] yBytes, ECDomainParameters domainParameters) {
        return toPublicParams(BigIntegers.fromUnsignedByteArray(xBytes), BigIntegers.fromUnsignedByteArray(yBytes), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param x                公钥X
     * @param y                公钥Y
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(BigInteger x, BigInteger y, ECDomainParameters domainParameters) {
        if (null == x || null == y) {
            return null;
        }
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.createPoint(x, y), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param pointEncoded     被编码的曲线坐标点
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(String pointEncoded, ECDomainParameters domainParameters) {
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.decodePoint(decode(pointEncoded)), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param pointEncoded     被编码的曲线坐标点
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(byte[] pointEncoded, ECDomainParameters domainParameters) {
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.decodePoint(pointEncoded), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param point            曲线坐标点
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(org.bouncycastle.math.ec.ECPoint point, ECDomainParameters domainParameters) {
        return new ECPublicKeyParameters(point, domainParameters);
    }

    /**
     * 公钥转换为 {@link ECPublicKeyParameters}
     *
     * @param publicKey 公钥，传入null返回null
     * @return {@link ECPublicKeyParameters}或null
     */
    public static ECPublicKeyParameters toPublicParams(PublicKey publicKey) {
        if (null == publicKey) {
            return null;
        }
        try {
            return (ECPublicKeyParameters) ECUtil.generatePublicKeyParameter(publicKey);
        } catch (InvalidKeyException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值16进制字符串
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(String d) {
        return toPrivateParams(d, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(byte[] d) {
        return toPrivateParams(d, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(BigInteger d) {
        return toPrivateParams(d, SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值16进制字符串
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toPrivateParams(String d, ECDomainParameters domainParameters) {
        return toPrivateParams(BigIntegers.fromUnsignedByteArray(decode(d)), domainParameters);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toPrivateParams(byte[] d, ECDomainParameters domainParameters) {
        return toPrivateParams(BigIntegers.fromUnsignedByteArray(d), domainParameters);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toPrivateParams(BigInteger d, ECDomainParameters domainParameters) {
        if (null == d) {
            return null;
        }
        return new ECPrivateKeyParameters(d, domainParameters);
    }

    /**
     * 私钥转换为 {@link ECPrivateKeyParameters}
     *
     * @param privateKey 私钥，传入null返回null
     * @return {@link ECPrivateKeyParameters}或null
     */
    public static ECPrivateKeyParameters toPrivateParams(PrivateKey privateKey) {
        if (null == privateKey) {
            return null;
        }
        try {
            return (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(privateKey);
        } catch (InvalidKeyException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * BigInteger转固定长度bytes
     *
     * @param rOrS {@link BigInteger}
     * @return 固定长度bytes
     */
    private static byte[] bigIntToFixedLengthBytes(BigInteger rOrS) {
        byte[] rs = rOrS.toByteArray();
        if (rs.length == RS_LEN) {
            return rs;
        } else if (rs.length == RS_LEN + 1 && rs[0] == 0) {
            return Arrays.copyOfRange(rs, 1, RS_LEN + 1);
        } else if (rs.length < RS_LEN) {
            byte[] result = new byte[RS_LEN];
            Arrays.fill(result, (byte) 0);
            System.arraycopy(rs, 0, result, RS_LEN - rs.length, rs.length);
            return result;
        } else {
            throw new InstrumentException("Error rs: {}", Hex.toHexString(rs));
        }
    }

}
