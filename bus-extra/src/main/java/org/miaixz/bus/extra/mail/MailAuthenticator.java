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

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * 用户名密码授权
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class MailAuthenticator extends Authenticator {

    private final PasswordAuthentication auth;

    /**
     * 创建账号密码形式的{@link java.net.Authenticator} 实现。
     *
     * @param user 用户名
     * @param pass 密码
     * @return PassAuth
     */
    public static MailAuthenticator of(final String user, final String pass) {
        return new MailAuthenticator(user, pass);
    }

    /**
     * 构造
     *
     * @param mailAccount 邮箱账号信息
     */
    public MailAuthenticator(final MailAccount mailAccount) {
        this.auth = new PasswordAuthentication(mailAccount.getUser(), String.valueOf(mailAccount.getPass()));
    }

    /**
     * 构造
     *
     * @param userName 用户名
     * @param password 密码
     */
    public MailAuthenticator(final String userName, final String password) {
        this.auth = new PasswordAuthentication(userName, password);
    }

    /**
     * 构造
     *
     * @param auth 密码授权信息
     */
    public MailAuthenticator(final PasswordAuthentication auth) {
        this.auth = auth;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return this.auth;
    }

}
