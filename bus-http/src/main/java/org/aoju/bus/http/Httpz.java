package org.aoju.bus.http;

import org.aoju.bus.http.magic.GetBuilder;
import org.aoju.bus.http.magic.HttpBuilder;
import org.aoju.bus.http.magic.PostBuilder;
import org.aoju.bus.http.magic.PutBuilder;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.*;
import java.security.SecureRandom;

/**
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class Httpz {

    private static Httpd httpd;

    static {
        Httpd.Builder builder = new Httpd().newBuilder();
        final X509TrustManager trustManager = new org.aoju.bus.http.secure.X509TrustManager();
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
        httpd = builder.sslSocketFactory(sslSocketFactory, trustManager).hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();
    }

    public Httpz(Httpd httpd) {
        this.httpd = httpd;
    }

    public static HttpBuilder newBuilder() {
        return new HttpBuilder(httpd);
    }

    public static HttpBuilder newBuilder(Httpd httpd) {
        return new HttpBuilder(httpd);
    }

    public GetBuilder get() {
        return new GetBuilder(httpd);
    }

    public PostBuilder post() {
        return new PostBuilder(httpd);
    }

    public PutBuilder put() {
        return new PutBuilder(httpd);
    }

}
