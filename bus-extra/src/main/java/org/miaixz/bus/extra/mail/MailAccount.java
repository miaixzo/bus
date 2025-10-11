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

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.setting.Setting;

/**
 * Represents a mail account, encapsulating all necessary configuration for sending emails. This includes SMTP server
 * details, authentication credentials, and connection settings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MailAccount implements Serializable {

    /**
     * Default paths for loading mail configuration settings.
     */
    public static final String[] MAIL_SETTING_PATHS = new String[] { "config/mail.setting",
            "config/mailAccount.setting", "mail.setting" };
    @Serial
    private static final long serialVersionUID = 2852285572363L;
    /**
     * Mail transport protocol property key.
     */
    private static final String MAIL_PROTOCOL = "mail.transport.protocol";
    /**
     * SMTP server host property key.
     */
    private static final String SMTP_HOST = "mail.smtp.host";
    /**
     * SMTP server port property key.
     */
    private static final String SMTP_PORT = "mail.smtp.port";
    /**
     * SMTP authentication property key.
     */
    private static final String SMTP_AUTH = "mail.smtp.auth";
    /**
     * SMTP authentication mechanisms property key.
     */
    private static final String SMTP_AUTH_MECHANISMS = "mail.smtp.auth.mechanisms";
    /**
     * SMTP timeout property key.
     */
    private static final String SMTP_TIMEOUT = "mail.smtp.timeout";
    /**
     * SMTP connection timeout property key.
     */
    private static final String SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
    /**
     * SMTP write timeout property key.
     */
    private static final String SMTP_WRITE_TIMEOUT = "mail.smtp.writetimeout";
    /**
     * STARTTLS enable property key.
     */
    private static final String STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    /**
     * SSL enable property key.
     */
    private static final String SSL_ENABLE = "mail.smtp.ssl.enable";
    /**
     * SSL protocols property key.
     */
    private static final String SSL_PROTOCOLS = "mail.smtp.ssl.protocols";
    /**
     * Socket factory class property key.
     */
    private static final String SOCKET_FACTORY = "mail.smtp.socketFactory.class";
    /**
     * Socket factory fallback property key.
     */
    private static final String SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
    /**
     * Socket factory port property key.
     */
    private static final String SOCKET_FACTORY_PORT = "smtp.socketFactory.port";
    /**
     * Mail debug property key.
     */
    private static final String MAIL_DEBUG = "mail.debug";

    /**
     * The SMTP server host.
     */
    private String host;
    /**
     * The SMTP server port.
     */
    private Integer port;
    /**
     * Indicates whether authentication is required.
     */
    private Boolean auth;
    /**
     * Authentication mechanisms, such as XOAUTH2, separated by spaces or commas.
     */
    private String authMechanisms;
    /**
     * The username for authentication.
     */
    private String user;
    /**
     * The password for authentication, stored as a char array for security.
     */
    private char[] pass;
    /**
     * The sender's email address, compliant with RFC-822.
     */
    private String from;
    /**
     * Enables debug mode, which displays communication with the mail server.
     */
    private boolean debug;
    /**
     * The character set for encoding email content and headers.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;
    /**
     * Indicates whether to encode attachment filenames using the specified charset.
     */
    private boolean encodefilename = true;
    /**
     * Enables STARTTLS for a secure connection, upgrading a plain text connection to an encrypted one.
     */
    private boolean starttlsEnable = false;
    /**
     * Enables SSL for a secure connection.
     */
    private Boolean sslEnable;
    /**
     * The SSL protocols to use, separated by spaces.
     */
    private String sslProtocols;
    /**
     * The name of the class implementing javax.net.SocketFactory for creating SMTP sockets.
     */
    private String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    /**
     * If true, falls back to java.net.Socket if the specified socket factory fails.
     */
    private boolean socketFactoryFallback;
    /**
     * The port to connect to when using the specified socket factory.
     */
    private int socketFactoryPort = 465;
    /**
     * The SMTP timeout in milliseconds.
     */
    private long timeout;
    /**
     * The socket connection timeout in milliseconds.
     */
    private long connectionTimeout;
    /**
     * The socket write timeout in milliseconds.
     */
    private long writeTimeout;
    /**
     * A map for custom properties that will override default settings.
     */
    private final Map<String, Object> customProperty = new HashMap<>();

    /**
     * Constructs a new {@code MailAccount} with default values.
     */
    public MailAccount() {
    }

    /**
     * Constructs a new {@code MailAccount} from a configuration file.
     *
     * @param settingPath The path to the configuration file.
     */
    public MailAccount(final String settingPath) {
        this(new Setting(settingPath));
    }

    /**
     * Constructs a new {@code MailAccount} from a {@link Setting} object.
     *
     * @param setting The {@link Setting} object containing mail configuration.
     */
    public MailAccount(final Setting setting) {
        setting.toBean(this);
        setting.forEach((key, value) -> {
            if (StringKit.startWith(key, "mail.")) {
                this.setCustomProperty(key, value);
            }
        });
    }

    /**
     * Retrieves the SMTP server host.
     *
     * @return The SMTP server host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the SMTP server host.
     *
     * @param host The SMTP server host.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * Retrieves the SMTP server port.
     *
     * @return The SMTP server port.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the SMTP server port.
     *
     * @param port The SMTP server port.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setPort(final Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Checks if authentication is required.
     *
     * @return {@code true} if authentication is required, {@code false} otherwise.
     */
    public Boolean isAuth() {
        return auth;
    }

    /**
     * Sets whether authentication is required.
     *
     * @param isAuth {@code true} to enable authentication, {@code false} to disable it.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setAuth(final boolean isAuth) {
        this.auth = isAuth;
        return this;
    }

    /**
     * Retrieves the authentication mechanisms.
     *
     * @return The authentication mechanisms.
     */
    public String getAuthMechanisms() {
        return this.authMechanisms;
    }

    /**
     * Sets the authentication mechanisms.
     *
     * @param authMechanisms The authentication mechanisms.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setAuthMechanisms(final String authMechanisms) {
        this.authMechanisms = authMechanisms;
        return this;
    }

    /**
     * Retrieves the username for authentication.
     *
     * @return The username.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the username for authentication.
     *
     * @param user The username.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setUser(final String user) {
        this.user = user;
        return this;
    }

    /**
     * Retrieves the password for authentication.
     *
     * @return The password as a char array.
     */
    public char[] getPass() {
        return pass;
    }

    /**
     * Sets the password for authentication.
     *
     * @param pass The password as a char array.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setPass(final char[] pass) {
        this.pass = pass;
        return this;
    }

    /**
     * Retrieves the sender's email address.
     *
     * @return The sender's email address.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the sender's email address, which can be in the format "user@example.com" or "Sender Name
     * &lt;user@example.com&gt;".
     *
     * @param from The sender's email address.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setFrom(final String from) {
        this.from = from;
        return this;
    }

    /**
     * Checks if debug mode is enabled.
     *
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets whether to enable debug mode.
     *
     * @param debug {@code true} to enable debug mode, {@code false} to disable it.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setDebug(final boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Retrieves the character set for encoding.
     *
     * @return The character set, or null if not set.
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * Sets the character set for encoding. If null, the global default (mail.mime.charset) is used.
     *
     * @param charset The character set.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Checks if attachment filenames should be encoded using the specified charset.
     *
     * @return {@code true} if filenames should be encoded, {@code false} otherwise.
     */
    public boolean isEncodefilename() {
        return encodefilename;
    }

    /**
     * Sets whether to encode attachment filenames using the specified charset.
     *
     * @param encodefilename {@code true} to encode filenames, {@code false} to rely on system properties.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setEncodefilename(final boolean encodefilename) {
        this.encodefilename = encodefilename;
        return this;
    }

    /**
     * Checks if STARTTLS is enabled.
     *
     * @return {@code true} if STARTTLS is enabled, {@code false} otherwise.
     */
    public boolean isStarttlsEnable() {
        return this.starttlsEnable;
    }

    /**
     * Sets whether to enable STARTTLS.
     *
     * @param startttlsEnable {@code true} to enable STARTTLS, {@code false} to disable it.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setStarttlsEnable(final boolean startttlsEnable) {
        this.starttlsEnable = startttlsEnable;
        return this;
    }

    /**
     * Checks if SSL is enabled.
     *
     * @return {@code true} if SSL is enabled, {@code false} otherwise.
     */
    public Boolean isSslEnable() {
        return this.sslEnable;
    }

    /**
     * Sets whether to enable SSL.
     *
     * @param sslEnable {@code true} to enable SSL, {@code false} to disable it.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setSslEnable(final Boolean sslEnable) {
        this.sslEnable = sslEnable;
        return this;
    }

    /**
     * Retrieves the SSL protocols to be used for the connection, separated by spaces.
     *
     * @return The SSL protocols string.
     */
    public String getSslProtocols() {
        return sslProtocols;
    }

    /**
     * Sets the SSL protocols to be used for the connection, separated by spaces.
     *
     * @param sslProtocols The SSL protocols string.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setSslProtocols(final String sslProtocols) {
        this.sslProtocols = sslProtocols;
        return this;
    }

    /**
     * Retrieves the socket factory class name.
     *
     * @return The socket factory class name.
     */
    public String getSocketFactoryClass() {
        return socketFactoryClass;
    }

    /**
     * Sets the socket factory class name.
     *
     * @param socketFactoryClass The socket factory class name.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setSocketFactoryClass(final String socketFactoryClass) {
        this.socketFactoryClass = socketFactoryClass;
        return this;
    }

    /**
     * Checks if socket factory fallback is enabled.
     *
     * @return {@code true} if fallback is enabled, {@code false} otherwise.
     */
    public boolean isSocketFactoryFallback() {
        return socketFactoryFallback;
    }

    /**
     * Sets whether to enable socket factory fallback.
     *
     * @param socketFactoryFallback {@code true} to enable fallback, {@code false} to disable it.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setSocketFactoryFallback(final boolean socketFactoryFallback) {
        this.socketFactoryFallback = socketFactoryFallback;
        return this;
    }

    /**
     * Retrieves the socket factory port.
     *
     * @return The socket factory port.
     */
    public int getSocketFactoryPort() {
        return socketFactoryPort;
    }

    /**
     * Sets the socket factory port.
     *
     * @param socketFactoryPort The socket factory port.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setSocketFactoryPort(final int socketFactoryPort) {
        this.socketFactoryPort = socketFactoryPort;
        return this;
    }

    /**
     * Sets the SMTP timeout.
     *
     * @param timeout The timeout in milliseconds.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setTimeout(final long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the socket connection timeout.
     *
     * @param connectionTimeout The timeout in milliseconds.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setConnectionTimeout(final long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the socket write timeout.
     *
     * @param writeTimeout The timeout in milliseconds.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setWriteTimeout(final long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Retrieves the map of custom properties.
     *
     * @return The map of custom properties.
     */
    public Map<String, Object> getCustomProperty() {
        return customProperty;
    }

    /**
     * Sets a custom property.
     *
     * @param key   The property key.
     * @param value The property value.
     * @return This {@code MailAccount} instance for method chaining.
     */
    public MailAccount setCustomProperty(final String key, final Object value) {
        if (StringKit.isNotBlank(key) && ObjectKit.isNotNull(value)) {
            this.customProperty.put(key, value);
        }
        return this;
    }

    /**
     * Retrieves the SMTP properties for this account.
     *
     * @return A {@link Properties} object containing SMTP settings.
     */
    public Properties getSmtpProps() {
        final Properties p = new Properties();
        p.put(MAIL_PROTOCOL, "smtp");
        p.put(SMTP_HOST, this.host);
        p.put(SMTP_PORT, String.valueOf(this.port));
        p.put(SMTP_AUTH, String.valueOf(this.auth));
        if (StringKit.isNotBlank(this.authMechanisms)) {
            p.put(SMTP_AUTH_MECHANISMS, this.authMechanisms);
        }
        if (this.timeout > 0) {
            p.put(SMTP_TIMEOUT, String.valueOf(this.timeout));
        }
        if (this.connectionTimeout > 0) {
            p.put(SMTP_CONNECTION_TIMEOUT, String.valueOf(this.connectionTimeout));
        }
        if (this.writeTimeout > 0) {
            p.put(SMTP_WRITE_TIMEOUT, String.valueOf(this.writeTimeout));
        }
        p.put(MAIL_DEBUG, String.valueOf(this.debug));

        if (this.starttlsEnable) {
            p.put(STARTTLS_ENABLE, "true");
            if (null == this.sslEnable) {
                this.sslEnable = true;
            }
        }

        if (null != this.sslEnable && this.sslEnable) {
            p.put(SSL_ENABLE, "true");
            p.put(SOCKET_FACTORY, socketFactoryClass);
            p.put(SOCKET_FACTORY_FALLBACK, String.valueOf(this.socketFactoryFallback));
            p.put(SOCKET_FACTORY_PORT, String.valueOf(this.socketFactoryPort));
            if (StringKit.isNotBlank(this.sslProtocols)) {
                p.put(SSL_PROTOCOLS, this.sslProtocols);
            }
        }

        p.putAll(this.customProperty);

        return p;
    }

    /**
     * Fills in default values for any fields that are null or blank. This is useful for creating a complete
     * configuration from minimal information.
     *
     * @return This {@code MailAccount} instance for method chaining.
     * @throws NullPointerException if the 'from' address is not set.
     */
    public MailAccount defaultIfEmpty() {
        Assert.notBlank(this.from, "'from' must not blank!");

        final String fromAddress = InternalMail.parseFirstAddress(this.from, this.charset).getAddress();

        if (StringKit.isBlank(this.host)) {
            this.host = StringKit
                    .format("smtp.{}", StringKit.subSuf(fromAddress, fromAddress.indexOf(Symbol.C_AT) + 1));
        }
        if (StringKit.isBlank(user)) {
            this.user = fromAddress;
        }
        if (null == this.auth) {
            this.auth = ArrayKit.isNotEmpty(this.pass);
        }
        if (null == this.port) {
            this.port = (null != this.sslEnable && this.sslEnable) ? this.socketFactoryPort : 25;
        }
        if (null == this.charset) {
            this.charset = Charset.UTF_8;
        }

        return this;
    }

    /**
     * Returns a string representation of the MailAccount, excluding the password for security.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "MailAccount [host=" + host + ", port=" + port + ", auth=" + auth + ", user=" + user + ", pass="
                + (ArrayKit.isEmpty(this.pass) ? "" : "******") + ", from=" + from + ", startttlsEnable="
                + starttlsEnable + ", socketFactoryClass=" + socketFactoryClass + ", socketFactoryFallback="
                + socketFactoryFallback + ", socketFactoryPort=" + socketFactoryPort + "]";
    }

}
