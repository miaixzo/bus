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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.FileTypeMap;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * 邮件发送客户端
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mail implements Builder<MimeMessage> {

    @Serial
    private static final long serialVersionUID = 2852353795275L;

    /**
     * 邮箱帐户信息以及一些客户端配置信息
     */
    private final MailAccount mailAccount;
    /**
     * 正文、附件和图片的混合部分
     */
    private final Multipart multipart = new MimeMultipart();
    /**
     * 收件人列表
     */
    private String[] tos;
    /**
     * 抄送人列表（carbon copier）
     */
    private String[] ccs;
    /**
     * 密送人列表（blind carbon copier）
     */
    private String[] bccs;
    /**
     * 回复地址(reply-to)
     */
    private String[] reply;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 是否为HTML
     */
    private boolean isHtml;
    /**
     * 是否使用全局会话，默认为false
     */
    private boolean useGlobalSession = false;

    /**
     * debug输出位置，可以自定义debug日志
     */
    private PrintStream debugOutput;

    /**
     * 构造，使用全局邮件帐户
     */
    public Mail() {
        this(GlobalMailAccount.INSTANCE.getAccount());
    }

    /**
     * 构造
     *
     * @param mailAccount 邮件帐户，如果为null使用默认配置文件的全局邮件配置
     */
    public Mail(MailAccount mailAccount) {
        mailAccount = (null != mailAccount) ? mailAccount : GlobalMailAccount.INSTANCE.getAccount();
        this.mailAccount = mailAccount.defaultIfEmpty();
    }

    /**
     * 创建邮件客户端
     *
     * @param mailAccount 邮件帐号
     * @return Mail
     */
    public static Mail of(final MailAccount mailAccount) {
        return new Mail(mailAccount);
    }

    /**
     * 创建邮件客户端，使用全局邮件帐户
     *
     * @return Mail
     */
    public static Mail of() {
        return new Mail();
    }

    /**
     * 设置收件人
     *
     * @param tos 收件人列表
     * @return this
     * @see #setTos(String...)
     */
    public Mail to(final String... tos) {
        return setTos(tos);
    }

    /**
     * 设置多个收件人
     *
     * @param tos 收件人列表
     * @return this
     */
    public Mail setTos(final String... tos) {
        this.tos = tos;
        return this;
    }

    /**
     * 设置多个抄送人（carbon copier）
     *
     * @param ccs 抄送人列表
     * @return this
     */
    public Mail setCcs(final String... ccs) {
        this.ccs = ccs;
        return this;
    }

    /**
     * 设置多个密送人（blind carbon copier）
     *
     * @param bccs 密送人列表
     * @return this
     */
    public Mail setBccs(final String... bccs) {
        this.bccs = bccs;
        return this;
    }

    /**
     * 设置多个回复地址(reply-to)
     *
     * @param reply 回复地址(reply-to)列表
     * @return this
     */
    public Mail setReply(final String... reply) {
        this.reply = reply;
        return this;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     * @return this
     */
    public Mail setTitle(final String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置正文 正文可以是普通文本也可以是HTML（默认普通文本），可以通过调用{@link #setHtml(boolean)} 设置是否为HTML
     *
     * @param content 正文
     * @return this
     */
    public Mail setContent(final String content) {
        this.content = content;
        return this;
    }

    /**
     * 设置是否是HTML
     *
     * @param isHtml 是否为HTML
     * @return this
     */
    public Mail setHtml(final boolean isHtml) {
        this.isHtml = isHtml;
        return this;
    }

    /**
     * 设置正文
     *
     * @param content 正文内容
     * @param isHtml  是否为HTML
     * @return this
     */
    public Mail setContent(final String content, final boolean isHtml) {
        setContent(content);
        return setHtml(isHtml);
    }

    /**
     * 设置文件类型附件，文件可以是图片文件，此时自动设置cid（正文中引用图片），默认cid为文件名
     *
     * @param files 附件文件列表
     * @return this
     */
    public Mail setFiles(final File... files) {
        if (ArrayKit.isEmpty(files)) {
            return this;
        }

        final DataSource[] attachments = new DataSource[files.length];
        for (int i = 0; i < files.length; i++) {
            attachments[i] = new FileDataSource(files[i]);
        }
        return setAttachments(attachments);
    }

    /**
     * 增加图片，图片的键对应到邮件模板中的占位字符串，图片类型默认为"image/jpeg"
     *
     * @param cid         图片与占位符，占位符格式为cid:${cid}
     * @param imageStream 图片文件
     * @return this
     */
    public Mail addImage(final String cid, final InputStream imageStream) {
        return addImage(cid, imageStream, null);
    }

    /**
     * 增加图片，图片的键对应到邮件模板中的占位字符串
     *
     * @param cid         图片与占位符，占位符格式为cid:${cid}
     * @param imageStream 图片流，不关闭
     * @param contentType 图片类型，null赋值默认的"image/jpeg"
     * @return this
     */
    public Mail addImage(final String cid, final InputStream imageStream, final String contentType) {
        final ByteArrayDataSource imgSource;
        try {
            imgSource = new ByteArrayDataSource(imageStream, ObjectKit.defaultIfNull(contentType, "image/jpeg"));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        imgSource.setName(cid);
        return setAttachments(imgSource);
    }

    /**
     * 增加图片，图片的键对应到邮件模板中的占位字符串
     *
     * @param cid       图片与占位符，占位符格式为cid:${cid}
     * @param imageFile 图片文件
     * @return this
     */
    public Mail addImage(final String cid, final File imageFile) {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(imageFile);
            return addImage(cid, in, FileTypeMap.getDefaultFileTypeMap().getContentType(imageFile));
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 增加附件或图片，附件使用{@link DataSource} 形式表示，可以使用{@link FileDataSource}包装文件表示文件附件
     *
     * @param attachments 附件列表
     * @return this
     */
    public Mail setAttachments(final DataSource... attachments) {
        if (ArrayKit.isNotEmpty(attachments)) {
            final Charset charset = this.mailAccount.getCharset();
            MimeBodyPart bodyPart;
            String nameEncoded;
            try {
                for (final DataSource attachment : attachments) {
                    bodyPart = new MimeBodyPart();
                    bodyPart.setDataHandler(new DataHandler(attachment));
                    nameEncoded = attachment.getName();
                    if (this.mailAccount.isEncodefilename()) {
                        nameEncoded = InternalMail.encodeText(nameEncoded, charset);
                    }
                    // 普通附件文件名
                    bodyPart.setFileName(nameEncoded);
                    if (StringKit.startWith(attachment.getContentType(), "image/")) {
                        // 图片附件，用于正文中引用图片
                        bodyPart.setContentID(nameEncoded);
                        bodyPart.setDisposition(MimeBodyPart.INLINE);
                    }
                    this.multipart.addBodyPart(bodyPart);
                }
            } catch (final MessagingException e) {
                throw new InternalException(e);
            }
        }
        return this;
    }

    /**
     * 设置字符集编码
     *
     * @param charset 字符集编码
     * @return this
     * @see MailAccount#setCharset(Charset)
     */
    public Mail setCharset(final Charset charset) {
        this.mailAccount.setCharset(charset);
        return this;
    }

    /**
     * 设置是否使用全局会话，默认为true
     *
     * @param isUseGlobalSession 是否使用全局会话，默认为true
     * @return this
     */
    public Mail setUseGlobalSession(final boolean isUseGlobalSession) {
        this.useGlobalSession = isUseGlobalSession;
        return this;
    }

    /**
     * 设置debug输出位置，可以自定义debug日志
     *
     * @param debugOutput debug输出位置
     * @return this
     */
    public Mail setDebugOutput(final PrintStream debugOutput) {
        this.debugOutput = debugOutput;
        return this;
    }

    @Override
    public MimeMessage build() {
        try {
            return buildMsg();
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 发送
     *
     * @return message-data
     * @throws InternalException 邮件发送异常
     */
    public String send() throws InternalException {
        try {
            return doSend();
        } catch (final MessagingException e) {
            if (e instanceof SendFailedException) {
                // 当地址无效时，显示更加详细的无效地址信息
                final Address[] invalidAddresses = ((SendFailedException) e).getInvalidAddresses();
                final String msg = StringKit.format("Invalid Addresses: {}", ArrayKit.toString(invalidAddresses));
                throw new InternalException(msg, e);
            }
            throw new InternalException(e);
        }
    }

    /**
     * 执行发送
     *
     * @return message-data
     * @throws MessagingException 发送异常
     */
    private String doSend() throws MessagingException {
        final MimeMessage mimeMessage = buildMsg();
        Transport.send(mimeMessage);
        return mimeMessage.getMessageID();
    }

    /**
     * 构建消息
     *
     * @return {@link MimeMessage}消息
     * @throws MessagingException 消息异常
     */
    private MimeMessage buildMsg() throws MessagingException {
        final Charset charset = this.mailAccount.getCharset();
        final MimeMessage msg = new MimeMessage(getSession());
        // 发件人
        final String from = this.mailAccount.getFrom();
        if (StringKit.isEmpty(from)) {
            // 用户未提供发送方，则从Session中自动获取
            msg.setFrom();
        } else {
            msg.setFrom(InternalMail.parseFirstAddress(from, charset));
        }
        // 标题
        msg.setSubject(this.title, (null == charset) ? null : charset.name());
        // 发送时间
        msg.setSentDate(new Date());
        // 内容和附件
        msg.setContent(buildContent(charset));
        // 收件人
        msg.setRecipients(MimeMessage.RecipientType.TO, InternalMail.parseAddressFromStrs(this.tos, charset));
        // 抄送人
        if (ArrayKit.isNotEmpty(this.ccs)) {
            msg.setRecipients(MimeMessage.RecipientType.CC, InternalMail.parseAddressFromStrs(this.ccs, charset));
        }
        // 密送人
        if (ArrayKit.isNotEmpty(this.bccs)) {
            msg.setRecipients(MimeMessage.RecipientType.BCC, InternalMail.parseAddressFromStrs(this.bccs, charset));
        }
        // 回复地址(reply-to)
        if (ArrayKit.isNotEmpty(this.reply)) {
            msg.setReplyTo(InternalMail.parseAddressFromStrs(this.reply, charset));
        }

        return msg;
    }

    /**
     * 构建邮件信息主体
     *
     * @param charset 编码，{@code null}则使用{@link MimeUtility#getDefaultJavaCharset()}
     * @return 邮件信息主体
     * @throws MessagingException 消息异常
     */
    private Multipart buildContent(final Charset charset) throws MessagingException {
        final String charsetStr = null != charset ? charset.name() : MimeUtility.getDefaultJavaCharset();
        // 正文
        final MimeBodyPart body = new MimeBodyPart();
        body.setContent(content, StringKit.format("text/{}; charset={}", isHtml ? "html" : "plain", charsetStr));
        this.multipart.addBodyPart(body);

        return this.multipart;
    }

    /**
     * 获取默认邮件会话 如果为全局单例的会话，则全局只允许一个邮件帐号，否则每次发送邮件会新建一个新的会话
     *
     * @return 邮件会话 {@link Session}
     */
    private Session getSession() {
        final Session session = MailKit.getSession(this.mailAccount, this.useGlobalSession);

        if (null != this.debugOutput) {
            session.setDebugOut(debugOutput);
        }

        return session;
    }

}
