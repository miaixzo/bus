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
