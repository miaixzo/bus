/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.extra.ftp;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * FTP配置项参数信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FtpConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置服务器语言
     */
    private String serverLanguageCode;

    /**
     * 主机
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * 用户名
     */
    private String user;
    /**
     * 密码
     */
    private String password;
    /**
     * 编码
     */
    private Charset charset;

    /**
     * 连接超时时长，单位毫秒
     */
    private long connectionTimeout;

    /**
     * Socket连接超时时长，单位毫秒
     */
    private long soTimeout;
    /**
     * 设置服务器系统关键词
     */
    private String systemKey;

    /**
     * 构造
     *
     * @param host     主机
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @param charset  编码
     */
    public FtpConfig(String host, int port, String user, String password, Charset charset) {
        this(host, port, user, password, charset, null, null);
    }

    /**
     * 构造
     */
    public FtpConfig() {
    }

    /**
     * 构造
     *
     * @param host               主机
     * @param port               端口
     * @param user               用户名
     * @param password           密码
     * @param charset            编码
     * @param serverLanguageCode 服务器语言
     * @param systemKey          系统关键字
     */
    public FtpConfig(String host, int port, String user, String password, Charset charset, String serverLanguageCode, String systemKey) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.charset = charset;
        this.serverLanguageCode = serverLanguageCode;
        this.systemKey = systemKey;
    }

    public static FtpConfig create() {
        return new FtpConfig();
    }

    public String getHost() {
        return host;
    }

    public FtpConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public FtpConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public FtpConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FtpConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public FtpConfig setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public FtpConfig setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public long getSoTimeout() {
        return soTimeout;
    }

    public FtpConfig setSoTimeout(long soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public String getServerLanguageCode() {
        return serverLanguageCode;
    }

    public FtpConfig setServerLanguageCode(String serverLanguageCode) {
        this.serverLanguageCode = serverLanguageCode;
        return this;
    }

    public String getSystemKey() {
        return systemKey;
    }

    public FtpConfig setSystemKey(String systemKey) {
        this.systemKey = systemKey;
        return this;
    }

}
