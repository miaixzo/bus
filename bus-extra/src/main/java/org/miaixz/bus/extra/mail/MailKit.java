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
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

/**
 * 邮件工具类，基于jakarta.mail封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MailKit {
    /**
     * 使用配置文件中设置的账户发送文本邮件，发送给单个或多个收件人 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     * @return message-data
     */
    public static String sendText(final String to, final String subject, final String content, final File... files) {
        return send(to, subject, content, false, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给单个或多个收件人 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     * @return message-data
     */
    public static String sendHtml(final String to, final String subject, final String content, final File... files) {
        return send(to, subject, content, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     * @return message-data
     */
    public static String send(final String to, final String subject, final String content, final boolean isHtml,
            final File... files) {
        return send(splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人 多个收件人、抄送人、密送人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to      收件人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param cc      抄送人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param bcc     密送人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     * @return message-data
     */
    public static String send(final String to, final String cc, final String bcc, final String subject,
            final String content, final boolean isHtml, final File... files) {
        return send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送文本邮件，发送给多人
     *
     * @param tos     收件人列表
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     * @return message-data
     */
    public static String sendText(final Collection<String> tos, final String subject, final String content,
            final File... files) {
        return send(tos, subject, content, false, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给多人
     *
     * @param tos     收件人列表
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     * @return message-data
     */
    public static String sendHtml(final Collection<String> tos, final String subject, final String content,
            final File... files) {
        return send(tos, subject, content, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos     收件人列表
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     * @return message-data
     */
    public static String send(final Collection<String> tos, final String subject, final String content,
            final boolean isHtml, final File... files) {
        return send(tos, null, null, subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos     收件人列表
     * @param ccs     抄送人列表，可以为null或空
     * @param bccs    密送人列表，可以为null或空
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     * @return message-data
     */
    public static String send(final Collection<String> tos, final Collection<String> ccs, final Collection<String> bccs,
            final String subject, final String content, final boolean isHtml, final File... files) {
        return send(GlobalMailAccount.INSTANCE.getAccount(), true, tos, ccs, bccs, subject, content, null, isHtml,
                files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件认证对象
     * @param to          收件人，多个收件人逗号或者分号隔开
     * @param subject     标题
     * @param content     正文
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     * @return message-data
     */
    public static String send(final MailAccount mailAccount, final String to, final String subject,
            final String content, final boolean isHtml, final File... files) {
        return send(mailAccount, splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件帐户信息
     * @param tos         收件人列表
     * @param subject     标题
     * @param content     正文
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     * @return message-data
     */
    public static String send(final MailAccount mailAccount, final Collection<String> tos, final String subject,
            final String content, final boolean isHtml, final File... files) {
        return send(mailAccount, tos, null, null, subject, content, isHtml, files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件帐户信息
     * @param tos         收件人列表
     * @param ccs         抄送人列表，可以为null或空
     * @param bccs        密送人列表，可以为null或空
     * @param subject     标题
     * @param content     正文
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     * @return message-data
     */
    public static String send(final MailAccount mailAccount, final Collection<String> tos, final Collection<String> ccs,
            final Collection<String> bccs, final String subject, final String content, final boolean isHtml,
            final File... files) {
        return send(mailAccount, false, tos, ccs, bccs, subject, content, null, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给单个或多个收件人 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to       收件人
     * @param subject  标题
     * @param content  正文
     * @param imageMap 图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param files    附件列表
     * @return message-data
     */
    public static String sendHtml(final String to, final String subject, final String content,
            final Map<String, InputStream> imageMap, final File... files) {
        return send(to, subject, content, imageMap, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人 多个收件人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to       收件人
     * @param subject  标题
     * @param content  正文
     * @param imageMap 图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml   是否为HTML
     * @param files    附件列表
     * @return message-data
     */
    public static String send(final String to, final String subject, final String content,
            final Map<String, InputStream> imageMap, final boolean isHtml, final File... files) {
        return send(splitAddress(to), subject, content, imageMap, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送单个或多个收件人 多个收件人、抄送人、密送人可以使用逗号“,”分隔，也可以通过分号“;”分隔
     *
     * @param to       收件人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param cc       抄送人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param bcc      密送人，可以使用逗号“,”分隔，也可以通过分号“;”分隔
     * @param subject  标题
     * @param content  正文
     * @param imageMap 图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml   是否为HTML
     * @param files    附件列表
     * @return message-data
     */
    public static String send(final String to, final String cc, final String bcc, final String subject,
            final String content, final Map<String, InputStream> imageMap, final boolean isHtml, final File... files) {
        return send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, imageMap, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件，发送给多人
     *
     * @param tos      收件人列表
     * @param subject  标题
     * @param content  正文
     * @param imageMap 图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param files    附件列表
     * @return message-data
     */
    public static String sendHtml(final Collection<String> tos, final String subject, final String content,
            final Map<String, InputStream> imageMap, final File... files) {
        return send(tos, subject, content, imageMap, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos      收件人列表
     * @param subject  标题
     * @param content  正文
     * @param imageMap 图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml   是否为HTML
     * @param files    附件列表
     * @return message-data
     */
    public static String send(final Collection<String> tos, final String subject, final String content,
            final Map<String, InputStream> imageMap, final boolean isHtml, final File... files) {
        return send(tos, null, null, subject, content, imageMap, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件，发送给多人
     *
     * @param tos      收件人列表
     * @param ccs      抄送人列表，可以为null或空
     * @param bccs     密送人列表，可以为null或空
     * @param subject  标题
     * @param content  正文
     * @param imageMap 图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml   是否为HTML
     * @param files    附件列表
     * @return message-data
     */
    public static String send(final Collection<String> tos, final Collection<String> ccs, final Collection<String> bccs,
            final String subject, final String content, final Map<String, InputStream> imageMap, final boolean isHtml,
            final File... files) {
        return send(GlobalMailAccount.INSTANCE.getAccount(), true, tos, ccs, bccs, subject, content, imageMap, isHtml,
                files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件认证对象
     * @param to          收件人，多个收件人逗号或者分号隔开
     * @param subject     标题
     * @param content     正文
     * @param imageMap    图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     * @return message-data
     */
    public static String send(final MailAccount mailAccount, final String to, final String subject,
            final String content, final Map<String, InputStream> imageMap, final boolean isHtml, final File... files) {
        return send(mailAccount, splitAddress(to), subject, content, imageMap, isHtml, files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件帐户信息
     * @param tos         收件人列表
     * @param subject     标题
     * @param content     正文
     * @param imageMap    图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     * @return message-data
     */
    public static String send(final MailAccount mailAccount, final Collection<String> tos, final String subject,
            final String content, final Map<String, InputStream> imageMap, final boolean isHtml, final File... files) {
        return send(mailAccount, tos, null, null, subject, content, imageMap, isHtml, files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件帐户信息
     * @param tos         收件人列表
     * @param ccs         抄送人列表，可以为null或空
     * @param bccs        密送人列表，可以为null或空
     * @param subject     标题
     * @param content     正文
     * @param imageMap    图片与占位符，占位符格式为cid:$IMAGE_PLACEHOLDER
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     * @return message-data
     */
    public static String send(final MailAccount mailAccount, final Collection<String> tos, final Collection<String> ccs,
            final Collection<String> bccs, final String subject, final String content,
            final Map<String, InputStream> imageMap, final boolean isHtml, final File... files) {
        return send(mailAccount, false, tos, ccs, bccs, subject, content, imageMap, isHtml, files);
    }

    /**
     * 根据配置文件，获取邮件客户端会话
     *
     * @param mailAccount 邮件账户配置
     * @param isSingleton 是否单例（全局共享会话）
     * @return {@link Session}
     */
    public static Session getSession(final MailAccount mailAccount, final boolean isSingleton) {
        Authenticator authenticator = null;
        if (mailAccount.isAuth()) {
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailAccount.getUser(), String.valueOf(mailAccount.getPass()));
                }
            };
        }

        return isSingleton ? Session.getDefaultInstance(mailAccount.getSmtpProps(), authenticator) //
                : Session.getInstance(mailAccount.getSmtpProps(), authenticator);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount      邮件帐户信息
     * @param useGlobalSession 是否全局共享Session
     * @param tos              收件人列表
     * @param ccs              抄送人列表，可以为null或空
     * @param bccs             密送人列表，可以为null或空
     * @param subject          标题
     * @param content          正文
     * @param imageMap         图片与占位符，占位符格式为cid:${cid}
     * @param isHtml           是否为HTML格式
     * @param files            附件列表
     * @return message-data
     */
    private static String send(final MailAccount mailAccount, final boolean useGlobalSession,
            final Collection<String> tos, final Collection<String> ccs, final Collection<String> bccs,
            final String subject, final String content, final Map<String, InputStream> imageMap, final boolean isHtml,
            final File... files) {
        final Mail mail = Mail.of(mailAccount).setUseGlobalSession(useGlobalSession);

        // 可选抄送人
        if (CollKit.isNotEmpty(ccs)) {
            mail.setCcs(ccs.toArray(new String[0]));
        }
        // 可选密送人
        if (CollKit.isNotEmpty(bccs)) {
            mail.setBccs(bccs.toArray(new String[0]));
        }

        mail.setTos(tos.toArray(new String[0]));
        mail.setTitle(subject);
        mail.setContent(content);
        mail.setHtml(isHtml);
        mail.setFiles(files);

        // 图片
        if (MapKit.isNotEmpty(imageMap)) {
            for (final Entry<String, InputStream> entry : imageMap.entrySet()) {
                mail.addImage(entry.getKey(), entry.getValue());
                // 关闭流
                IoKit.closeQuietly(entry.getValue());
            }
        }

        return mail.send();
    }

    /**
     * 将多个联系人转为列表，分隔符为逗号或者分号
     *
     * @param addresses 多个联系人，如果为空返回null
     * @return 联系人列表
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
