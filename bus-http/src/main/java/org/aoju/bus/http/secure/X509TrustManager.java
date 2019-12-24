package org.aoju.bus.http.secure;

import java.security.cert.X509Certificate;

/**
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class X509TrustManager implements javax.net.ssl.X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

}
