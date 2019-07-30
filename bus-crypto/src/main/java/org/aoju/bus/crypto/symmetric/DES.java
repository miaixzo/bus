package org.aoju.bus.crypto.symmetric;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.Mode;
import org.aoju.bus.crypto.Padding;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * DES加密算法实现<br>
 * DES全称为Data Encryption Standard，即数据加密标准，是一种使用密钥加密的块算法<br>
 * Java中默认实现为：DES/CBC/PKCS5Padding
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DES extends Symmetric {

    /**
     * 构造，默认DES/CBC/PKCS5Padding，使用随机密钥
     */
    public DES() {
        super(Mode.DES);
    }

    /**
     * 构造，使用默认的DES/CBC/PKCS5Padding
     *
     * @param key 密钥
     */
    public DES(byte[] key) {
        super(Mode.DES, key);
    }

    /**
     * 构造，使用随机密钥
     *
     * @param mode    模式{@link Mode}
     * @param padding {@link Padding}补码方式
     */
    public DES(Mode mode, Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(Mode mode, Padding padding, byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      偏移向量，加盐
     * @since 3.3.0
     */
    public DES(Mode mode, Padding padding, byte[] key, byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     * @since 3.3.0
     */
    public DES(Mode mode, Padding padding, SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      偏移向量，加盐
     * @since 3.3.0
     */
    public DES(Mode mode, Padding padding, SecretKey key, IvParameterSpec iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     */
    public DES(String mode, String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(String mode, String padding, byte[] key) {
        this(mode, padding, CryptoUtils.generateKey("DES", key), null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      加盐
     */
    public DES(String mode, String padding, byte[] key, byte[] iv) {
        this(mode, padding, CryptoUtils.generateKey("DES", key), null == iv ? null : new IvParameterSpec(iv));
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(String mode, String padding, SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      加盐
     */
    public DES(String mode, String padding, SecretKey key, IvParameterSpec iv) {
        super(StringUtils.format("DES/{}/{}", mode, padding), key, iv);
    }

    /**
     * 设置偏移向量
     *
     * @param iv {@link IvParameterSpec}偏移向量
     * @return 自身
     */
    public DES setIv(IvParameterSpec iv) {
        super.setParams(iv);
        return this;
    }

    /**
     * 设置偏移向量
     *
     * @param iv 偏移向量，加盐
     * @return 自身
     * @since 3.3.0
     */
    public DES setIv(byte[] iv) {
        setIv(new IvParameterSpec(iv));
        return this;
    }

}
