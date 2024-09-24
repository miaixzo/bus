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
package org.miaixz.bus.crypto.center;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Set;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.asymmetric.Asymmetric;
import org.miaixz.bus.crypto.builtin.asymmetric.Crypto;

/**
 * 签名包装，{@link Signature} 包装类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sign extends Asymmetric<Sign> {

    private static final long serialVersionUID = -1L;

    /**
     * 签名，用于签名和验证
     */
    protected Signature signature;

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm {@link Algorithm}
     */
    public Sign(final Algorithm algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Sign(final String algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm 算法，见{@link Algorithm}
     * @param keyPair   密钥对，{@code null}表示随机生成
     */
    public Sign(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm {@link Algorithm}
     * @param keyPair   密钥对，{@code null}表示随机生成
     */
    public Sign(final Algorithm algorithm, final KeyPair keyPair) {
        this(algorithm.getValue(), keyPair);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public Sign(final Algorithm algorithm, final String privateKey, final String publicKey) {
        this(algorithm.getValue(), Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(final Algorithm algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  非对称加密算法
     * @param privateKey 私钥Base64
     * @param publicKey  公钥Base64
     */
    public Sign(final String algorithm, final String privateKey, final String publicKey) {
        this(algorithm, Base64.decode(privateKey), Base64.decode(publicKey));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(final String algorithm, final byte[] privateKey, final byte[] publicKey) {
        this(algorithm, new KeyPair(Keeper.generatePublicKey(algorithm, publicKey),
                Keeper.generatePrivateKey(algorithm, privateKey)));
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param keyPair   密钥对，{@code null}表示随机生成
     * @return this
     */
    @Override
    public Sign init(final String algorithm, final KeyPair keyPair) {
        signature = Builder.createSignature(algorithm);
        super.init(algorithm, keyPair);
        return this;
    }

    /**
     * 设置签名的参数
     *
     * @param params {@link AlgorithmParameterSpec}
     * @return this
     */
    public Sign setParameter(final AlgorithmParameterSpec params) {
        try {
            this.signature.setParameter(params);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
        return this;
    }

    /**
     * 生成文件签名
     *
     * @param data    被签名数据
     * @param charset 编码
     * @return 签名
     */
    public byte[] sign(final String data, final java.nio.charset.Charset charset) {
        return sign(ByteKit.toBytes(data, charset));
    }

    /**
     * 生成文件签名
     *
     * @param data 被签名数据
     * @return 签名
     */
    public byte[] sign(final String data) {
        return sign(data, Charset.UTF_8);
    }

    /**
     * 生成文件签名，并转为16进制字符串
     *
     * @param data    被签名数据
     * @param charset 编码
     * @return 签名
     */
    public String signHex(final String data, final java.nio.charset.Charset charset) {
        return HexKit.encodeString(sign(data, charset));
    }

    /**
     * 生成文件签名
     *
     * @param data 被签名数据
     * @return 签名
     */
    public String signHex(final String data) {
        return signHex(data, Charset.UTF_8);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @return 签名
     */
    public byte[] sign(final byte[] data) {
        return sign(new ByteArrayInputStream(data), -1);
    }

    /**
     * 生成签名，并转为16进制字符串
     *
     * @param data 被签名数据
     * @return 签名
     */
    public String signHex(final byte[] data) {
        return HexKit.encodeString(sign(data));
    }

    /**
     * 生成签名，并转为16进制字符串 使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param data 被签名数据
     * @return 签名
     */
    public String signHex(final InputStream data) {
        return HexKit.encodeString(sign(data));
    }

    /**
     * 生成签名，使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param data {@link InputStream} 数据流
     * @return 签名bytes
     */
    public byte[] sign(final InputStream data) {
        return sign(data, Normal._8192);
    }

    /**
     * 生成签名，并转为16进制字符串 使用默认缓存大小，见 {@link Normal#_8192}
     *
     * @param data         被签名数据
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#_8192} 做为默认值
     * @return 签名
     */
    public String digestHex(final InputStream data, final int bufferLength) {
        return HexKit.encodeString(sign(data, bufferLength));
    }

    /**
     * 生成签名
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link Normal#_8192} 做为默认值
     * @return 签名bytes
     */
    public byte[] sign(final InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = Normal._8192;
        }

        final byte[] buffer = new byte[bufferLength];
        lock.lock();
        try {
            signature.initSign(this.privateKey);
            final byte[] result;
            try {
                int read = data.read(buffer, 0, bufferLength);
                while (read > -1) {
                    signature.update(buffer, 0, read);
                    read = data.read(buffer, 0, bufferLength);
                }
                result = signature.sign();
            } catch (final Exception e) {
                throw new CryptoException(e);
            }
            return result;
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param data 数据
     * @param sign 签名
     * @return 是否验证通过
     */
    public boolean verify(final byte[] data, final byte[] sign) {
        lock.lock();
        try {
            signature.initVerify(this.publicKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (final Exception e) {
            throw new CryptoException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获得签名对象
     *
     * @return {@link Signature}
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * 设置签名
     *
     * @param signature 签名对象 {@link Signature}
     * @return 自身 {@link Crypto}
     */
    public Sign setSignature(final Signature signature) {
        this.signature = signature;
        return this;
    }

    /**
     * 设置{@link Certificate} 为PublicKey 如果Certificate是X509Certificate，我们需要检查是否有密钥扩展
     *
     * @param certificate {@link Certificate}
     * @return this
     */
    public Sign setCertificate(final Certificate certificate) {
        // If the certificate is of type X509Certificate,
        // we should check whether it has a Key Usage
        // extension marked as critical.
        if (certificate instanceof X509Certificate) {
            // Check whether the cert has a data usage extension
            // marked as a critical extension.
            // The OID for KeyUsage extension is 2.5.29.15.
            final X509Certificate cert = (X509Certificate) certificate;
            final Set<String> critSet = cert.getCriticalExtensionOIDs();

            if (CollKit.isNotEmpty(critSet) && critSet.contains("2.5.29.15")) {
                final boolean[] keyUsageInfo = cert.getKeyUsage();
                // keyUsageInfo[0] is for digitalSignature.
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false)) {
                    throw new CryptoException("Wrong data usage");
                }
            }
        }
        this.publicKey = certificate.getPublicKey();
        return this;
    }

}
