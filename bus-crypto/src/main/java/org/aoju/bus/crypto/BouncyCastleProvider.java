package org.aoju.bus.crypto;

import java.security.Provider;

/**
 * 全局单例的 org.bouncycastle.jce.provider.BouncyCastleProvider 对象
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum BouncyCastleProvider {

    INSTANCE;
    private static boolean useBouncyCastle = true;
    private Provider provider;

    BouncyCastleProvider() {
        try {
            this.provider = ProviderFactory.createBouncyCastleProvider();
        } catch (NoClassDefFoundError e) {
            // ignore
        }
    }

    /**
     * 设置是否使用Bouncy Castle库<br>
     * 如果设置为false，表示强制关闭Bouncy Castle而使用JDK
     *
     * @param isUseBouncyCastle
     * @since 4.5.2
     */
    public static void setUseBouncyCastle(boolean isUseBouncyCastle) {
        useBouncyCastle = isUseBouncyCastle;
    }

    /**
     * 获取{@link Provider}
     *
     * @return {@link Provider}
     */
    public Provider getProvider() {
        return useBouncyCastle ? this.provider : null;
    }
}
