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

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.KeyType;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 非对称基础，提供锁、私钥和公钥的持有
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class BaseAsymmetric<T extends BaseAsymmetric<T>> {

    /**
     * 算法
     */
    protected String algorithm;
    /**
     * 公钥
     */
    protected PublicKey publicKey;
    /**
     * 私钥
     */
    protected PrivateKey privateKey;
    /**
     * 锁
     */
    protected Lock lock = new ReentrantLock();

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @since 3.1.1
     */
    public BaseAsymmetric(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        init(algorithm, privateKey, publicKey);
    }

    /**
     * 初始化
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密（签名）或者解密（校验）
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return this
     */
    protected T init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        this.algorithm = algorithm;

        if (null == privateKey && null == publicKey) {
            initKeys();
        } else {
            if (null != privateKey) {
                this.privateKey = privateKey;
            }
            if (null != publicKey) {
                this.publicKey = publicKey;
            }
        }
        return (T) this;
    }

    /**
     * 生成公钥和私钥
     *
     * @return this
     */
    public T initKeys() {
        KeyPair keyPair = CryptoUtils.generateKeyPair(this.algorithm);
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        return (T) this;
    }

    /**
     * 获得公钥
     *
     * @return 获得公钥
     */
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * 设置公钥
     *
     * @param publicKey 公钥
     * @return this
     */
    public T setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return (T) this;
    }

    /**
     * 获得公钥
     *
     * @return 获得公钥
     */
    public String getPublicKeyBase64() {
        final PublicKey publicKey = getPublicKey();
        return (null == publicKey) ? null : Base64.encode(publicKey.getEncoded());
    }

    /**
     * 获得私钥
     *
     * @return 获得私钥
     */
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    /**
     * 设置私钥
     *
     * @param privateKey 私钥
     * @return this
     */
    public T setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return (T) this;
    }

    /**
     * 获得私钥
     *
     * @return 获得私钥
     */
    public String getPrivateKeyBase64() {
        return Base64.encode(getPrivateKey().getEncoded());
    }

    /**
     * 根据密钥类型获得相应密钥
     *
     * @param type 类型 {@link KeyType}
     * @return {@link Key}
     */
    protected Key getKeyByType(KeyType type) {
        switch (type) {
            case PrivateKey:
                if (null == this.privateKey) {
                    throw new NullPointerException("Private key must not null when use it !");
                }
                return this.privateKey;
            case PublicKey:
                if (null == this.publicKey) {
                    throw new NullPointerException("Public key must not null when use it !");
                }
                return this.publicKey;
        }
        throw new CommonException("Uknown key type: " + type);
    }
}
