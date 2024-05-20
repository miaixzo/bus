/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.notify.metric.generic;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.notify.magic.Property;

import java.io.File;

/**
 * 电子邮件消息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@SuperBuilder
public class GenericProperty extends Property {

    private static final String SMTP_HOST = "mail.smtp.host";
    private static final String SMTP_PORT = "mail.smtp.port";
    private static final String SMTP_AUTH = "mail.smtp.auth";
    private static final String SMTP_TIMEOUT = "mail.smtp.timeout";
    private static final String SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";

    private static final String SOCKEY_FACTORY = "mail.smtp.socketFactory.class";
    private static final String SOCKEY_FACTORY_PORT = "smtp.socketFactory.port";
    private static final String SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback";

    private static final String MAIL_TLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String MAIL_PROTOCOL = "mail.transport.protocol";

    private static final String SPLIT_LONG_PARAMS = "mail.mime.splitlongparameters";
    private static final String MAIL_DEBUG = "mail.debug";

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
     * 用户名
     */
    private String user;
    /**
     * 密码
     */
    private String pass;
    /**
     * 是否打开调试模式,调试模式会显示与邮件服务器通信过程,默认不开启
     */
    private boolean debug;
    /**
     * 编码用于编码邮件正文和发送人、收件人等中文
     */
    private java.nio.charset.Charset charset;
    /**
     * 对于超长参数是否切分为多份,默认为false(国内邮箱附件不支持切分的附件名)
     */
    private boolean splitlongparameters;

    /**
     * 使用 STARTTLS安全连接,STARTTLS是对纯文本通信协议的扩展 它将纯文本连接升级为加密连接(TLS或SSL), 而不是使用一个单独的加密通信端口
     */
    private boolean startttlsEnable;
    /**
     * 使用 SSL安全连接
     */
    private Boolean sslEnable;
    /**
     * 指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字
     */
    private String socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
    /**
     * 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类, 默认值为true
     */
    private boolean socketFactoryFallback;
    /**
     * 指定的端口连接到在使用指定的套接字工厂 如果没有设置,将使用默认端口
     */
    private int socketFactoryPort = 465;

    /**
     * SMTP超时时长,单位毫秒,缺省值不超时
     */
    private long timeout;
    /**
     * Socket连接超时值,单位毫秒,缺省值不超时
     */
    private long connectionTimeout;

    /**
     * 抄送人列表(carbon copy)
     */
    private String ccs;
    /**
     * 密送人列表(blind carbon copy)
     */
    private String bccs;

    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;

    /**
     * 附件列表
     */
    private File[] attachments;
    /**
     * 是否使用全局会话,默认为true
     */
    private boolean useGlobalSession;

    /**
     * 如果某些值为null,使用默认值
     *
     * @return this
     */
    public GenericProperty defaultIfEmpty() {
        if (StringKit.isBlank(this.host)) {
            // 如果SMTP地址为空,默认使用smtp.<发件人邮箱后缀>
            this.host = StringKit.format("smtp.{}", StringKit.subSuf(this.sender, this.sender.indexOf(Symbol.C_AT) + 1));
        }
        if (StringKit.isBlank(user)) {
            // 如果用户名为空,默认为发件人邮箱前缀
            this.user = StringKit.subPre(this.sender, this.sender.indexOf(Symbol.C_AT));
        }
        if (null == this.auth) {
            // 如果密码非空白,则使用认证模式
            this.auth = (false == StringKit.isBlank(this.pass));
        }
        if (null == this.port) {
            // 端口在SSL状态下默认与socketFactoryPort一致,非SSL状态下默认为25
            this.port = (null != this.sslEnable && this.sslEnable) ? this.socketFactoryPort : 25;
        }
        if (null == this.charset) {
            // 默认UTF-8编码
            this.charset = Charset.UTF_8;
        }
        return this;
    }

    /**
     * 获得SMTP相关信息
     *
     * @return {@link java.util.Properties}
     */
    public java.util.Properties getSmtpProps() {
        //全局系统参数
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
            //STARTTLS是对纯文本通信协议的扩展 它将纯文本连接升级为加密连接(TLS或SSL), 而不是使用一个单独的加密通信端口
            p.put(MAIL_TLS_ENABLE, String.valueOf(this.startttlsEnable));

            if (null == this.sslEnable) {
                //为了兼容旧版本,当用户没有此项配置时,按照startttlsEnable开启状态时对待
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
