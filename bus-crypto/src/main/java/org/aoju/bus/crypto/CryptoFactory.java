package org.aoju.bus.crypto;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface CryptoFactory {

    /**
     * 加密
     *
     * @param key     密钥
     * @param content 需要加密的内容
     * @return 加密结果
     * @throws RuntimeException RuntimeException
     */
    byte[] encrypt(String key, byte[] content) throws RuntimeException;

    /**
     * 解密
     *
     * @param key     密钥
     * @param content 需要解密的内容
     * @return 解密结果
     * @throws RuntimeException RuntimeException
     */
    byte[] decrypt(String key, byte[] content) throws RuntimeException;
}
