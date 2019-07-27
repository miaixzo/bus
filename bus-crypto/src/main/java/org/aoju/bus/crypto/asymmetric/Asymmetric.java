package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.Mode;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 非对称加密算法
 *
 * <pre>
 * 1、签名：使用私钥加密，公钥解密。
 * 用于让所有公钥所有者验证私钥所有者的身份并且用来防止私钥所有者发布的内容被篡改，但是不用来保证内容不被他人获得。
 *
 * 2、加密：用公钥加密，私钥解密。
 * 用于向公钥所有者发布信息,这个信息可能被他人篡改,但是无法被他人获得。
 * </pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Asymmetric extends AbstractAsymmetric<Asymmetric> {

    /**
     * Cipher负责完成加密或解密工作
     */
    protected Cipher cipher;

    /**
     * 加密的块大小
     */
    protected int encryptBlockSize = -1;
    /**
     * 解密的块大小
     */
    protected int decryptBlockSize = -1;

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm {@link Mode}
     */
    public Asymmetric(Mode algorithm) {
        this(algorithm, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造，创建新的私钥公钥对
     *
     * @param algorithm 算法
     */
    public Asymmetric(String algorithm) {
        this(algorithm, (byte[]) null, (byte[]) null);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm     {@link Mode}
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public Asymmetric(Mode algorithm, String privateKeyStr, String publicKeyStr) {
        this(algorithm.getValue(), CryptoUtils.decode(privateKeyStr), CryptoUtils.decode(publicKeyStr));
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Mode}
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Asymmetric(Mode algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  {@link Mode}
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @since 3.1.1
     */
    public Asymmetric(Mode algorithm, PrivateKey privateKey, PublicKey publicKey) {
        this(algorithm.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm        非对称加密算法
     * @param privateKeyBase64 私钥Base64
     * @param publicKeyBase64  公钥Base64
     */
    public Asymmetric(String algorithm, String privateKeyBase64, String publicKeyBase64) {
        this(algorithm, Base64.decode(privateKeyBase64), Base64.decode(publicKeyBase64));
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public Asymmetric(String algorithm, byte[] privateKey, byte[] publicKey) {
        this(algorithm, //
                CryptoUtils.generatePrivateKey(algorithm, privateKey), //
                CryptoUtils.generatePublicKey(algorithm, publicKey)//
        );
    }

    /**
     * 构造
     * <p>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @since 3.1.1
     */
    public Asymmetric(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 获取加密块大小
     *
     * @return 加密块大小
     */
    public int getEncryptBlockSize() {
        return encryptBlockSize;
    }

    /**
     * 设置加密块大小
     *
     * @param encryptBlockSize 加密块大小
     */
    public void setEncryptBlockSize(int encryptBlockSize) {
        this.encryptBlockSize = encryptBlockSize;
    }

    /**
     * 获取解密块大小
     *
     * @return 解密块大小
     */
    public int getDecryptBlockSize() {
        return decryptBlockSize;
    }

    /**
     * 设置解密块大小
     *
     * @param decryptBlockSize 解密块大小
     */
    public void setDecryptBlockSize(int decryptBlockSize) {
        this.decryptBlockSize = decryptBlockSize;
    }

    @Override
    public Asymmetric init(String algorithm, PrivateKey privateKey, PublicKey publicKey) {
        super.init(algorithm, privateKey, publicKey);
        initCipher();
        return this;
    }

    /**
     * 加密
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        final int inputLen = data.length;
        final int maxBlockSize = this.encryptBlockSize < 0 ? inputLen : this.encryptBlockSize;

        lock.lock();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int offSet = 0;
            byte[] cache;
            // 剩余长度
            int remainLength = inputLen;
            // 对数据分段加密
            while (remainLength > 0) {
                cache = cipher.doFinal(data, offSet, Math.min(remainLength, maxBlockSize));
                out.write(cache, 0, cache.length);

                offSet += maxBlockSize;
                remainLength = inputLen - offSet;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CommonException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解密
     *
     * @param bytes   被解密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 解密后的bytes
     */
    @Override
    public byte[] decrypt(byte[] bytes, KeyType keyType) {
        final Key key = getKeyByType(keyType);
        // 模长
        final int inputLen = bytes.length;
        final int maxBlockSize = this.decryptBlockSize < 0 ? inputLen : this.decryptBlockSize;

        lock.lock();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            cipher.init(Cipher.DECRYPT_MODE, key);
            int offSet = 0;
            byte[] cache;
            // 剩余长度
            int remainLength = inputLen;
            // 对数据分段解密
            while (remainLength > 0) {
                cache = cipher.doFinal(bytes, offSet, Math.min(remainLength, maxBlockSize));
                out.write(cache, 0, cache.length);

                offSet += maxBlockSize;
                remainLength = inputLen - offSet;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new CommonException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获得加密或解密器
     *
     * @return 加密或解密
     */
    public Cipher getClipher() {
        return cipher;
    }

    /**
     * 初始化{@link Cipher}，默认尝试加载BC库
     *
     * @since 4.5.2
     */
    protected void initCipher() {
        this.cipher = CryptoUtils.createCipher(algorithm);
    }

}
