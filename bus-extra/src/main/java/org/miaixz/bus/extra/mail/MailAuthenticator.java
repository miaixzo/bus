/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.mail;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * An authenticator that provides username and password authentication for a mail session. This class extends
 * {@link Authenticator} and is used to supply credentials to the mail server.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MailAuthenticator extends Authenticator {

    /**
     * The password authentication object containing the username and password.
     */
    private final PasswordAuthentication auth;

    /**
     * Creates a new {@code MailAuthenticator} instance with the given username and password.
     *
     * @param user The username for authentication.
     * @param pass The password for authentication.
     * @return A new {@code MailAuthenticator} instance.
     */
    public static MailAuthenticator of(final String user, final String pass) {
        return new MailAuthenticator(user, pass);
    }

    /**
     * Constructs a new {@code MailAuthenticator} from a {@link MailAccount} object.
     *
     * @param mailAccount The {@link MailAccount} containing the username and password.
     */
    public MailAuthenticator(final MailAccount mailAccount) {
        this.auth = new PasswordAuthentication(mailAccount.getUser(), String.valueOf(mailAccount.getPass()));
    }

    /**
     * Constructs a new {@code MailAuthenticator} with the given username and password.
     *
     * @param userName The username for authentication.
     * @param password The password for authentication.
     */
    public MailAuthenticator(final String userName, final String password) {
        this.auth = new PasswordAuthentication(userName, password);
    }

    /**
     * Constructs a new {@code MailAuthenticator} with a pre-existing {@link PasswordAuthentication} object.
     *
     * @param auth The {@link PasswordAuthentication} object.
     */
    public MailAuthenticator(final PasswordAuthentication auth) {
        this.auth = auth;
    }

    /**
     * Returns the {@link PasswordAuthentication} object containing the credentials. This method is called by the
     * Jakarta Mail API to get the authentication details.
     *
     * @return The {@link PasswordAuthentication} object.
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return this.auth;
    }

}
