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
package org.miaixz.bus.crypto.builtin.asymmetric;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.crypto.Keeper;

import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 非对称基础，提供锁、私钥和公钥的持有
 *
 * @param <T> this类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Asymmetric<T extends Asymmetric<T>> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 锁
     */
    protected final Lock lock = new ReentrantLock();
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
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Asymmetric(final String algorithm, final PrivateKey privateKey, final PublicKey publicKey) {
        init(algorithm, privateKey, publicKey);
    }

    /**
     * 初始化 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密（签名）或者解密（校验）
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return this
     */
    protected T init(final String algorithm, final PrivateKey privateKey, final PublicKey publicKey) {
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
        final KeyPair keyPair = Keeper.generateKeyPair(this.algorithm);
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
    public T setPublicKey(final PublicKey publicKey) {
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
    public T setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
        return (T) this;
    }

    /**
     * 获得私钥
     *
     * @return 获得私钥
     */
    public String getPrivateKeyBase64() {
        final PrivateKey privateKey = getPrivateKey();
        return (null == privateKey) ? null : Base64.encode(privateKey.getEncoded());
    }

    /**
     * 设置密钥，可以是公钥{@link PublicKey}或者私钥{@link PrivateKey}
     *
     * @param key 密钥，可以是公钥{@link PublicKey}或者私钥{@link PrivateKey}
     * @return this
     */
    public T setKey(final Key key) {
        Assert.notNull(key, "data must be not null !");

        if (key instanceof PublicKey) {
            return setPublicKey((PublicKey) key);
        } else if (key instanceof PrivateKey) {
            return setPrivateKey((PrivateKey) key);
        }
        throw new CryptoException("Unsupported data type: {}", key.getClass());
    }

    /**
     * 根据密钥类型获得相应密钥
     *
     * @param type 类型 {@link KeyType}
     * @return {@link Key}
     */
    protected Key getKeyByType(final KeyType type) {
        switch (type) {
        case PrivateKey:
            if (null == this.privateKey) {
                throw new NullPointerException("Private data must not null when use it !");
            }
            return this.privateKey;
        case PublicKey:
            if (null == this.publicKey) {
                throw new NullPointerException("Public data must not null when use it !");
            }
            return this.publicKey;
        }
        throw new CryptoException("Unsupported data type: " + type);
    }

}
