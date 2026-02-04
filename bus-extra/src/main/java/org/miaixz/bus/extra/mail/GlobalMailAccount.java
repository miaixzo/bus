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
package org.miaixz.bus.extra.mail;

import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Represents a global mail account, implemented as a singleton enum. This class is responsible for loading and holding
 * a single, globally accessible {@link MailAccount} instance from a configuration file specified in
 * {@link MailAccount#MAIL_SETTING_PATHS}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum GlobalMailAccount {

    /**
     * The singleton instance of the global mail account.
     */
    INSTANCE;

    /**
     * Property key for splitting long parameters in MIME messages.
     */
    private static final String SPLIT_LONG_PARAMS = "mail.mime.splitlongparameters";
    /**
     * Property key for the charset used in MIME messages.
     */
    private static final String CHARSET = "mail.mime.charset";

    static {
        // By default, disable splitting of long parameters to ensure compatibility with certain mail clients.
        System.setProperty(SPLIT_LONG_PARAMS, "false");
        // Set the global default charset for MIME messages based on the loaded account configuration.
        System.setProperty(CHARSET, INSTANCE.mailAccount.getCharset().name());
    }

    /**
     * The globally accessible mail account instance.
     */
    private final MailAccount mailAccount;

    /**
     * Constructs the singleton instance and initializes the default mail account.
     */
    GlobalMailAccount() {
        mailAccount = createDefaultAccount();
    }

    /**
     * Retrieves the globally configured mail account.
     *
     * @return The global {@link MailAccount} instance.
     */
    public MailAccount getAccount() {
        return this.mailAccount;
    }

    /**
     * Sets whether to split long parameters in MIME headers. This is a global setting. Note: This method calls
     * {@link System#setProperty(String, String)}.
     *
     * @param splitLongParams {@code true} to enable splitting, {@code false} to disable it.
     */
    public void setSplitLongParams(final boolean splitLongParams) {
        System.setProperty(SPLIT_LONG_PARAMS, String.valueOf(splitLongParams));
    }

    /**
     * Sets the global default character set for MIME messages. Note: This method calls
     * {@link System#setProperty(String, String)}.
     *
     * @param charset The character set to set as the global default.
     */
    public void setCharset(final Charset charset) {
        System.setProperty(CHARSET, charset.name());
    }

    /**
     * Creates the default mail account by searching for a configuration file in predefined paths.
     *
     * @return The loaded {@link MailAccount}, or null if no configuration file is found.
     */
    private MailAccount createDefaultAccount() {
        for (final String mailSettingPath : MailAccount.MAIL_SETTING_PATHS) {
            try {
                return new MailAccount(mailSettingPath);
            } catch (final InternalException ignore) {
                // Ignore if a specific configuration file is not found and try the next one.
            }
        }
        return null;
    }

}
