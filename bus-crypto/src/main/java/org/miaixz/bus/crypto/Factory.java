package org.miaixz.bus.crypto;

/**
 * 加解密服务提供
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Factory {

    /**
     * 数据加密
     * 1. 私钥加密
     * 2. 公钥加密
     *
     * @param key     密钥,字符串,分割
     *                示例: 5c3,5c3,PrivateKey
     * @param content 需要加密的内容
     * @return 加密结果
     */
    byte[] encrypt(String key, byte[] content);

    /**
     * 数据解密
     * 1. 公钥解密
     * 2. 私钥解密
     *
     * @param key     密钥, 字符串使用,分割
     *                格式: 私钥,公钥,类型
     *                示例: 5c3,5c3,PrivateKey
     *                1. 私钥加密,公钥解密
     *                2. 公钥加密,私钥解密
     * @param content 需要解密的内容
     * @return 解密结果
     */
    byte[] decrypt(String key, byte[] content);

}
