/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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

import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.crypto.Builder;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.math.ec.*;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

/**
 * 国密SM2算法实现,基于BC库StringUtils
 * SM2算法只支持公钥加密,私钥解密
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class SM2 extends Safety<SM2> {

    protected SM2Signer signer;

    private Digest digest = new SM3Digest();
    private SM2Mode mode;

    private boolean forEncryption;
    private ECKeyParameters ecKey;
    private ECDomainParameters ecParams;
    private int curveLength;
    private Random random;

    private ECPublicKeyParameters publicKeyParams;
    private ECPrivateKeyParameters privateKeyParams;

    /**
     * 构造,生成新的私钥公钥对
     */
    public SM2() {
        this((byte[]) null, null);
    }

    /**
     * 构造StringUtils
     * 私钥和公钥同时为空时生成一对新的私钥和公钥StringUtils
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public SM2(String privateKey, String publicKey) {
        this(Builder.decode(privateKey), Builder.decode(publicKey));
    }

    /**
     * 构造 StringUtils
     * 私钥和公钥同时为空时生成一对新的私钥和公钥StringUtils
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public SM2(byte[] privateKey, byte[] publicKey) {
        this(Builder.generatePrivateKey(Algorithm.SM2, privateKey), Builder.generatePublicKey(Algorithm.SM2, publicKey));
    }

    /**
     * 构造 StringUtils
     * 私钥和公钥同时为空时生成一对新的私钥和公钥StringUtils
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public SM2(PrivateKey privateKey, PublicKey publicKey) {
        super(Algorithm.SM2, privateKey, publicKey);
    }

    @Override
    protected SM2 init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super.init(algorithm, privateKey, publicKey);
        return initCipherParams();
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
     * 初始化StringUtils
     * 私钥和公钥同时为空时生成一对新的私钥和公钥StringUtils
     * 私钥和公钥可以单独传入一个,如此则只能使用此钥匙来做加密（签名）或者解密（校验）
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return this
     */
    public SM2 init(PrivateKey privateKey, PublicKey publicKey) {
        return this.init(Algorithm.SM2, privateKey, publicKey);
    }

    /**
     * 获取密钥类型对应的加密参数对象{@link CipherParameters}
     *
     * @param keyType Key类型枚举,包括私钥或公钥
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
     * 加密,SM2非对称加密的结果由C1,C2,C3三部分组成,其中：
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
     */
    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) {
        if (KeyType.PublicKey != keyType) {
            throw new IllegalArgumentException("Encrypt is only support by public key");
        }
        ckeckKey(keyType);

        lock.lock();
        try {
            init(true, new ParametersWithRandom(getCipherParameters(keyType)));
            return processBlock(data, 0, data.length);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解密
     *
     * @param data    SM2密文,实际包含三部分：ECC公钥、真正的密文、公钥和原文的SM3-HASH值
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    @Override
    public byte[] decrypt(byte[] data, KeyType keyType) {
        if (KeyType.PrivateKey != keyType) {
            throw new IllegalArgumentException("Decrypt is only support by private key");
        }
        ckeckKey(keyType);

        lock.lock();
        try {
            init(false, getCipherParameters(keyType));
            return processBlock(data, 0, data.length);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @param id   可以为null,若为null,则默认withId为字节数组:"1234567812345678".getBytes()
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
     * @param id   可以为null,若为null,则默认withId为字节数组:"1234567812345678".getBytes()
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
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
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
            throw new InstrumentException(e);
        }

        return this;
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

    /**
     * 初始化引擎
     *
     * @param forEncryption 是否为加密模式
     * @param param         {@link CipherParameters},此处应为{@link ParametersWithRandom}（加密时）或{@link ECKeyParameters}（解密时）
     */
    public void init(boolean forEncryption, CipherParameters param) {
        this.forEncryption = forEncryption;

        if (param instanceof ParametersWithRandom) {
            final ParametersWithRandom rParam = (ParametersWithRandom) param;
            this.ecKey = (ECKeyParameters) rParam.getParameters();
            this.random = rParam.getRandom();
        } else {
            this.ecKey = (ECKeyParameters) param;
        }
        this.ecParams = this.ecKey.getParameters();

        if (forEncryption) {
            // 检查曲线点
            final ECPoint ecPoint = ((ECPublicKeyParameters) ecKey).getQ().multiply(ecParams.getH());
            if (ecPoint.isInfinity()) {
                throw new IllegalArgumentException("invalid key: [h]Q at infinity");
            }

            // 检查随机参数
            if (null == this.random) {
                this.random = CryptoServicesRegistrar.getSecureRandom();
            }
        }

        // 曲线位长度
        this.curveLength = (this.ecParams.getCurve().getFieldSize() + 7) / 8;
    }

    /**
     * 处理块,包括加密和解密
     *
     * @param in    数据
     * @param inOff 数据开始位置
     * @param inLen 数据长度
     * @return 结果
     */
    public byte[] processBlock(byte[] in, int inOff, int inLen) {
        if (forEncryption) {
            return encrypt(in, inOff, inLen);
        } else {
            return decrypt(in, inOff, inLen);
        }
    }

    /**
     * 设置加密类型
     *
     * @param mode {@link SM2Mode}
     * @return this
     */
    public SM2 setMode(SM2Mode mode) {
        this.mode = ObjectUtils.defaultIfNull(mode, SM2Mode.C1C3C2);
        return this;
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    /**
     * 加密
     *
     * @param in    数据
     * @param inOff 位置
     * @param inLen 长度
     * @return 密文
     */
    private byte[] encrypt(byte[] in, int inOff, int inLen) {
        // 加密数据
        byte[] c2 = new byte[inLen];
        System.arraycopy(in, inOff, c2, 0, c2.length);

        final ECMultiplier multiplier = createBasePointMultiplier();

        byte[] c1;
        ECPoint kPB;
        BigInteger k;
        do {
            k = nextK();
            // 产生随机数计算出曲线点C1
            c1 = multiplier.multiply(ecParams.getG(), k).normalize().getEncoded(false);
            kPB = ((ECPublicKeyParameters) ecKey).getQ().multiply(k).normalize();
            kdf(kPB, c2);
        } while (notEncrypted(c2, in, inOff));

        // 杂凑值,效验数据
        byte[] c3 = new byte[digest.getDigestSize()];

        addFieldElement(kPB.getAffineXCoord());
        this.digest.update(in, inOff, inLen);
        addFieldElement(kPB.getAffineYCoord());

        this.digest.doFinal(c3, 0);

        // 按照对应模式输出结果
        switch (mode) {
            case C1C3C2:
                return Arrays.concatenate(c1, c3, c2);
            default:
                return Arrays.concatenate(c1, c2, c3);
        }
    }

    /**
     * 解密,只支持私钥解密
     *
     * @param in    密文
     * @param inOff 位置
     * @param inLen 长度
     * @return 解密后的内容
     */
    private byte[] decrypt(byte[] in, int inOff, int inLen) {
        // 获取曲线点
        final byte[] c1 = new byte[this.curveLength * 2 + 1];
        System.arraycopy(in, inOff, c1, 0, c1.length);

        ECPoint c1P = this.ecParams.getCurve().decodePoint(c1);
        if (c1P.multiply(this.ecParams.getH()).isInfinity()) {
            throw new InstrumentException("[h]C1 at infinity");
        }
        c1P = c1P.multiply(((ECPrivateKeyParameters) ecKey).getD()).normalize();

        final int digestSize = this.digest.getDigestSize();

        // 解密C2数据
        final byte[] c2 = new byte[inLen - c1.length - digestSize];

        if (SM2Mode.C1C3C2 == this.mode) {
            // C2位于第三部分
            System.arraycopy(in, inOff + c1.length + digestSize, c2, 0, c2.length);
        } else {
            // C2位于第二部分
            System.arraycopy(in, inOff + c1.length, c2, 0, c2.length);
        }
        kdf(c1P, c2);

        // 使用摘要验证C2数据
        final byte[] c3 = new byte[digestSize];

        addFieldElement(c1P.getAffineXCoord());
        this.digest.update(c2, 0, c2.length);
        addFieldElement(c1P.getAffineYCoord());
        this.digest.doFinal(c3, 0);

        int check = 0;
        for (int i = 0; i != c3.length; i++) {
            check |= c3[i] ^ in[inOff + c1.length + ((SM2Mode.C1C3C2 == this.mode) ? 0 : c2.length) + i];
        }

        Arrays.fill(c1, (byte) 0);
        Arrays.fill(c3, (byte) 0);

        if (check != 0) {
            Arrays.fill(c2, (byte) 0);
            throw new InstrumentException("invalid cipher text");
        }

        return c2;
    }

    private boolean notEncrypted(byte[] encData, byte[] in, int inOff) {
        for (int i = 0; i != encData.length; i++) {
            if (encData[i] != in[inOff + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解密数据
     *
     * @param c1      c1点
     * @param encData 密文
     */
    private void kdf(ECPoint c1, byte[] encData) {
        final Digest digest = this.digest;
        int digestSize = digest.getDigestSize();
        byte[] buf = new byte[Math.max(4, digestSize)];
        int off = 0;

        Memoable memo = null;
        Memoable copy = null;

        if (digest instanceof Memoable) {
            addFieldElement(c1.getAffineXCoord());
            addFieldElement(c1.getAffineYCoord());
            memo = (Memoable) digest;
            copy = memo.copy();
        }

        int ct = 0;

        while (off < encData.length) {
            if (memo != null) {
                memo.reset(copy);
            } else {
                addFieldElement(c1.getAffineXCoord());
                addFieldElement(c1.getAffineYCoord());
            }

            Pack.intToBigEndian(++ct, buf, 0);
            digest.update(buf, 0, 4);
            digest.doFinal(buf, 0);

            int xorLen = Math.min(digestSize, encData.length - off);
            xor(encData, buf, off, xorLen);
            off += xorLen;
        }
    }

    /**
     * 异或
     *
     * @param data       数据
     * @param kdfOut     kdf输出值
     * @param dOff       d偏移
     * @param dRemaining d剩余
     */
    private void xor(byte[] data, byte[] kdfOut, int dOff, int dRemaining) {
        for (int i = 0; i != dRemaining; i++) {
            data[dOff + i] ^= kdfOut[i];
        }
    }

    /**
     * 下一个K值
     *
     * @return K值
     */
    private BigInteger nextK() {
        final int qBitLength = this.ecParams.getN().bitLength();

        BigInteger k;
        do {
            k = new BigInteger(qBitLength, this.random);
        } while (k.equals(ECConstants.ZERO) || k.compareTo(this.ecParams.getN()) >= 0);

        return k;
    }

    /**
     * 增加字段节点
     *
     * @param v 节点信息
     */
    private void addFieldElement(ECFieldElement v) {
        final byte[] p = BigIntegers.asUnsignedByteArray(this.curveLength, v.toBigInteger());
        this.digest.update(p, 0, p.length);
    }

    /**
     * SM2算法模式<br>
     * 在SM2算法中,C1C2C3为旧标准模式,C1C3C2为新标准模式
     */
    public enum SM2Mode {
        C1C2C3, C1C3C2
    }

}
