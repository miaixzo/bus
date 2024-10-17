/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AlphabetMapper;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DefaultBufferedBlockCipher;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.Arrays;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.codec.binary.Hex;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.crypto.builtin.digest.Digester;
import org.miaixz.bus.crypto.builtin.digest.mac.BCHMac;
import org.miaixz.bus.crypto.builtin.digest.mac.Mac;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;
import org.miaixz.bus.crypto.center.*;

/**
 * 安全相关工具类 加密分为三种： 1、对称加密（symmetric），例如：AES、DES等 2、非对称加密（asymmetric），例如：RSA、DSA等
 * 3、摘要加密（digest），例如：MD5、SHA-1、SHA-256、HMAC等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * SM2默认曲线
     */
    public static final String SM2_CURVE_NAME = "sm2p256v1";
    /**
     * SM2椭圆曲线参数类
     */
    public static final ECParameterSpec SM2_EC_SPEC = ECNamedCurveTable.getParameterSpec(SM2_CURVE_NAME);
    /**
     * SM2推荐曲线参数（来自https://github.com/ZZMarquis/gmhelper）
     */
    public static final ECDomainParameters SM2_DOMAIN_PARAMS = toDomainParams(SM2_EC_SPEC);
    /**
     * SM2国密算法公钥参数的Oid标识
     */
    public static final ASN1ObjectIdentifier ID_SM2_PUBLIC_KEY_PARAM = new ASN1ObjectIdentifier("1.2.156.10197.1.301");
    private static final int RS_LEN = 32;
    /**
     * 自定义系统属性：是否解码Hex字符
     */
    public static String CRYPTO_DECODE_HEX = "bus.crypto.decodeHex";

    /**
     * 生成算法，格式为XXXwithXXX
     *
     * @param asymmetricAlgorithm 非对称算法
     * @param digestAlgorithm     摘要算法
     * @return 算法
     */
    public static String generateAlgorithm(final Algorithm asymmetricAlgorithm, final Algorithm digestAlgorithm) {
        final String digestPart = (null == digestAlgorithm) ? "NONE" : digestAlgorithm.name();
        return StringKit.format("{}with{}", digestPart, asymmetricAlgorithm.getValue());
    }

    /**
     * AES加密，生成随机KEY。注意解密时必须使用相同 {@link AES}对象或者使用相同KEY 例：
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
     * AES加密 例：
     * 
     * <pre>
     * AES加密：aes(data).encrypt(data)
     * AES解密：aes(data).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link Crypto}
     */
    public static AES aes(final byte[] key) {
        return new AES(key);
    }

    /**
     * DES加密，生成随机KEY。注意解密时必须使用相同 {@link DES}对象或者使用相同KEY 例：
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
     * DES加密 例：
     * 
     * <pre>
     * DES加密：des(data).encrypt(data)
     * DES解密：des(data).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link DES}
     */
    public static DES des(final byte[] key) {
        return new DES(key);
    }

    /**
     * 三重数据加密算法,缩写为TDEA（又名3DES、TripleDES），生成随机KEY。 注意解密时必须使用相同 {@link TDEA}对象或者使用相同KEY
     * Java中默认实现为：DESede/ECB/PKCS5Padding 例：
     * 
     * <pre>
     * DESede加密：tdea().encrypt(data)
     * DESede解密：tdea().decrypt(data)
     * </pre>
     *
     * @return {@link TDEA}
     */
    public static TDEA tdea() {
        return new TDEA();
    }

    /**
     * 三重数据加密算法,缩写为TDEA（又名3DES、TripleDES），生成随机KEY。 注意解密时必须使用相同 {@link TDEA}对象或者使用相同KEY
     * Java中默认实现为：DESede/ECB/PKCS5Padding 例：
     * 
     * <pre>
     * DESede加密：tdea(data).encrypt(data)
     * DESede解密：tdea(data).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link TDEA}
     */
    public static TDEA tdea(final byte[] key) {
        return new TDEA(key);
    }

    /**
     * MD5加密 例：
     * 
     * <pre>
     * MD5加密：md5().digest(data)
     * MD5加密并转为16进制字符串：md5().digestHex(data)
     * </pre>
     *
     * @return {@link Digester}
     */
    public static MD5 md5() {
        return MD5.of();
    }

    /**
     * MD5加密，生成16进制MD5字符串
     *
     * @param data 数据
     * @return MD5字符串
     */
    public static String md5(final String data) {
        return MD5.of().digestHex(data);
    }

    /**
     * MD5加密，生成16进制MD5字符串
     *
     * @param data 数据
     * @return MD5字符串
     */
    public static String md5(final InputStream data) {
        return MD5.of().digestHex(data);
    }

    /**
     * MD5加密文件，生成16进制MD5字符串
     *
     * @param dataFile 被加密文件
     * @return MD5字符串
     */
    public static String md5(final File dataFile) {
        return MD5.of().digestHex(dataFile);
    }

    /**
     * SHA1加密 例： SHA1加密：sha1().digest(data) SHA1加密并转为16进制字符串：sha1().digestHex(data)
     *
     * @return {@link Digester}
     */
    public static Digester sha1() {
        return new Digester(Algorithm.SHA1);
    }

    /**
     * SHA1加密，生成16进制SHA1字符串
     *
     * @param data 数据
     * @return SHA1字符串
     */
    public static String sha1(final String data) {
        return new Digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * SHA1加密，生成16进制SHA1字符串
     *
     * @param data 数据
     * @return SHA1字符串
     */
    public static String sha1(final InputStream data) {
        return new Digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * SHA1加密文件，生成16进制SHA1字符串
     *
     * @param dataFile 被加密文件
     * @return SHA1字符串
     */
    public static String sha1(final File dataFile) {
        return new Digester(Algorithm.SHA1).digestHex(dataFile);
    }

    /**
     * SHA256加密 例： SHA256加密：sha256().digest(data) SHA256加密并转为16进制字符串：sha256().digestHex(data)
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
    public static String sha256(final String data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * SHA256加密，生成16进制SHA256字符串
     *
     * @param data 数据
     * @return SHA1字符串
     */
    public static String sha256(final InputStream data) {
        return new Digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * SHA256加密文件，生成16进制SHA256字符串
     *
     * @param dataFile 被加密文件
     * @return SHA256字符串
     */
    public static String sha256(final File dataFile) {
        return new Digester(Algorithm.SHA256).digestHex(dataFile);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(final Algorithm algorithm, final String key) {
        return new HMac(algorithm, ByteKit.toBytes(key));
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(final Algorithm algorithm, final byte[] key) {
        return new HMac(algorithm, key);
    }

    /**
     * 创建HMac对象，调用digest方法可获得hmac值
     *
     * @param algorithm {@link Algorithm}
     * @param key       密钥{@link SecretKey}，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmac(final Algorithm algorithm, final SecretKey key) {
        return new HMac(algorithm, key);
    }

    /**
     * HmacMD5加密器 例： HmacMD5加密：hmacMd5(data).digest(data) HmacMD5加密并转为16进制字符串：hmacMd5(data).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(final String key) {
        return hmacMd5(ByteKit.toBytes(key));
    }

    /**
     * HmacMD5加密器 例： HmacMD5加密：hmacMd5(data).digest(data) HmacMD5加密并转为16进制字符串：hmacMd5(data).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacMd5(final byte[] key) {
        return new HMac(Algorithm.HMACMD5, key);
    }

    /**
     * HmacMD5加密器，生成随机KEY 例： HmacMD5加密：hmacMd5().digest(data) HmacMD5加密并转为16进制字符串：hmacMd5().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacMd5() {
        return new HMac(Algorithm.HMACMD5);
    }

    /**
     * HmacSHA1加密器 例： HmacSHA1加密：hmacSha1(data).digest(data) HmacSHA1加密并转为16进制字符串：hmacSha1(data).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha1(final String key) {
        return hmacSha1(ByteKit.toBytes(key));
    }

    /**
     * HmacSHA1加密器 例： HmacSHA1加密：hmacSha1(data).digest(data) HmacSHA1加密并转为16进制字符串：hmacSha1(data).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha1(final byte[] key) {
        return new HMac(Algorithm.HMACSHA1, key);
    }

    /**
     * HmacSHA1加密器，生成随机KEY 例： HmacSHA1加密：hmacSha1().digest(data) HmacSHA1加密并转为16进制字符串：hmacSha1().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacSha1() {
        return new HMac(Algorithm.HMACSHA1);
    }

    /**
     * HmacSHA256加密器 例： HmacSHA256加密：hmacSha256(data).digest(data)
     * HmacSHA256加密并转为16进制字符串：hmacSha256(data).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha256(final String key) {
        return hmacSha256(ByteKit.toBytes(key));
    }

    /**
     * HmacSHA256加密器 例： HmacSHA256加密：hmacSha256(data).digest(data)
     * HmacSHA256加密并转为16进制字符串：hmacSha256(data).digestHex(data)
     *
     * @param key 加密密钥，如果为{@code null}生成随机密钥
     * @return {@link HMac}
     */
    public static HMac hmacSha256(final byte[] key) {
        return new HMac(Algorithm.HMACSHA256, key);
    }

    /**
     * HmacSHA256加密器，生成随机KEY 例： HmacSHA256加密：hmacSha256().digest(data)
     * HmacSHA256加密并转为16进制字符串：hmacSha256().digestHex(data)
     *
     * @return {@link HMac}
     */
    public static HMac hmacSha256() {
        return new HMac(Algorithm.HMACSHA256);
    }

    /**
     * 创建RSA算法对象 生成新的私钥公钥对
     *
     * @return {@link RSA}
     */
    public static RSA rsa() {
        return new RSA();
    }

    /**
     * 创建RSA算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     * @return {@link RSA}
     */
    public static RSA rsa(final String privateKeyBase64, final String publicKeyBase64) {
        return new RSA(privateKeyBase64, publicKeyBase64);
    }

    /**
     * 创建RSA算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link RSA}
     */
    public static RSA rsa(final byte[] privateKey, final byte[] publicKey) {
        return new RSA(privateKey, publicKey);
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
    public static void addProvider(final java.security.Provider provider) {
        if (ArrayKit.contains(Security.getProviders(), provider)) {
            // 如果已经注册过Provider，不再重新注册
            return;
        }
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
     */
    public static byte[] decode(final String key) {
        if (Objects.isNull(key)) {
            return null;
        }

        // 某些特殊字符串会无法区分Hex还是Base64，此处使用系统属性强制关闭Hex解析
        final boolean decodeHex = Keys.getBoolean(CRYPTO_DECODE_HEX, true);
        if (decodeHex && Validator.isHex(key)) {
            return Hex.decode(key);
        } else if (Base64.isTypeBase64(key)) {
            return Base64.decode(key);
        }
        throw new IllegalArgumentException("Value is not hex or base64!");
    }

    /**
     * 创建{@link Cipher} 当provider为{@code null}时，使用{@link Holder}查找提供方，找不到使用JDK默认提供方。
     *
     * @param algorithm 算法
     * @return {@link Cipher}
     */
    public static Cipher createCipher(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final Cipher cipher;
        try {
            cipher = (null == provider) ? Cipher.getInstance(algorithm) : Cipher.getInstance(algorithm, provider);
        } catch (final Exception e) {
            throw new CryptoException(e);
        }

        return cipher;
    }

    /**
     * 创建{@link MessageDigest} 当provider为{@code null}时，使用{@link Holder}查找提供方，找不到使用JDK默认提供方。
     *
     * @param algorithm 算法
     * @param provider  算法提供方，{@code null}表示使用{@link Holder}找到的提供方。
     * @return {@link MessageDigest}
     */
    public static MessageDigest createMessageDigest(final String algorithm, java.security.Provider provider) {
        if (null == provider) {
            provider = Holder.getProvider();
        }

        final MessageDigest messageDigest;
        try {
            messageDigest = (null == provider) ? MessageDigest.getInstance(algorithm)
                    : MessageDigest.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return messageDigest;
    }

    /**
     * 创建{@link MessageDigest}，使用JDK默认的Provider
     *
     * @param algorithm 算法
     * @return {@link MessageDigest}
     */
    public static MessageDigest createJdkMessageDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 创建{@link javax.crypto.Mac}
     *
     * @param algorithm 算法
     * @return {@link javax.crypto.Mac}
     */
    public static javax.crypto.Mac createMac(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final javax.crypto.Mac mac;
        try {
            mac = (null == provider) ? javax.crypto.Mac.getInstance(algorithm)
                    : javax.crypto.Mac.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return mac;
    }

    /**
     * RC4算法
     *
     * @param key 密钥
     * @return {@link Crypto}
     */
    public static Crypto rc4(final byte[] key) {
        return new Crypto(Algorithm.RC4, key);
    }

    /**
     * 强制关闭自定义{@link Provider}的使用，如Bouncy Castle库，全局有效
     */
    public static void disableCustomProvider() {
        Holder.setUseCustomProvider(false);
    }

    /**
     * PBKDF2加密密码
     *
     * @param password 密码
     * @param salt     盐
     * @return 盐，一般为16位
     */
    public static String pbkdf2(final char[] password, final byte[] salt) {
        return new PBKDF2().encryptHex(password, salt);
    }

    /**
     * FPE(Format Preserving Encryption)实现，支持FF1和FF3-1模式。
     *
     * @param mode   FPE模式枚举，可选FF1或FF3-1
     * @param key    密钥，{@code null}表示随机密钥，长度必须是16bit、24bit或32bit
     * @param mapper Alphabet字典映射，被加密的字符范围和这个映射必须一致，例如手机号、银行卡号等字段可以采用数字字母字典表
     * @param tweak  Tweak是为了解决因局部加密而导致结果冲突问题，通常情况下将数据的不可变部分作为Tweak
     * @return {@link FPE}
     */
    public static FPE fpe(final FPE.FPEMode mode, final byte[] key, final AlphabetMapper mapper, final byte[] tweak) {
        return new FPE(mode, key, mapper, tweak);
    }

    /**
     * 祖冲之算法集（ZUC-128算法）实现，基于BouncyCastle实现。
     *
     * @param key 密钥
     * @param iv  加盐，长度16bytes，{@code null}是随机加盐
     * @return {@link ZUC}
     */
    public static ZUC zuc128(final byte[] key, final byte[] iv) {
        return new ZUC(Algorithm.ZUC_128, key, iv);
    }

    /**
     * 祖冲之算法集（ZUC-256算法）实现，基于BouncyCastle实现。
     *
     * @param key 密钥
     * @param iv  加盐，长度25bytes，{@code null}是随机加盐
     * @return {@link ZUC}
     */
    public static ZUC zuc256(final byte[] key, final byte[] iv) {
        return new ZUC(Algorithm.ZUC_256, key, iv);
    }

    /**
     * 创建SM2算法对象 生成新的私钥公钥对
     *
     * @return {@link SM2}
     */
    public static SM2 sm2() {
        return new SM2();
    }

    /**
     * 创建SM2算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     * @return {@link SM2}
     */
    public static SM2 sm2(final String privateKey, final String publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * 创建SM2算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥，必须使用PKCS#8规范
     * @param publicKey  公钥，必须使用X509规范
     * @return {@link SM2}
     */
    public static SM2 sm2(final byte[] privateKey, final byte[] publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * 创建SM2算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link SM2}
     */
    public static SM2 sm2(final PrivateKey privateKey, final PublicKey publicKey) {
        return new SM2(privateKey, publicKey);
    }

    /**
     * 创建SM2算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyParams 私钥参数
     * @param publicKeyParams  公钥参数
     * @return {@link SM2}
     */
    public static SM2 sm2(final ECPrivateKeyParameters privateKeyParams, final ECPublicKeyParameters publicKeyParams) {
        return new SM2(privateKeyParams, publicKeyParams);
    }

    /**
     * SM3加密 例： SM3加密：sm3().digest(data) SM3加密并转为16进制字符串：sm3().digestHex(data)
     *
     * @return {@link SM3}
     */
    public static SM3 sm3() {
        return new SM3();
    }

    /**
     * SM3加密，可以传入盐
     *
     * @param salt 加密盐
     * @return {@link SM3}
     */
    public static SM3 sm3WithSalt(final byte[] salt) {
        return new SM3(salt);
    }

    /**
     * SM3加密，生成16进制SM3字符串
     *
     * @param data 数据
     * @return SM3字符串
     */
    public static String sm3(final String data) {
        return sm3().digestHex(data);
    }

    /**
     * SM3加密，生成16进制SM3字符串
     *
     * @param data 数据
     * @return SM3字符串
     */
    public static String sm3(final InputStream data) {
        return sm3().digestHex(data);
    }

    /**
     * SM3加密文件，生成16进制SM3字符串
     *
     * @param dataFile 被加密文件
     * @return SM3字符串
     */
    public static String sm3(final File dataFile) {
        return sm3().digestHex(dataFile);
    }

    /**
     * SM4加密，生成随机KEY。注意解密时必须使用相同 {@link Crypto}对象或者使用相同KEY 例：
     *
     * <pre>
     * SM4加密：sm4().encrypt(data)
     * SM4解密：sm4().decrypt(data)
     * </pre>
     *
     * @return {@link Crypto}
     */
    public static SM4 sm4() {
        return new SM4();
    }

    /**
     * SM4加密 例：
     *
     * <pre>
     * SM4加密：sm4(data).encrypt(data)
     * SM4解密：sm4(data).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link SM4}
     */
    public static SM4 sm4(final byte[] key) {
        return new SM4(key);
    }

    /**
     * bc加解密使用旧标c1||c2||c3，此方法在加密后调用，将结果转化为c1||c3||c2
     *
     * @param c1c2c3             加密后的bytes，顺序为C1C2C3
     * @param ecDomainParameters {@link ECDomainParameters}
     * @return 加密后的bytes，顺序为C1C3C2
     */
    public static byte[] changeC1C2C3ToC1C3C2(final byte[] c1c2c3, final ECDomainParameters ecDomainParameters) {
        // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        final byte[] result = new byte[c1c2c3.length];
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
    public static byte[] changeC1C3C2ToC1C2C3(final byte[] c1c3c2, final ECDomainParameters ecDomainParameters) {
        // sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c1Len = (ecDomainParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32; // new SM3Digest().getDigestSize();
        final byte[] result = new byte[c1c3c2.length];
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
     */
    public static byte[] rsAsn1ToPlain(final byte[] rsDer) {
        final BigInteger[] decode;
        try {
            decode = StandardDSAEncoding.INSTANCE.decode(SM2_DOMAIN_PARAMS.getN(), rsDer);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        final byte[] r = toFixedLengthBytes(decode[0]);
        final byte[] s = toFixedLengthBytes(decode[1]);

        return ArrayKit.addAll(r, s);
    }

    /**
     * BC的SM3withSM2验签需要的rs是asn1格式的，这个方法将直接拼接r||s的字节数组转化成asn1格式
     *
     * @param sign in plain byte array
     * @return rs result in asn1 format
     */
    public static byte[] rsPlainToAsn1(final byte[] sign) {
        if (sign.length != RS_LEN * 2) {
            throw new CryptoException("err rs. ");
        }
        final BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, RS_LEN));
        final BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, RS_LEN, RS_LEN * 2));
        try {
            return StandardDSAEncoding.INSTANCE.encode(SM2_DOMAIN_PARAMS.getN(), r, s);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建HmacSM3算法的{@link Mac}
     *
     * @param key 密钥
     * @return {@link Mac}
     */
    public static Mac createHmacSm3Engine(final byte[] key) {
        return new BCHMac(new SM3Digest(), key);
    }

    /**
     * HmacSM3算法实现
     *
     * @param key 密钥
     * @return {@link HMac} 对象，调用digestXXX即可
     */
    public static HMac hmacSm3(final byte[] key) {
        return new HMac(Algorithm.HMACSM3, key);
    }

    /**
     * BigInteger转固定长度bytes
     *
     * @param rOrS {@link BigInteger}
     * @return 固定长度bytes
     */
    private static byte[] toFixedLengthBytes(final BigInteger rOrS) {
        // for sm2p256v1, n is 00fffffffeffffffffffffffffffffffff7203df6b21c6052b53bbf40939d54123,
        // r and s are the result of mod n, so they should be less than n and have length<=32
        final byte[] rs = rOrS.toByteArray();
        if (rs.length == RS_LEN) {
            return rs;
        } else if (rs.length == RS_LEN + 1 && rs[0] == 0) {
            return Arrays.copyOfRange(rs, 1, RS_LEN + 1);
        } else if (rs.length < RS_LEN) {
            final byte[] result = new byte[RS_LEN];
            Arrays.fill(result, (byte) 0);
            System.arraycopy(rs, 0, result, RS_LEN - rs.length, rs.length);
            return result;
        } else {
            throw new CryptoException("Error rs: {}", org.bouncycastle.util.encoders.Hex.toHexString(rs));
        }
    }

    /**
     * 生成签名对象，仅用于非对称加密
     *
     * @param asymmetricAlgorithm {@link Algorithm} 非对称加密算法
     * @param digestAlgorithm     {@link Algorithm} 摘要算法
     * @return {@link Signature}
     */
    public static Signature createSignature(final Algorithm asymmetricAlgorithm, final Algorithm digestAlgorithm) {
        return createSignature(generateAlgorithm(asymmetricAlgorithm, digestAlgorithm));
    }

    /**
     * 创建{@link Signature}签名对象
     *
     * @param algorithm 算法
     * @return {@link Signature}
     */
    public static Signature createSignature(final String algorithm) {
        final java.security.Provider provider = Holder.getProvider();

        final Signature signature;
        try {
            signature = (null == provider) ? Signature.getInstance(algorithm)
                    : Signature.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }

        return signature;
    }

    /**
     * 创建签名算法对象 生成新的私钥公钥对
     *
     * @param algorithm 签名算法
     * @return {@link Sign}
     */
    public static Sign sign(final Algorithm algorithm) {
        return new Sign(algorithm);
    }

    /**
     * 创建签名算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm        签名算法
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     * @return {@link Sign}
     */
    public static Sign sign(final Algorithm algorithm, final String privateKeyBase64, final String publicKeyBase64) {
        return new Sign(algorithm, privateKeyBase64, publicKeyBase64);
    }

    /**
     * 创建Sign算法对象 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法枚举
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link Sign}
     */
    public static Sign sign(final Algorithm algorithm, final byte[] privateKey, final byte[] publicKey) {
        return new Sign(algorithm, privateKey, publicKey);
    }

    /**
     * 对参数做签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param crypto      对称加密算法
     * @param params      参数
     * @param otherParams 其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParams(final Crypto crypto, final Map<?, ?> params, final String... otherParams) {
        return signParams(crypto, params, Normal.EMPTY, Normal.EMPTY, true, otherParams);
    }

    /**
     * 对参数做签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     *
     * @param crypto            对称加密算法
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParams(final Crypto crypto, final Map<?, ?> params, final String separator,
            final String keyValueSeparator, final boolean isIgnoreNull, final String... otherParams) {
        return crypto.encryptHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, otherParams));
    }

    /**
     * 对参数做md5签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params      参数
     * @param otherParams 其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParamsMd5(final Map<?, ?> params, final String... otherParams) {
        return signParams(Algorithm.MD5, params, otherParams);
    }

    /**
     * 对参数做Sha1签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params      参数
     * @param otherParams 其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParamsSha1(final Map<?, ?> params, final String... otherParams) {
        return signParams(Algorithm.SHA1, params, otherParams);
    }

    /**
     * 对参数做Sha256签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param params      参数
     * @param otherParams 其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParamsSha256(final Map<?, ?> params, final String... otherParams) {
        return signParams(Algorithm.SHA256, params, otherParams);
    }

    /**
     * 对参数做签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param digestAlgorithm 摘要算法
     * @param params          参数
     * @param otherParams     其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParams(final Algorithm digestAlgorithm, final Map<?, ?> params,
            final String... otherParams) {
        return signParams(digestAlgorithm, params, Normal.EMPTY, Normal.EMPTY, true, otherParams);
    }

    /**
     * 对参数做签名 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     *
     * @param digestAlgorithm   摘要算法
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 签名
     */
    public static String signParams(final Algorithm digestAlgorithm, final Map<?, ?> params, final String separator,
            final String keyValueSeparator, final boolean isIgnoreNull, final String... otherParams) {
        return new Digester(digestAlgorithm)
                .digestHex(MapKit.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, otherParams));
    }

    /**
     * 计算32位MD5摘要值
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] md5(final byte[] data) {
        return MD5.of().digest(data);
    }

    /**
     * 计算32位MD5摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要
     */
    public static byte[] md5(final String data, final java.nio.charset.Charset charset) {
        return MD5.of().digest(data, charset);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(final byte[] data) {
        return MD5.of().digestHex(data);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(final String data, final java.nio.charset.Charset charset) {
        return MD5.of().digestHex(data, charset);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(final String data) {
        return md5Hex(data, Charset.UTF_8);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(final InputStream data) {
        return MD5.of().digestHex(data);
    }

    /**
     * 计算32位MD5摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex(final File file) {
        return MD5.of().digestHex(file);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(final byte[] data) {
        return MD5.of().digestHex16(data);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(final String data, final java.nio.charset.Charset charset) {
        return MD5.of().digestHex16(data, charset);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(final String data) {
        return md5Hex16(data, Charset.UTF_8);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(final InputStream data) {
        return MD5.of().digestHex16(data);
    }

    /**
     * 计算16位MD5摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return MD5摘要的16进制表示
     */
    public static String md5Hex16(final File file) {
        return MD5.of().digestHex16(file);
    }

    /**
     * 32位MD5转16位MD5
     *
     * @param md5Hex 32位MD5
     * @return 16位MD5
     */
    public static String md5HexTo16(final String md5Hex) {
        return md5Hex.substring(8, 24);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data 被摘要数据
     * @return SHA-1摘要
     */
    public static byte[] sha1(final byte[] data) {
        return digester(Algorithm.SHA1).digest(data);
    }

    /**
     * 计算SHA-1摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-1摘要
     */
    public static byte[] sha1(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA1).digest(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(final byte[] data) {
        return digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA1).digestHex(data, charset);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(final String data) {
        return sha1Hex(data, Charset.UTF_8);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(final InputStream data) {
        return digester(Algorithm.SHA1).digestHex(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-1摘要的16进制表示
     */
    public static String sha1Hex(final File file) {
        return digester(Algorithm.SHA1).digestHex(file);
    }

    /**
     * 计算SHA-256摘要值
     *
     * @param data 被摘要数据
     * @return SHA-256摘要
     */
    public static byte[] sha256(final byte[] data) {
        return digester(Algorithm.SHA256).digest(data);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(final byte[] data) {
        return digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA256).digestHex(data, charset);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(final String data) {
        return sha256Hex(data, Charset.UTF_8);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(final InputStream data) {
        return digester(Algorithm.SHA256).digestHex(data);
    }

    /**
     * 计算SHA-256摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-256摘要的16进制表示
     */
    public static String sha256Hex(final File file) {
        return digester(Algorithm.SHA256).digestHex(file);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param data 被摘要数据
     * @return SHA-512摘要
     */
    public static byte[] sha512(final byte[] data) {
        return digester(Algorithm.SHA512).digest(data);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-512摘要
     */
    public static byte[] sha512(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA512).digest(data, charset);
    }

    /**
     * 计算sha512摘要值，使用UTF-8编码
     *
     * @param data 被摘要数据
     * @return MD5摘要
     */
    public static byte[] sha512(final String data) {
        return sha512(data, Charset.UTF_8);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param data 被摘要数据
     * @return SHA-512摘要
     */
    public static byte[] sha512(final InputStream data) {
        return digester(Algorithm.SHA512).digest(data);
    }

    /**
     * 计算SHA-512摘要值
     *
     * @param file 被摘要文件
     * @return SHA-512摘要
     */
    public static byte[] sha512(final File file) {
        return digester(Algorithm.SHA512).digest(file);
    }

    /**
     * 计算SHA-1摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final byte[] data) {
        return digester(Algorithm.SHA512).digestHex(data);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final String data, final java.nio.charset.Charset charset) {
        return digester(Algorithm.SHA512).digestHex(data, charset);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final String data) {
        return sha512Hex(data, Charset.UTF_8);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final InputStream data) {
        return digester(Algorithm.SHA512).digestHex(data);
    }

    /**
     * 计算SHA-512摘要值，并转为16进制字符串
     *
     * @param file 被摘要文件
     * @return SHA-512摘要的16进制表示
     */
    public static String sha512Hex(final File file) {
        return digester(Algorithm.SHA512).digestHex(file);
    }

    /**
     * 新建摘要器
     *
     * @param algorithm 签名算法
     * @return Digester
     */
    public static Digester digester(final Algorithm algorithm) {
        return digester(algorithm.getValue());
    }

    /**
     * 新建摘要器
     *
     * @param algorithm 签名算法
     * @return Digester
     */
    public static Digester digester(final String algorithm) {
        return new Digester(algorithm);
    }

    /**
     * 生成Bcrypt加密后的密文
     *
     * @param password 明文密码
     * @return 加密后的密文
     */
    public static String hashpw(final String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 验证密码是否与Bcrypt加密后的密文匹配
     *
     * @param password 明文密码
     * @param hashed   hash值（加密后的值）
     * @return 是否匹配
     */
    public static boolean checkpw(final String password, final String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param parameterSpec ECParameterSpec
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(final ECParameterSpec parameterSpec) {
        return new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(), parameterSpec.getN(),
                parameterSpec.getH());
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param curveName Curve名称
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(final String curveName) {
        return toDomainParams(ECUtil.getNamedCurveByName(curveName));
    }

    /**
     * 构建ECDomainParameters对象
     *
     * @param x9ECParameters {@link X9ECParameters}
     * @return {@link ECDomainParameters}
     */
    public static ECDomainParameters toDomainParams(final X9ECParameters x9ECParameters) {
        return new ECDomainParameters(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN(),
                x9ECParameters.getH());
    }

    /**
     * Java中的PKCS#8格式私钥转换为OpenSSL支持的PKCS#1格式
     *
     * @param privateKey PKCS#8格式私钥
     * @return PKCS#1格式私钥
     */
    public static byte[] toPkcs1(final PrivateKey privateKey) {
        final PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        try {
            return pkInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Java中的X.509格式公钥转换为OpenSSL支持的PKCS#1格式
     *
     * @param publicKey X.509格式公钥
     * @return PKCS#1格式公钥
     */
    public static byte[] toPkcs1(final PublicKey publicKey) {
        final SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        try {
            return spkInfo.parsePublicKey().getEncoded();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将{@link BlockCipher}包装为指定mode和padding的{@link BufferedBlockCipher}
     *
     * @param cipher  {@link BlockCipher}
     * @param mode    模式
     * @param padding 补码方式
     * @return {@link BufferedBlockCipher}，无对应Cipher返回{@code null}
     */
    public static BufferedBlockCipher wrap(BlockCipher cipher, final Algorithm.Mode mode, final Padding padding) {
        switch (mode) {
        case CBC:
            cipher = CBCBlockCipher.newInstance(cipher);
            break;
        case CFB:
            cipher = CFBBlockCipher.newInstance(cipher, cipher.getBlockSize() * 8);
            break;
        case CTR:
            cipher = SICBlockCipher.newInstance(cipher);
            break;
        case OFB:
            cipher = new OFBBlockCipher(cipher, cipher.getBlockSize() * 8);
        case CTS:
            return new CTSBlockCipher(cipher);
        }

        switch (padding) {
        case NoPadding:
            return new DefaultBufferedBlockCipher(cipher);
        case PKCS5Padding:
            return new PaddedBufferedBlockCipher(cipher);
        case ZeroPadding:
            return new PaddedBufferedBlockCipher(cipher, new ZeroBytePadding());
        case ISO10126Padding:
            return new PaddedBufferedBlockCipher(cipher, new ISO10126d2Padding());
        }

        return null;
    }

    /**
     * 根据算法创建{@link KeySpec}
     * <ul>
     * <li>DESede: {@link DESedeKeySpec}</li>
     * <li>DES : {@link DESedeKeySpec}</li>
     * <li>其它 : {@link SecretKeySpec}</li>
     * </ul>
     *
     * @param algorithm 算法
     * @param key       密钥
     * @return {@link KeySpec}
     */
    public static KeySpec createKeySpec(final String algorithm, byte[] key) {
        try {
            if (algorithm.startsWith("DESede")) {
                if (null == key) {
                    key = RandomKit.randomBytes(24);
                }
                // DESede兼容
                return new DESedeKeySpec(key);
            } else if (algorithm.startsWith("DES")) {
                if (null == key) {
                    key = RandomKit.randomBytes(8);
                }
                return new DESKeySpec(key);
            }
        } catch (final InvalidKeyException e) {
            throw new CryptoException(e);
        }

        return new SecretKeySpec(key, algorithm);
    }

    /**
     * 创建{@link PBEKeySpec} PBE算法没有密钥的概念，密钥在其它对称加密算法中是经过算法计算得出来的，PBE算法则是使用口令替代了密钥。
     *
     * @param password 口令
     * @return {@link PBEKeySpec}
     */
    public static PBEKeySpec createPBEKeySpec(char[] password) {
        if (null == password) {
            password = RandomKit.randomStringLower(32).toCharArray();
        }
        return new PBEKeySpec(password);
    }

    /**
     * 创建{@link PBEParameterSpec}
     *
     * @param salt           加盐值
     * @param iterationCount 摘要次数
     * @return {@link PBEParameterSpec}
     */
    public static PBEParameterSpec createPBEParameterSpec(final byte[] salt, final int iterationCount) {
        return new PBEParameterSpec(salt, iterationCount);
    }

    /**
     * 数据加密
     *
     * @param algorithm 加密算法
     * @param key       密钥, 字符串使用,分割 格式: 私钥,公钥,类型
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
     * @param key       密钥, 字符串使用,分割 格式: 私钥,公钥,类型
     * @param content   需要加密的内容
     * @param charset   字符集
     * @return 加密结果
     */
    public static String encrypt(String algorithm, String key, String content, java.nio.charset.Charset charset) {
        return HexKit.encodeString(encrypt(algorithm, key, content.getBytes(charset)));
    }

    /**
     * 数据加密
     *
     * @param algorithm   加密算法
     * @param key         密钥, 字符串使用,分割 格式: 私钥,公钥,类型
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
     * @param key       密钥, 字符串使用,分割 格式: 私钥,公钥,类型
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
     * @param key       密钥, 字符串使用,分割 格式: 私钥,公钥,类型
     * @param content   需要解密的内容
     * @param charset   字符集
     * @return 解密结果
     */
    public static String decrypt(String algorithm, String key, String content, java.nio.charset.Charset charset) {
        return new String(decrypt(algorithm, key, HexKit.decode(content)), charset);
    }

    /**
     * 数据解密
     *
     * @param algorithm   解密算法
     * @param key         密钥, 字符串使用,分割 格式: 私钥,公钥,类型
     * @param inputStream 需要解密的内容
     * @return 解密结果
     */
    public static InputStream decrypt(String algorithm, String key, InputStream inputStream) {
        return new ByteArrayInputStream(Registry.require(algorithm).decrypt(key, IoKit.readBytes(inputStream)));
    }

}
