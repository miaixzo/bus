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
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.crypto.Builder;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Set;

/**
 * 签名包装,{@link Signature} 包装类
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class Sign extends Keys<Sign> {

    /**
     * 签名,用于签名和验证
     */
    protected Signature signature;


    /**
     * 构造,创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Sign(String algorithm) {
        this(algorithm, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public Sign(String algorithm, String privateKey, String publicKey) {
        this(algorithm, Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做签名或验证
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
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做签名或验证
     *
     * @param algorithm 算法
     * @param keyPair   密钥对（包括公钥和私钥）
     */
    public Sign(String algorithm, KeyPair keyPair) {
        this(algorithm, keyPair.getPrivate(), keyPair.getPublic());
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
        try {
            signature = Signature.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new InstrumentException(e);
        }
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
            throw new InstrumentException(e);
        }
        return this;
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @return 签名
     */
    public byte[] sign(byte[] data) {
        lock.lock();
        try {
            signature.initSign(this.privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new InstrumentException(e);
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
            throw new InstrumentException(e);
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
     * @return 自身 {@link Asymmetric}
     */
    public Sign setSignature(Signature signature) {
        this.signature = signature;
        return this;
    }

    /**
     * 设置{@link Certificate} 为PublicKey<br>
     * 如果Certificate是X509Certificate,我们需要检查是否有密钥扩展
     *
     * @param certificate {@link Certificate}
     * @return this
     */
    public Sign setCertificate(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            final X509Certificate cert = (X509Certificate) certificate;
            final Set<String> critSet = cert.getCriticalExtensionOIDs();

            if (CollUtils.isNotEmpty(critSet) && critSet.contains("2.5.29.15")) {
                final boolean[] keyUsageInfo = cert.getKeyUsage();
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false)) {
                    throw new InstrumentException("Wrong key usage");
                }
            }
        }
        this.publicKey = certificate.getPublicKey();
        return this;
    }

}
