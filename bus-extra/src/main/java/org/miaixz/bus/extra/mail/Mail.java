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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.FileTypeMap;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * 邮件发送客户端
 * <p>
 * 提供了一个构建器模式的邮件发送客户端，支持设置收件人、抄送人、密送人、邮件主题、内容、附件等信息。 支持HTML格式和纯文本格式的邮件内容，并可以添加文件附件或图片附件。
 * <p>
 * 使用示例：
 * 
 * <pre>
 * // 创建邮件客户端
 * Mail mail = Mail.of();
 *
 * // 设置邮件基本信息
 * mail.setTos("recipient@example.com").setTitle("邮件主题").setContent("邮件内容", true) // true表示HTML格式
 *         .addFiles(new File("附件.pdf"));
 *
 * // 发送邮件
 * String messageId = mail.send();
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mail implements Builder<MimeMessage> {

    /**
     * 邮箱帐户信息以及一些客户端配置信息
     * <p>
     * 包含SMTP服务器地址、端口、用户名、密码、是否需要认证等邮件发送所需的基本配置信息
     * </p>
     */
    private final MailAccount mailAccount;

    /**
     * 收件人列表
     * <p>
     * 存储邮件的主要接收人地址数组，支持多个收件人
     * </p>
     */
    private String[] tos;

    /**
     * 抄送人列表（carbon copy）
     * <p>
     * 存储需要抄送邮件的接收人地址数组，抄送人可以看到其他收件人
     * </p>
     */
    private String[] ccs;

    /**
     * 密送人列表（blind carbon copy）
     * <p>
     * 存储需要密送邮件的接收人地址数组，密送人不会被其他收件人看到
     * </p>
     */
    private String[] bccs;

    /**
     * 回复地址(reply-to)
     * <p>
     * 存储邮件的回复地址数组，当收件人点击回复时，邮件将发送到此地址而非发件人地址
     * </p>
     */
    private String[] reply;

    /**
     * 标题
     * <p>
     * 邮件的主题行内容
     * </p>
     */
    private String title;

    /**
     * 内容
     * <p>
     * 邮件的正文内容，可以是纯文本或HTML格式
     * </p>
     */
    private String content;

    /**
     * 是否为HTML
     * <p>
     * 标记邮件内容是否为HTML格式，true表示HTML格式，false表示纯文本格式
     * </p>
     */
    private boolean isHtml;

    /**
     * 附件或图片
     * <p>
     * 存储邮件附件的数据源数组，可以是文件附件或图片附件
     * </p>
     */
    private DataSource[] attachments;

    /**
     * 是否使用全局会话，默认为false
     * <p>
     * 标记是否使用全局邮件会话，使用全局会话可以提高性能，但可能在多账户环境下产生问题
     * </p>
     */
    private boolean useGlobalSession = false;

    /**
     * debug输出位置，可以自定义debug日志
     * <p>
     * 指定邮件发送过程中的调试信息输出流，可用于排查邮件发送问题
     * </p>
     */
    private PrintStream debugOutput;

    /**
     * 创建邮件客户端
     *
     * @param mailAccount 邮件帐号
     *                    <p>
     *                    使用指定的邮件帐号创建邮件客户端实例
     *                    </p>
     * @return Mail 邮件客户端实例
     */
    public static Mail of(final MailAccount mailAccount) {
        return new Mail(mailAccount);
    }

    /**
     * 创建邮件客户端，使用全局邮件帐户
     * <p>
     * 使用全局邮件配置创建邮件客户端实例，全局配置通常从配置文件中读取
     * </p>
     *
     * @return Mail 邮件客户端实例
     */
    public static Mail of() {
        return new Mail();
    }

    /**
     * 构造，使用全局邮件帐户
     * <p>
     * 使用默认配置文件中的全局邮件帐户初始化邮件客户端
     * </p>
     */
    public Mail() {
        this(GlobalMailAccount.INSTANCE.getAccount());
    }

    /**
     * 构造
     *
     * @param mailAccount 邮件帐户，如果为null使用默认配置文件的全局邮件配置
     *                    <p>
     *                    使用指定的邮件帐户初始化邮件客户端，如果传入null则使用全局邮件配置
     *                    </p>
     */
    public Mail(MailAccount mailAccount) {
        mailAccount = (null != mailAccount) ? mailAccount : GlobalMailAccount.INSTANCE.getAccount();
        this.mailAccount = mailAccount.defaultIfEmpty();
    }

    /**
     * 设置收件人
     *
     * @param tos 收件人列表
     *            <p>
     *            设置邮件的主要接收人地址，支持多个收件人
     *            </p>
     * @return this 当前邮件客户端实例，支持链式调用
     * @see #setTos(String...)
     */
    public Mail to(final String... tos) {
        return setTos(tos);
    }

    /**
     * 设置多个收件人
     *
     * @param tos 收件人列表
     *            <p>
     *            设置邮件的主要接收人地址数组，覆盖之前设置的收件人
     *            </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setTos(final String... tos) {
        this.tos = tos;
        return this;
    }

    /**
     * 设置多个抄送人（carbon copy）
     *
     * @param ccs 抄送人列表
     *            <p>
     *            设置邮件的抄送接收人地址数组，抄送人可以看到其他收件人，覆盖之前设置的抄送人
     *            </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setCcs(final String... ccs) {
        this.ccs = ccs;
        return this;
    }

    /**
     * 设置多个密送人（blind carbon copy）
     *
     * @param bccs 密送人列表
     *             <p>
     *             设置邮件的密送接收人地址数组，密送人不会被其他收件人看到，覆盖之前设置的密送人
     *             </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setBccs(final String... bccs) {
        this.bccs = bccs;
        return this;
    }

    /**
     * 设置多个回复地址(reply-to)
     *
     * @param reply 回复地址(reply-to)列表
     *              <p>
     *              设置邮件的回复地址数组，当收件人点击回复时，邮件将发送到此地址而非发件人地址，覆盖之前设置的回复地址
     *              </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setReply(final String... reply) {
        this.reply = reply;
        return this;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     *              <p>
     *              设置邮件的主题行内容，覆盖之前设置的标题
     *              </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setTitle(final String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置正文<br>
     * 正文可以是普通文本也可以是HTML（默认普通文本），可以通过调用{@link #setHtml(boolean)} 设置是否为HTML
     *
     * @param content 正文
     *                <p>
     *                设置邮件的正文内容，可以是纯文本或HTML格式，覆盖之前设置的内容
     *                </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setContent(final String content) {
        this.content = content;
        return this;
    }

    /**
     * 设置是否是HTML
     *
     * @param isHtml 是否为HTML
     *               <p>
     *               设置邮件内容是否为HTML格式，true表示HTML格式，false表示纯文本格式
     *               </p>
     * @return this 当前邮件客户端实例，支持链式调用
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
     *                <p>
     *                同时设置邮件的正文内容和内容格式，覆盖之前的设置
     *                </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setContent(final String content, final boolean isHtml) {
        setContent(content);
        return setHtml(isHtml);
    }

    /**
     * 设置文件类型附件，文件可以是图片文件，此时自动设置cid（正文中引用图片），默认cid为文件名
     *
     * @param files 附件文件列表
     *              <p>
     *              添加文件作为邮件附件，支持多个文件。如果是图片文件，会自动设置cid以便在正文中引用。 这些附件将添加到现有附件列表中，而不是替换现有附件。
     *              </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail addFiles(final File... files) {
        if (ArrayKit.isEmpty(files)) {
            return this;
        }
        final DataSource[] attachments = new DataSource[files.length];
        for (int i = 0; i < files.length; i++) {
            attachments[i] = new FileDataSource(files[i]);
        }
        return addAttachments(attachments);
    }

    /**
     * 增加图片，图片的键对应到邮件模板中的占位字符串，图片类型默认为"image/jpeg"
     *
     * @param cid         图片与占位符，占位符格式为cid:${cid}
     *                    <p>
     *                    图片的内容ID，用于在HTML邮件正文中引用图片，格式为cid:${cid}
     *                    </p>
     * @param imageStream 图片文件
     *                    <p>
     *                    图片的输入流，方法不会自动关闭此流，调用者需要自行管理流的生命周期
     *                    </p>
     */
    public Mail addImage(final String cid, final InputStream imageStream) {
        return addImage(cid, imageStream, null);
    }

    /**
     * 增加图片，图片的键对应到邮件模板中的占位字符串
     *
     * @param cid         图片与占位符，占位符格式为cid:${cid}
     *                    <p>
     *                    图片的内容ID，用于在HTML邮件正文中引用图片，格式为cid:${cid}
     *                    </p>
     * @param imageStream 图片流，不关闭
     *                    <p>
     *                    图片的输入流，方法不会自动关闭此流，调用者需要自行管理流的生命周期
     *                    </p>
     * @param contentType 图片类型，null赋值默认的"image/jpeg"
     *                    <p>
     *                    图片的MIME类型，如"image/jpeg"、"image/png"等，如果为null则默认使用"image/jpeg"
     *                    </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail addImage(final String cid, final InputStream imageStream, final String contentType) {
        final ByteArrayDataSource imgSource;
        try {
            imgSource = new ByteArrayDataSource(imageStream, ObjectKit.defaultIfNull(contentType, "image/jpeg"));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        imgSource.setName(cid);
        return addAttachments(imgSource);
    }

    /**
     * 增加图片，图片的键对应到邮件模板中的占位字符串
     *
     * @param cid       图片与占位符，占位符格式为cid:${cid}
     *                  <p>
     *                  图片的内容ID，用于在HTML邮件正文中引用图片，格式为cid:${cid}
     *                  </p>
     * @param imageFile 图片文件
     *                  <p>
     *                  图片文件对象，方法会自动读取文件内容并关闭文件流
     *                  </p>
     * @return this 当前邮件客户端实例，支持链式调用
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
     *                    <p>
     *                    添加附件或图片到邮件中，附件使用DataSource形式表示。 这些附件将添加到现有附件列表中，而不是替换现有附件。
     *                    </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail addAttachments(final DataSource... attachments) {
        if (ArrayKit.isNotEmpty(attachments)) {
            if (null == this.attachments) {
                this.attachments = attachments;
            } else {
                this.attachments = ArrayKit.addAll(this.attachments, attachments);
            }
        }
        return this;
    }

    /**
     * 设置字符集编码
     *
     * @param charset 字符集编码
     *                <p>
     *                设置邮件的字符集编码，如UTF-8、GBK等，影响邮件主题和内容的编码
     *                </p>
     * @return this 当前邮件客户端实例，支持链式调用
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
     *                           <p>
     *                           设置是否使用全局邮件会话，使用全局会话可以提高性能，但可能在多账户环境下产生问题。 如果为true，则所有邮件发送使用同一个会话；如果为false，则每次发送创建新会话。
     *                           </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setUseGlobalSession(final boolean isUseGlobalSession) {
        this.useGlobalSession = isUseGlobalSession;
        return this;
    }

    /**
     * 设置debug输出位置，可以自定义debug日志
     *
     * @param debugOutput debug输出位置
     *                    <p>
     *                    设置邮件发送过程中的调试信息输出流，可用于排查邮件发送问题。 如果为null，则使用系统默认输出。
     *                    </p>
     * @return this 当前邮件客户端实例，支持链式调用
     */
    public Mail setDebugOutput(final PrintStream debugOutput) {
        this.debugOutput = debugOutput;
        return this;
    }

    /**
     * 构建邮件消息
     * <p>
     * 根据当前设置构建SMTPMessage对象，准备发送。此方法会整合所有设置的邮件信息， 包括收件人、主题、内容、附件等，创建一个完整的邮件消息对象。
     * </p>
     *
     * @return SMTPMessage 构建好的SMTP消息对象
     */
    @Override
    public SMTPMessage build() {
        return SMTPMessage.of(this.mailAccount, this.useGlobalSession, this.debugOutput)
                // 标题
                .setTitle(this.title)
                // 收件人
                .setTos(this.tos)
                // 抄送人
                .setCcs(this.ccs)
                // 密送人
                .setBccs(this.bccs)
                // 回复地址(reply-to)
                .setReply(this.reply)
                // 内容和附件
                .setContent(this.content, this.isHtml);
    }

    /**
     * 发送邮件
     * <p>
     * 构建并发送邮件，返回邮件的唯一标识符（message-id）。 如果发送成功，message-id可用于追踪邮件；如果发送失败，将抛出异常。
     * </p>
     *
     * @return message-id 邮件的唯一标识符，可用于追踪邮件
     */
    public String send() {
        return build().send();
    }

}
