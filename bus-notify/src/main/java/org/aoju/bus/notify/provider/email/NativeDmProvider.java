/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.notify.provider.email;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.notify.Builder;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.magic.Property;
import org.aoju.bus.notify.provider.AbstractProvider;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 邮箱
 *
 * @author Justubborn
 * @version 6.0.9
 * @since JDK1.8+
 */
public class NativeDmProvider extends AbstractProvider<NativeDmProperty, Context> {

    public NativeDmProvider(Context properties) {
        super(properties);
    }

    @Override
    public Message send(NativeDmProperty template) {
        try {
            Transport.send(build(template));
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
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .errmsg(Builder.ErrorCode.SUCCESS.getMsg())
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
            throw new InstrumentException(e);
        }
        //编码用户名
        if (ArrayKit.isNotEmpty(addresses)) {
            for (InternetAddress internetAddress : addresses) {
                try {
                    internetAddress.setPersonal(internetAddress.getPersonal(), charset.name());
                } catch (UnsupportedEncodingException e) {
                    throw new InstrumentException(e);
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
                throw new InstrumentException(e);
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
    private MimeMessage build(NativeDmProperty template) throws MessagingException {
        template.defaultIfEmpty();
        final Charset charset = template.getCharset();
        final MimeMessage msg = new MimeMessage(getSession(template));
        // 发件人
        final String from = template.getSender();
        if (StringKit.isEmpty(from)) {
            // 用户未提供发送方,则从Session中自动获取
            msg.setFrom();
        } else {
            msg.setFrom(getFirstAddress(from, charset));
        }
        // 标题
        msg.setSubject(template.getTitle(), charset.name());
        // 发送时间
        msg.setSentDate(new Date());
        // 内容和附件

        final Multipart mainPart = new MimeMultipart();

        // 正文
        final BodyPart body = new MimeBodyPart();
        body.setContent(template.getContent(), StringKit.format("text/{}; charset={}", Property.ContentType.HTML.equals(template.getContentType()) ? "html" : "plain", template.getCharset()));
        mainPart.addBodyPart(body);

        // 附件
        if (ArrayKit.isNotEmpty(template.getAttachments())) {
            BodyPart bodyPart;
            for (File file : template.getAttachments()) {
                DataSource dataSource = new FileDataSource(file);
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(dataSource));
                try {
                    bodyPart.setFileName(MimeUtility.encodeText(dataSource.getName(), template.getCharset().name(), null));
                } catch (UnsupportedEncodingException e) {

                }
                mainPart.addBodyPart(bodyPart);
            }
        }

        msg.setContent(mainPart);

        // 收件人
        msg.setRecipients(MimeMessage.RecipientType.TO, getAddress(StringKit.split(template.getReceive(), Symbol.COMMA), charset));
        // 抄送人
        if (StringKit.isNotEmpty(template.getCcs())) {
            msg.setRecipients(MimeMessage.RecipientType.CC, getAddress(StringKit.split(template.getCcs(), Symbol.COMMA), charset));
        }
        // 密送人
        if (StringKit.isNotEmpty(template.getBccs())) {
            msg.setRecipients(MimeMessage.RecipientType.BCC, getAddress(StringKit.split(template.getBccs(), Symbol.COMMA), charset));
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
    private Session getSession(NativeDmProperty template) {
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
