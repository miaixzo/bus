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

import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * 全局邮件帐户，依赖于邮件配置文件{@link MailAccount#MAIL_SETTING_PATHS}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum GlobalMailAccount {
    /**
     * 单例
     */
    INSTANCE;

    private static final String SPLIT_LONG_PARAMS = "mail.mime.splitlongparameters";
    private static final String CHARSET = "mail.mime.charset";

    static {
        System.setProperty(SPLIT_LONG_PARAMS, "false");
        System.setProperty(CHARSET, INSTANCE.mailAccount.getCharset().name());
    }

    private final MailAccount mailAccount;

    /**
     * 构造
     */
    GlobalMailAccount() {
        mailAccount = createDefaultAccount();
    }

    /**
     * 获得邮件帐户
     *
     * @return 邮件帐户
     */
    public MailAccount getAccount() {
        return this.mailAccount;
    }

    /**
     * 设置对于超长参数是否切分为多份，默认为false（国内邮箱附件不支持切分的附件名） 注意此项为全局设置，此项会调用
     * 
     * <pre>
     * System.setProperty("mail.mime.splitlongparameters", true)
     * </pre>
     *
     * @param splitLongParams 对于超长参数是否切分为多份
     */
    public void setSplitLongParams(final boolean splitLongParams) {
        System.setProperty(SPLIT_LONG_PARAMS, String.valueOf(splitLongParams));
    }

    /**
     * 设置全局默认编码
     *
     * @param charset 编码
     */
    public void setCharset(final Charset charset) {
        System.setProperty(CHARSET, charset.name());
    }

    /**
     * 创建默认帐户
     *
     * @return {@link MailAccount}
     */
    private MailAccount createDefaultAccount() {
        for (final String mailSettingPath : MailAccount.MAIL_SETTING_PATHS) {
            try {
                return new MailAccount(mailSettingPath);
            } catch (final InternalException ignore) {
                // ignore
            }
        }
        return null;
    }

}
