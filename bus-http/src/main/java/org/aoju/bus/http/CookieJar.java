package org.aoju.bus.http;

import java.util.Collections;
import java.util.List;

/**
 * Provides <strong>policy</strong> and <strong>persistence</strong> for HTTP cookies.
 *
 * <p>As policy, implementations of this interface are responsible for selecting which cookies to
 * accept and which to reject. A reasonable policy is to reject all cookies, though that may
 * interfere with session-based authentication schemes that require cookies.
 *
 * <p>As persistence, implementations of this interface must also provide storage of cookies. Simple
 * implementations may store cookies in memory; sophisticated ones may use the file system or
 * database to hold accepted cookies. The <a
 * href="https://tools.ietf.org/html/rfc6265#section-5.3">cookie storage model</a> specifies
 * policies for updating and expiring cookies.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface CookieJar {
    /**
     * A cookie jar that never accepts any cookies.
     */
    CookieJar NO_COOKIES = new CookieJar() {
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            return Collections.emptyList();
        }
    };

    /**
     * Saves {@code cookies} from an HTTP response to this store according to this jar's policy.
     *
     * <p>Note that this method may be called a second time for a single HTTP response if the response
     * includes a trailer. For this obscure HTTP feature, {@code cookies} contains only the trailer's
     * cookies.
     */
    void saveFromResponse(HttpUrl url, List<Cookie> cookies);

    /**
     * Load cookies from the jar for an HTTP request to {@code url}. This method returns a possibly
     * empty list of cookies for the network request.
     *
     * <p>Simple implementations will return the accepted cookies that have not yet expired and that
     * {@linkplain Cookie#matches match} {@code url}.
     */
    List<Cookie> loadForRequest(HttpUrl url);
}
