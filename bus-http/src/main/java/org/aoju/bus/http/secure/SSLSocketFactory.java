package org.aoju.bus.http.secure;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;

public class SSLSocketFactory {

    /**
     * Https SSL证书
     *
     * @param X509TrustManager
     * @return SSLSocketFactory
     */
    public static javax.net.ssl.SSLSocketFactory createTrustAllSSLFactory(X509TrustManager X509TrustManager) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{X509TrustManager}, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

}
