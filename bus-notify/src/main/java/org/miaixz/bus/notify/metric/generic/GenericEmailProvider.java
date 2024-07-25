/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Material;
import org.miaixz.bus.notify.metric.AbstractProvider;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 电子邮件消息
 *
 * @author Justubborn
 * @since Java 17+
 */
public class GenericEmailProvider extends AbstractProvider<GenericMaterial, Context> {

    public GenericEmailProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(GenericMaterial entity) {
        try {
            Transport.send(build(entity));
        } catch (MessagingException e) {
            String message = e.getMessage();
            if (e instanceof SendFailedException) {
                // 当地址无效时，显示更加详细的无效地址信息
                final Address[] invalidAddresses = ((SendFailedException) e).getInvalidAddresses();
                message = StringKit.format("Invalid Addresses: {}", ArrayKit.toString(invalidAddresses));
            }
            Logger.error(message);
        }
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .errmsg(ErrorCode.SUCCESS.getDesc())
                .build();
    }

    /**
     * 将一个地址字符串解析为多个地址
     * 地址间使用" "、","、";"分隔
     *
     * @param address 地址字符串
     * @param charset 编码
     * @return 地址列表
     */
    public InternetAddress[] getAddress(String address, Charset charset) {
        InternetAddress[] addresses;
        try {
            addresses = InternetAddress.parse(address);
        } catch (AddressException e) {
            throw new InternalException(e);
        }
        //编码用户名
        if (ArrayKit.isNotEmpty(addresses)) {
            for (InternetAddress internetAddress : addresses) {
                try {
                    internetAddress.setPersonal(internetAddress.getPersonal(), charset.name());
                } catch (UnsupportedEncodingException e) {
                    throw new InternalException(e);
                }
            }
        }

        return addresses;
    }

    /**
     * 将多个字符串邮件地址转为{@link InternetAddress}列表
     * 单个字符串地址可以是多个地址合并的字符串
     *
     * @param address 地址数组
     * @param charset 编码(主要用于中文用户名的编码)
     * @return 地址数组
     */
    private InternetAddress[] getAddress(String[] address, Charset charset) {
        final List<InternetAddress> resultList = new ArrayList<>(address.length);
        InternetAddress[] addrs;
        for (int i = 0; i < address.length; i++) {
            addrs = getAddress(address[i], charset);
            if (ArrayKit.isNotEmpty(addrs)) {
                for (int j = 0; j < addrs.length; j++) {
                    resultList.add(addrs[j]);
                }
            }
        }
        return resultList.toArray(new InternetAddress[resultList.size()]);
    }

    /**
     * 解析第一个地址
     *
     * @param address 地址字符串
     * @param charset 编码
     * @return 地址列表
     */
    private InternetAddress getFirstAddress(String address, Charset charset) {
        final InternetAddress[] internetAddresses = getAddress(address, charset);
        if (ArrayKit.isEmpty(internetAddresses)) {
            try {
                return new InternetAddress(address);
            } catch (AddressException e) {
                throw new InternalException(e);
            }
        }
        return internetAddresses[0];
    }

    /**
     * 构建消息
     *
     * @return {@link MimeMessage}消息
     * @throws MessagingException 消息异常
     */
    private MimeMessage build(GenericMaterial entity) throws MessagingException {
        entity.defaultIfEmpty();
        final Charset charset = entity.getCharset();
        final MimeMessage msg = new MimeMessage(getSession(entity));
        // 发件人
        final String from = entity.getSender();
        if (StringKit.isEmpty(from)) {
            // 用户未提供发送方,则从Session中自动获取
            msg.setFrom();
        } else {
            msg.setFrom(getFirstAddress(from, charset));
        }
        // 标题
        msg.setSubject(entity.getTitle(), charset.name());
        // 发送时间
        msg.setSentDate(new Date());
        // 内容和附件

        final Multipart mainPart = new MimeMultipart();

        // 正文
        final BodyPart body = new MimeBodyPart();
        body.setContent(entity.getContent(), StringKit.format("text/{}; charset={}", Material.Type.HTML.equals(entity.getType()) ? "html" : "plain", entity.getCharset()));
        mainPart.addBodyPart(body);

        // 附件
        if (ArrayKit.isNotEmpty(entity.getAttachments())) {
            BodyPart bodyPart;
            for (File file : entity.getAttachments()) {
                DataSource dataSource = new FileDataSource(file);
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(dataSource));
                try {
                    bodyPart.setFileName(MimeUtility.encodeText(dataSource.getName(), entity.getCharset().name(), null));
                } catch (UnsupportedEncodingException e) {

                }
                mainPart.addBodyPart(bodyPart);
            }
        }

        msg.setContent(mainPart);

        // 收件人
        msg.setRecipients(MimeMessage.RecipientType.TO, getAddress(StringKit.splitToArray(entity.getReceive(), Symbol.COMMA), charset));
        // 抄送人
        if (StringKit.isNotEmpty(entity.getCcs())) {
            msg.setRecipients(MimeMessage.RecipientType.CC, getAddress(StringKit.splitToArray(entity.getCcs(), Symbol.COMMA), charset));
        }
        // 密送人
        if (StringKit.isNotEmpty(entity.getBccs())) {
            msg.setRecipients(MimeMessage.RecipientType.BCC, getAddress(StringKit.splitToArray(entity.getBccs(), Symbol.COMMA), charset));
        }
        return msg;
    }

    /**
     * 获取默认邮件会话
     * 如果为全局单例的会话,则全局只允许一个邮件帐号,否则每次发送邮件会新建一个新的会话
     *
     * @param template 是否使用单例Session
     * @return 邮件会话 {@link Session}
     */
    private Session getSession(GenericMaterial template) {
        Authenticator authenticator = null;
        if (template.getAuth()) {
            authenticator = new UserPassAuthenticator(template.getUser(), template.getPass());
        }

        return template.isUseGlobalSession() ? Session.getDefaultInstance(template.getSmtpProps(), authenticator)
                : Session.getInstance(template.getSmtpProps(), authenticator);
    }

    class UserPassAuthenticator extends Authenticator {

        private final String user;
        private final String pass;

        /**
         * 构造
         *
         * @param user 用户名
         * @param pass 密码
         */
        public UserPassAuthenticator(String user, String pass) {
            super();
            this.user = user;
            this.pass = pass;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.user, this.pass);
        }

    }

}
