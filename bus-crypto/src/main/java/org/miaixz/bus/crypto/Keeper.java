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
package org.miaixz.bus.crypto;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.spec.OpenSSHPrivateKeySpec;
import org.bouncycastle.jcajce.spec.OpenSSHPublicKeySpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.Provider;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.*;

/**
 * 密钥工具
 * <pre>
 * 1、生成密钥（单密钥、密钥对）
 * 2、读取密钥文件
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Keeper {

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
     * Java密钥库(Java Key Store，JKS)KEY_STORE
     */
    public static final String TYPE_JKS = "JKS";
    /**
     * jceks
     */
    public static final String TYPE_JCEKS = "jceks";
    /**
     * PKCS12是公钥加密标准，它规定了可包含所有私钥、公钥和证书。其以二进制格式存储，也称为 PFX 文件
     */
    public static final String TYPE_PKCS12 = "pkcs12";
    /**
     * Certification类型：X.509
     */
    public static final String TYPE_X509 = "X.509";

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法，支持PBE算法
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(final String algorithm) {
        return generateKey(algorithm, -1);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     * 当指定keySize&lt;0时，AES默认长度为128，其它算法不指定。
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度，&lt;0表示不设定密钥长度，即使用默认长度
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(final String algorithm, final int keySize) {
        return generateKey(algorithm, keySize, null);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     * 当指定keySize&lt;0时，AES默认长度为128，其它算法不指定。
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度，&lt;0表示不设定密钥长度，即使用默认长度
     * @param random    随机数生成器，null表示默认
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, int keySize, final SecureRandom random) {
        algorithm = getMainAlgorithm(algorithm);

        final KeyGenerator keyGenerator = getKeyGenerator(algorithm);
        if (keySize <= 0 && Algorithm.AES.getValue().equals(algorithm)) {
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
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法
     * @param key       密钥，如果为{@code null} 自动生成随机密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(final String algorithm, final byte[] key) {
        Assert.notBlank(algorithm, "Algorithm is blank!");
        final SecretKey secretKey;
        if (algorithm.startsWith("PBE")) {
            // PBE密钥
            secretKey = generatePBEKey(algorithm, (null == key) ? null : StringKit.toString(key).toCharArray());
        } else if (algorithm.startsWith("DES")) {
            // DES密钥
            secretKey = generateDESKey(algorithm, key);
        } else {
            // 其它算法密钥
            secretKey = (null == key) ? generateKey(algorithm) : new SecretKeySpec(key, algorithm);
        }
        return secretKey;
    }

    /**
     * 生成 {@link SecretKey}
     *
     * @param algorithm DES算法，包括DES、DESede等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateDESKey(final String algorithm, final byte[] key) {
        if (StringKit.isBlank(algorithm) || !algorithm.startsWith("DES")) {
            throw new CryptoException("Algorithm [{}] is not a DES algorithm!", algorithm);
        }

        final SecretKey secretKey;
        if (null == key) {
            secretKey = generateKey(algorithm);
        } else {
            secretKey = generateKey(algorithm,
                    Builder.createKeySpec(algorithm, key));
        }
        return secretKey;
    }

    /**
     * 生成PBE {@link SecretKey}
     *
     * @param algorithm PBE算法，包括：PBEWithMD5AndDES、PBEWithSHA1AndDESede、PBEWithSHA1AndRC2_40等
     * @param password  口令
     * @return {@link SecretKey}
     */
    public static SecretKey generatePBEKey(final String algorithm, char[] password) {
        if (StringKit.isBlank(algorithm) || !algorithm.startsWith("PBE")) {
            throw new CryptoException("Algorithm [{}] is not a PBE algorithm!", algorithm);
        }

        if (null == password) {
            password = RandomKit.randomStringLower(32).toCharArray();
        }
        return generateKey(algorithm, Builder.createPBEKeySpec(password));
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(final String algorithm, final KeySpec keySpec) {
        final SecretKeyFactory keyFactory = getSecretKeyFactory(algorithm);
        try {
            return keyFactory.generateSecret(keySpec);
        } catch (final InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 检查{@link KeyPair} 是否为空，空的条件是：
     * <ul>
     *     <li>keyPair本身为{@code null}</li>
     *     <li>{@link KeyPair#getPrivate()}和{@link KeyPair#getPublic()}都为{@code null}</li>
     * </ul>
     *
     * @param keyPair 密钥对
     * @return 是否为空
     */
    public static boolean isEmpty(final KeyPair keyPair) {
        if (null == keyPair) {
            return false;
        }
        return null != keyPair.getPrivate() || null != keyPair.getPublic();
    }

    /**
     * 生成RSA私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     * 算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">...</a>
     *
     * @param key 密钥，必须为DER编码存储
     * @return RSA私钥 {@link PrivateKey}
     */
    public static PrivateKey generateRSAPrivateKey(final byte[] key) {
        return generatePrivateKey(Algorithm.RSA.getValue(), key);
    }

    /**
     * 生成私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     * 算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">...</a>
     *
     * @param algorithm 算法，如RSA、EC、SM2等
     * @param key       密钥，PKCS#8格式
     * @return 私钥 {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(final String algorithm, final byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePrivateKey(algorithm, new PKCS8EncodedKeySpec(key));
    }

    /**
     * 生成私钥，仅用于非对称加密
     * 算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">...</a>
     *
     * @param algorithm 算法，如RSA、EC、SM2等
     * @param keySpec   {@link KeySpec}
     * @return 私钥 {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(String algorithm, final KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        algorithm = getAlgorithmAfterWith(algorithm);
        try {
            return getKeyFactory(algorithm).generatePrivate(keySpec);
        } catch (final Exception e) {
            throw new CryptoException(e);
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
    public static PrivateKey generatePrivateKey(final KeyStore keyStore, final String alias, final char[] password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 生成RSA公钥，仅用于非对称加密
     * 采用X509证书规范
     * 算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">...</a>
     *
     * @param key 密钥，必须为DER编码存储
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generateRSAPublicKey(final byte[] key) {
        return generatePublicKey(Algorithm.RSA.getValue(), key);
    }

    /**
     * 生成公钥，仅用于非对称加密
     * 采用X509证书规范
     * 算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">...</a>
     *
     * @param algorithm 算法
     * @param key       密钥，必须为DER编码存储
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generatePublicKey(final String algorithm, final byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePublicKey(algorithm, new X509EncodedKeySpec(key));
    }

    /**
     * 生成公钥，仅用于非对称加密
     * 算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory">...</a>
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generatePublicKey(String algorithm, final KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        algorithm = getAlgorithmAfterWith(algorithm);
        try {
            return getKeyFactory(algorithm).generatePublic(keySpec);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 通过RSA私钥生成RSA公钥
     *
     * @param privateKey RSA私钥
     * @return RSA公钥，null表示私钥不被支持
     */
    public static PublicKey getRSAPublicKey(final PrivateKey privateKey) {
        if (privateKey instanceof RSAPrivateCrtKey) {
            final RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
            return getRSAPublicKey(privk.getModulus(), privk.getPublicExponent());
        }
        return null;
    }

    /**
     * 获得RSA公钥对象
     *
     * @param modulus        Modulus
     * @param publicExponent Public Exponent
     * @return 公钥
     */
    public static PublicKey getRSAPublicKey(final String modulus, final String publicExponent) {
        return getRSAPublicKey(
                new BigInteger(modulus, 16), new BigInteger(publicExponent, 16));
    }

    /**
     * 获得RSA公钥对象
     *
     * @param modulus        Modulus
     * @param publicExponent Public Exponent
     * @return 公钥
     */
    public static PublicKey getRSAPublicKey(final BigInteger modulus, final BigInteger publicExponent) {
        final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        try {
            return getKeyFactory(Algorithm.RSA.getValue()).generatePublic(publicKeySpec);
        } catch (final InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 生成用于非对称加密的公钥和私钥，仅用于非对称加密
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(final String algorithm) {
        int keySize = DEFAULT_KEY_SIZE;
        if ("ECIES".equalsIgnoreCase(algorithm)) {
            // ECIES算法对KEY的长度有要求，此处默认256
            keySize = 256;
        }

        return generateKeyPair(algorithm, keySize);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模（modulus ）长度
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(final String algorithm, final int keySize) {
        return generateKeyPair(algorithm, keySize, null);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模（modulus ）长度
     * @param seed      种子
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(final String algorithm, final int keySize, final byte[] seed) {
        // SM2算法需要单独定义其曲线生成
        if ("SM2".equalsIgnoreCase(algorithm)) {
            final ECGenParameterSpec sm2p256v1 = new ECGenParameterSpec(Builder.SM2_CURVE_NAME);
            return generateKeyPair(algorithm, keySize, seed, sm2p256v1);
        }

        return generateKeyPair(algorithm, keySize, seed, (AlgorithmParameterSpec[]) null);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
     *
     * @param algorithm 非对称加密算法
     * @param params    {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(final String algorithm, final AlgorithmParameterSpec params) {
        return generateKeyPair(algorithm, null, params);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
     *
     * @param algorithm 非对称加密算法
     * @param param     {@link AlgorithmParameterSpec}
     * @param seed      种子
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(final String algorithm, final byte[] seed, final AlgorithmParameterSpec param) {
        return generateKeyPair(algorithm, DEFAULT_KEY_SIZE, seed, param);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
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
     * <b>EC（Elliptic Curve）：</b>
     * <pre>
     * EC256：256 bits
     * EC384：384 bits
     * EC512：512 bits
     * </pre>
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模（modulus ）长度（单位bit）
     * @param seed      种子
     * @param params    {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(final String algorithm, final int keySize, final byte[] seed, final AlgorithmParameterSpec... params) {
        return generateKeyPair(algorithm, keySize, RandomKit.createSecureRandom(seed), params);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：<a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">...</a>
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
     * <b>EC（Elliptic Curve）：</b>
     * <pre>
     * EC256：256 bits
     * EC384：384 bits
     * EC512：512 bits
     * </pre>
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模（modulus ）长度（单位bit）
     * @param random    {@link SecureRandom} 对象，创建时可选传入seed
     * @param params    {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize, final SecureRandom random, final AlgorithmParameterSpec... params) {
        algorithm = getAlgorithmAfterWith(algorithm);
        final KeyPairGenerator keyPairGen = getKeyPairGenerator(algorithm);

        // 密钥模（modulus ）长度初始化定义
        if (keySize > 0) {
            // key长度适配修正
            if ("EC".equalsIgnoreCase(algorithm) && keySize > 256) {
                // 对于EC（EllipticCurve）算法，密钥长度有限制，在此使用默认256
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
            for (final AlgorithmParameterSpec param : params) {
                if (null == param) {
                    continue;
                }
                try {
                    if (null != random) {
                        keyPairGen.initialize(param, random);
                    } else {
                        keyPairGen.initialize(param);
                    }
                } catch (final InvalidAlgorithmParameterException e) {
                    throw new CryptoException(e);
                }
            }
        }
        return keyPairGen.generateKeyPair();
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
    public static KeyPair getKeyPair(final String type, final InputStream in, final char[] password, final String alias) {
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
    public static KeyPair getKeyPair(final KeyStore keyStore, final char[] password, final String alias) {
        final PublicKey publicKey;
        final PrivateKey privateKey;
        try {
            publicKey = keyStore.getCertificate(alias).getPublicKey();
            privateKey = (PrivateKey) keyStore.getKey(alias, password);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * 获取{@link KeyPairGenerator}
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyPairGenerator}
     */
    public static KeyPairGenerator getKeyPairGenerator(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final KeyPairGenerator keyPairGen;
        try {
            keyPairGen = (null == provider) //
                    ? KeyPairGenerator.getInstance(getMainAlgorithm(algorithm)) //
                    : KeyPairGenerator.getInstance(getMainAlgorithm(algorithm), provider);//
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyPairGen;
    }

    /**
     * 获取{@link KeyFactory}
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyFactory}
     */
    public static KeyFactory getKeyFactory(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final KeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? KeyFactory.getInstance(getMainAlgorithm(algorithm)) //
                    : KeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyFactory;
    }

    /**
     * 获取{@link SecretKeyFactory}
     *
     * @param algorithm 对称加密算法
     * @return {@link KeyFactory}
     */
    public static SecretKeyFactory getSecretKeyFactory(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final SecretKeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? SecretKeyFactory.getInstance(getMainAlgorithm(algorithm)) //
                    : SecretKeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyFactory;
    }

    /**
     * 获取{@link KeyGenerator}
     *
     * @param algorithm 对称加密算法
     * @return {@link KeyGenerator}
     */
    public static KeyGenerator getKeyGenerator(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();
        final KeyGenerator generator;
        try {
            generator = (null == provider) //
                    ? KeyGenerator.getInstance(getMainAlgorithm(algorithm)) //
                    : KeyGenerator.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return generator;
    }

    /**
     * 获取主体算法名，例如RSA/ECB/PKCS1Padding的主体算法是RSA
     *
     * @param algorithm XXXwithXXX算法
     * @return 主体算法名
     */
    public static String getMainAlgorithm(final String algorithm) {
        Assert.notBlank(algorithm, "Algorithm must be not blank!");
        final int slashIndex = algorithm.indexOf(Symbol.C_SLASH);
        if (slashIndex > 0) {
            return algorithm.substring(0, slashIndex);
        }
        return algorithm;
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

        if (StringKit.startWithIgnoreCase(algorithm, "ECIESWith")) {
            return "EC";
        }

        final int indexOfWith = StringKit.lastIndexOfIgnoreCase(algorithm, "with");
        if (indexOfWith > 0) {
            algorithm = StringKit.subSuf(algorithm, indexOfWith + "with".length());
        }
        if ("ECDSA".equalsIgnoreCase(algorithm)
                || "SM2".equalsIgnoreCase(algorithm)
                || "ECIES".equalsIgnoreCase(algorithm)
        ) {
            algorithm = "EC";
        }
        return algorithm;
    }

    /**
     * 读取X.509 Certification文件中的公钥
     * Certification为证书文件
     * see: <a href="https://www.cnblogs.com/yinliang/p/10115519.html">...</a>
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @return {@link KeyStore}
     */
    public static PublicKey readPublicKeyFromCert(final InputStream in) {
        final Certificate certificate = readX509Certificate(in);
        if (null != certificate) {
            return certificate.getPublicKey();
        }
        return null;
    }

    /**
     * 编码压缩EC公钥（基于BouncyCastle）
     * 见：<a href="https://www.cnblogs.com/xinzhao/p/8963724.html">...</a>
     *
     * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @return 压缩得到的X
     */
    public static byte[] encodeECPublicKey(final PublicKey publicKey) {
        return encodeECPublicKey(publicKey);
    }

    /**
     * 将密钥编码为Base64格式
     *
     * @param key 密钥
     * @return Base64格式密钥
     */
    public static String toBase64(final Key key) {
        return Base64.encode(key.getEncoded());
    }

    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param keyFile  证书文件
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readJKSKeyStore(final File keyFile, final char[] password) {
        return readKeyStore(TYPE_JKS, keyFile, password);
    }

    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readJKSKeyStore(final InputStream in, final char[] password) {
        return readKeyStore(TYPE_JKS, in, password);
    }

    /**
     * 读取PKCS12 KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param keyFile  证书文件
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readPKCS12KeyStore(final File keyFile, final char[] password) {
        return readKeyStore(TYPE_PKCS12, keyFile, password);
    }

    /**
     * 读取PKCS12 KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readPKCS12KeyStore(final InputStream in, final char[] password) {
        return readKeyStore(TYPE_PKCS12, in, password);
    }

    /**
     * 读取KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param type     类型
     * @param keyFile  证书文件
     * @param password 密码，null表示无密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(final String type, final File keyFile, final char[] password) {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(keyFile);
            return readKeyStore(type, in, password);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 读取KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @param password 密码，null表示无密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(final String type, final InputStream in, final char[] password) {
        final KeyStore keyStore = getKeyStore(type);
        try {
            keyStore.load(in, password);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
        return keyStore;
    }

    /**
     * 获取{@link KeyStore}对象
     *
     * @param type 类型
     * @return {@link KeyStore}
     */
    public static KeyStore getKeyStore(final String type) {
        final java.security.Provider provider = Holder.getProvider();
        try {
            return null == provider ? KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
        } catch (final KeyStoreException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 读取PEM格式的私钥
     *
     * @param pemStream pem流
     * @return {@link PrivateKey}
     */
    public static PrivateKey readPemPrivateKey(final InputStream pemStream) {
        return (PrivateKey) readPemKey(pemStream);
    }

    /**
     * 读取PEM格式的公钥
     *
     * @param pemStream pem流
     * @return {@link PublicKey}
     */
    public static PublicKey readPemPublicKey(final InputStream pemStream) {
        return (PublicKey) readPemKey(pemStream);
    }

    /**
     * 从pem文件中读取公钥或私钥
     * 根据类型返回 {@link PublicKey} 或者 {@link PrivateKey}
     *
     * @param keyStream pem流
     * @return {@link Key}，null表示无法识别的密钥类型
     */
    public static Key readPemKey(final InputStream keyStream) {
        final PemObject object = readPemObject(keyStream);
        final String type = object.getType();
        if (StringKit.isNotBlank(type)) {
            //private
            if (type.endsWith("EC PRIVATE KEY")) {
                try {
                    // 尝试PKCS#8
                    return generatePrivateKey("EC", object.getContent());
                } catch (final Exception e) {
                    // 尝试PKCS#1
                    return generatePrivateKey("EC", createOpenSSHPrivateKeySpec(object.getContent()));
                }
            }
            if (type.endsWith("PRIVATE KEY")) {
                return generateRSAPrivateKey(object.getContent());
            }

            // public
            if (type.endsWith("EC PUBLIC KEY")) {
                try {
                    // 尝试DER
                    return generatePublicKey("EC", object.getContent());
                } catch (final Exception ignore) {
                    // 尝试PKCS#1
                    return generatePublicKey("EC", createOpenSSHPublicKeySpec(object.getContent()));
                }
            } else if (type.endsWith("PUBLIC KEY")) {
                return generateRSAPublicKey(object.getContent());
            } else if (type.endsWith("CERTIFICATE")) {
                return readPublicKeyFromCert(IoKit.toStream(object.getContent()));
            }
        }

        //表示无法识别的密钥类型
        return null;
    }

    /**
     * 从pem流中读取公钥或私钥
     *
     * @param keyStream pem流
     * @return 密钥bytes
     */
    public static byte[] readPem(final InputStream keyStream) {
        final PemObject pemObject = readPemObject(keyStream);
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
    public static PemObject readPemObject(final InputStream keyStream) {
        return readPemObject(IoKit.toUtf8Reader(keyStream));
    }

    /**
     * 读取pem文件中的信息，包括类型、头信息和密钥内容
     *
     * @param reader pem Reader
     * @return {@link PemObject}
     */
    public static PemObject readPemObject(final Reader reader) {
        PemReader pemReader = null;
        try {
            pemReader = new PemReader(reader);
            return pemReader.readPemObject();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(pemReader);
        }
    }

    /**
     * 将私钥或公钥转换为PEM格式的字符串
     *
     * @param type    密钥类型（私钥、公钥、证书）
     * @param content 密钥内容
     * @return PEM内容
     */
    public static String toPem(final String type, final byte[] content) {
        final StringWriter stringWriter = new StringWriter();
        writePemObject(type, content, stringWriter);
        return stringWriter.toString();
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param type      密钥类型（私钥、公钥、证书）
     * @param content   密钥内容，需为PKCS#1格式
     * @param keyStream pem流
     */
    public static void writePemObject(final String type, final byte[] content, final OutputStream keyStream) {
        writePemObject(new PemObject(type, content), keyStream);
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param type    密钥类型（私钥、公钥、证书）
     * @param content 密钥内容，需为PKCS#1格式
     * @param writer  pemWriter
     */
    public static void writePemObject(final String type, final byte[] content, final Writer writer) {
        writePemObject(new PemObject(type, content), writer);
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param pemObject pem对象，包括密钥和密钥类型等信息
     * @param keyStream pem流
     */
    public static void writePemObject(final PemObjectGenerator pemObject, final OutputStream keyStream) {
        writePemObject(pemObject, IoKit.toUtf8Writer(keyStream));
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param pemObject pem对象，包括密钥和密钥类型等信息
     * @param writer    pemWriter
     */
    public static void writePemObject(final PemObjectGenerator pemObject, final Writer writer) {
        final PemWriter pemWriter = new PemWriter(writer);
        try {
            pemWriter.writeObject(pemObject);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(pemWriter);
        }
    }

    /**
     * 只获取私钥里的d，32位字节
     *
     * @param privateKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
     * @return 压缩得到的X
     */
    public static byte[] encodeECPrivateKey(final PrivateKey privateKey) {
        return ((BCECPrivateKey) privateKey).getD().toByteArray();
    }

    /**
     * 编码压缩EC公钥（基于BouncyCastle），即Q值
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey    {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @param isCompressed 是否压缩
     * @return 得到的Q
     */
    public static byte[] encodeECPublicKey(final PublicKey publicKey, final boolean isCompressed) {
        return ((BCECPublicKey) publicKey).getQ().getEncoded(isCompressed);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param encode    压缩公钥
     * @param curveName EC曲线名
     * @return 公钥
     */
    public static PublicKey decodeECPoint(final String encode, final String curveName) {
        return decodeECPoint(Builder.decode(encode), curveName);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）
     *
     * @param encodeByte 压缩公钥
     * @param curveName  EC曲线名，例如{@link Builder#SM2_DOMAIN_PARAMS}
     * @return 公钥
     */
    public static PublicKey decodeECPoint(final byte[] encodeByte, final String curveName) {
        final X9ECParameters x9ECParameters = ECUtil.getNamedCurveByName(curveName);
        final ECCurve curve = x9ECParameters.getCurve();
        final ECPoint point = EC5Util.convertPoint(curve.decodePoint(encodeByte));

        // 根据曲线恢复公钥格式
        final ECNamedCurveSpec ecSpec = new ECNamedCurveSpec(curveName, curve, x9ECParameters.getG(), x9ECParameters.getN());
        return generatePublicKey("EC", new ECPublicKeySpec(point, ecSpec));
    }

    /**
     * 密钥转换为AsymmetricKeyParameter
     *
     * @param key PrivateKey或者PublicKey
     * @return ECPrivateKeyParameters或者ECPublicKeyParameters
     */
    public static AsymmetricKeyParameter toParams(final Key key) {
        if (key instanceof PrivateKey) {
            return toPrivateParams((PrivateKey) key);
        } else if (key instanceof PublicKey) {
            return toPublicParams((PublicKey) key);
        }

        return null;
    }

    /**
     * 根据私钥参数获取公钥参数
     *
     * @param privateKeyParameters 私钥参数
     * @return 公钥参数
     */
    public static ECPublicKeyParameters getPublicParams(final ECPrivateKeyParameters privateKeyParameters) {
        final ECDomainParameters domainParameters = privateKeyParameters.getParameters();
        final org.bouncycastle.math.ec.ECPoint q = new FixedPointCombMultiplier().multiply(domainParameters.getG(), privateKeyParameters.getD());
        return new ECPublicKeyParameters(q, domainParameters);
    }

    /**
     * 转换为 ECPublicKeyParameters
     *
     * @param q 公钥Q值
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(final byte[] q) {
        return toPublicParams(q, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPublicKeyParameters
     *
     * @param q 公钥Q值
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(final String q) {
        return toPublicParams(q, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param x 公钥X
     * @param y 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(final String x, final String y) {
        return toPublicParams(x, y, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param xBytes 公钥X
     * @param yBytes 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2PublicParams(final byte[] xBytes, final byte[] yBytes) {
        return toPublicParams(xBytes, yBytes, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param x                公钥X
     * @param y                公钥Y
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters，x或y为{@code null}则返回{@code null}
     */
    public static ECPublicKeyParameters toPublicParams(final String x, final String y, final ECDomainParameters domainParameters) {
        return toPublicParams(Builder.decode(x), Builder.decode(y), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param xBytes           公钥X
     * @param yBytes           公钥Y
     * @param domainParameters ECDomainParameters曲线参数
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(final byte[] xBytes, final byte[] yBytes, final ECDomainParameters domainParameters) {
        if (null == xBytes || null == yBytes) {
            return null;
        }
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
    public static ECPublicKeyParameters toPublicParams(final BigInteger x, final BigInteger y, final ECDomainParameters domainParameters) {
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
    public static ECPublicKeyParameters toPublicParams(final String pointEncoded, final ECDomainParameters domainParameters) {
        final ECCurve curve = domainParameters.getCurve();
        return toPublicParams(curve.decodePoint(Builder.decode(pointEncoded)), domainParameters);
    }

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param pointEncoded     被编码的曲线坐标点
     * @param domainParameters ECDomainParameters
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toPublicParams(final byte[] pointEncoded, final ECDomainParameters domainParameters) {
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
    public static ECPublicKeyParameters toPublicParams(final org.bouncycastle.math.ec.ECPoint point, final ECDomainParameters domainParameters) {
        return new ECPublicKeyParameters(point, domainParameters);
    }

    /**
     * 公钥转换为 {@link ECPublicKeyParameters}
     *
     * @param publicKey 公钥，传入null返回null
     * @return {@link ECPublicKeyParameters}或null
     */
    public static ECPublicKeyParameters toPublicParams(final PublicKey publicKey) {
        if (null == publicKey) {
            return null;
        }
        try {
            return (ECPublicKeyParameters) ECUtil.generatePublicKeyParameter(publicKey);
        } catch (final InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值16进制字符串
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(final String d) {
        return toPrivateParams(d, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(final byte[] d) {
        return toPrivateParams(d, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2PrivateParams(final BigInteger d) {
        return toPrivateParams(d, Builder.SM2_DOMAIN_PARAMS);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值16进制字符串
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toPrivateParams(final String d, final ECDomainParameters domainParameters) {
        if (null == d) {
            return null;
        }
        return toPrivateParams(BigIntegers.fromUnsignedByteArray(Builder.decode(d)), domainParameters);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toPrivateParams(final byte[] d, final ECDomainParameters domainParameters) {
        if (null == d) {
            return null;
        }
        return toPrivateParams(BigIntegers.fromUnsignedByteArray(d), domainParameters);
    }

    /**
     * 转换为 ECPrivateKeyParameters
     *
     * @param d                私钥d值
     * @param domainParameters ECDomainParameters
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toPrivateParams(final BigInteger d, final ECDomainParameters domainParameters) {
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
    public static ECPrivateKeyParameters toPrivateParams(final PrivateKey privateKey) {
        if (null == privateKey) {
            return null;
        }
        try {
            return (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(privateKey);
        } catch (final InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 将SM2算法的{@link ECPrivateKey} 转换为 {@link PrivateKey}
     *
     * @param privateKey {@link ECPrivateKey}
     * @return {@link PrivateKey}
     */
    public static PrivateKey toSm2PrivateKey(final ECPrivateKey privateKey) {
        try {
            final PrivateKeyInfo info = new PrivateKeyInfo(
                    new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, Builder.ID_SM2_PUBLIC_KEY_PARAM), privateKey);
            return generatePrivateKey("SM2", info.getEncoded());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建{@link OpenSSHPrivateKeySpec}
     *
     * @param key 私钥，需为PKCS#1格式
     * @return {@link OpenSSHPrivateKeySpec}
     */
    public static KeySpec createOpenSSHPrivateKeySpec(final byte[] key) {
        return new OpenSSHPrivateKeySpec(key);
    }

    /**
     * 创建{@link OpenSSHPublicKeySpec}
     *
     * @param key 公钥，需为PKCS#1格式
     * @return {@link OpenSSHPublicKeySpec}
     */
    public static KeySpec createOpenSSHPublicKeySpec(final byte[] key) {
        return new OpenSSHPublicKeySpec(key);
    }

    /**
     * 尝试解析转换各种类型私钥为{@link ECPrivateKeyParameters}，支持包括：
     *
     * <ul>
     *     <li>D值</li>
     *     <li>PKCS#8</li>
     *     <li>PKCS#1</li>
     * </ul>
     *
     * @param privateKeyBytes 私钥
     * @return {@link ECPrivateKeyParameters}
     */
    public static ECPrivateKeyParameters decodePrivateKeyParams(final byte[] privateKeyBytes) {
        if (null == privateKeyBytes) {
            return null;
        }
        try {
            // 尝试D值
            return toSm2PrivateParams(privateKeyBytes);
        } catch (final Exception ignore) {
            // ignore
        }

        PrivateKey privateKey;
        //尝试PKCS#8
        try {
            privateKey = generatePrivateKey("sm2", privateKeyBytes);
        } catch (final Exception ignore) {
            // 尝试PKCS#1
            privateKey = generatePrivateKey("sm2", createOpenSSHPrivateKeySpec(privateKeyBytes));
        }

        return toPrivateParams(privateKey);
    }

    /**
     * 尝试解析转换各种类型公钥为{@link ECPublicKeyParameters}，支持包括：
     *
     * <ul>
     *     <li>Q值</li>
     *     <li>X.509</li>
     *     <li>PKCS#1</li>
     * </ul>
     *
     * @param publicKeyBytes 公钥
     * @return {@link ECPublicKeyParameters}
     */
    public static ECPublicKeyParameters decodePublicKeyParams(final byte[] publicKeyBytes) {
        if (null == publicKeyBytes) {
            return null;
        }
        try {
            // 尝试Q值
            return toSm2PublicParams(publicKeyBytes);
        } catch (final Exception ignore) {
            // ignore
        }

        PublicKey publicKey;
        //尝试X.509
        try {
            publicKey = generatePublicKey("sm2", publicKeyBytes);
        } catch (final Exception ignore) {
            // 尝试PKCS#1
            publicKey = generatePublicKey("sm2", createOpenSSHPublicKeySpec(publicKeyBytes));
        }

        return toPublicParams(publicKey);
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @return {@link KeyStore}
     */
    public static Certificate readX509Certificate(final InputStream in) {
        return readCertificate(TYPE_X509, in);
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param in       {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     */
    public static Certificate readX509Certificate(final InputStream in, final char[] password, final String alias) {
        return readCertificate(TYPE_X509, in, password, alias);
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param type     类型，例如X.509
     * @param in       {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     */
    public static Certificate readCertificate(final String type, final InputStream in, final char[] password, final String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        try {
            return keyStore.getCertificate(alias);
        } catch (final KeyStoreException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     * see: <a href="http://snowolf.iteye.com/blog/391931">...</a>
     *
     * @param type 类型，例如X.509
     * @param in   {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(File)} 读取
     * @return {@link Certificate}
     */
    public static Certificate readCertificate(final String type, final InputStream in) {
        try {
            return getCertificateFactory(type).generateCertificate(in);
        } catch (final CertificateException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 获得 Certification
     *
     * @param keyStore {@link KeyStore}
     * @param alias    别名
     * @return {@link Certificate}
     */
    public static Certificate getCertificate(final KeyStore keyStore, final String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 获取{@link CertificateFactory}
     *
     * @param type 类型，例如X.509
     * @return {@link KeyPairGenerator}
     */
    public static CertificateFactory getCertificateFactory(final String type) {
        final Provider provider = Holder.getProvider();

        final CertificateFactory factory;
        try {
            factory = (null == provider) ? CertificateFactory.getInstance(type) : CertificateFactory.getInstance(type, provider);
        } catch (final CertificateException e) {
            throw new CryptoException(e);
        }
        return factory;
    }

}
