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
package org.miaixz.bus.notify.metric.generic;

import java.io.File;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.notify.magic.Notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents the notice for generic email messages.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GenericNotice extends Notice {

    /**
     * SMTP host property key.
     */
    private static final String SMTP_HOST = "mail.smtp.host";
    /**
     * SMTP port property key.
     */
    private static final String SMTP_PORT = "mail.smtp.port";
    /**
     * SMTP authentication property key.
     */
    private static final String SMTP_AUTH = "mail.smtp.auth";
    /**
     * SMTP timeout property key.
     */
    private static final String SMTP_TIMEOUT = "mail.smtp.timeout";
    /**
     * SMTP connection timeout property key.
     */
    private static final String SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";

    /**
     * Socket factory class property key.
     */
    private static final String SOCKEY_FACTORY = "mail.smtp.socketFactory.class";
    /**
     * Socket factory port property key.
     */
    private static final String SOCKEY_FACTORY_PORT = "smtp.socketFactory.port";
    /**
     * Socket factory fallback property key.
     */
    private static final String SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

    /**
     * TLS enable property key.
     */
    private static final String MAIL_TLS_ENABLE = "mail.smtp.starttls.enable";
    /**
     * Mail protocol property key.
     */
    private static final String MAIL_PROTOCOL = "mail.transport.protocol";

    /**
     * Split long parameters property key.
     */
    private static final String SPLIT_LONG_PARAMS = "mail.mime.splitlongparameters";
    /**
     * Mail debug property key.
     */
    private static final String MAIL_DEBUG = "mail.debug";

    /**
     * The SMTP server domain name.
     */
    private String host;
    /**
     * The SMTP service port.
     */
    private Integer port;
    /**
     * Indicates whether username and password authentication is required.
     */
    private Boolean auth;
    /**
     * The username for authentication.
     */
    private String user;
    /**
     * The password for authentication.
     */
    private String pass;
    /**
     * Whether to enable debug mode. Debug mode displays the communication process with the mail server. Disabled by
     * default.
     */
    private boolean debug;
    /**
     * The character set used for encoding email body, sender, recipient, and other Chinese characters.
     */
    private java.nio.charset.Charset charset;
    /**
     * Whether to split overly long parameters into multiple parts. Defaults to false (attachment names for domestic
     * email do not support splitting).
     */
    private boolean splitlongparameters;

    /**
     * Uses STARTTLS for secure connection. STARTTLS is an extension to plain text communication protocols that upgrades
     * a plain text connection to an encrypted connection (TLS or SSL), rather than using a separate encrypted
     * communication port.
     */
    private boolean startttlsEnable;
    /**
     * Uses SSL for secure connection.
     */
    private Boolean sslEnable;
    /**
     * The name of the class that implements the {@code javax.net.SocketFactory} interface. This class will be used to
     * create SMTP sockets.
     */
    private String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    /**
     * If set to true, failure to create a socket using the specified socket factory class will result in using a socket
     * created with {@code java.net.Socket} class. Defaults to true.
     */
    private boolean socketFactoryFallback;
    /**
     * The specified port to connect to when using the specified socket factory. If not set, the default port will be
     * used.
     */
    private int socketFactoryPort = 465;

    /**
     * The SMTP timeout duration in milliseconds. Defaults to no timeout.
     */
    private long timeout;
    /**
     * The socket connection timeout value in milliseconds. Defaults to no timeout.
     */
    private long connectionTimeout;

    /**
     * A comma-separated list of carbon copy (CC) recipients.
     */
    private String ccs;
    /**
     * A comma-separated list of blind carbon copy (BCC) recipients.
     */
    private String bccs;

    /**
     * The subject of the email.
     */
    private String title;
    /**
     * The content of the email.
     */
    private String content;

    /**
     * A list of attachments for the email.
     */
    private File[] attachments;
    /**
     * Whether to use a global session. Defaults to true.
     */
    private boolean useGlobalSession;

    /**
     * Fills in default values for properties if they are null or empty.
     *
     * @return This {@code GenericNotice} instance with default values applied.
     */
    public GenericNotice defaultIfEmpty() {
        if (StringKit.isBlank(this.host)) {
            // If the SMTP address is empty, default to smtp.<sender_email_suffix>
            this.host = StringKit
                    .format("smtp.{}", StringKit.subSuf(this.sender, this.sender.indexOf(Symbol.C_AT) + 1));
        }
        if (StringKit.isBlank(user)) {
            // If the username is empty, default to the sender's email prefix
            this.user = StringKit.subPre(this.sender, this.sender.indexOf(Symbol.C_AT));
        }
        if (null == this.auth) {
            // If the password is not blank, use authentication mode
            this.auth = ArrayKit.isNotEmpty(this.pass);
        }
        if (null == this.port) {
            // Port defaults to socketFactoryPort in SSL state, and 25 in non-SSL state
            this.port = (null != this.sslEnable && this.sslEnable) ? this.socketFactoryPort : 25;
        }
        if (null == this.charset) {
            // Default to UTF-8 encoding
            this.charset = Charset.UTF_8;
        }
        return this;
    }

    /**
     * Retrieves SMTP-related properties.
     *
     * @return A {@link java.util.Properties} object containing SMTP configuration.
     */
    public java.util.Properties getSmtpProps() {
        // Global system parameters
        System.setProperty(SPLIT_LONG_PARAMS, String.valueOf(this.splitlongparameters));

        final java.util.Properties p = new java.util.Properties();
        p.put(MAIL_PROTOCOL, "smtp");
        p.put(SMTP_HOST, this.host);
        p.put(SMTP_PORT, String.valueOf(this.port));
        p.put(SMTP_AUTH, String.valueOf(this.auth));
        if (this.timeout > 0) {
            p.put(SMTP_TIMEOUT, String.valueOf(this.timeout));
        }
        if (this.connectionTimeout > 0) {
            p.put(SMTP_CONNECTION_TIMEOUT, String.valueOf(this.connectionTimeout));
        }

        p.put(MAIL_DEBUG, String.valueOf(this.debug));

        if (this.startttlsEnable) {
            // STARTTLS is an extension to plain text communication protocols that upgrades a plain text connection to
            // an encrypted connection (TLS or SSL), rather than using a separate encrypted communication port
            p.put(MAIL_TLS_ENABLE, String.valueOf(this.startttlsEnable));

            if (null == this.sslEnable) {
                // For compatibility with older versions, if this item is not configured by the user, it is treated as
                // enabled when startttlsEnable is true
                this.sslEnable = true;
            }
        }

        // SSL
        if (null != this.sslEnable && this.sslEnable) {
            p.put(SOCKEY_FACTORY, socketFactoryClass);
            p.put(SOCKET_FACTORY_FALLBACK, String.valueOf(this.socketFactoryFallback));
            p.put(SOCKEY_FACTORY_PORT, String.valueOf(this.socketFactoryPort));
        }

        return p;
    }

}
