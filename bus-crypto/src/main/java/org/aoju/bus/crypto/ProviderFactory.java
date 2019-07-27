package org.aoju.bus.crypto;

import javax.crypto.Cipher;
import java.security.Provider;

/**
 * Provider对象生产法工厂类
 *
 * <pre>
 * 1. 调用{@link #createBouncyCastleProvider()} 用于新建一个org.bouncycastle.jce.provider.BouncyCastleProvider对象
 * </pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class ProviderFactory implements CryptoFactory {

    protected volatile Cipher encryptCipher = null;
    protected volatile Cipher decryptCipher = null;

    /**
     * 创建Bouncy Castle 提供者
     *
     * @return {@link Provider}
     */
    public static Provider createBouncyCastleProvider() {
        return new org.bouncycastle.jce.provider.BouncyCastleProvider();
    }

}
