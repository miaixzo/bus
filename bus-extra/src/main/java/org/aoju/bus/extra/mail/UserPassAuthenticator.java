package org.aoju.bus.extra.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 用户名密码验证器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class UserPassAuthenticator extends Authenticator {

    private String user;
    private String pass;

    /**
     * 构造
     *
     * @param user 用户名
     * @param pass 密码
     */
    public UserPassAuthenticator(String user, String pass) {
        super();
        this.user = user;
        this.pass = pass;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.user, this.pass);
    }

}
