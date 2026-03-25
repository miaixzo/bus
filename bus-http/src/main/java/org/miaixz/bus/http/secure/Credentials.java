/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.secure;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;

/**
 * A factory for creating HTTP authorization credentials.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Credentials {

    /** Private constructor to prevent instantiation. */
    private Credentials() {

    }

    /**
     * Returns a credential string for Basic HTTP authentication using the ISO-8859-1 charset.
     *
     * @param username The username.
     * @param password The password.
     * @return The formatted credential string (e.g., "Basic dXNlcjpwYXNz").
     */
    public static String basic(String username, String password) {
        return basic(username, password, Charset.ISO_8859_1);
    }

    /**
     * Returns a credential string for Basic HTTP authentication using the specified charset.
     *
     * @param username The username.
     * @param password The password.
     * @param charset  The character set to use for encoding the credentials.
     * @return The formatted credential string (e.g., "Basic dXNlcjpwYXNz").
     */
    public static String basic(String username, String password, java.nio.charset.Charset charset) {
        String usernameAndPassword = username + Symbol.COLON + password;
        String encoded = ByteString.encodeString(usernameAndPassword, charset).base64();
        return "Basic " + encoded;
    }

}
