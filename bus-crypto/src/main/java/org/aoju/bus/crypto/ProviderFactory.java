package org.aoju.bus.crypto;

import java.security.Provider;

/**
 * Provider对象生产法工厂类
 *
 * <pre>
 * 调用{@link #createBouncyCastleProvider()} 用于新建一个BouncyCastleProvider对象
 * </pre>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class ProviderFactory implements CryptoFactory {

    /**
     * 创建Bouncy Castle 提供者
     *
     * @return {@link Provider}
     */
    public static Provider createBouncyCastleProvider() {
        return new org.bouncycastle.jce.provider.BouncyCastleProvider();
    }

}
