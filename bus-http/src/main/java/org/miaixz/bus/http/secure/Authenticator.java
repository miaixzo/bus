/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
