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

import org.miaixz.bus.core.lang.Normal;
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
 * Represents an SMTP message, extending {@link MimeMessage} to provide a fluent builder pattern for creating and
 * sending emails. This class simplifies the process of setting headers, content, and attachments.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SMTPMessage extends MimeMessage {

    /**
     * Creates a new {@code SMTPMessage} instance.
     *
     * @param mailAccount      The mail account configuration.
     * @param useGlobalSession If {@code true}, uses a globally shared session; otherwise, creates a new session.
     * @param debugOutput      The {@link PrintStream} for debug output. If null, no debug information is printed.
     * @return A new {@code SMTPMessage} instance.
     */
    public static SMTPMessage of(
            final MailAccount mailAccount,
            final boolean useGlobalSession,
            final PrintStream debugOutput) {
        final Session session = MailKit.getSession(mailAccount, useGlobalSession);
        if (null != debugOutput) {
            session.setDebugOut(debugOutput);
        }
        return new SMTPMessage(mailAccount, session);
    }

    /**
     * The mail account configuration.
     */
    private final MailAccount mailAccount;
    /**
     * The multipart content of the email, holding the body, attachments, and images.
     */
    private final Multipart multipart;

    /**
     * Constructs a new {@code SMTPMessage} with the specified mail account and session.
     *
     * @param mailAccount The mail account configuration.
     * @param session     The Jakarta Mail {@link Session}.
     */
    public SMTPMessage(final MailAccount mailAccount, final Session session) {
        super(session);
        this.mailAccount = mailAccount;
        multipart = new MimeMultipart();
        init();
    }

    /**
     * Initializes the message with default values, such as the sender and sent date.
     */
    private void init() {
        final String from = this.mailAccount.getFrom();
        try {
            if (StringKit.isEmpty(from)) {
                super.setFrom();
            } else {
                super.setFrom(InternalMail.parseFirstAddress(from, this.mailAccount.getCharset()));
            }
            super.setSentDate(DateKit.now());
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Sets the email subject.
     *
     * @param title The subject of the email.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setTitle(final String title) {
        try {
            super.setSubject(title, ObjectKit.apply(mailAccount.getCharset(), Object::toString));
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Sets the recipient email addresses.
     *
     * @param tos An array of recipient email addresses.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setTos(final String... tos) {
        return this.setRecipients(Message.RecipientType.TO, tos);
    }

    /**
     * Sets the carbon copy (CC) recipient email addresses.
     *
     * @param ccs An array of CC recipient email addresses.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setCcs(final String... ccs) {
        return this.setRecipients(Message.RecipientType.CC, ccs);
    }

    /**
     * Sets the blind carbon copy (BCC) recipient email addresses.
     *
     * @param bccs An array of BCC recipient email addresses.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setBccs(final String... bccs) {
        return this.setRecipients(Message.RecipientType.BCC, bccs);
    }

    /**
     * Sets the recipients for a given recipient type (TO, CC, BCC).
     *
     * @param type      The recipient type.
     * @param addresses An array of recipient email addresses.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setRecipients(final Message.RecipientType type, final String... addresses) {
        try {
            super.setRecipients(type, InternalMail.parseAddressFromStrs(addresses, this.mailAccount.getCharset()));
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Sets the 'Reply-To' email addresses.
     *
     * @param reply An array of 'Reply-To' email addresses.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setReply(final String... reply) {
        try {
            super.setReplyTo(InternalMail.parseAddressFromStrs(reply, this.mailAccount.getCharset()));
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Sets the email body content.
     *
     * @param content The email body.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage setContent(final String content, final boolean isHtml) {
        try {
            super.setContent(buildContent(content, this.mailAccount.getCharset(), isHtml));
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Adds an embedded image to the email.
     *
     * @param cid         The Content-ID for embedding the image.
     * @param imageStream The {@link InputStream} of the image.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addImage(final String cid, final InputStream imageStream) {
        return addImage(cid, imageStream, null);
    }

    /**
     * Adds an embedded image from a file to the email.
     *
     * @param cid       The Content-ID for embedding the image.
     * @param imageFile The image file.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addImage(final String cid, final File imageFile) {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(imageFile);
            return addImage(cid, in, FileTypeMap.getDefaultFileTypeMap().getContentType(imageFile));
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Adds an embedded image to the email with a specified content type.
     *
     * @param cid         The Content-ID for embedding the image.
     * @param imageStream The {@link InputStream} of the image.
     * @param contentType The MIME type of the image.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addImage(final String cid, final InputStream imageStream, final String contentType) {
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
     * Adds file attachments to the email.
     *
     * @param files An array of files to be attached.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addFiles(final File... files) {
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
     * Adds attachments to the email, represented by {@link DataSource} objects.
     *
     * @param attachments An array of {@link DataSource} objects to attach.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addAttachments(final DataSource... attachments) {
        if (ArrayKit.isNotEmpty(attachments)) {
            final Charset charset = this.mailAccount.getCharset();
            for (final DataSource attachment : attachments) {
                addBodyPart(buildBodyPart(attachment, charset));
            }
        }
        return this;
    }

    /**
     * Adds a {@link BodyPart} to the email's multipart content.
     *
     * @param bodyPart The {@link BodyPart} to add.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addBodyPart(final BodyPart bodyPart) {
        try {
            this.multipart.addBodyPart(bodyPart);
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Adds a {@link BodyPart} to the email's multipart content at a specific index.
     *
     * @param bodyPart The {@link BodyPart} to add.
     * @param index    The index at which to insert the body part.
     * @return This {@code SMTPMessage} instance for method chaining.
     */
    public SMTPMessage addBodyPart(final BodyPart bodyPart, final int index) {
        try {
            this.multipart.addBodyPart(bodyPart, index);
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Sends the email.
     *
     * @return The unique message-id of the sent email.
     * @throws InternalException if sending the email fails.
     */
    public String send() throws InternalException {
        try {
            return doSend();
        } catch (final MessagingException e) {
            if (e instanceof SendFailedException) {
                final Address[] invalidAddresses = ((SendFailedException) e).getInvalidAddresses();
                final String msg = StringKit.format("Invalid Addresses: {}", ArrayKit.toString(invalidAddresses));
                throw new InternalException(msg, e);
            }
            throw new InternalException(e);
        }
    }

    /**
     * Builds the email's main content part.
     *
     * @param content Content, if {@code null} then use {@link Normal#EMPTY} instead
     * @param charset The character set for encoding.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @return The multipart content.
     * @throws MessagingException if an error occurs while building the content.
     */
    private Multipart buildContent(final String content, final Charset charset, final boolean isHtml)
            throws MessagingException {
        final String charsetStr = null != charset ? charset.name() : MimeUtility.getDefaultJavaCharset();
        // Body
        final MimeBodyPart body = new MimeBodyPart();
        // Content will throw an exception if null, use empty string instead
        body.setContent(
                StringKit.emptyIfNull(content),
                StringKit.format("text/{}; charset={}", isHtml ? "html" : "plain", charsetStr));
        addBodyPart(body, 0);
        return this.multipart;
    }

    /**
     * Executes the send operation.
     *
     * @return The message-id of the sent email.
     * @throws MessagingException if an error occurs during sending.
     */
    private String doSend() throws MessagingException {
        Transport.send(this);
        return getMessageID();
    }

    /**
     * Builds a {@link MimeBodyPart} for an attachment.
     *
     * @param attachment The attachment data source.
     * @param charset    The character set for encoding the filename.
     * @return A {@link MimeBodyPart} for the attachment.
     */
    private MimeBodyPart buildBodyPart(final DataSource attachment, final Charset charset) {
        final MimeBodyPart bodyPart = new MimeBodyPart();

        try {
            bodyPart.setDataHandler(new DataHandler(attachment));
            String nameEncoded = attachment.getName();
            if (this.mailAccount.isEncodefilename()) {
                nameEncoded = InternalMail.encodeText(nameEncoded, charset);
            }
            bodyPart.setFileName(nameEncoded);
            if (StringKit.startWith(attachment.getContentType(), "image/")) {
                bodyPart.setContentID(nameEncoded);
                bodyPart.setDisposition(MimeBodyPart.INLINE);
            }
        } catch (final MessagingException e) {
            throw new InternalException(e);
        }
        return bodyPart;
    }

}
