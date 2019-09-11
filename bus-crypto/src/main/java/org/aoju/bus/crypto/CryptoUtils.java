/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.crypto;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Validator;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.*;
import org.aoju.bus.crypto.asymmetric.RSA;
import org.aoju.bus.crypto.asymmetric.SM2;
import org.aoju.bus.crypto.asymmetric.Sign;
import org.aoju.bus.crypto.digest.BCrypt;
import org.aoju.bus.crypto.digest.Digester;
import org.aoju.bus.crypto.digest.HMac;
import org.aoju.bus.crypto.digest.MD5;
import org.aoju.bus.crypto.factory.AesCryptoFactory;
import org.aoju.bus.crypto.factory.DesCryptoFactory;
import org.aoju.bus.crypto.factory.RsaCryptoFactory;
import org.aoju.bus.crypto.symmetric.*;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.*;
import java.util.Map;

/**
 * 安全相关工具类
 * 加密分为三种：
 * 1、对称加密（symmetric），例如：AES、DES等
 * 2、非对称加密（asymmetric），例如：RSA、DSA等
 * 3、摘要加密（digest），例如：MD5、SHA-1、SHA-256、HMAC等
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public final class CryptoUtils {

    /**
     * Java密钥库(Java Key Store，JKS)KEY_STORE
     */
    public static final String KEY_STORE = "JKS";
    public static final String X509 = "X.509";

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

    private final static int RS_LEN = 32;
    /**
     * Table for HEX to DEC byte translation.
     */
    private static final int[] DEC = {
            00, 01, 02, 03, 04, 05, 06, 07, 8, 9, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15,
    };
    /**
     * Table for DEC to HEX byte translation.
     */
    private static final byte[] HEX =
            {(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
                    (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
                    (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'};
    /**
     * Table for byte to hex string translation.
     */
    private static final char[] hex = "0123456789abcdef".toCharArray();
    private static String SM3 = "SM3";
    private static String SM4 = "SM4";

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param mode 算法，支持PBE算法
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String mode) {
        return generateKey(mode, -1);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param mode    算法，支持PBE算法
     * @param keySize 密钥长度
     * @return {@link SecretKey}
     * @since 3.1.9
     */
    public static SecretKey generateKey(String mode, int keySize) {
        mode = getMainAlgorithm(mode);

        final KeyGenerator keyGenerator = getKeyGenerator(mode);
        if (keySize > 0) {
            keyGenerator.init(keySize);
        } else if (ModeType.AES.equals(mode)) {
            // 对于AES的密钥，除非指定，否则强制使用128位
            keyGenerator.init(128);
        }
        return keyGenerator.generateKey();
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param mode 算法
     * @param key  密钥，如果为{@code null} 自动生成随机密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String mode, byte[] key) {
        Assert.notBlank(mode, "mode is blank!");
        SecretKey secretKey;
        if (mode.startsWith("PBE")) {
            // PBE密钥
            secretKey = generatePBEKey(mode, (null == key) ? null : StringUtils.str(key, Symbol.SLASH).toCharArray());
        } else if (mode.startsWith("DES")) {
            // DES密钥
            secretKey = generateDESKey(mode, key);
        } else {
            // 其它算法密钥
            secretKey = (null == key) ? generateKey(mode) : new SecretKeySpec(key, mode);
        }
        return secretKey;
    }

    /**
     * 生成 {@link SecretKey}
     *
     * @param mode DES算法，包括DES、DESede等
     * @param key  密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateDESKey(String mode, byte[] key) {
        if (StringUtils.isBlank(mode) || false == mode.startsWith("DES")) {
            throw new CommonException("mode [{}] is not a DES mode!");
        }

        SecretKey secretKey = null;
        if (null == key) {
            secretKey = generateKey(mode);
        } else {
            KeySpec keySpec;
            try {
                if (mode.startsWith("DESede")) {
                    // DESede兼容
                    keySpec = new DESedeKeySpec(key);
                } else {
                    keySpec = new DESKeySpec(key);
                }
            } catch (InvalidKeyException e) {
                throw new CommonException(e);
            }
            secretKey = generateKey(mode, keySpec);
        }
        return secretKey;
    }

    /**
     * 生成PBE {@link SecretKey}
     *
     * @param mode PBE算法，包括：PBEWithMD5AndDES、PBEWithSHA1AndDESede、PBEWithSHA1AndRC2_40等
     * @param key  密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generatePBEKey(String mode, char[] key) {
        if (StringUtils.isBlank(mode) || false == mode.startsWith("PBE")) {
            throw new CommonException("mode [{}] is not a PBE mode!");
        }

        if (null == key) {
            key = RandomUtils.randomString(32).toCharArray();
        }
        PBEKeySpec keySpec = new PBEKeySpec(key);
        return generateKey(mode, keySpec);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法
     *
     * @param mode    算法
     * @param keySpec {@link KeySpec}
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String mode, KeySpec keySpec) {
        final SecretKeyFactory keyFactory = getSecretKeyFactory(mode);
        try {
            return keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 生成RSA私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     *
     * @param key 密钥，必须为DER编码存储
     * @return RSA私钥 {@link PrivateKey}
     * @since 4.5.2
     */
    public static PrivateKey generateRSAPrivateKey(byte[] key) {
        return generatePrivateKey(ModeType.RSA, key);
    }

    /**
     * 生成私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     *
     * @param mode 算法
     * @param key  密钥，必须为DER编码存储
     * @return 私钥 {@link PrivateKey}
     */
    public static PrivateKey generatePrivateKey(String mode, byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePrivateKey(mode, new PKCS8EncodedKeySpec(key));
    }

    /**
     * 生成私钥，仅用于非对称加密
     *
     * @param mode    算法
     * @param keySpec {@link KeySpec}
     * @return 私钥 {@link PrivateKey}
     * @since 3.1.1
     */
    public static PrivateKey generatePrivateKey(String mode, KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        mode = getAlgorithmAfterWith(mode);
        try {
            return getKeyFactory(mode).generatePrivate(keySpec);
        } catch (Exception e) {
            throw new CommonException(e);
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
            throw new CommonException(e);
        }
    }

    /**
     * 生成RSA公钥，仅用于非对称加密
     * 采用X509证书规范
     *
     * @param key 密钥，必须为DER编码存储
     * @return 公钥 {@link PublicKey}
     * @since 4.5.2
     */
    public static PublicKey generateRSAPublicKey(byte[] key) {
        return generatePublicKey(ModeType.RSA, key);
    }

    /**
     * 生成公钥，仅用于非对称加密
     * 采用X509证书规范
     *
     * @param mode 算法
     * @param key  密钥，必须为DER编码存储
     * @return 公钥 {@link PublicKey}
     */
    public static PublicKey generatePublicKey(String mode, byte[] key) {
        if (null == key) {
            return null;
        }
        return generatePublicKey(mode, new X509EncodedKeySpec(key));
    }

    /**
     * 生成公钥，仅用于非对称加密
     *
     * @param mode    算法
     * @param keySpec {@link KeySpec}
     * @return 公钥 {@link PublicKey}
     * @since 3.1.1
     */
    public static PublicKey generatePublicKey(String mode, KeySpec keySpec) {
        if (null == keySpec) {
            return null;
        }
        mode = getAlgorithmAfterWith(mode);
        try {
            return getKeyFactory(mode).generatePublic(keySpec);
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    /**
     * 生成用于非对称加密的公钥和私钥，仅用于非对称加密
     *
     * @param mode 非对称加密算法
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String mode) {
        return generateKeyPair(mode, DEFAULT_KEY_SIZE);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     *
     * @param mode    非对称加密算法
     * @param keySize 密钥模（modulus ）长度
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String mode, int keySize) {
        return generateKeyPair(mode, keySize, null);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     *
     * @param mode    非对称加密算法
     * @param keySize 密钥模（modulus ）长度
     * @param seed    种子
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String mode, int keySize, byte[] seed) {
        // SM2算法需要单独定义其曲线生成
        if ("SM2".equalsIgnoreCase(mode)) {
            final ECGenParameterSpec sm2p256v1 = new ECGenParameterSpec(SM2_DEFAULT_CURVE);
            return generateKeyPair(mode, keySize, seed, sm2p256v1);
        }

        return generateKeyPair(mode, keySize, seed, (AlgorithmParameterSpec[]) null);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     *
     * @param mode   非对称加密算法
     * @param params {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     * @since 4.3.3
     */
    public static KeyPair generateKeyPair(String mode, AlgorithmParameterSpec params) {
        return generateKeyPair(mode, null, params);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     *
     * @param mode  非对称加密算法
     * @param param {@link AlgorithmParameterSpec}
     * @param seed  种子
     * @return {@link KeyPair}
     * @since 4.3.3
     */
    public static KeyPair generateKeyPair(String mode, byte[] seed, AlgorithmParameterSpec param) {
        return generateKeyPair(mode, DEFAULT_KEY_SIZE, seed, param);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     *
     * @param mode    非对称加密算法
     * @param keySize 密钥模（modulus ）长度
     * @param seed    种子
     * @param params  {@link AlgorithmParameterSpec}
     * @return {@link KeyPair}
     * @since 4.3.3
     */
    public static KeyPair generateKeyPair(String mode, int keySize, byte[] seed, AlgorithmParameterSpec... params) {
        mode = getAlgorithmAfterWith(mode);
        final KeyPairGenerator keyPairGen = getKeyPairGenerator(mode);

        // 密钥模（modulus ）长度初始化定义
        if (keySize > 0) {
            // key长度适配修正
            if ("EC".equalsIgnoreCase(mode) && keySize > 256) {
                // 对于EC算法，密钥长度有限制，在此使用默认256
                keySize = 256;
            }
            if (null != seed) {
                keyPairGen.initialize(keySize, new SecureRandom(seed));
            } else {
                keyPairGen.initialize(keySize);
            }
        }

        // 自定义初始化参数
        if (ArrayUtils.isNotEmpty(params)) {
            for (AlgorithmParameterSpec param : params) {
                if (null == param) {
                    continue;
                }
                try {
                    if (null != seed) {
                        keyPairGen.initialize(param, new SecureRandom(seed));
                    } else {
                        keyPairGen.initialize(param);
                    }
                } catch (InvalidAlgorithmParameterException e) {
                    throw new CommonException(e);
                }
            }
        }
        return keyPairGen.generateKeyPair();
    }

    /**
     * 获取{@link KeyPairGenerator}
     *
     * @param mode 非对称加密算法
     * @return {@link KeyPairGenerator}
     * @since 4.4.3
     */
    public static KeyPairGenerator getKeyPairGenerator(String mode) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = (null == provider) //
                    ? KeyPairGenerator.getInstance(getMainAlgorithm(mode)) //
                    : KeyPairGenerator.getInstance(getMainAlgorithm(mode), provider);//
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException(e);
        }
        return keyPairGen;
    }

    /**
     * 获取{@link KeyFactory}
     *
     * @param mode 非对称加密算法
     * @return {@link KeyFactory}
     * @since 4.4.4
     */
    public static KeyFactory getKeyFactory(String mode) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        KeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? KeyFactory.getInstance(getMainAlgorithm(mode)) //
                    : KeyFactory.getInstance(getMainAlgorithm(mode), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException(e);
        }
        return keyFactory;
    }

    /**
     * 获取{@link SecretKeyFactory}
     *
     * @param mode 对称加密算法
     * @return {@link KeyFactory}
     * @since 4.5.2
     */
    public static SecretKeyFactory getSecretKeyFactory(String mode) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        SecretKeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? SecretKeyFactory.getInstance(getMainAlgorithm(mode)) //
                    : SecretKeyFactory.getInstance(getMainAlgorithm(mode), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException(e);
        }
        return keyFactory;
    }

    /**
     * 获取{@link KeyGenerator}
     *
     * @param mode 对称加密算法
     * @return {@link KeyGenerator}
     * @since 4.5.2
     */
    public static KeyGenerator getKeyGenerator(String mode) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        KeyGenerator generator;
        try {
            generator = (null == provider) //
                    ? KeyGenerator.getInstance(getMainAlgorithm(mode)) //
                    : KeyGenerator.getInstance(getMainAlgorithm(mode), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException(e);
        }
        return generator;
    }

    /**
     * 获取用于密钥生成的算法
     * 获取XXXwithXXX算法的后半部分算法，如果为ECDSA或SM2，返回算法为EC
     *
     * @param mode XXXwithXXX算法
     * @return 算法
     */
    public static String getAlgorithmAfterWith(String mode) {
        Assert.notNull(mode, "mode must be not null !");
        int indexOfWith = StringUtils.lastIndexOfIgnoreCase(mode, "with");
        if (indexOfWith > 0) {
            mode = StringUtils.subSuf(mode, indexOfWith + "with".length());
        }
        if ("ECDSA".equalsIgnoreCase(mode) || "SM2".equalsIgnoreCase(mode)) {
            mode = "EC";
        }
        return mode;
    }

    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param in       {@link InputStream} 如果想从文件读取文件
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readJKSKeyStore(InputStream in, char[] password) {
        return readKeyStore(KEY_STORE, in, password);
    }

    /**
     * 读取KeyStore文件
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取文件
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(String type, InputStream in, char[] password) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(type);
            keyStore.load(in, password);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return keyStore;
    }

    /**
     * 从KeyStore中获取私钥公钥
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取文件
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyPair}
     * @since 4.4.1
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
     * @since 4.4.1
     */
    public static KeyPair getKeyPair(KeyStore keyStore, char[] password, String alias) {
        PublicKey publicKey;
        PrivateKey privateKey;
        try {
            publicKey = keyStore.getCertificate(alias).getPublicKey();
            privateKey = (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception e) {
            throw new CommonException(e);
        }
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     *
     * @param in       {@link InputStream} 如果想从文件读取.cer文件
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     * @since 4.4.1
     */
    public static Certificate readX509Certificate(InputStream in, char[] password, String alias) {
        return readCertificate(X509, in, password, alias);
    }

    /**
     * 读取X.509 Certification文件中的公钥
     * Certification为证书文件
     *
     * @param in {@link InputStream} 如果想从文件读取文件
     * @return {@link KeyStore}
     * @since 4.5.2
     */
    public static PublicKey readPublicKeyFromCert(InputStream in) {
        final Certificate certificate = readX509Certificate(in);
        if (null != certificate) {
            return certificate.getPublicKey();
        }
        return null;
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     *
     * @param in {@link InputStream} 如果想从文件读取文件
     * @return {@link KeyStore}
     * @since 4.4.1
     */
    public static Certificate readX509Certificate(InputStream in) {
        return readCertificate(X509, in);
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     *
     * @param type     类型，例如X.509
     * @param in       {@link InputStream} 如果想从文件读取.cer文件
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     * @since 4.4.1
     */
    public static Certificate readCertificate(String type, InputStream in, char[] password, String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type 类型，例如X.509
     * @param in   {@link InputStream} 如果想从文件读取.cer文件
     * @return {@link Certificate}
     */
    public static Certificate readCertificate(String type, InputStream in) {
        try {
            return getCertificateFactory(type).generateCertificate(in);
        } catch (CertificateException e) {
            throw new CommonException(e);
        }
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
            throw new CommonException(e);
        }
    }

    /**
     * 获取{@link CertificateFactory}
     *
     * @param type 类型，例如X.509
     * @return {@link KeyPairGenerator}
     * @since 4.5.0
     */
    public static CertificateFactory getCertificateFactory(String type) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        CertificateFactory factory;
        try {
            factory = (null == provider) ? CertificateFactory.getInstance(type) : CertificateFactory.getInstance(type, provider);
        } catch (CertificateException e) {
            throw new CommonException(e);
        }
        return factory;
    }

    /**
     * 获取主体算法名，例如RSA/ECB/PKCS1Padding的主体算法是RSA
     *
     * @param mode 模型
     * @return 主体算法名
     * @since 4.5.2
     */
    public static String getMainAlgorithm(String mode) {
        final int slashIndex = mode.indexOf(Symbol.C_SLASH);
        if (slashIndex > 0) {
            return mode.substring(0, slashIndex);
        }
        return mode;
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
     * DESede加密（又名3DES、TripleDES），生成随机KEY。注意解密时必须使用相同 {@link DESede}对象或者使用相同KEY
     * Java中默认实现为：DESede/ECB/PKCS5Padding
     * 例：
     *
     * <pre>
     * DESede加密：desede().encrypt(data)
     * DESede解密：desede().decrypt(data)
     * </pre>
     *
     * @return {@link DESede}
     * @since 3.3.0
     */
    public static DESede desede() {
        return new DESede();
    }

    /**
     * DESede加密（又名3DES、TripleDES）
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
     * @since 3.3.0
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
     * MD5加密，生成16进制MD5字符串
     *
     * @param data 数据
     * @return MD5字符串
     */
    public static String md5(String data) {
        return new MD5().digestHex(data);
    }

    /**
     * MD5加密，生成16进制MD5字符串
     *
     * @param data 数据
     * @return MD5字符串
     */
    public static String md5(InputStream data) {
        return new MD5().digestHex(data);
    }

    /**
     * MD5加密文件，生成16进制MD5字符串
     *
     * @param dataFile 被加密文件
     * @return MD5字符串
     */
    public static String md5(File dataFile) {
        return new MD5().digestHex(dataFile);
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
        return new Digester(ModeType.SHA1);
    }

    /**
     * SHA1加密，生成16进制SHA1字符串
     *
     * @param data 数据
     * @return SHA1字符串
     */
    public static String sha1(String data) {
        return new Digester(ModeType.SHA1).digestHex(data);
    }

    /**
     * SHA1加密，生成16进制SHA1字符串
     *
     * @param data 数据
     * @return SHA1字符串
     */
    public static String sha1(InputStream data) {
        return new Digester(ModeType.SHA1).digestHex(data);
    }

    /**
     * SHA1加密文件，生成16进制SHA1字符串
     *
     * @param dataFile 被加密文件
     * @return SHA1字符串
     */
    public static String sha1(File dataFile) {
        return new Digester(ModeType.SHA1).digestHex(dataFile);
    }

    /**
     * SHA256加密
     * 例：
     * SHA256加密：sha256().digest(data)
     * SHA256加密并转为16进制字符串：sha256().digestHex(data)
     *
     * @return {@link Digester}
     * @since 4.3.2
     */
    public static Digester sha256() {
        return new Digester(ModeType.SHA256);
    }

    /**
     * SHA256加密，生成16进制SHA256字符串
     *
     * @param data 数据
     * @return SHA256字符串
     * @since 4.3.2
     */
    public static String sha256(String data) {
        return new Digester(ModeType.SHA256).digestHex(data);
    }

    /**
     * SHA256加密，生成16进制SHA256字符串
     *
     * @param data 数据
     * @return SHA1字符串
     * @since 4.3.2
     */
    public static String sha256(InputStream data) {
        return new Digester(ModeType.SHA256).digestHex(data);
    }

    /**
     * SHA256加密文件，生成16进制SHA256字符串
     *
     * @param dataFile 被加密文件
     * @return SHA256字符串
     * @since 4.3.2
     */
    public static String sha256(File dataFile) {
        return new Digester(ModeType.SHA256).digestHex(dataFile);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param mode {@link ModeType}
     * @param key  密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     * @since 3.3.0
     */
    public static HMac hmac(String mode, String key) {
        return new HMac(mode, StringUtils.bytes(key));
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param mode {@link ModeType}
     * @param key  密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     * @since 3.0.3
     */
    public static HMac hmac(String mode, byte[] key) {
        return new HMac(mode, key);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param mode {@link ModeType}
     * @param key  密钥{@link SecretKey}，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     * @since 3.0.3
     */
    public static HMac hmac(String mode, SecretKey key) {
        return new HMac(mode, key);
    }

    /**
     * HmacMD5加密器
     * 例：
     * HmacMD5加密：hmacMd5(key).digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5(key).digestHex(data)
     *
     * @param key 加密密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     * @since 3.3.0
     */
    public static HMac hmacMd5(String key) {
        return hmacMd5(StringUtils.bytes(key));
    }

    /**
     * HmacMD5加密器
     * 例：
     * HmacMD5加密：hmacMd5(key).digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5(key).digestHex(data)
     *
     * @param key 加密密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(byte[] key) {
        return new HMac(ModeType.HmacMD5, key);
    }

    /**
     * HmacMD5加密器，生成随机KEY
     * 例：
     * HmacMD5加密：hmacMd5().digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacMd5() {
        return new HMac(ModeType.HmacMD5);
    }

    /**
     * HmacSHA1加密器
     * 例：
     * HmacSHA1加密：hmacSha1(key).digest(data)
     * HmacSHA1加密并转为16进制字符串：hmacSha1(key).digestHex(data)
     *
     * @param key 加密密钥，如果为<code>null</code>生成随机密钥
     * @return {@link HMac}
     * @since 3.3.0
     */
    public static HMac hmacSha1(String key) {
        return hmacSha1(StringUtils.bytes(key));
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
        return new HMac(ModeType.HmacSHA1, key);
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
        return new HMac(ModeType.HmacSHA1);
    }

    /**
     * 创建RSA算法对象
     * 生成新的私钥公钥对
     *
     * @return {@link RSA}
     * @since 3.1.9
     */
    public static RSA rsa() {
        return new RSA();
    }

    /**
     * 创建RSA算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     * @return {@link RSA}
     * @since 3.1.9
     */
    public static RSA rsa(String privateKeyBase64, String publicKeyBase64) {
        return new RSA(privateKeyBase64, publicKeyBase64);
    }

    /**
     * 创建RSA算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link RSA}
     * @since 3.1.9
     */
    public static RSA rsa(byte[] privateKey, byte[] publicKey) {
        return new RSA(privateKey, publicKey);
    }

    /**
     * 创建签名算法对象
     * 生成新的私钥公钥对
     *
     * @param mode 签名算法
     * @return {@link Sign}
     * @since 3.3.0
     */
    public static Sign sign(String mode) {
        return new Sign(mode);
    }

    /**
     * 创建签名算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param mode             算法
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     * @return {@link Sign}
     * @since 3.3.0
     */
    public static Sign sign(String mode, String privateKeyBase64, String publicKeyBase64) {
        return new Sign(mode, privateKeyBase64, publicKeyBase64);
    }

    /**
     * 创建Sign算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param mode       算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link Sign}
     * @since 3.3.0
     */
    public static Sign sign(String mode, byte[] privateKey, byte[] publicKey) {
        return new Sign(mode, privateKey, publicKey);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param crypto 对称加密算法
     * @param params 参数
     * @return 签名
     * @since 4.0.1
     */
    public static String signParams(Symmetric crypto, Map<?, ?> params) {
        return signParams(crypto, params, Normal.EMPTY, Normal.EMPTY, true);
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
     * @return 签名
     * @since 4.0.1
     */
    public static String signParams(Symmetric crypto, Map<?, ?> params, String separator, String keyValueSeparator, boolean isIgnoreNull) {
        if (MapUtils.isEmpty(params)) {
            return null;
        }
        String paramsStr = MapUtils.join(MapUtils.sort(params), separator, keyValueSeparator, isIgnoreNull);
        return crypto.encryptHex(paramsStr);
    }

    /**
     * 对参数做md5签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params 参数
     * @return 签名
     * @since 4.0.1
     */
    public static String signParamsMd5(Map<?, ?> params) {
        return signParams(ModeType.MD5, params);
    }

    /**
     * 对参数做Sha1签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params 参数
     * @return 签名
     * @since 4.0.8
     */
    public static String signParamsSha1(Map<?, ?> params) {
        return signParams(ModeType.SHA1, params);
    }

    /**
     * 对参数做Sha256签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params 参数
     * @return 签名
     * @since 4.0.1
     */
    public static String signParamsSha256(Map<?, ?> params) {
        return signParams(ModeType.SHA256, params);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param Mode   算法
     * @param params 参数
     * @return 签名
     * @since 4.0.1
     */
    public static String signParams(String Mode, Map<?, ?> params) {
        return signParams(Mode, params, Normal.EMPTY, Normal.EMPTY, true);
    }

    /**
     * 对参数做签名
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     *
     * @param Mode              算法
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @return 签名
     * @since 4.0.1
     */
    public static String signParams(String Mode, Map<?, ?> params, String separator, String keyValueSeparator, boolean isIgnoreNull) {
        if (MapUtils.isEmpty(params)) {
            return null;
        }
        final String paramsStr = MapUtils.join(MapUtils.sort(params), separator, keyValueSeparator, isIgnoreNull);
        return new Digester(Mode).digestHex(paramsStr);
    }

    /**
     * 增加加密解密的算法提供者，默认优先使用，例如：
     *
     * <pre>
     * addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
     * </pre>
     *
     * @param provider 算法提供者
     * @since 4.1.22
     */
    public static void addProvider(Provider provider) {
        Security.insertProviderAt(provider, 0);
    }

    /**
     * 解码字符串密钥，可支持的编码如下：
     *
     * <pre>
     * 1. Hex（16进制）编码
     * 1. Base64编码
     * </pre>
     *
     * @param key 被解码的密钥字符串
     * @return 密钥
     * @since 4.3.3
     */
    public static byte[] decode(String key) {
        return Validator.isHex(key) ? HexUtils.decodeHex(key) : Base64.decode(key);
    }

    /**
     * 创建{@link Cipher}
     *
     * @param mode 算法
     * @return cipher
     * @since 4.5.2
     */
    public static Cipher createCipher(String mode) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        Cipher cipher;
        try {
            cipher = (null == provider) ? Cipher.getInstance(mode) : Cipher.getInstance(mode, provider);
        } catch (Exception e) {
            throw new CommonException(e);
        }

        return cipher;
    }

    /**
     * 创建{@link MessageDigest}
     *
     * @param mode 算法
     * @return MessageDigest
     * @since 4.5.2
     */
    public static MessageDigest createMessageDigest(String mode) {
        final Provider provider = BouncyCastleProvider.INSTANCE.getProvider();

        MessageDigest messageDigest;
        try {
            messageDigest = (null == provider) ? MessageDigest.getInstance(mode) : MessageDigest.getInstance(mode, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException(e);
        }

        return messageDigest;
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
     * 强制关闭Bouncy Castle库的使用，全局有效
     *
     * @since 4.5.2
     */
    public static void disableBouncyCastle() {
        BouncyCastleProvider.setUseBouncyCastle(false);
    }


    /**
     * 编码压缩EC公钥（基于BouncyCastle）
     *
     * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @return 压缩得到的X
     * @since 4.4.4
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey) {
        return ((BCECPublicKey) publicKey).getQ().getEncoded(true);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）
     *
     * @param encode    压缩公钥
     * @param curveName EC曲线名
     * @return PublicKey
     * @since 4.4.4
     */
    public static PublicKey decodeECPoint(String encode, String curveName) {
        return decodeECPoint(decode(encode), curveName);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）
     *
     * @param encodeByte 压缩公钥
     * @param curveName  EC曲线名
     * @return PublicKey
     * @since 4.4.4
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

        final KeyFactory PubKeyGen = getKeyFactory("EC");
        try {
            return PubKeyGen.generatePublic(new ECPublicKeySpec(point, ecSpec));
        } catch (GeneralSecurityException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 读取PEM格式的私钥
     *
     * @param pemStream pem流
     * @return {@link PrivateKey}
     * @since 4.5.2
     */
    public static PrivateKey readPrivateKey(InputStream pemStream) {
        return generateRSAPrivateKey(readKeyBytes(pemStream));
    }

    /**
     * 读取PEM格式的公钥
     *
     * @param pemStream pem流
     * @return {@link PublicKey}
     * @since 4.5.2
     */
    public static PublicKey readPublicKey(InputStream pemStream) {
        final Certificate certificate = readX509Certificate(pemStream);
        if (null == certificate) {
            return null;
        }
        return certificate.getPublicKey();
    }

    /**
     * 从pem文件中读取公钥或私钥
     * 根据类型返回{@link PublicKey} 或者 {@link PrivateKey}
     *
     * @param keyStream pem流
     * @return {@link Key}
     * @since 4.5.2
     */
    public static Key readKey(InputStream keyStream) {
        final PemObject object = readPemObject(keyStream);
        final String type = object.getType();
        if (StringUtils.isNotBlank(type) && type.endsWith("PRIVATE KEY")) {
            return generateRSAPrivateKey(object.getContent());
        } else {
            return readX509Certificate(keyStream).getPublicKey();
        }
    }

    /**
     * 从pem文件中读取公钥或私钥
     *
     * @param keyStream pem流
     * @return 密钥bytes
     * @since 4.5.2
     */
    public static byte[] readKeyBytes(InputStream keyStream) {
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
     * @since 4.5.2
     */
    public static PemObject readPemObject(InputStream keyStream) {
        PemReader pemReader = null;
        try {
            pemReader = new PemReader(IoUtils.getReader(keyStream, Symbol.SLASH));
            return pemReader.readPemObject();
        } catch (IOException e) {
            throw new CommonException(e);
        } finally {
            IoUtils.close(pemReader);
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
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     * @return {@link SM2}
     */
    public static SM2 sm2(String privateKeyStr, String publicKeyStr) {
        return new SM2(privateKeyStr, publicKeyStr);
    }

    /**
     * 创建SM2算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
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
     * @return {@link Digester}
     */
    public static Digester sm3() {
        return new Digester(SM3);
    }

    /**
     * SM3加密，生成16进制SM3字符串
     *
     * @param data 数据
     * @return SM3字符串
     */
    public static String sm3(String data) {
        return new Digester(SM3).digestHex(data);
    }

    /**
     * SM3加密，生成16进制SM3字符串
     *
     * @param data 数据
     * @return SM3字符串
     */
    public static String sm3(InputStream data) {
        return new Digester(SM3).digestHex(data);
    }

    /**
     * SM3加密文件，生成16进制SM3字符串
     *
     * @param dataFile 被加密文件
     * @return SM3字符串
     */
    public static String sm3(File dataFile) {
        return new Digester(SM3).digestHex(dataFile);
    }

    /**
     * SM4加密，生成随机KEY。注意解密时必须使用相同 {@link Symmetric}对象或者使用相同KEY
     * 例：
     *
     * <pre>
     * SM4加密：sm4().encrypt(data)
     * SM4解密：sm4().decrypt(data)
     * </pre>
     *
     * @return {@link Symmetric}
     */
    public static Symmetric sm4() {
        return new Symmetric(SM4);
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
        return new Symmetric(SM4, key);
    }

    /**
     * bc加解密使用旧标c1||c2||c3，此方法在加密后调用，将结果转化为c1||c3||c2
     *
     * @param c1c2c3             加密后的bytes，顺序为C1C2C3
     * @param ecDomainParameters {@link ECDomainParameters}
     * @return 加密后的bytes，顺序为C1C3C2
     */
    public static byte[] changeC1C2C3ToC1C3C2(byte[] c1c2c3, ECDomainParameters ecDomainParameters) {
        // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c2c3.length];
        System.arraycopy(c1c2c3, 0, result, 0, c1Len); // c1
        System.arraycopy(c1c2c3, c1c2c3.length - c3Len, result, c1Len, c3Len); // c3
        System.arraycopy(c1c2c3, c1Len, result, c1Len + c3Len, c1c2c3.length - c1Len - c3Len); // c2
        return result;
    }

    /**
     * bc加解密使用旧标c1||c3||c2，此方法在解密前调用，将密文转化为c1||c2||c3再去解密
     *
     * @param c1c3c2             加密后的bytes，顺序为C1C3C2
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
     * BC的SM3withSM2签名得到的结果的rs是asn1格式的，这个方法转化成直接拼接r||s
     *
     * @param rsDer rs in asn1 format
     * @return sign result in plain byte array
     * @since 4.5.0
     */
    public static byte[] rsAsn1ToPlain(byte[] rsDer) {
        ASN1Sequence seq = ASN1Sequence.getInstance(rsDer);
        byte[] r = bigIntToFixexLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(0)).getValue());
        byte[] s = bigIntToFixexLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(1)).getValue());
        byte[] result = new byte[RS_LEN * 2];
        System.arraycopy(r, 0, result, 0, r.length);
        System.arraycopy(s, 0, result, RS_LEN, s.length);
        return result;
    }

    /**
     * BC的SM3withSM2验签需要的rs是asn1格式的，这个方法将直接拼接r||s的字节数组转化成asn1格式
     *
     * @param sign in plain byte array
     * @return rs result in asn1 format
     * @since 4.5.0
     */
    public static byte[] rsPlainToAsn1(byte[] sign) {
        if (sign.length != RS_LEN * 2) {
            throw new CommonException("err rs. ");
        }
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, RS_LEN));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, RS_LEN, RS_LEN * 2));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        try {
            return new DERSequence(v).getEncoded("DER");
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    /**
     * BigInteger转固定长度bytes
     *
     * @param rOrS {@link BigInteger}
     * @return 固定长度bytes
     * @since 4.5.0
     */
    private static byte[] bigIntToFixexLengthBytes(BigInteger rOrS) {
        // for sm2p256v1, n is 00fffffffeffffffffffffffffffffffff7203df6b21c6052b53bbf40939d54123,
        // r and s are the result of mod n, so they should be less than n and have length<=32
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
            throw new CommonException("Error rs: {}", Hex.toHexString(rs));
        }
    }


    /**
     * 计算16位MD5摘要值
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] md5(byte[] data) {
        return new MD5().digest(data);
    }

    /**
     * 计算16位MD5摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要
     */
    public static byte[] md5(String data, String charset) {
        return new MD5().digest(data, charset);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(byte[] data) {
        return new MD5().digestHex(data);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(String data, String charset) {
        return new MD5().digestHex(data, charset);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(String data) {
        return md5Hex(data, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(InputStream data) {
        return new MD5().digestHex(data);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(File file) {
        return new MD5().digestHex(file);
    }

    /**
     * 32位MD5转16位MD5
     *
     * @param md5Hex 32位MD5
     * @return 16位MD5
     * @since 4.4.1
     */
    public static String md5HexTo16(String md5Hex) {
        return md5Hex.substring(8, 24);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data 被摘要数据
     * @return SHA-1摘要
     */
    public static byte[] sha1(byte[] data) {
        return new Digester(ModeType.SHA1).digest(data);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-1摘要
     */
    public static byte[] sha1(String data, String charset) {
        return new Digester(ModeType.SHA1).digest(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(byte[] data) {
        return new Digester(ModeType.SHA1).digestHex(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(String data, String charset) {
        return new Digester(ModeType.SHA1).digestHex(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(String data) {
        return sha1Hex(data, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(InputStream data) {
        return new Digester(ModeType.SHA1).digestHex(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(File file) {
        return new Digester(ModeType.SHA1).digestHex(file);
    }

    /**
     * 计算SHA-256摘要值
     *
     * @param data 被摘要数据
     * @return SHA-256摘要
     */
    public static byte[] sha256(byte[] data) {
        return new Digester(ModeType.SHA256).digest(data);
    }

    /**
     * 计算SHA-256摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-256摘要
     */
    public static byte[] sha256(String data, String charset) {
        return new Digester(ModeType.SHA256).digest(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(byte[] data) {
        return new Digester(ModeType.SHA256).digestHex(data);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(String data, String charset) {
        return new Digester(ModeType.SHA256).digestHex(data, charset);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(String data) {
        return sha256Hex(data, org.aoju.bus.core.consts.Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(InputStream data) {
        return new Digester(ModeType.SHA256).digestHex(data);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(File file) {
        return new Digester(ModeType.SHA256).digestHex(file);
    }

    /**
     * 新建摘要器
     *
     * @param mode 签名算法
     * @return Digester
     * @since 4.2.1
     */
    public static Digester digester(String mode) {
        return new Digester(mode);
    }

    /**
     * 生成Bcrypt加密后的密文
     *
     * @param password 明文密码
     * @return 加密后的密文
     * @since 4.1.1
     */
    public static String bcrypt(String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 验证密码是否与Bcrypt加密后的密文匹配
     *
     * @param password 明文密码
     * @param hashed   密文
     * @return 是否匹配
     * @since 4.1.1
     */
    public static boolean bcryptCheck(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    private static CryptoFactory getFactory(String type) {
        if (ModeType.AES.equals(type)) {
            return LazyCryptoHolder.AES_CRYPTO_FACTORY;
        } else if (ModeType.DES.equals(type)) {
            return LazyCryptoHolder.DES_CRYPTO_FACTORY;
        } else if (ModeType.RSA.equals(type)) {
            return LazyCryptoHolder.RSA_CRYPTO_FACTORY;
        } else {
            throw new NullPointerException("未检测到加密");
        }
    }

    public static InputStream encrypt(String type, String key, InputStream inputStream) {
        final CryptoFactory factory = getFactory(type);
        final byte[] bytes = IoUtils.readBytes(inputStream);
        return new ByteArrayInputStream(factory.encrypt(key, bytes));
    }

    public static InputStream decrypt(String type, String key, InputStream inputStream) {
        final CryptoFactory factory = getFactory(type);
        final byte[] bytes = IoUtils.readBytes(inputStream);
        final byte[] decrypt = factory.decrypt(key, bytes);
        return new ByteArrayInputStream(decrypt);
    }

    public static byte[] encrypt(String type, String key, byte[] content) {
        final CryptoFactory factory = getFactory(type);
        return factory.encrypt(key, content);
    }

    public static byte[] decrypt(String type, String key, byte[] content) {
        final CryptoFactory factory = getFactory(type);
        return factory.decrypt(key, content);
    }

    public static String encrypt(String type, String key, String content, Charset charset) {
        return toHexString(encrypt(type, key, content.getBytes(charset)));
    }

    public static String decrypt(String type, String key, String content, Charset charset) {
        final byte[] bytes = fromHexString(content);
        final byte[] decrypt = decrypt(type, key, bytes);
        return new String(decrypt, charset);
    }

    public static int getDec(int index) {
        try {
            return DEC[index - '0'];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    public static byte getHex(int index) {
        return HEX[index];
    }

    public static String toHexString(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        StringBuilder sb = new StringBuilder(bytes.length << 1);
        for (int i = 0; i < bytes.length; ++i) {
            sb.append(hex[(bytes[i] & 0xf0) >> 4])
                    .append(hex[(bytes[i] & 0x0f)])
            ;
        }
        return sb.toString();
    }

    public static byte[] fromHexString(String input) {
        if (input == null) {
            return null;
        }

        if ((input.length() & 1) == 1) {
            // Odd number of characters
            throw new IllegalArgumentException("The input must consist of an even number of hex digits");
        }
        char[] inputChars = input.toCharArray();
        byte[] result = new byte[input.length() >> 1];
        for (int i = 0; i < result.length; i++) {
            int upperNibble = getDec(inputChars[2 * i]);
            int lowerNibble = getDec(inputChars[2 * i + 1]);
            if (upperNibble < 0 || lowerNibble < 0) {
                // Non hex character
                throw new IllegalArgumentException("The input must consist only of hex digits");
            }
            result[i] = (byte) ((upperNibble << 4) + lowerNibble);
        }
        return result;
    }

    private static class LazyCryptoHolder {
        private final static CryptoFactory AES_CRYPTO_FACTORY = new AesCryptoFactory();
        private final static CryptoFactory DES_CRYPTO_FACTORY = new DesCryptoFactory();
        private final static CryptoFactory RSA_CRYPTO_FACTORY = new RsaCryptoFactory();
    }

}
