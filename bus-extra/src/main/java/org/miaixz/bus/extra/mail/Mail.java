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
 * Represents a mail client for sending emails, designed with a fluent builder pattern. It simplifies the process of
 * setting recipients, subject, content, and attachments. Supports both HTML and plain text content, and allows for file
 * and image attachments.
 * <p>
 * Usage Example:
 * 
 * <pre>
 * {@code
 * // Create a mail client
 * Mail mail = Mail.of();
 *
 * // Set basic email information
 * mail.setTos("recipient@example.com").setTitle("Email Subject").setContent("<h1>Hello!</h1>", true) // true indicates
 *                                                                                                    // HTML format
 *         .addFiles(new File("attachment.pdf"));
 *
 * // Send the email
 * String messageId = mail.send();
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mail implements Builder<MimeMessage> {

    /**
     * The mail account configuration, including SMTP server settings, authentication details, etc.
     */
    private final MailAccount mailAccount;

    /**
     * An array of recipient email addresses (the 'To' field).
     */
    private String[] tos;

    /**
     * An array of carbon copy (CC) recipient email addresses.
     */
    private String[] ccs;

    /**
     * An array of blind carbon copy (BCC) recipient email addresses.
     */
    private String[] bccs;

    /**
     * An array of email addresses to be used for the 'Reply-To' header.
     */
    private String[] reply;

    /**
     * The subject of the email.
     */
    private String title;

    /**
     * The body content of the email.
     */
    private String content;

    /**
     * Flag indicating whether the email content is in HTML format.
     */
    private boolean isHtml;

    /**
     * An array of {@link DataSource} objects representing email attachments (files or images).
     */
    private DataSource[] attachments;

    /**
     * Flag indicating whether to use a global session for sending mail. Defaults to false. Using a global session can
     * improve performance but may cause issues in multi-account environments.
     */
    private boolean useGlobalSession = false;

    /**
     * The {@link PrintStream} for debug output. If null, no debug information is printed.
     */
    private PrintStream debugOutput;

    /**
     * Creates a new {@code Mail} instance with the specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @return A new {@code Mail} instance.
     */
    public static Mail of(final MailAccount mailAccount) {
        return new Mail(mailAccount);
    }

    /**
     * Creates a new {@code Mail} instance using the global mail account configuration.
     *
     * @return A new {@code Mail} instance.
     */
    public static Mail of() {
        return new Mail();
    }

    /**
     * Constructs a new {@code Mail} instance using the global mail account.
     */
    public Mail() {
        this(GlobalMailAccount.INSTANCE.getAccount());
    }

    /**
     * Constructs a new {@code Mail} instance with the specified mail account. If the provided account is null, the
     * global mail account configuration is used.
     *
     * @param mailAccount The mail account to use. If null, the global account is used.
     */
    public Mail(MailAccount mailAccount) {
        mailAccount = (null != mailAccount) ? mailAccount : GlobalMailAccount.INSTANCE.getAccount();
        this.mailAccount = mailAccount.defaultIfEmpty();
    }

    /**
     * Sets the recipient email addresses.
     *
     * @param tos The recipient email addresses.
     * @return This {@code Mail} instance for method chaining.
     * @see #setTos(String...)
     */
    public Mail to(final String... tos) {
        return setTos(tos);
    }

    /**
     * Sets multiple recipient email addresses, overwriting any previously set recipients.
     *
     * @param tos An array of recipient email addresses.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setTos(final String... tos) {
        this.tos = tos;
        return this;
    }

    /**
     * Sets multiple carbon copy (CC) recipient email addresses, overwriting any previously set CC recipients.
     *
     * @param ccs An array of CC recipient email addresses.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setCcs(final String... ccs) {
        this.ccs = ccs;
        return this;
    }

    /**
     * Sets multiple blind carbon copy (BCC) recipient email addresses, overwriting any previously set BCC recipients.
     *
     * @param bccs An array of BCC recipient email addresses.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setBccs(final String... bccs) {
        this.bccs = bccs;
        return this;
    }

    /**
     * Sets multiple 'Reply-To' email addresses, overwriting any previously set reply-to addresses.
     *
     * @param reply An array of 'Reply-To' email addresses.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setReply(final String... reply) {
        this.reply = reply;
        return this;
    }

    /**
     * Sets the subject of the email.
     *
     * @param title The subject of the email.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setTitle(final String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the body content of the email. The content can be plain text or HTML. Use {@link #setHtml(boolean)} to
     * specify the content type.
     *
     * @param content The body content of the email.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setContent(final String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets whether the email content is in HTML format.
     *
     * @param isHtml {@code true} if the content is HTML, {@code false} for plain text.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setHtml(final boolean isHtml) {
        this.isHtml = isHtml;
        return this;
    }

    /**
     * Sets both the body content and the content type (HTML or plain text).
     *
     * @param content The body content of the email.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setContent(final String content, final boolean isHtml) {
        setContent(content);
        return setHtml(isHtml);
    }

    /**
     * Adds file attachments to the email. If a file is an image, a CID (Content-ID) is automatically generated for
     * embedding in the email body, using the filename as the default CID.
     *
     * @param files An array of {@link File} objects to attach.
     * @return This {@code Mail} instance for method chaining.
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
     * Adds an embedded image to the email, referenced by a CID in the HTML body. The image content type defaults to
     * "image/jpeg".
     *
     * @param cid         The Content-ID for embedding the image (e.g., "my-image").
     * @param imageStream The {@link InputStream} of the image. This stream is not closed by this method.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail addImage(final String cid, final InputStream imageStream) {
        return addImage(cid, imageStream, null);
    }

    /**
     * Adds an embedded image to the email with a specified content type.
     *
     * @param cid         The Content-ID for embedding the image.
     * @param imageStream The {@link InputStream} of the image. This stream is not closed by this method.
     * @param contentType The MIME type of the image (e.g., "image/png"). Defaults to "image/jpeg" if null.
     * @return This {@code Mail} instance for method chaining.
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
     * Adds an embedded image from a {@link File} to the email.
     *
     * @param cid       The Content-ID for embedding the image.
     * @param imageFile The image {@link File}.
     * @return This {@code Mail} instance for method chaining.
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
     * Adds attachments to the email, represented by {@link DataSource} objects.
     *
     * @param attachments An array of {@link DataSource} objects to attach.
     * @return This {@code Mail} instance for method chaining.
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
     * Sets the character set encoding for the email.
     *
     * @param charset The character set (e.g., UTF-8, GBK).
     * @return This {@code Mail} instance for method chaining.
     * @see MailAccount#setCharset(Charset)
     */
    public Mail setCharset(final Charset charset) {
        this.mailAccount.setCharset(charset);
        return this;
    }

    /**
     * Sets whether to use a global session for sending mail. Defaults to false.
     *
     * @param isUseGlobalSession {@code true} to use a global session, {@code false} to create a new session for each
     *                           email.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setUseGlobalSession(final boolean isUseGlobalSession) {
        this.useGlobalSession = isUseGlobalSession;
        return this;
    }

    /**
     * Sets the debug output stream for logging mail sending details.
     *
     * @param debugOutput The {@link PrintStream} for debug output. If null, system default is used.
     * @return This {@code Mail} instance for method chaining.
     */
    public Mail setDebugOutput(final PrintStream debugOutput) {
        this.debugOutput = debugOutput;
        return this;
    }

    /**
     * Builds the {@link MimeMessage} object from the current configuration. This method consolidates all settings into
     * a complete mail message object ready for sending.
     *
     * @return The constructed {@link SMTPMessage} object.
     */
    @Override
    public SMTPMessage build() {
        return SMTPMessage.of(this.mailAccount, this.useGlobalSession, this.debugOutput).setTitle(this.title)
                .setTos(this.tos).setCcs(this.ccs).setBccs(this.bccs).setReply(this.reply)
                .setContent(this.content, this.isHtml);
    }

    /**
     * Builds and sends the email.
     *
     * @return The unique message-id of the sent email, which can be used for tracking.
     * @throws InternalException if sending the email fails.
     */
    public String send() {
        return build().send();
    }

}
