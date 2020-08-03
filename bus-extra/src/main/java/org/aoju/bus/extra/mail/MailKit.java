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
package org.aoju.bus.extra.mail;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 邮件工具类,基于javax.mail封装
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
public class MailKit {

    /**
     * 使用配置文件中设置的账户发送文本邮件,发送给单个或多个收件人
     * 多个收件人可以使用逗号“,”分隔,也可以通过分号“;”分隔
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     */
    public static void sendText(String to, String subject, String content, File... files) {
        send(to, subject, content, false, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件,发送给单个或多个收件人
     * 多个收件人可以使用逗号“,”分隔,也可以通过分号“;”分隔
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     */
    public static void sendHtml(String to, String subject, String content, File... files) {
        send(to, subject, content, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件,发送单个或多个收件人
     * 多个收件人可以使用逗号“,”分隔,也可以通过分号“;”分隔
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     */
    public static void send(String to, String subject, String content, boolean isHtml, File... files) {
        send(splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件,发送单个或多个收件人
     * 多个收件人、抄送人、密送人可以使用逗号“,”分隔,也可以通过分号“;”分隔
     *
     * @param to      收件人,可以使用逗号“,”分隔,也可以通过分号“;”分隔
     * @param cc      抄送人,可以使用逗号“,”分隔,也可以通过分号“;”分隔
     * @param bcc     密送人,可以使用逗号“,”分隔,也可以通过分号“;”分隔
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     */
    public static void send(String to, String cc, String bcc, String subject, String content, boolean isHtml, File... files) {
        send(splitAddress(to), splitAddress(cc), splitAddress(bcc), subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送文本邮件,发送给多人
     *
     * @param tos     收件人列表
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     */
    public static void sendText(Collection<String> tos, String subject, String content, File... files) {
        send(tos, subject, content, false, files);
    }

    /**
     * 使用配置文件中设置的账户发送HTML邮件,发送给多人
     *
     * @param tos     收件人列表
     * @param subject 标题
     * @param content 正文
     * @param files   附件列表
     */
    public static void sendHtml(Collection<String> tos, String subject, String content, File... files) {
        send(tos, subject, content, true, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件,发送给多人
     *
     * @param tos     收件人列表
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     */
    public static void send(Collection<String> tos, String subject, String content, boolean isHtml, File... files) {
        send(GlobalMailAccount.INSTANCE.getAccount(), tos, subject, content, isHtml, files);
    }

    /**
     * 使用配置文件中设置的账户发送邮件,发送给多人
     *
     * @param tos     收件人列表
     * @param ccs     抄送人列表,可以为null或空
     * @param bccs    密送人列表,可以为null或空
     * @param subject 标题
     * @param content 正文
     * @param isHtml  是否为HTML
     * @param files   附件列表
     */
    public static void send(Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content, boolean isHtml, File... files) {
        send(GlobalMailAccount.INSTANCE.getAccount(), tos, ccs, bccs, subject, content, isHtml, files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件认证对象
     * @param to          收件人,多个收件人逗号或者分号隔开
     * @param subject     标题
     * @param content     正文
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     */
    public static void send(MailAccount mailAccount, String to, String subject, String content, boolean isHtml, File... files) {
        send(mailAccount, splitAddress(to), subject, content, isHtml, files);
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件认证对象
     * @param tos         收件人列表
     * @param subject     标题
     * @param content     正文
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     */
    public static void send(MailAccount mailAccount, Collection<String> tos, String subject, String content, boolean isHtml, File... files) {
        Mail.create(mailAccount)
                .setTos(tos.toArray(new String[tos.size()]))
                .setTitle(subject)
                .setContent(content)
                .setHtml(isHtml)
                .setFiles(files)
                .send();
    }

    /**
     * 发送邮件给多人
     *
     * @param mailAccount 邮件认证对象
     * @param tos         收件人列表
     * @param ccs         抄送人列表,可以为null或空
     * @param bccs        密送人列表,可以为null或空
     * @param subject     标题
     * @param content     正文
     * @param isHtml      是否为HTML格式
     * @param files       附件列表
     */
    public static void send(MailAccount mailAccount, Collection<String> tos, Collection<String> ccs, Collection<String> bccs, String subject, String content, boolean isHtml, File... files) {
        final Mail mail = Mail.create(mailAccount);

        //可选抄送人
        if (CollKit.isNotEmpty(ccs)) {
            mail.setCcs(ccs.toArray(new String[ccs.size()]));
        }
        //可选密送人
        if (CollKit.isNotEmpty(bccs)) {
            mail.setBccs(bccs.toArray(new String[bccs.size()]));
        }

        mail.setTos(tos.toArray(new String[tos.size()]));
        mail.setTitle(subject);
        mail.setContent(content);
        mail.setHtml(isHtml);
        mail.setFiles(files);

        mail.send();
    }

    /**
     * 将多个联系人转为列表,分隔符为逗号或者分号
     *
     * @param addresses 多个联系人,如果为空返回null
     * @return 联系人列表
     */
    private static List<String> splitAddress(String addresses) {
        if (StringKit.isBlank(addresses)) {
            return null;
        }
        List<String> result;
        if (StringKit.contains(addresses, Symbol.C_COMMA)) {
            result = StringKit.splitTrim(addresses, Symbol.C_COMMA);
        } else if (StringKit.contains(addresses, Symbol.C_SEMICOLON)) {
            result = StringKit.splitTrim(addresses, Symbol.C_SEMICOLON);
        } else {
            result = CollKit.newArrayList(addresses);
        }
        return result;
    }

}
