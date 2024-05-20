/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.net.tls;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

import javax.net.ssl.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.X509Certificate;

/**
 * 信任所有信任管理器，默认信任所有客户端和服务端证书
 * 注意此类慎用，信任全部可能会有中间人攻击风险
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TrustAnyTrustManager extends X509ExtendedTrustManager {

    /**
     * 全局单例信任管理器，默认信任所有客户端和服务端证书
     */
    public static final TrustAnyTrustManager INSTANCE = new TrustAnyTrustManager();
    /**
     * 信任所有
     */
    public static final X509TrustManager[] TRUST_ANYS = {INSTANCE};
    private static final X509Certificate[] EMPTY_X509_CERTIFICATE_ARRAY = {};

    /**
     * 获取默认的{@link TrustManager}，为SunX509
     * 此方法主要用于获取自签证书的{@link X509TrustManager}
     *
     * @return {@link X509TrustManager} or {@code null}
     */
    public static X509TrustManager getDefaultTrustManager() {
        return getTrustManager(null, null);
    }

    /**
     * 获取指定的{@link X509TrustManager}
     * 此方法主要用于获取自签证书的{@link X509TrustManager}
     *
     * @param keyStore {@link KeyStore}
     * @param provider 算法提供者，如bc，{@code null}表示默认
     * @return {@link X509TrustManager} or {@code null}
     */
    public static X509TrustManager getTrustManager(final KeyStore keyStore, final Provider provider) {
        return getTrustManager(keyStore, null, provider);
    }

    /**
     * 获取指定的{@link X509TrustManager}
     * 此方法主要用于获取自签证书的{@link X509TrustManager}
     *
     * @param keyStore  {@link KeyStore}
     * @param algorithm 算法名称，如"SunX509"，{@code null}表示默认SunX509
     * @param provider  算法提供者，如bc，{@code null}表示默认SunJSSE
     * @return {@link X509TrustManager} or {@code null}
     */
    public static X509TrustManager getTrustManager(final KeyStore keyStore, final String algorithm, final Provider provider) {
        final TrustManager[] tms = getTrustManagers(keyStore, algorithm, provider);
        for (final TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }

        return null;
    }

    /**
     * 获取默认的{@link TrustManager}，为SunX509
     * 此方法主要用于获取自签证书的{@link TrustManager}
     *
     * @return {@link X509TrustManager} or {@code null}
     */
    public static TrustManager[] getDefaultTrustManagers() {
        return getTrustManagers(null, null, null);
    }

    /**
     * 获取指定的{@link TrustManager}
     * 此方法主要用于获取自签证书的{@link TrustManager}
     *
     * @param keyStore  {@link KeyStore}
     * @param algorithm 算法名称，如"SunX509"，{@code null}表示默认SunX509
     * @param provider  算法提供者，如bc，{@code null}表示默认SunJSSE
     * @return {@link TrustManager} or {@code null}
     */
    public static TrustManager[] getTrustManagers(final KeyStore keyStore, String algorithm, final Provider provider) {
        final TrustManagerFactory tmf;

        if (StringKit.isEmpty(algorithm)) {
            algorithm = TrustManagerFactory.getDefaultAlgorithm();
        }
        try {
            if (null == provider) {
                tmf = TrustManagerFactory.getInstance(algorithm);
            } else {
                tmf = TrustManagerFactory.getInstance(algorithm, provider);
            }
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
        try {
            tmf.init(keyStore);
        } catch (final KeyStoreException e) {
            throw new InternalException(e);
        }

        return tmf.getTrustManagers();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EMPTY_X509_CERTIFICATE_ARRAY;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s, final Socket socket) {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s, final Socket socket) {
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s, final SSLEngine sslEngine) {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s, final SSLEngine sslEngine) {
    }

}
