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
package org.miaixz.bus.crypto.asymmetric;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.CryptoException;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.HexKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.crypto.Builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Set;

/**
 * 签名包装，{@link Signature} 包装类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sign extends Asymmetric<Sign> {

    private static final long serialVersionUID = 1L;

    /**
     * 签名 用于签名和验证
     */
    protected Signature signature;

    /**
     * 构造 创建新的私钥公钥对
     *
     * @param algorithm {@link Algorithm}
     */
    public Sign(Algorithm algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * 构造 创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Sign(String algorithm) {
        this(algorithm, null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm     {@link Algorithm}
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public Sign(Algorithm algorithm, String privateKeyStr, String publicKeyStr) {
        this(algorithm.getValue(), Builder.decode(privateKeyStr), Builder.decode(publicKeyStr));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(Algorithm algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm {@link Algorithm}
     * @param keyPair   密钥对（包括公钥和私钥）
     */
    public Sign(Algorithm algorithm, KeyPair keyPair) {
        this(algorithm.getValue(), keyPair);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  {@link Algorithm}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(Algorithm algorithm, PrivateKey privateKey, PublicKey publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm        非对称加密算法
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     */
    public Sign(String algorithm, String privateKeyBase64, String publicKeyBase64) {
        this(algorithm, Base64.decode(privateKeyBase64), Base64.decode(publicKeyBase64));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(String algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm,
                Builder.generatePrivateKey(algorithm, privateKey),
                Builder.generatePublicKey(algorithm, publicKey)
        );
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm 算法，见{@link Algorithm}
     * @param keyPair   密钥对（包括公钥和私钥）
     */
    public Sign(String algorithm, KeyPair keyPair) {
        this(algorithm, keyPair.getPrivate(), keyPair.getPublic());
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 初始化
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return this
     */
    @Override
    public Sign init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        signature = Builder.createSignature(algorithm);
        super.init(algorithm, privateKey, publicKey);
        return this;
    }

    /**
     * 设置签名的参数
     *
     * @param params {@link AlgorithmParameterSpec}
     * @return this
     */
    public Sign setParameter(AlgorithmParameterSpec params) {
        try {
            this.signature.setParameter(params);
        } catch (InvalidAlgorithmParameterException e) {
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
    public byte[] sign(String data, Charset charset) {
        return sign(StringKit.bytes(data, charset));
    }

    /**
     * 生成文件签名
     *
     * @param data 被签名数据
     * @return 签名
     */
    public byte[] sign(String data) {
        return sign(data, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 生成文件签名，并转为16进制字符串
     *
     * @param data    被签名数据
     * @param charset 编码
     * @return 签名
     */
    public String signHex(String data, Charset charset) {
        return HexKit.encodeHexString(sign(data, charset));
    }

    /**
     * 生成文件签名
     *
     * @param data 被签名数据
     * @return 签名
     */
    public String signHex(String data) {
        return signHex(data, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @return 签名
     */
    public byte[] sign(byte[] data) {
        return sign(new ByteArrayInputStream(data), -1);
    }

    /**
     * 生成签名，并转为16进制字符串
     *
     * @param data 被签名数据
     * @return 签名
     */
    public String signHex(byte[] data) {
        return HexKit.encodeHexString(sign(data));
    }

    /**
     * 生成签名，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param data 被签名数据
     * @return 签名
     */
    public String signHex(InputStream data) {
        return HexKit.encodeHexString(sign(data));
    }

    /**
     * 生成签名，使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param data {@link InputStream} 数据流
     * @return 签名bytes
     */
    public byte[] sign(InputStream data) {
        return sign(data, IoKit.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 生成签名，并转为16进制字符串
     * 使用默认缓存大小，见 {@link IoKit#DEFAULT_BUFFER_SIZE}
     *
     * @param data         被签名数据
     * @param bufferLength 缓存长度，不足1使用 {@link IoKit#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 签名
     */
    public String digestHex(InputStream data, int bufferLength) {
        return HexKit.encodeHexString(sign(data, bufferLength));
    }

    /**
     * 生成签名
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link IoKit#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 签名bytes
     */
    public byte[] sign(InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = IoKit.DEFAULT_BUFFER_SIZE;
        }

        final byte[] buffer = new byte[bufferLength];
        lock.lock();
        try {
            signature.initSign(this.privateKey);
            byte[] result;
            try {
                int read = data.read(buffer, 0, bufferLength);
                while (read > -1) {
                    signature.update(buffer, 0, read);
                    read = data.read(buffer, 0, bufferLength);
                }
                result = signature.sign();
            } catch (Exception e) {
                throw new CryptoException(e);
            }
            return result;
        } catch (Exception e) {
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
    public boolean verify(byte[] data, byte[] sign) {
        lock.lock();
        try {
            signature.initVerify(this.publicKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (Exception e) {
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
    public Sign setSignature(Signature signature) {
        this.signature = signature;
        return this;
    }

    /**
     * 设置{@link Certificate} 为PublicKey
     * 如果Certificate是X509Certificate，我们需要检查是否有密钥扩展
     *
     * @param certificate {@link Certificate}
     * @return this
     */
    public Sign setCertificate(Certificate certificate) {
        // 如果证书的类型是X509Certificate，
        // 我们应该检查它是否有一个被标记为critical的密钥使用扩展名
        if (certificate instanceof X509Certificate) {
            // 检查cert是否将密钥使用扩展标记为关键扩展.
            // 密钥使用扩展的OID是2.5.29.15
            final X509Certificate cert = (X509Certificate) certificate;
            final Set<String> critSet = cert.getCriticalExtensionOIDs();

            if (CollKit.isNotEmpty(critSet) && critSet.contains("2.5.29.15")) {
                final boolean[] keyUsageInfo = cert.getKeyUsage();
                // keyUsageInfo[0] 是数字签名
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false)) {
                    throw new CryptoException("Wrong key usage");
                }
            }
        }
        this.publicKey = certificate.getPublicKey();
        return this;
    }

}
