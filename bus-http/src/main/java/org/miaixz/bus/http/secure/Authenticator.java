/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.secure;

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.Route;

import java.io.IOException;

/**
 * Responds to authentication challenges from origin web servers or proxies. Implementations may either attempt to
 * authenticate preemptively (before a challenge is received) or reactively (in response to a challenge). An application
 * can supply a single authenticator for both origin and proxy authentication, or separate authenticators for each.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Authenticator {

    /**
     * An authenticator that does not attempt to authenticate and always returns null.
     */
    Authenticator NONE = (route, response) -> null;

    /**
     * Returns a request that includes a credential to satisfy an authentication challenge in {@code response}. Returns
     * {@code null} if the challenge cannot be satisfied.
     * <p>
     * The {@code route} parameter is a best-effort and may not always be provided, especially when an authenticator is
     * invoked manually in an application interceptor (e.g., for client-specific retries).
     *
     * @param route    The route that resulted in the challenge, or null if not available.
     * @param response The response containing the authentication challenge.
     * @return A new request with the appropriate authentication header, or null if no response is possible.
     * @throws IOException if an I/O error occurs.
     */
    Request authenticate(Route route, Response response) throws IOException;

}
