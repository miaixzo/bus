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

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.KeyType;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 国密SM2算法实现，基于BC库
 * SM2算法只支持公钥加密，私钥解密
 * <p>
 * /**
 *
 * @author Kimi Liu
 * @version 3.5.3
 * @since JDK 1.8
 */
public class SM2 extends AbstractAsymmetric<SM2> {

    /**
     * 算法EC
     */
    private static final String ALGORITHM_SM2 = "SM2";

    protected SM2Engine engine;
    protected SM2Signer signer;

    private SM2Engine.SM2Mode mode;
    private ECPublicKeyParameters publicKeyParams;
    private ECPrivateKeyParameters privateKeyParams;

    /**
     * 构造，生成新的私钥公钥对
     */
    public SM2() {
        this((byte[]) null, (byte[]) null);
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public SM2(String privateKeyStr, String publicKeyStr) {
        this(CryptoUtils.decode(privateKeyStr), CryptoUtils.decode(publicKeyStr));
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public SM2(byte[] privateKey, byte[] publicKey) {
        this(//
                CryptoUtils.generatePrivateKey(ALGORITHM_SM2, privateKey), //
                CryptoUtils.generatePublicKey(ALGORITHM_SM2, publicKey)//
        );
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public SM2(PrivateKey privateKey, PublicKey publicKey) {
        super(ALGORITHM_SM2, privateKey, publicKey);
    }

    /**
     * 初始化
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密（签名）或者解密（校验）
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return this
     */
    public SM2 init(PrivateKey privateKey, PublicKey publicKey) {
        return this.init(ALGORITHM_SM2, privateKey, publicKey);
    }

    @Override
    protected SM2 init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super.init(algorithm, privateKey, publicKey);
        return initCipherParams();
    }

    /**
     * 加密，SM2非对称加密的结果由C1,C2,C3三部分组成，其中：
     *
     * <pre>
     * C1 生成随机数的计算出的椭圆曲线点
     * C2 密文数据
     * C3 SM3的摘要值
     * </pre>
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     * @throws CommonException 包括InvalidKeyException和InvalidCipherTextException的包装异常
     */
    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) throws CommonException {
        if (KeyType.PublicKey != keyType) {
            throw new IllegalArgumentException("Encrypt is only support by public key");
        }
        ckeckKey(keyType);

        lock.lock();
        final SM2Engine engine = getEngine();
        try {
            engine.init(true, new ParametersWithRandom(getCipherParameters(keyType)));
            return engine.processBlock(data, 0, data.length);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解密
     *
     * @param data    SM2密文，实际包含三部分：ECC公钥、真正的密文、公钥和原文的SM3-HASH值
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     * @throws CommonException 包括InvalidKeyException和InvalidCipherTextException的包装异常
     */
    @Override
    public byte[] decrypt(byte[] data, KeyType keyType) throws CommonException {
        if (KeyType.PrivateKey != keyType) {
            throw new IllegalArgumentException("Decrypt is only support by private key");
        }
        ckeckKey(keyType);

        lock.lock();
        final SM2Engine engine = getEngine();
        try {
            engine.init(false, getCipherParameters(keyType));
            return engine.processBlock(data, 0, data.length);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @return 签名
     */
    public byte[] sign(byte[] data) {
        return sign(data, null);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @param id   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 签名
     */
    public byte[] sign(byte[] data, byte[] id) {
        lock.lock();
        final SM2Signer signer = getSigner();
        try {
            CipherParameters param = new ParametersWithRandom(getCipherParameters(KeyType.PrivateKey));
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(true, param);
            signer.update(data, 0, data.length);
            return signer.generateSignature();
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
        return verify(data, sign, null);
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param data 数据
     * @param sign 签名
     * @param id   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 是否验证通过
     */
    public boolean verify(byte[] data, byte[] sign, byte[] id) {
        lock.lock();
        final SM2Signer signer = getSigner();
        try {
            CipherParameters param = getCipherParameters(KeyType.PublicKey);
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(false, param);
            signer.update(data, 0, data.length);
            return signer.verifySignature(sign);
        } catch (Exception e) {
            throw new CommonException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设置加密类型
     *
     * @param mode {@link SM2Engine.SM2Mode}
     * @return this
     */
    public SM2 setMode(SM2Engine.SM2Mode mode) {
        this.mode = mode;
        if (null != this.engine) {
            this.engine.setMode(mode);
        }
        return this;
    }

    /**
     * 初始化加密解密参数
     *
     * @return this
     */
    private SM2 initCipherParams() {
        try {
            if (null != this.publicKey) {
                this.publicKeyParams = (ECPublicKeyParameters) ECUtil.generatePublicKeyParameter(this.publicKey);
            }
            if (null != privateKey) {
                this.privateKeyParams = (ECPrivateKeyParameters) ECUtil.generatePrivateKeyParameter(this.privateKey);
            }
        } catch (InvalidKeyException e) {
            throw new CommonException(e);
        }

        return this;
    }

    /**
     * 获取密钥类型对应的加密参数对象{@link CipherParameters}
     *
     * @param keyType Key类型枚举，包括私钥或公钥
     * @return {@link CipherParameters}
     */
    private CipherParameters getCipherParameters(KeyType keyType) {
        switch (keyType) {
            case PublicKey:
                return this.publicKeyParams;
            case PrivateKey:
                return this.privateKeyParams;
        }

        return null;
    }

    /**
     * 检查对应类型的Key是否存在
     *
     * @param keyType key类型
     */
    private void ckeckKey(KeyType keyType) {
        switch (keyType) {
            case PublicKey:
                if (null == this.publicKey) {
                    throw new NullPointerException("No public key provided");
                }
                break;
            case PrivateKey:
                if (null == this.privateKey) {
                    throw new NullPointerException("No private key provided");
                }
                break;
        }
    }

    /**
     * 获取{@link SM2Engine}
     *
     * @return {@link SM2Engine}
     */
    private SM2Engine getEngine() {
        if (null == this.engine) {
            this.engine = new SM2Engine(this.mode);
        }
        return this.engine;
    }

    /**
     * 获取{@link SM2Signer}
     *
     * @return {@link SM2Signer}
     */
    private SM2Signer getSigner() {
        if (null == this.signer) {
            this.signer = new SM2Signer();
        }
        return this.signer;
    }

}
