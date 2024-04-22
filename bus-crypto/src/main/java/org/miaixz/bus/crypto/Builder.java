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

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.spec.OpenSSHPrivateKeySpec;
import org.bouncycastle.jcajce.spec.OpenSSHPublicKeySpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.CryptoException;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.io.stream.FastByteOutputStream;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.toolkit.*;
import org.miaixz.bus.crypto.asymmetric.RSA;
import org.miaixz.bus.crypto.asymmetric.SM2;
import org.miaixz.bus.crypto.asymmetric.Sign;
import org.miaixz.bus.crypto.digest.*;
import org.miaixz.bus.crypto.digest.mac.BCHMacEngine;
import org.miaixz.bus.crypto.digest.mac.MacEngine;
import org.miaixz.bus.crypto.symmetric.*;

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
import java.security.interfaces.RSAPrivateCrtKey;
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
 * @since Java 17+
 */
public class Builder {

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
    public static final int DEFAULT_KEY_SIZE = Normal._1024;
    /**
     * SM2默认曲线
     *
     * <pre>
     * Default SM2 curve
     * </pre>
     */
    public static final String SM2_DEFAULT_CURVE = "sm2p256v1";
    /**
     * SM2推荐曲线参数（来自https://github.com/ZZMarquis/gmhelper）
     */
    public static final ECDomainParameters SM2_DOMAIN_PARAMS = toDomainParams(GMNamedCurves.getByName(SM2_DEFAULT_CURVE));
    /**
     * SM2国密算法公钥参数的Oid标识
     */
    public static final ASN1ObjectIdentifier ID_SM2_PUBLIC_KEY_PARAM = new ASN1ObjectIdentifier("1.2.156.10197.1.301");

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
     * 计算sha256摘要值，使用UTF-8编码
     *
     * @param data 被摘要数据
     * @return SHA-256摘要
     */
    public static byte[] sha256(String data) {
        return sha256(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-256摘要值
     *
     * @param data 被摘要数据
     * @return SHA-256摘要
     */
    public static byte[] sha256(InputStream data) {
        return new Digester(Algorithm.SHA256).digest(data);
    }

    /**
     * 计算SHA-256摘要值
     *
     * @param file 被摘要文件
     * @return SHA-256摘要
     */
    public static byte[] sha256(File file) {
        return new Digester(Algorithm.SHA256).digest(file);
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
     * @param key       密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(Algorithm algorithm, String key) {
        return new HMac(algorithm, StringKit.isNotEmpty(key) ? StringKit.bytes(key) : null);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(Algorithm algorithm, byte[] key) {
        if (ArrayKit.isEmpty(key)) {
            key = generateKey(algorithm.getValue()).getEncoded();
        }
        return new HMac(algorithm, key);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥{@link SecretKey}，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(Algorithm algorithm, SecretKey key) {
        if (ObjectKit.isNull(key)) {
            key = generateKey(algorithm.getValue());
        }
        return new HMac(algorithm, key);
    }

    /**
     * HmacMD5加密器
     * 例：
     * HmacMD5加密：hmacMd5(key).digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5(key).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(String key) {
        return hmacMd5(StringKit.isNotEmpty(key) ? StringKit.bytes(key) : null);
    }

    /**
     * HmacMD5加密器
     * 例：
     * HmacMD5加密：hmacMd5(key).digest(data)
     * HmacMD5加密并转为16进制字符串：hmacMd5(key).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(byte[] key) {
        if (ArrayKit.isEmpty(key)) {
            key = generateKey(Algorithm.HMACMD5.getValue()).getEncoded();
        }
        return new HMac(Algorithm.HMACMD5, key);
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
        return new HMac(Algorithm.HMACMD5);
    }

    /**
     * HmacSHA1加密器
     * 例：
     * HmacSHA1加密：hmacSha1(key).digest(data)
     * HmacSHA1加密并转为16进制字符串：hmacSha1(key).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha1(String key) {
        return hmacSha1(StringKit.isNotEmpty(key) ? StringKit.bytes(key) : null);
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
        if (ArrayKit.isEmpty(key)) {
            key = generateKey(Algorithm.HMACMD5.getValue()).getEncoded();
        }
        return new HMac(Algorithm.HMACSHA1, key);
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
        return new HMac(Algorithm.HMACSHA1);
    }

    /**
     * HmacSHA256加密器
     * 例：
     * HmacSHA256加密：hmacSha256(key).digest(data)
     * HmacSHA256加密并转为16进制字符串：hmacSha256(key).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha256(String key) {
        return hmacSha256(StringKit.isNotEmpty(key) ? StringKit.bytes(key) : null);
    }

    /**
     * HmacSHA256加密器
     * 例：
     * HmacSHA256加密：hmacSha256(key).digest(data)
     * HmacSHA256加密并转为16进制字符串：hmacSha256(key).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha256(byte[] key) {
        if (ArrayKit.isEmpty(key)) {
            key = generateKey(Algorithm.HMACMD5.getValue()).getEncoded();
        }
        return new HMac(Algorithm.HMACSHA256, key);
    }

    /**
     * HmacSHA256加密器，生成随机KEY
     * 例：
     * HmacSHA256加密：hmacSha256().digest(data)
     * HmacSHA256加密并转为16进制字符串：hmacSha256().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacSha256() {
        return new HMac(Algorithm.HMACSHA256);
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
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥，必须使用PKCS#8规范
     * @param publicKey  公钥，必须使用X509规范
     * @return {@link SM2}
     */
    public static SM2 sm2(byte[] privateKey, byte[] publicKey) {
        return new SM2(privateKey, publicKey);
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
    public static SM2 sm2(PrivateKey privateKey, PublicKey publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * 创建SM2算法对象
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyParams 私钥参数
     * @param publicKeyParams  公钥参数
     * @return {@link SM2}
     */
    public static SM2 sm2(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams) {
        return new SM2(privateKeyParams, publicKeyParams);
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
     * SM3加密，可以传入盐
     *
     * @param salt 加密盐
     * @return {@link SM3}
     */
    public static SM3 sm3(byte[] salt) {
        return new SM3(salt);
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
     * SM4加密,生成随机KEY 注意解密时必须使用相同 {@link SM4}对象或者使用相同KEY
     * 例：
     *
     * <pre>
     * SM4加密：sm4().encrypt(data)
     * SM4解密：sm4().decrypt(data)
     * </pre>
     *
     * @return {@link SM4}
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
     * @return {@link SM4}
     */
    public static SM4 sm4(byte[] key) {
        return new SM4(key);
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
     * @return {@link Crypto}
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
     */
    public static DESede desede(byte[] key) {
        return new DESede(key);
    }

    /**
     * 创建签名算法对象
     * 生成新的私钥公钥对
     *
     * @param algorithm 签名算法
     * @return {@link Sign}
     */
    public static Sign sign(Algorithm algorithm) {
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
    public static Sign sign(Algorithm algorithm, String privateKey, String publicKey) {
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
    public static Sign sign(Algorithm algorithm, byte[] privateKey, byte[] publicKey) {
        return new Sign(algorithm, privateKey, publicKey);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param data 被摘要数据
     * @return SHA-512摘要
     */
    public static byte[] sha512(final byte[] data) {
        return new Digester(Algorithm.SHA512).digest(data);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-512摘要
     */
    public static byte[] sha512(final String data, final String charset) {
        return new Digester(Algorithm.SHA512).digest(data, charset);
    }

    /**
     * 计算sha512摘要值，使用UTF-8编码
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] sha512(final String data) {
        return sha512(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param data 被摘要数据
     * @return SHA-512摘要
     */
    public static byte[] sha512(final InputStream data) {
        return new Digester(Algorithm.SHA512).digest(data);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param file 被摘要文件
     * @return SHA-512摘要
     */
    public static byte[] sha512(final File file) {
        return new Digester(Algorithm.SHA512).digest(file);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final byte[] data) {
        return new Digester(Algorithm.SHA512).digestHex(data);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final String data, final String charset) {
        return new Digester(Algorithm.SHA512).digestHex(data, charset);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final String data) {
        return sha512Hex(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final InputStream data) {
        return new Digester(Algorithm.SHA512).digestHex(data);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final File file) {
        return new Digester(Algorithm.SHA512).digestHex(file);
    }

    /**
     * 新建摘要器
     *
     * @param algorithm 签名算法
     * @return Digester
     */
    public static Digester digester(Algorithm algorithm) {
        return new Digester(algorithm);
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
        return HexKit.encodeHexString(encrypt(algorithm, key, content.getBytes(charset)));
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
     * 编码为DER格式
     *
     * @param elements ASN.1元素
     * @return 编码后的bytes
     */
    public static byte[] encode(ASN1Encodable... elements) {
        return encode(ASN1Encoding.DER, elements);
    }

    /**
     * 编码为指定ASN1格式
     *
     * @param asn1Encoding 编码格式，见{@link ASN1Encoding}，可选DER、BER或DL
     * @param elements     ASN.1元素
     * @return 编码后的bytes
     */
    public static byte[] encode(String asn1Encoding, ASN1Encodable... elements) {
        final FastByteOutputStream out = new FastByteOutputStream();
        encode(asn1Encoding, out, elements);
        return out.toByteArray();
    }

    /**
     * 编码为指定ASN1格式
     *
     * @param asn1Encoding 编码格式，见{@link ASN1Encoding}，可选DER、BER或DL
     * @param out          输出流
     * @param elements     ASN.1元素
     */
    public static void encode(String asn1Encoding, OutputStream out, ASN1Encodable... elements) {
        org.bouncycastle.asn1.ASN1Sequence sequence;
        switch (asn1Encoding) {
            case ASN1Encoding.DER:
                sequence = new DERSequence(elements);
                break;
            case ASN1Encoding.BER:
                sequence = new BERSequence(elements);
                break;
            case ASN1Encoding.DL:
                sequence = new DLSequence(elements);
                break;
            default:
                throw new CryptoException("Unsupported ASN1 encoding: {}", asn1Encoding);
        }
        try {
            sequence.encodeTo(out);
        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 读取ASN.1数据流为{@link ASN1Object}
     *
     * @param in ASN.1数据
     * @return {@link ASN1Object}
     */
    public static ASN1Object decode(InputStream in) {
        final ASN1InputStream asn1In = new ASN1InputStream(in);
        try {
            return asn1In.readObject();
        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 获取ASN1格式的导出格式，一般用于调试
     *
     * @param in ASN.1数据
     * @return {@link ASN1Object}的字符串表示形式
     * @see ASN1Dump#dumpAsString(Object)
     */
    public static String getDumpString(InputStream in) {
        return ASN1Dump.dumpAsString(decode(in));
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
     * 编码压缩EC公钥（基于BouncyCastle），即Q值
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @return 压缩得到的Q
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey) {
        return encodeECPublicKey(publicKey, true);
    }

    /**
     * 编码压缩EC公钥（基于BouncyCastle），即Q值
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey    {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @param isCompressed 是否压缩
     * @return 得到的Q
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey, boolean isCompressed) {
        return ((BCECPublicKey) publicKey).getQ().getEncoded(isCompressed);
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
     */
    public static byte[] decode(String key) {
        return Validator.isHex(key) ? HexKit.decodeHex(key) : Base64.decode(key);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param encode    压缩公钥
     * @param curveName EC曲线名
     * @return 公钥
     */
    public static PublicKey decodeECPoint(String encode, String curveName) {
        return decodeECPoint(decode(encode), curveName);
    }

    /**
     * 解码恢复EC压缩公钥,支持Base64和Hex编码,（基于BouncyCastle）
     *
     * @param encodeByte 压缩公钥
     * @param curveName  EC曲线名，例如{@link #SM2_DOMAIN_PARAMS}
     * @return 公钥
     */
    public static PublicKey decodeECPoint(byte[] encodeByte, String curveName) {
        final X9ECParameters x9ECParameters = ECUtil.getNamedCurveByName(curveName);
        final ECCurve curve = x9ECParameters.getCurve();
        final ECPoint point = EC5Util.convertPoint(curve.decodePoint(encodeByte));

        // 根据曲线恢复公钥格式
        final ECNamedCurveSpec ecSpec = new ECNamedCurveSpec(curveName, curve, x9ECParameters.getG(), x9ECParameters.getN());
        return generatePublicKey("EC", new ECPublicKeySpec(point, ecSpec));
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param parameterSpec ECParameterSpec
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(ECParameterSpec parameterSpec) {
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
     * 转换为 ECPrivateKeyParameters
     *
     * @param d 私钥d值
     * @return ECPrivateKeyParameters
     */
    public static ECPrivateKeyParameters toSm2Params(String d) {
        return toSm2PrivateParams(d);
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
        return toSm2PrivateParams(d);
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
        return toSm2PublicParams(xHex, yHex);
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
        return toPublicParams(xHex, yHex, domainParameters);
    }

    /**
     * 转换为SM2的ECPublicKeyParameters
     *
     * @param xBytes 公钥X
     * @param yBytes 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toSm2Params(byte[] xBytes, byte[] yBytes) {
        return toSm2PublicParams(xBytes, yBytes);
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
     * Java中的PKCS#8格式私钥转换为OpenSSL支持的PKCS#1格式
     *
     * @param privateKey PKCS#8格式私钥
     * @return PKCS#1格式私钥
     */
    public static byte[] toPkcs1(PrivateKey privateKey) {
        final PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        try {
            return pkInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Java中的X.509格式公钥转换为OpenSSL支持的PKCS#1格式
     *
     * @param publicKey X.509格式公钥
     * @return PKCS#1格式公钥
     */
    public static byte[] toPkcs1(PublicKey publicKey) {
        final SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo
                .getInstance(publicKey.getEncoded());
        try {
            return spkInfo.parsePublicKey().getEncoded();
        } catch (IOException e) {
            throw new CryptoException(e);
        }
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
    public static String signParams(Crypto crypto, Map<?, ?> params, String... other) {
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
    public static String signParams(Crypto crypto, Map<?, ?> params, String separator,
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
     * @param algorithm 摘要算法
     * @param params    参数
     * @param other     其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParams(Algorithm algorithm, Map<?, ?> params, String... other) {
        return signParams(algorithm, params, Normal.EMPTY, Normal.EMPTY, true, other);
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
     * @param other             其它附加参数字符串(例如密钥)
     * @return 签名
     */
    public static String signParams(Algorithm algorithm, Map<?, ?> params, String separator,
                                    String keyValueSeparator, boolean isIgnoreNull, String... other) {
        return new Digester(algorithm).digestHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, other));
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
     * 根据私钥参数获取公钥参数
     *
     * @param privateKeyParameters 私钥参数
     * @return 公钥参数
     */
    public static ECPublicKeyParameters getPublicParams(ECPrivateKeyParameters privateKeyParameters) {
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
     * @return ECPublicKeyParameters，x或y为{@code null}则返回{@code null}
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
            throw new CryptoException(e);
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
    public static ECPrivateKeyParameters toPrivateParams(PrivateKey privateKey) {
        if (null == privateKey) {
            return null;
        }
        try {
            return (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(privateKey);
        } catch (InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 将SM2算法的{@link ECPrivateKey} 转换为 {@link PrivateKey}
     *
     * @param privateKey {@link ECPrivateKey}
     * @return {@link PrivateKey}
     */
    public static PrivateKey toSm2PrivateKey(ECPrivateKey privateKey) {
        try {
            final PrivateKeyInfo info = new PrivateKeyInfo(
                    new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, ID_SM2_PUBLIC_KEY_PARAM), privateKey);
            return generatePrivateKey("SM2", info.getEncoded());
        } catch (IOException e) {
            throw new CryptoException(e);
        }
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
     * 创建{@link OpenSSHPrivateKeySpec}
     *
     * @param key 私钥，需为PKCS#1格式
     * @return {@link OpenSSHPrivateKeySpec}
     */
    public static KeySpec createOpenSSHPrivateKeySpec(byte[] key) {
        return new OpenSSHPrivateKeySpec(key);
    }

    /**
     * 创建{@link OpenSSHPublicKeySpec}
     *
     * @param key 公钥，需为PKCS#1格式
     * @return {@link OpenSSHPublicKeySpec}
     */
    public static KeySpec createOpenSSHPublicKeySpec(byte[] key) {
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
    public static ECPrivateKeyParameters decodePrivateKeyParams(byte[] privateKeyBytes) {
        try {
            // 尝试D值
            return toSm2PrivateParams(privateKeyBytes);
        } catch (Exception ignore) {
            // ignore
        }

        PrivateKey privateKey;
        // 尝试PKCS#8
        try {
            privateKey = generatePrivateKey("sm2", privateKeyBytes);
        } catch (Exception ignore) {
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
    public static ECPublicKeyParameters decodePublicKeyParams(byte[] publicKeyBytes) {
        try {
            // 尝试Q值
            return toSm2PublicParams(publicKeyBytes);
        } catch (Exception ignore) {
            // ignore
        }

        PublicKey publicKey;
        // 尝试X.509
        try {
            publicKey = generatePublicKey(Algorithm.SM2.getValue(), publicKeyBytes);
        } catch (Exception ignore) {
            // 尝试PKCS#1
            publicKey = generatePublicKey(Algorithm.SM2.getValue(), createOpenSSHPublicKeySpec(publicKeyBytes));
        }

        return toPublicParams(publicKey);
    }

    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     * 当指定keySize&lt;0时，AES默认长度为128，其它算法不指定。
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度，&lt;0表示不设定密钥长度，即使用默认长度
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, int keySize) {
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
    public static SecretKey generateKey(String algorithm, int keySize, SecureRandom random) {
        algorithm = getMainAlgorithm(algorithm);

        final KeyGenerator keyGenerator = getKeyGenerator(algorithm);
        if (keySize <= 0 && Algorithm.AES.getValue().equals(algorithm)) {
            // 对于AES的密钥，除非指定，否则强制使用128位
            keySize = Normal._128;
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
    public static SecretKey generateKey(String algorithm, byte[] key) {
        Assert.notBlank(algorithm, "Algorithm is blank!");
        SecretKey secretKey;
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
    public static SecretKey generateDESKey(String algorithm, byte[] key) {
        if (StringKit.isBlank(algorithm) || false == algorithm.startsWith("DES")) {
            throw new CryptoException("Algorithm [{}] is not a DES algorithm!", algorithm);
        }

        SecretKey secretKey;
        if (null == key) {
            secretKey = generateKey(algorithm);
        } else {
            KeySpec keySpec;
            try {
                if (algorithm.startsWith(Algorithm.DESEDE.getValue())) {
                    // DESede兼容
                    keySpec = new DESedeKeySpec(key);
                } else {
                    keySpec = new DESKeySpec(key);
                }
            } catch (InvalidKeyException e) {
                throw new CryptoException(e);
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
            throw new CryptoException("Algorithm [{}] is not a PBE algorithm!", algorithm);
        }

        if (null == key) {
            key = RandomKit.randomString(Normal._32).toCharArray();
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
            throw new CryptoException(e);
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
        return generatePrivateKey(Algorithm.RSA.getValue(), key);
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
     * 生成私钥，仅用于非对称加密
     * 采用PKCS#8规范，此规范定义了私钥信息语法和加密私钥语法
     * 算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyFactory
     *
     * @param algorithm 算法，如RSA、EC、SM2等
     * @param key       密钥，PKCS#8格式
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
     * @param algorithm 算法，如RSA、EC、SM2等
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
    public static PrivateKey generatePrivateKey(KeyStore keyStore, String alias, char[] password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception e) {
            throw new CryptoException(e);
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
        return generatePublicKey(Algorithm.RSA.getValue(), key);
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
            throw new CryptoException(e);
        }
    }

    /**
     * 生成用于非对称加密的公钥和私钥，仅用于非对称加密
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm) {
        int keySize = DEFAULT_KEY_SIZE;
        if ("ECIES".equalsIgnoreCase(algorithm)) {
            // ECIES算法对KEY的长度有要求，此处默认256
            keySize = Normal._256;
        }

        return generateKeyPair(algorithm, keySize);
    }

    /**
     * 生成用于非对称加密的公钥和私钥
     * 密钥对生成算法见：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     *
     * @param algorithm 非对称加密算法
     * @param keySize   密钥模（modulus ）长度
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
     * @param keySize   密钥模（modulus ）长度
     * @param seed      种子
     * @return {@link KeyPair}
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize, byte[] seed) {
        // SM2算法需要单独定义其曲线生成
        if (Algorithm.SM2.getValue().equalsIgnoreCase(algorithm)) {
            final ECGenParameterSpec sm2p256v1 = new ECGenParameterSpec(SM2_DEFAULT_CURVE);
            return generateKeyPair(algorithm, keySize, seed, sm2p256v1);
        }

        return generateKeyPair(algorithm, keySize, seed, (AlgorithmParameterSpec[]) null);
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
    public static KeyPair generateKeyPair(String algorithm, int keySize, SecureRandom random, AlgorithmParameterSpec... params) {
        algorithm = getAlgorithmAfterWith(algorithm);
        final KeyPairGenerator keyPairGen = getKeyPairGenerator(algorithm);

        // 密钥模（modulus ）长度初始化定义
        if (keySize > 0) {
            // key长度适配修正
            if ("EC".equalsIgnoreCase(algorithm) && keySize > 256) {
                // 对于EC（EllipticCurve）算法，密钥长度有限制，在此使用默认256
                keySize = Normal._256;
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
                    throw new CryptoException(e);
                }
            }
        }
        return keyPairGen.generateKeyPair();
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
    public static KeyFactory getKeyFactory(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        KeyFactory keyFactory;
        try {
            keyFactory = (null == provider)
                    ? KeyFactory.getInstance(getMainAlgorithm(algorithm))
                    : KeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyFactory;
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
            throw new CryptoException(e);
        }
        return keyFactory;
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
            throw new CryptoException(e);
        }
        return generator;
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
     * 获取主体算法名，例如RSA/ECB/PKCS1Padding的主体算法是RSA
     *
     * @param algorithm XXXwithXXX算法
     * @return 主体算法名
     */
    public static String getMainAlgorithm(String algorithm) {
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

        int indexOfWith = StringKit.lastIndexOfIgnoreCase(algorithm, "with");
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
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();
        try {
            return null == provider ? KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
        } catch (final KeyStoreException e) {
            throw new CryptoException(e);
        }
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
            throw new CryptoException(e);
        }
        return new KeyPair(publicKey, privateKey);
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
    public static java.security.cert.Certificate readX509Certificate(InputStream in, char[] password, String alias) {
        return readCertificate(CERT_TYPE_X509, in, password, alias);
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
        final java.security.cert.Certificate certificate = readX509Certificate(in);
        if (null != certificate) {
            return certificate.getPublicKey();
        }
        return null;
    }

    /**
     * 读取X.509 Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @return {@link KeyStore}
     */
    public static java.security.cert.Certificate readX509Certificate(InputStream in) {
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
    public static java.security.cert.Certificate readCertificate(String type, InputStream in, char[] password, String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 读取Certification文件
     * Certification为证书文件
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type 类型，例如X.509
     * @param in   {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileKit#getInputStream(java.io.File)} 读取
     * @return {@link java.security.cert.Certificate}
     */
    public static java.security.cert.Certificate readCertificate(String type, InputStream in) {
        try {
            return getCertificateFactory(type).generateCertificate(in);
        } catch (CertificateException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 获得 Certification
     *
     * @param keyStore {@link KeyStore}
     * @param alias    别名
     * @return {@link java.security.cert.Certificate}
     */
    public static Certificate getCertificate(KeyStore keyStore, String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (Exception e) {
            throw new CryptoException(e);
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
            throw new CryptoException(e);
        }
        return factory;
    }

    /**
     * 通过RSA私钥生成RSA公钥
     *
     * @param privateKey RSA私钥
     * @return RSA公钥，null表示私钥不被支持
     */
    public static PublicKey getRSAPublicKey(PrivateKey privateKey) {
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
    public static PublicKey getRSAPublicKey(String modulus, String publicExponent) {
        return getRSAPublicKey(
                new BigInteger(modulus, Normal._16), new BigInteger(publicExponent, Normal._16));
    }

    /**
     * 从pem流中读取公钥或私钥
     *
     * @param keyStream pem流
     * @return 密钥bytes
     */
    public static byte[] readPem(InputStream keyStream) {
        final PemObject pemObject = readPemObject(keyStream);
        if (null != pemObject) {
            return pemObject.getContent();
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
    public static PublicKey getRSAPublicKey(BigInteger modulus, BigInteger publicExponent) {
        final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        try {
            return getKeyFactory(Algorithm.RSA.name()).generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 读取PEM格式的私钥
     *
     * @param pemStream pem流
     * @return {@link PrivateKey}
     */
    public static PrivateKey readPemPrivateKey(InputStream pemStream) {
        return (PrivateKey) readPemKey(pemStream);
    }

    /**
     * 读取加密的 PEM 格式私钥
     *
     * @param pemStream pem 流
     * @param password  私钥的密码
     * @return {@link PrivateKey}
     */
    public static PrivateKey readPemPrivateKey(InputStream pemStream, final char[] password) {
        return (PrivateKey) readPemKey(pemStream, password);
    }

    /**
     * 读取PEM格式的公钥
     *
     * @param pemStream pem流
     * @return {@link PublicKey}
     */
    public static PublicKey readPemPublicKey(InputStream pemStream) {
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
        return readPemKey(keyStream, null);
    }

    /**
     * 从pem文件中读取公钥或私钥
     * 根据类型返回{@link PublicKey} 或者 {@link PrivateKey}
     *
     * @param keyStream pem 流
     * @param password  私钥密码
     * @return {@link Key}，null 表示无法识别的密钥类型
     */
    public static Key readPemKey(InputStream keyStream, final char[] password) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(keyStream))) {
            Object keyObject = pemParser.readObject();
            JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter().setProvider(provider);
            if (keyObject instanceof PrivateKeyInfo) {
                // PrivateKeyInfo
                return pemKeyConverter.getPrivateKey((PrivateKeyInfo) keyObject);
            } else if (keyObject instanceof PEMKeyPair) {
                // PemKeyPair
                return pemKeyConverter.getKeyPair((PEMKeyPair) keyObject).getPrivate();
            } else if (keyObject instanceof PKCS8EncryptedPrivateKeyInfo) {
                // Encrypted PrivateKeyInfo
                InputDecryptorProvider decryptProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider(provider).build(password);
                PrivateKeyInfo privateKeyInfo = ((PKCS8EncryptedPrivateKeyInfo) keyObject).decryptPrivateKeyInfo(decryptProvider);
                return pemKeyConverter.getPrivateKey(privateKeyInfo);
            } else if (keyObject instanceof PEMEncryptedKeyPair) {
                // Encrypted PemKeyPair
                PEMDecryptorProvider decryptProvider = new JcePEMDecryptorProviderBuilder().setProvider(provider).build(password);
                PrivateKeyInfo privateKeyInfo = ((PEMEncryptedKeyPair) keyObject).decryptKeyPair(decryptProvider).getPrivateKeyInfo();
                return pemKeyConverter.getPrivateKey(privateKeyInfo);
            } else if (keyObject instanceof SubjectPublicKeyInfo) {
                // SubjectPublicKeyInfo
                return pemKeyConverter.getPublicKey((SubjectPublicKeyInfo) keyObject);
            } else if (keyObject instanceof X509CertificateHolder) {
                // X509 Certificate
                return pemKeyConverter.getPublicKey(((X509CertificateHolder) keyObject).getSubjectPublicKeyInfo());
            } else if (keyObject instanceof X509TrustedCertificateBlock) {
                // X509 Trusted Certificate
                return pemKeyConverter.getPublicKey(((X509TrustedCertificateBlock) keyObject).getCertificateHolder().getSubjectPublicKeyInfo());
            } else if (keyObject instanceof PKCS10CertificationRequest) {
                // PKCS#10 CSR
                return pemKeyConverter.getPublicKey(((PKCS10CertificationRequest) keyObject).getSubjectPublicKeyInfo());
            } else {
                // 表示无法识别的密钥类型
                return null;
            }
        } catch (IOException | OperatorCreationException | PKCSException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取pem文件中的信息，包括类型、头信息和密钥内容
     *
     * @param keyStream pem流
     * @return {@link PemObject}
     */
    public static PemObject readPemObject(InputStream keyStream) {
        return readPemObject(IoKit.getReader(keyStream, org.miaixz.bus.core.lang.Charset.UTF_8));
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
            throw new CryptoException(e);
        } finally {
            IoKit.close(pemReader);
        }
    }

    /**
     * 将私钥或公钥转换为PEM格式的字符串
     *
     * @param type    密钥类型（私钥、公钥、证书）
     * @param content 密钥内容
     * @return PEM内容
     */
    public static String toPem(String type, byte[] content) {
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
    public static void writePemObject(String type, byte[] content, OutputStream keyStream) {
        writePemObject(new PemObject(type, content), keyStream);
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param type    密钥类型（私钥、公钥、证书）
     * @param content 密钥内容，需为PKCS#1格式
     * @param writer  pemWriter
     */
    public static void writePemObject(String type, byte[] content, Writer writer) {
        writePemObject(new PemObject(type, content), writer);
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param pemObject pem对象，包括密钥和密钥类型等信息
     * @param keyStream pem流
     */
    public static void writePemObject(PemObjectGenerator pemObject, OutputStream keyStream) {
        writePemObject(pemObject, IoKit.getWriter(keyStream, org.miaixz.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 写出pem密钥（私钥、公钥、证书）
     *
     * @param pemObject pem对象，包括密钥和密钥类型等信息
     * @param writer    pemWriter
     */
    public static void writePemObject(PemObjectGenerator pemObject, Writer writer) {
        final PemWriter pemWriter = new PemWriter(writer);
        try {
            pemWriter.writeObject(pemObject);
        } catch (IOException e) {
            throw new CryptoException(e);
        } finally {
            IoKit.close(pemWriter);
        }
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
     * @param hashed   hash值（加密后的值）
     * @return 是否匹配
     */
    public static boolean checkpw(String password, String hashed) {
        return new BCrypt().checkpw(password, hashed);
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
        final int c3Len = Normal._32;
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
        // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = Normal._32;
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
        final BigInteger[] decode;
        try {
            decode = StandardDSAEncoding.INSTANCE.decode(SM2_DOMAIN_PARAMS.getN(), rsDer);
        } catch (IOException e) {
            throw new CryptoException(e);
        }

        final byte[] r = bigIntToFixedLengthBytes(decode[0]);
        final byte[] s = bigIntToFixedLengthBytes(decode[1]);

        return ArrayKit.addAll(r, s);
    }

    /**
     * BC的SM3withSM2验签需要的rs是asn1格式的，这个方法将直接拼接r||s的字节数组转化成asn1格式
     *
     * @param sign 字节数组
     * @return Rs结果为asn1格式
     */
    public static byte[] rsPlainToAsn1(byte[] sign) {
        if (sign.length != Normal._32 * 2) {
            throw new CryptoException("err rs. ");
        }
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, Normal._32));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, Normal._32, Normal._32 * 2));
        try {
            return StandardDSAEncoding.INSTANCE.encode(SM2_DOMAIN_PARAMS.getN(), r, s);
        } catch (IOException e) {
            throw new CryptoException(e);
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
     * 增加加密解密的算法提供者，默认优先使用，例如：
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
            throw new CryptoException(e);
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
            throw new CryptoException(e);
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
            throw new CryptoException(e);
        }

        return mac;
    }

    /**
     * 创建{@link Signature}
     *
     * @param algorithm 算法
     * @return {@link Signature}
     */
    public static Signature createSignature(String algorithm) {
        final java.security.Provider provider = Instances.singletion(Holder.class).getProvider();

        Signature signature;
        try {
            signature = (null == provider) ? Signature.getInstance(algorithm) : Signature.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return signature;
    }

    /**
     * BigInteger转固定长度bytes
     *
     * @param rOrS {@link BigInteger}
     * @return 固定长度bytes
     */
    private static byte[] bigIntToFixedLengthBytes(BigInteger rOrS) {
        byte[] rs = rOrS.toByteArray();
        if (rs.length == Normal._32) {
            return rs;
        } else if (rs.length == Normal._32 + 1 && rs[0] == 0) {
            return Arrays.copyOfRange(rs, 1, Normal._32 + 1);
        } else if (rs.length < Normal._32) {
            byte[] result = new byte[Normal._32];
            Arrays.fill(result, (byte) 0);
            System.arraycopy(rs, 0, result, Normal._32 - rs.length, rs.length);
            return result;
        } else {
            throw new CryptoException("Error rs: {}", Hex.toHexString(rs));
        }
    }

}
