/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.net.tls;

import java.security.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * {@link KeyManager}相关工具 此工具用于读取和使用数字证书、对称密钥等相关信息
 *
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnyKeyManager {

    /**
     * 获取{@link KeyManagerFactory}
     *
     * @return {@link KeyManagerFactory}
     */
    public static KeyManagerFactory getDefaultKeyManagerFactory() {
        return getDefaultKeyManagerFactory(null);
    }

    /**
     * 获取{@link KeyManagerFactory}
     *
     * @param provider 算法提供者，{@code null}使用JDK默认
     * @return {@link KeyManagerFactory}
     */
    public static KeyManagerFactory getDefaultKeyManagerFactory(final Provider provider) {
        return getKeyManagerFactory(null, provider);
    }

    /**
     * 获取{@link KeyManagerFactory}
     *
     * @param algorithm 算法，{@code null}表示默认算法，如SunX509
     * @param provider  算法提供者，{@code null}使用JDK默认
     * @return {@link KeyManagerFactory}
     */
    public static KeyManagerFactory getKeyManagerFactory(String algorithm, final Provider provider) {
        if (StringKit.isBlank(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }

        try {
            return null == provider ? KeyManagerFactory.getInstance(algorithm)
                    : KeyManagerFactory.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 从KeyStore中获取{@link KeyManagerFactory}
     *
     * @param keyStore  KeyStore
     * @param password  密码
     * @param algorithm 算法，{@code null}表示默认算法，如SunX509
     * @param provider  算法提供者，{@code null}使用JDK默认
     * @return {@link KeyManager}列表
     */
    public static KeyManagerFactory getKeyManagerFactory(final KeyStore keyStore, final char[] password,
            final String algorithm, final Provider provider) {
        final KeyManagerFactory keyManagerFactory = getKeyManagerFactory(algorithm, provider);
        try {
            keyManagerFactory.init(keyStore, password);
        } catch (final KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new CryptoException(e);
        }
        return keyManagerFactory;
    }

    /**
     * 从KeyStore中获取{@link KeyManager}列表
     *
     * @param keyStore KeyStore
     * @param password 密码
     * @return {@link KeyManager}列表
     */
    public static KeyManager[] getKeyManagers(final KeyStore keyStore, final char[] password) {
        return getKeyManagers(keyStore, password, null, null);
    }

    /**
     * 从KeyStore中获取{@link KeyManager}列表
     *
     * @param keyStore  KeyStore
     * @param password  密码
     * @param algorithm 算法，{@code null}表示默认算法，如SunX509
     * @param provider  算法提供者，{@code null}使用JDK默认
     * @return {@link KeyManager}列表
     */
    public static KeyManager[] getKeyManagers(final KeyStore keyStore, final char[] password, final String algorithm,
            final Provider provider) {
        return getKeyManagerFactory(keyStore, password, algorithm, provider).getKeyManagers();
    }

}
