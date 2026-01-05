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
