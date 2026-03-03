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
