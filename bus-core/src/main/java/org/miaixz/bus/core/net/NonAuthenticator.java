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
package org.miaixz.bus.core.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * An {@link Authenticator} implementation that uses a username and password.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NonAuthenticator extends Authenticator {

    /**
     * Stores the username and password.
     */
    private final PasswordAuthentication auth;

    /**
     * Constructs a new {@code NonAuthenticator}.
     *
     * @param userName The username.
     * @param password The password.
     */
    public NonAuthenticator(final String userName, final char[] password) {
        this(new PasswordAuthentication(userName, password));
    }

    /**
     * Constructs a new {@code NonAuthenticator}.
     *
     * @param auth The password authentication details.
     */
    public NonAuthenticator(final PasswordAuthentication auth) {
        this.auth = auth;
    }

    /**
     * Creates a username/password-based {@link Authenticator} implementation.
     *
     * @param userName The username.
     * @param password The password.
     * @return A new {@code NonAuthenticator} instance.
     */
    public static NonAuthenticator of(final String userName, final char[] password) {
        return new NonAuthenticator(userName, password);
    }

    /**
     * Returns the password authentication for this request.
     * <p>
     * This method is called by the system when authentication is required.
     * </p>
     *
     * @return The {@link PasswordAuthentication} object containing the username and password.
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }

}
