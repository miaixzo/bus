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
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

import jakarta.mail.Authenticator;
import jakarta.mail.Session;

/**
 * A utility class for sending emails, built on top of the Jakarta Mail API. This class provides a set of static methods
 * for conveniently sending plain text and HTML emails.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MailKit {

    /**
     * Sends a plain text email to one or more recipients using the default account from the configuration. Multiple
     * recipients can be separated by commas (,) or semicolons (;).
     *
     * @param to      The recipient(s).
     * @param subject The email subject.
     * @param content The email body.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String sendText(final String to, final String subject, final String content, final File... files) {
        return send(to, subject, content, false, files);
    }

    /**
     * Sends an HTML email to one or more recipients using the default account from the configuration. Multiple
     * recipients can be separated by commas (,) or semicolons (;).
     *
     * @param to      The recipient(s).
     * @param subject The email subject.
     * @param content The HTML email body.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String sendHtml(final String to, final String subject, final String content, final File... files) {
        return send(to, subject, content, true, files);
    }

    /**
     * Sends an email to one or more recipients using the default account from the configuration. Multiple recipients
     * can be separated by commas (,) or semicolons (;).
     *
     * @param to      The recipient(s).
     * @param subject The email subject.
     * @param content The email body.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final String to,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * Sends an email to multiple recipients, including CC and BCC, using the default account from the configuration.
     * Recipients, CCs, and BCCs can be separated by commas (,) or semicolons (;).
     *
     * @param to      The recipient(s).
     * @param cc      The CC recipient(s).
     * @param bcc     The BCC recipient(s).
     * @param subject The email subject.
     * @param content The email body.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final String to,
            final String cc,
            final String bcc,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, isHtml, files);
    }

    /**
     * Sends a plain text email to a collection of recipients using the default account from the configuration.
     *
     * @param tos     A collection of recipient email addresses.
     * @param subject The email subject.
     * @param content The email body.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String sendText(
            final Collection<String> tos,
            final String subject,
            final String content,
            final File... files) {
        return send(tos, subject, content, false, files);
    }

    /**
     * Sends an HTML email to a collection of recipients using the default account from the configuration.
     *
     * @param tos     A collection of recipient email addresses.
     * @param subject The email subject.
     * @param content The HTML email body.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String sendHtml(
            final Collection<String> tos,
            final String subject,
            final String content,
            final File... files) {
        return send(tos, subject, content, true, files);
    }

    /**
     * Sends an email to a collection of recipients using the default account from the configuration.
     *
     * @param tos     A collection of recipient email addresses.
     * @param subject The email subject.
     * @param content The email body.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final Collection<String> tos,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(tos, null, null, subject, content, isHtml, files);
    }

    /**
     * Sends an email to multiple recipients, including CC and BCC, using the default account from the configuration.
     *
     * @param tos     A collection of recipient email addresses.
     * @param ccs     A collection of CC recipient email addresses (can be null or empty).
     * @param bccs    A collection of BCC recipient email addresses (can be null or empty).
     * @param subject The email subject.
     * @param content The email body.
     * @param isHtml  {@code true} if the content is HTML, {@code false} for plain text.
     * @param files   An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final Collection<String> tos,
            final Collection<String> ccs,
            final Collection<String> bccs,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(
                GlobalMailAccount.INSTANCE.getAccount(),
                true,
                tos,
                ccs,
                bccs,
                subject,
                content,
                null,
                isHtml,
                files);
    }

    /**
     * Sends an email to one or more recipients using a specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @param to          The recipient(s), separated by commas or semicolons.
     * @param subject     The email subject.
     * @param content     The email body.
     * @param isHtml      {@code true} if the content is HTML, {@code false} for plain text.
     * @param files       An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final MailAccount mailAccount,
            final String to,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(mailAccount, splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * Sends an email to a collection of recipients using a specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @param tos         A collection of recipient email addresses.
     * @param subject     The email subject.
     * @param content     The email body.
     * @param isHtml      {@code true} if the content is HTML, {@code false} for plain text.
     * @param files       An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final MailAccount mailAccount,
            final Collection<String> tos,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(mailAccount, tos, null, null, subject, content, isHtml, files);
    }

    /**
     * Sends an email to multiple recipients, including CC and BCC, using a specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @param tos         A collection of recipient email addresses.
     * @param ccs         A collection of CC recipient email addresses (can be null or empty).
     * @param bccs        A collection of BCC recipient email addresses (can be null or empty).
     * @param subject     The email subject.
     * @param content     The email body.
     * @param isHtml      {@code true} if the content is HTML, {@code false} for plain text.
     * @param files       An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final MailAccount mailAccount,
            final Collection<String> tos,
            final Collection<String> ccs,
            final Collection<String> bccs,
            final String subject,
            final String content,
            final boolean isHtml,
            final File... files) {
        return send(mailAccount, false, tos, ccs, bccs, subject, content, null, isHtml, files);
    }

    /**
     * Sends an HTML email with embedded images to one or more recipients using the default account.
     *
     * @param to       The recipient(s), separated by commas or semicolons.
     * @param subject  The email subject.
     * @param content  The HTML email body.
     * @param imageMap A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param files    An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String sendHtml(
            final String to,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final File... files) {
        return send(to, subject, content, imageMap, true, files);
    }

    /**
     * Sends an email with embedded images to one or more recipients using the default account.
     *
     * @param to       The recipient(s), separated by commas or semicolons.
     * @param subject  The email subject.
     * @param content  The email body.
     * @param imageMap A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml   {@code true} if the content is HTML, {@code false} for plain text.
     * @param files    An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final String to,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(splitAddress(to), subject, content, imageMap, isHtml, files);
    }

    /**
     * Sends an email with embedded images to multiple recipients, including CC and BCC, using the default account.
     *
     * @param to       The recipient(s), separated by commas or semicolons.
     * @param cc       The CC recipient(s), separated by commas or semicolons.
     * @param bcc      The BCC recipient(s), separated by commas or semicolons.
     * @param subject  The email subject.
     * @param content  The email body.
     * @param imageMap A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml   {@code true} if the content is HTML, {@code false} for plain text.
     * @param files    An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final String to,
            final String cc,
            final String bcc,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, imageMap, isHtml, files);
    }

    /**
     * Sends an HTML email with embedded images to a collection of recipients using the default account.
     *
     * @param tos      A collection of recipient email addresses.
     * @param subject  The email subject.
     * @param content  The HTML email body.
     * @param imageMap A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param files    An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String sendHtml(
            final Collection<String> tos,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final File... files) {
        return send(tos, subject, content, imageMap, true, files);
    }

    /**
     * Sends an email with embedded images to a collection of recipients using the default account.
     *
     * @param tos      A collection of recipient email addresses.
     * @param subject  The email subject.
     * @param content  The email body.
     * @param imageMap A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml   {@code true} if the content is HTML, {@code false} for plain text.
     * @param files    An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final Collection<String> tos,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(tos, null, null, subject, content, imageMap, isHtml, files);
    }

    /**
     * Sends an email with embedded images to multiple recipients, including CC and BCC, using the default account.
     *
     * @param tos      A collection of recipient email addresses.
     * @param ccs      A collection of CC recipient email addresses (can be null or empty).
     * @param bccs     A collection of BCC recipient email addresses (can be null or empty).
     * @param subject  The email subject.
     * @param content  The email body.
     * @param imageMap A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml   {@code true} if the content is HTML, {@code false} for plain text.
     * @param files    An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final Collection<String> tos,
            final Collection<String> ccs,
            final Collection<String> bccs,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(
                GlobalMailAccount.INSTANCE.getAccount(),
                true,
                tos,
                ccs,
                bccs,
                subject,
                content,
                imageMap,
                isHtml,
                files);
    }

    /**
     * Sends an email with embedded images to one or more recipients using a specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @param to          The recipient(s), separated by commas or semicolons.
     * @param subject     The email subject.
     * @param content     The email body.
     * @param imageMap    A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml      {@code true} if the content is HTML, {@code false} for plain text.
     * @param files       An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final MailAccount mailAccount,
            final String to,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(mailAccount, splitAddress(to), subject, content, imageMap, isHtml, files);
    }

    /**
     * Sends an email with embedded images to a collection of recipients using a specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @param tos         A collection of recipient email addresses.
     * @param subject     The email subject.
     * @param content     The email body.
     * @param imageMap    A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml      {@code true} if the content is HTML, {@code false} for plain text.
     * @param files       An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final MailAccount mailAccount,
            final Collection<String> tos,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(mailAccount, tos, null, null, subject, content, imageMap, isHtml, files);
    }

    /**
     * Sends an email with embedded images to multiple recipients, including CC and BCC, using a specified mail account.
     *
     * @param mailAccount The mail account configuration.
     * @param tos         A collection of recipient email addresses.
     * @param ccs         A collection of CC recipient email addresses (can be null or empty).
     * @param bccs        A collection of BCC recipient email addresses (can be null or empty).
     * @param subject     The email subject.
     * @param content     The email body.
     * @param imageMap    A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml      {@code true} if the content is HTML, {@code false} for plain text.
     * @param files       An array of files to be attached.
     * @return The message-id of the sent email.
     */
    public static String send(
            final MailAccount mailAccount,
            final Collection<String> tos,
            final Collection<String> ccs,
            final Collection<String> bccs,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        return send(mailAccount, false, tos, ccs, bccs, subject, content, imageMap, isHtml, files);
    }

    /**
     * Retrieves a mail {@link Session} based on the provided mail account configuration.
     *
     * @param mailAccount The mail account configuration.
     * @param isSingleton If {@code true}, returns a globally shared session; otherwise, creates a new session.
     * @return A {@link Session} instance.
     */
    public static Session getSession(final MailAccount mailAccount, final boolean isSingleton) {
        Authenticator authenticator = null;
        if (mailAccount.isAuth()) {
            authenticator = new MailAuthenticator(mailAccount);
        }

        return isSingleton ? Session.getDefaultInstance(mailAccount.getSmtpProps(), authenticator)
                : Session.getInstance(mailAccount.getSmtpProps(), authenticator);
    }

    /**
     * Sends an email to multiple recipients with full options.
     *
     * @param mailAccount      The mail account configuration.
     * @param useGlobalSession If {@code true}, uses a globally shared session.
     * @param tos              A collection of recipient email addresses.
     * @param ccs              A collection of CC recipient email addresses.
     * @param bccs             A collection of BCC recipient email addresses.
     * @param subject          The email subject.
     * @param content          The email body.
     * @param imageMap         A map of Content-IDs to {@link InputStream}s for embedded images.
     * @param isHtml           {@code true} if the content is HTML, {@code false} for plain text.
     * @param files            An array of files to be attached.
     * @return The message-id of the sent email.
     */
    private static String send(
            final MailAccount mailAccount,
            final boolean useGlobalSession,
            final Collection<String> tos,
            final Collection<String> ccs,
            final Collection<String> bccs,
            final String subject,
            final String content,
            final Map<String, InputStream> imageMap,
            final boolean isHtml,
            final File... files) {
        final Mail mail = Mail.of(mailAccount).setUseGlobalSession(useGlobalSession);

        if (CollKit.isNotEmpty(ccs)) {
            mail.setCcs(ccs.toArray(new String[0]));
        }
        if (CollKit.isNotEmpty(bccs)) {
            mail.setBccs(bccs.toArray(new String[0]));
        }

        mail.setTos(tos.toArray(new String[0]));
        mail.setTitle(subject);
        mail.setContent(content);
        mail.setHtml(isHtml);
        mail.addFiles(files);

        if (MapKit.isNotEmpty(imageMap)) {
            for (final Entry<String, InputStream> entry : imageMap.entrySet()) {
                mail.addImage(entry.getKey(), entry.getValue());
                IoKit.closeQuietly(entry.getValue());
            }
        }

        return mail.send();
    }

    /**
     * Splits a string of email addresses into a list. Delimiters can be commas (,) or semicolons (;).
     *
     * @param addresses A string of email addresses.
     * @return A list of email addresses, or null if the input is blank.
     */
    private static List<String> splitAddress(final String addresses) {
        if (StringKit.isBlank(addresses)) {
            return null;
        }

        final List<String> result;
        if (StringKit.contains(addresses, Symbol.C_COMMA)) {
            result = CharsBacker.splitTrim(addresses, Symbol.COMMA);
        } else if (StringKit.contains(addresses, Symbol.C_SEMICOLON)) {
            result = CharsBacker.splitTrim(addresses, Symbol.SEMICOLON);
        } else {
            result = ListKit.of(addresses);
        }
        return result;
    }

}
