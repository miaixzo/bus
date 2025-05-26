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
 * 邮件账户对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MailAccount implements Serializable {

    /**
     * 默认mail配置查询路径
     */
    public static final String[] MAIL_SETTING_PATHS = new String[] { "config/mail.setting",
            "config/mailAccount.setting", "mail.setting" };
    @Serial
    private static final long serialVersionUID = 2852281510160L;
    private static final String MAIL_PROTOCOL = "mail.transport.protocol";
    private static final String SMTP_HOST = "mail.smtp.host";
    private static final String SMTP_PORT = "mail.smtp.port";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    // 认证机制，多个机制使用空格或逗号隔开，如：XOAUTH2
    private static final String SMTP_AUTH_MECHANISMS = "mail.smtp.auth.mechanisms";
    private static final String SMTP_TIMEOUT = "mail.smtp.timeout";
    private static final String SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
    private static final String SMTP_WRITE_TIMEOUT = "mail.smtp.writetimeout";
    // SSL
    private static final String STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String SSL_ENABLE = "mail.smtp.ssl.enable";
    private static final String SSL_PROTOCOLS = "mail.smtp.ssl.protocols";
    private static final String SOCKET_FACTORY = "mail.smtp.socketFactory.class";
    private static final String SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";
    private static final String SOCKET_FACTORY_PORT = "smtp.socketFactory.port";

    // 其他
    private static final String MAIL_DEBUG = "mail.debug";
    /**
     * 自定义的其他属性，此自定义属性会覆盖默认属性
     */
    private final Map<String, Object> customProperty = new HashMap<>();
    /**
     * SMTP服务器域名
     */
    private String host;
    /**
     * SMTP服务端口
     */
    private Integer port;
    /**
     * 是否需要用户名密码验证
     */
    private Boolean auth;
    /**
     * 认证机制，多个机制使用空格或逗号隔开，如：XOAUTH2
     */
    private String authMechanisms;
    /**
     * 用户名
     */
    private String user;
    /**
     * 密码 使用char[]保存密码有利于及时擦除 见：https://www.cnblogs.com/okokabcd/p/16456966.html
     */
    private char[] pass;
    /**
     * 发送方，遵循RFC-822标准
     */
    private String from;
    /**
     * 是否打开调试模式，调试模式会显示与邮件服务器通信过程，默认不开启
     */
    private boolean debug;
    /**
     * 编码用于编码邮件正文和发送人、收件人等中文
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;
    /**
     * 对于文件名是否使用{@link #charset}编码，默认为 {@code true}
     */
    private boolean encodefilename = true;
    /**
     * 使用 STARTTLS安全连接，STARTTLS是对纯文本通信协议的扩展。它将纯文本连接升级为加密连接（TLS或SSL）， 而不是使用一个单独的加密通信端口。
     */
    private boolean starttlsEnable = false;
    /**
     * 使用 SSL安全连接
     */
    private Boolean sslEnable;
    /**
     * SSL协议，多个协议用空格分隔
     */
    private String sslProtocols;
    /**
     * 指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字
     */
    private String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    /**
     * 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
     */
    private boolean socketFactoryFallback;
    /**
     * 指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口
     */
    private int socketFactoryPort = 465;
    /**
     * SMTP超时时长，单位毫秒，缺省值不超时
     */
    private long timeout;
    /**
     * Socket连接超时值，单位毫秒，缺省值不超时
     */
    private long connectionTimeout;
    /**
     * Socket写出超时值，单位毫秒，缺省值不超时
     */
    private long writeTimeout;

    /**
     * 构造,所有参数需自行定义或保持默认值
     */
    public MailAccount() {
    }

    /**
     * 构造
     *
     * @param settingPath 配置文件路径
     */
    public MailAccount(final String settingPath) {
        this(new Setting(settingPath));
    }

    /**
     * 构造
     *
     * @param setting 配置文件
     */
    public MailAccount(final Setting setting) {
        setting.toBean(this);

        // 对于用户希望直接在配置文件中设置mail.xxx参数的情况，在此加入
        setting.forEach((key, value) -> {
            if (StringKit.startWith(key, "mail.")) {
                this.setCustomProperty(key, value);
            }
        });
    }

    /**
     * 获得SMTP服务器域名
     *
     * @return SMTP服务器域名
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置SMTP服务器域名
     *
     * @param host SMTP服务器域名
     * @return this
     */
    public MailAccount setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * 获得SMTP服务端口
     *
     * @return SMTP服务端口
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 设置SMTP服务端口
     *
     * @param port SMTP服务端口
     * @return this
     */
    public MailAccount setPort(final Integer port) {
        this.port = port;
        return this;
    }

    /**
     * 是否需要用户名密码验证
     *
     * @return 是否需要用户名密码验证
     */
    public Boolean isAuth() {
        return auth;
    }

    /**
     * 设置是否需要用户名密码验证
     *
     * @param isAuth 是否需要用户名密码验证
     * @return this
     */
    public MailAccount setAuth(final boolean isAuth) {
        this.auth = isAuth;
        return this;
    }

    /**
     * 获取认证机制，多个机制使用空格或逗号隔开，如：XOAUTH2
     *
     * @return 认证机制
     */
    public String getAuthMechanisms() {
        return this.authMechanisms;
    }

    /**
     * 设置认证机制，多个机制使用空格或逗号隔开，如：XOAUTH2
     *
     * @param authMechanisms 认证机制
     * @return this
     */
    public MailAccount setAuthMechanisms(final String authMechanisms) {
        this.authMechanisms = authMechanisms;
        return this;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置用户名
     *
     * @param user 用户名
     * @return this
     */
    public MailAccount setUser(final String user) {
        this.user = user;
        return this;
    }

    /**
     * 获取密码
     *
     * @return 密码
     */
    public char[] getPass() {
        return pass;
    }

    /**
     * 设置密码
     *
     * @param pass 密码
     * @return this
     */
    public MailAccount setPass(final char[] pass) {
        this.pass = pass;
        return this;
    }

    /**
     * 获取发送方，遵循RFC-822标准
     *
     * @return 发送方，遵循RFC-822标准
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置发送方，遵循RFC-822标准 发件人可以是以下形式：
     *
     * <pre>
     * 1. user@xxx.xx
     * 2.  name &lt;user@xxx.xx&gt;
     * </pre>
     *
     * @param from 发送方，遵循RFC-822标准
     * @return this
     */
    public MailAccount setFrom(final String from) {
        this.from = from;
        return this;
    }

    /**
     * 是否打开调试模式，调试模式会显示与邮件服务器通信过程，默认不开启
     *
     * @return 是否打开调试模式，调试模式会显示与邮件服务器通信过程，默认不开启
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置是否打开调试模式，调试模式会显示与邮件服务器通信过程，默认不开启
     *
     * @param debug 是否打开调试模式，调试模式会显示与邮件服务器通信过程，默认不开启
     * @return this
     */
    public MailAccount setDebug(final boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * 获取字符集编码
     *
     * @return 编码，可能为{@code null}
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * 设置字符集编码，此选项不会修改全局配置，若修改全局配置，请设置此项为{@code null}并设置：
     * 
     * <pre>
     * System.setProperty("mail.mime.charset", charset);
     * </pre>
     *
     * @param charset 字符集编码，{@code null} 则表示使用全局设置的默认编码，全局编码为mail.mime.charset系统属性
     * @return this
     */
    public MailAccount setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 对于文件名是否使用{@link #charset}编码，默认为 {@code true}
     *
     * @return 对于文件名是否使用{@link #charset}编码，默认为 {@code true}
     */
    public boolean isEncodefilename() {

        return encodefilename;
    }

    /**
     * 设置对于文件名是否使用{@link #charset}编码，此选项不会修改全局配置 如果此选项设置为{@code false}，则是否编码取决于两个系统属性：
     * <ul>
     * <li>mail.mime.encodefilename 是否编码附件文件名</li>
     * <li>mail.mime.charset 编码文件名的编码</li>
     * </ul>
     *
     * @param encodefilename 对于文件名是否使用{@link #charset}编码
     */
    public void setEncodefilename(final boolean encodefilename) {
        this.encodefilename = encodefilename;
    }

    /**
     * 是否使用 STARTTLS安全连接，STARTTLS是对纯文本通信协议的扩展。它将纯文本连接升级为加密连接（TLS或SSL）， 而不是使用一个单独的加密通信端口。
     *
     * @return 是否使用 STARTTLS安全连接
     */
    public boolean isStarttlsEnable() {
        return this.starttlsEnable;
    }

    /**
     * 设置是否使用STARTTLS安全连接，STARTTLS是对纯文本通信协议的扩展。它将纯文本连接升级为加密连接（TLS或SSL）， 而不是使用一个单独的加密通信端口。
     *
     * @param startttlsEnable 是否使用STARTTLS安全连接
     * @return this
     */
    public MailAccount setStarttlsEnable(final boolean startttlsEnable) {
        this.starttlsEnable = startttlsEnable;
        return this;
    }

    /**
     * 是否使用 SSL安全连接
     *
     * @return 是否使用 SSL安全连接
     */
    public Boolean isSslEnable() {
        return this.sslEnable;
    }

    /**
     * 设置是否使用SSL安全连接
     *
     * @param sslEnable 是否使用SSL安全连接
     * @return this
     */
    public MailAccount setSslEnable(final Boolean sslEnable) {
        this.sslEnable = sslEnable;
        return this;
    }

    /**
     * 获取SSL协议，多个协议用空格分隔
     *
     * @return SSL协议，多个协议用空格分隔
     */
    public String getSslProtocols() {
        return sslProtocols;
    }

    /**
     * 设置SSL协议，多个协议用空格分隔
     *
     * @param sslProtocols SSL协议，多个协议用空格分隔
     */
    public void setSslProtocols(final String sslProtocols) {
        this.sslProtocols = sslProtocols;
    }

    /**
     * 获取指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字
     *
     * @return 指定实现javax.net.SocketFactory接口的类的名称, 这个类将被用于创建SMTP的套接字
     */
    public String getSocketFactoryClass() {
        return socketFactoryClass;
    }

    /**
     * 设置指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字
     *
     * @param socketFactoryClass 指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字
     * @return this
     */
    public MailAccount setSocketFactoryClass(final String socketFactoryClass) {
        this.socketFactoryClass = socketFactoryClass;
        return this;
    }

    /**
     * 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
     *
     * @return 如果设置为true, 未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
     */
    public boolean isSocketFactoryFallback() {
        return socketFactoryFallback;
    }

    /**
     * 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
     *
     * @param socketFactoryFallback 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
     * @return this
     */
    public MailAccount setSocketFactoryFallback(final boolean socketFactoryFallback) {
        this.socketFactoryFallback = socketFactoryFallback;
        return this;
    }

    /**
     * 获取指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口
     *
     * @return 指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口
     */
    public int getSocketFactoryPort() {
        return socketFactoryPort;
    }

    /**
     * 指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口
     *
     * @param socketFactoryPort 指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口
     * @return this
     */
    public MailAccount setSocketFactoryPort(final int socketFactoryPort) {
        this.socketFactoryPort = socketFactoryPort;
        return this;
    }

    /**
     * 设置SMTP超时时长，单位毫秒，缺省值不超时
     *
     * @param timeout SMTP超时时长，单位毫秒，缺省值不超时
     * @return this
     */
    public MailAccount setTimeout(final long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置Socket连接超时值，单位毫秒，缺省值不超时
     *
     * @param connectionTimeout Socket连接超时值，单位毫秒，缺省值不超时
     * @return this
     */
    public MailAccount setConnectionTimeout(final long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * 设置Socket写出超时值，单位毫秒，缺省值不超时
     *
     * @param writeTimeout Socket写出超时值，单位毫秒，缺省值不超时
     * @return this
     */
    public MailAccount setWriteTimeout(final long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * 获取自定义属性列表
     *
     * @return 自定义参数列表
     */
    public Map<String, Object> getCustomProperty() {
        return customProperty;
    }

    /**
     * 设置自定义属性，如mail.smtp.ssl.socketFactory
     *
     * @param key   属性名，空白被忽略
     * @param value 属性值， null被忽略
     * @return this
     */
    public MailAccount setCustomProperty(final String key, final Object value) {
        if (StringKit.isNotBlank(key) && ObjectKit.isNotNull(value)) {
            this.customProperty.put(key, value);
        }
        return this;
    }

    /**
     * 获得SMTP相关信息
     *
     * @return {@link Properties}
     */
    public Properties getSmtpProps() {
        final Properties p = new Properties();
        p.put(MAIL_PROTOCOL, "smtp");
        p.put(SMTP_HOST, this.host);
        p.put(SMTP_PORT, String.valueOf(this.port));
        p.put(SMTP_AUTH, String.valueOf(this.auth));
        // 增加Oath2认证方式支持
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
            // STARTTLS是对纯文本通信协议的扩展。它将纯文本连接升级为加密连接（TLS或SSL）， 而不是使用一个单独的加密通信端口。
            p.put(STARTTLS_ENABLE, "true");

            if (null == this.sslEnable) {
                // 为了兼容旧版本，当用户没有此项配置时，按照starttlsEnable开启状态时对待
                this.sslEnable = true;
            }
        }

        // SSL
        if (null != this.sslEnable && this.sslEnable) {
            p.put(SSL_ENABLE, "true");
            p.put(SOCKET_FACTORY, socketFactoryClass);
            p.put(SOCKET_FACTORY_FALLBACK, String.valueOf(this.socketFactoryFallback));
            p.put(SOCKET_FACTORY_PORT, String.valueOf(this.socketFactoryPort));
            // 在Linux下需自定义SSL协议版本
            if (StringKit.isNotBlank(this.sslProtocols)) {
                p.put(SSL_PROTOCOLS, this.sslProtocols);
            }
        }

        // 补充自定义属性，允许自定属性覆盖已经设置的值
        p.putAll(this.customProperty);

        return p;
    }

    /**
     * 如果某些值为null，使用默认值
     *
     * @return this
     */
    public MailAccount defaultIfEmpty() {
        Assert.notBlank(this.from, "'from' must not blank!");

        // 去掉发件人的姓名部分
        final String fromAddress = InternalMail.parseFirstAddress(this.from, this.charset).getAddress();

        if (StringKit.isBlank(this.host)) {
            // 如果SMTP地址为空，默认使用smtp.<发件人邮箱后缀>
            this.host = StringKit.format("smtp.{}",
                    StringKit.subSuf(fromAddress, fromAddress.indexOf(Symbol.C_AT) + 1));
        }
        if (StringKit.isBlank(user)) {
            // 如果用户名为空，默认为发件人
            this.user = fromAddress;
        }
        if (null == this.auth) {
            // 如果密码非空白，则使用认证模式
            this.auth = ArrayKit.isNotEmpty(this.pass);
        }
        if (null == this.port) {
            // 端口在SSL状态下默认与socketFactoryPort一致，非SSL状态下默认为25
            this.port = (null != this.sslEnable && this.sslEnable) ? this.socketFactoryPort : 25;
        }
        if (null == this.charset) {
            // 默认UTF-8编码
            this.charset = Charset.UTF_8;
        }

        return this;
    }

    @Override
    public String toString() {
        return "MailAccount [host=" + host + ", port=" + port + ", auth=" + auth + ", user=" + user + ", pass="
                + (ArrayKit.isEmpty(this.pass) ? "" : "******") + ", from=" + from + ", startttlsEnable="
                + starttlsEnable + ", socketFactoryClass=" + socketFactoryClass + ", socketFactoryFallback="
                + socketFactoryFallback + ", socketFactoryPort=" + socketFactoryPort + "]";
    }

}
