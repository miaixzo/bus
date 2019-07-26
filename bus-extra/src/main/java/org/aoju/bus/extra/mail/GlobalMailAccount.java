package org.aoju.bus.extra.mail;

import org.aoju.bus.core.lang.exception.CommonException;

/**
 * 全局邮件帐户，依赖于邮件配置文件{@link MailAccount#MAIL_SETTING_PATH}或{@link MailAccount#MAIL_SETTING_PATH2}
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum GlobalMailAccount {

    INSTANCE;

    private final MailAccount mailAccount;

    /**
     * 构造
     */
    private GlobalMailAccount() {
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
     * 创建默认帐户
     *
     * @return MailAccount
     */
    private MailAccount createDefaultAccount() {
        MailAccount mailAccount;
        try {
            mailAccount = new MailAccount(MailAccount.MAIL_SETTING_PATH);
        } catch (CommonException e) {
            mailAccount = new MailAccount(MailAccount.MAIL_SETTING_PATH2);
        }
        return mailAccount;
    }
}
