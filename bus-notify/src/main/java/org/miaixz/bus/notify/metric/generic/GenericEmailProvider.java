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
package org.miaixz.bus.notify.metric.generic;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.mail.MailAuthenticator;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Material;
import org.miaixz.bus.notify.metric.AbstractProvider;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Generic email service provider implementation.
 *
 * @author Justubborn
 * @since Java 17+
 */
public class GenericEmailProvider extends AbstractProvider<GenericMaterial, Context> {

    /**
     * Constructs a {@code GenericEmailProvider} with the given context.
     *
     * @param properties The context containing configuration information for the provider.
     */
    public GenericEmailProvider(Context properties) {
        super(properties);
    }

    /**
     * Sends an email notification.
     *
     * @param entity The {@link GenericMaterial} containing email details.
     * @return A {@link Message} indicating the result of the email sending operation.
     */
    @Override
    public Message send(GenericMaterial entity) {
        try {
            Transport.send(build(entity));
        } catch (MessagingException e) {
            String message = e.getMessage();
            if (e instanceof SendFailedException) {
                // When the address is invalid, display more detailed invalid address information
                final Address[] invalidAddresses = ((SendFailedException) e).getInvalidAddresses();
                message = StringKit.format("Invalid Addresses: {}", ArrayKit.toString(invalidAddresses));
            }
            Logger.error(message);
        }
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
    }

    /**
     * Parses an address string into multiple {@link InternetAddress} objects. Addresses are separated by space, comma,
     * or semicolon.
     *
     * @param address The address string.
     * @param charset The character set for encoding personal names.
     * @return An array of {@link InternetAddress} objects.
     * @throws InternalException if there is an error parsing the address or encoding the personal name.
     */
    public InternetAddress[] getAddress(String address, Charset charset) {
        InternetAddress[] addresses;
        try {
            addresses = InternetAddress.parse(address);
        } catch (AddressException e) {
            throw new InternalException(e);
        }
        // Encode username
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
     * Converts an array of string email addresses into a list of {@link InternetAddress} objects. Each string address
     * can be a single address or multiple addresses merged into one string.
     *
     * @param address An array of address strings.
     * @param charset The character set for encoding (mainly for Chinese usernames).
     * @return An array of {@link InternetAddress} objects.
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
     * Parses the first address from an address string.
     *
     * @param address The address string.
     * @param charset The character set for encoding.
     * @return The first {@link InternetAddress} object.
     * @throws InternalException if there is an error parsing the address.
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
     * Builds a {@link MimeMessage} from the given {@link GenericMaterial}.
     *
     * @param entity The {@link GenericMaterial} containing email details.
     * @return The constructed {@link MimeMessage}.
     * @throws MessagingException if there is an error in building the message.
     */
    private MimeMessage build(GenericMaterial entity) throws MessagingException {
        entity.defaultIfEmpty();
        final Charset charset = entity.getCharset();
        final MimeMessage msg = new MimeMessage(getSession(entity));
        // Sender
        final String from = entity.getSender();
        if (StringKit.isEmpty(from)) {
            // If the user does not provide the sender, it is automatically obtained from the Session
            msg.setFrom();
        } else {
            msg.setFrom(getFirstAddress(from, charset));
        }
        // Subject
        msg.setSubject(entity.getTitle(), charset.name());
        // Sent date
        msg.setSentDate(new Date());
        // Content and attachments

        final Multipart mainPart = new MimeMultipart();

        // Body
        final BodyPart body = new MimeBodyPart();
        body.setContent(
                entity.getContent(),
                StringKit.format(
                        "text/{}; charset={}",
                        Material.Type.HTML.equals(entity.getType()) ? "html" : "plain",
                        entity.getCharset()));
        mainPart.addBodyPart(body);

        // Attachments
        if (ArrayKit.isNotEmpty(entity.getAttachments())) {
            BodyPart bodyPart;
            for (File file : entity.getAttachments()) {
                DataSource dataSource = new FileDataSource(file);
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(dataSource));
                try {
                    bodyPart.setFileName(
                            MimeUtility.encodeText(dataSource.getName(), entity.getCharset().name(), null));
                } catch (UnsupportedEncodingException e) {
                    // Log or handle the exception appropriately
                }
                mainPart.addBodyPart(bodyPart);
            }
        }

        msg.setContent(mainPart);

        // To recipients
        msg.setRecipients(
                MimeMessage.RecipientType.TO,
                getAddress(StringKit.splitToArray(entity.getReceive(), Symbol.COMMA), charset));
        // CC recipients
        if (StringKit.isNotEmpty(entity.getCcs())) {
            msg.setRecipients(
                    MimeMessage.RecipientType.CC,
                    getAddress(StringKit.splitToArray(entity.getCcs(), Symbol.COMMA), charset));
        }
        // BCC recipients
        if (StringKit.isNotEmpty(entity.getBccs())) {
            msg.setRecipients(
                    MimeMessage.RecipientType.BCC,
                    getAddress(StringKit.splitToArray(entity.getBccs(), Symbol.COMMA), charset));
        }
        return msg;
    }

    /**
     * Retrieves a mail session. If a global singleton session is used, only one email account is allowed globally;
     * otherwise, a new session will be created for each email sent.
     *
     * @param template The {@link GenericMaterial} containing session configuration.
     * @return The mail {@link Session}.
     */
    private Session getSession(GenericMaterial template) {
        Authenticator authenticator = null;
        if (template.getAuth()) {
            authenticator = MailAuthenticator.of(template.getUser(), template.getPass());
        }

        return template.isUseGlobalSession() ? Session.getDefaultInstance(template.getSmtpProps(), authenticator)
                : Session.getInstance(template.getSmtpProps(), authenticator);
    }

}
