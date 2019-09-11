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
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.crypto.CryptoUtils;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * 签名包装，{@link Signature} 包装类
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public class Sign extends BaseAsymmetric<Sign> {

    /**
     * 签名，用于签名和验证
     */
    protected Signature signature;

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param mode {@link ModeType}
     */
    public Sign(String mode) {
        this(mode, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param mode          {@link ModeType}
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public Sign(String mode, String privateKeyStr, String publicKeyStr) {
        this(mode, CryptoUtils.decode(privateKeyStr), CryptoUtils.decode(publicKeyStr));
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param mode       算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(String mode, byte[] privateKey, byte[] publicKey) {
        this(mode,
                CryptoUtils.generatePrivateKey(mode, privateKey),
                CryptoUtils.generatePublicKey(mode, publicKey)
        );
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param mode    算法，见{@link ModeType}
     * @param keyPair 密钥对（包括公钥和私钥）
     */
    public Sign(String mode, KeyPair keyPair) {
        this(mode, keyPair.getPrivate(), keyPair.getPublic());
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做签名或验证
     *
     * @param mode       算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Sign(String mode, PrivateKey privateKey, PublicKey publicKey) {
        super(mode, privateKey, publicKey);
    }

    /**
     * 初始化
     *
     * @param mode       算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return this
     */
    @Override
    public Sign init(String mode, PrivateKey privateKey, PublicKey publicKey) {
        try {
            signature = Signature.getInstance(mode);
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException(e);
        }
        super.init(mode, privateKey, publicKey);
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
            throw new CommonException(e);
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
            throw new CommonException(e);
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
     * 设置{@link Certificate} 为PublicKey
     * 如果Certificate是X509Certificate，我们需要检查是否有密钥扩展
     *
     * @param certificate {@link Certificate}
     * @return this
     */
    public Sign setCertificate(Certificate certificate) {
        // If the certificate is of type X509Certificate,
        // we should check whether it has a Key Usage
        // extension marked as critical.
        if (certificate instanceof X509Certificate) {
            // Check whether the cert has a key usage extension
            // marked as a critical extension.
            // The OID for KeyUsage extension is 2.5.29.15.
            final X509Certificate cert = (X509Certificate) certificate;
            final Set<String> critSet = cert.getCriticalExtensionOIDs();

            if (CollUtils.isNotEmpty(critSet) && critSet.contains("2.5.29.15")) {
                final boolean[] keyUsageInfo = cert.getKeyUsage();
                // keyUsageInfo[0] is for digitalSignature.
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false)) {
                    throw new CommonException("Wrong key usage");
                }
            }
        }
        this.publicKey = certificate.getPublicKey();
        return this;
    }
}
