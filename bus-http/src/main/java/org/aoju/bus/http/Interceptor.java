package org.aoju.bus.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Observes, modifies, and potentially short-circuits requests going out and the corresponding
 * responses coming back in. Typically interceptors add, remove, or transform headers on the request
 * or response.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Interceptor {

    Response intercept(Chain chain) throws IOException;

    interface Chain {
        Request request();

        Response proceed(Request request) throws IOException;

        /**
         * Returns the connection the request will be executed on. This is only available in the chains
         * of network interceptors; for application interceptors this is always null.
         */
        Connection connection();

        Call call();

        int connectTimeoutMillis();

        Chain withConnectTimeout(int timeout, TimeUnit unit);

        int readTimeoutMillis();

        Chain withReadTimeout(int timeout, TimeUnit unit);

        int writeTimeoutMillis();

        Chain withWriteTimeout(int timeout, TimeUnit unit);
    }
}
