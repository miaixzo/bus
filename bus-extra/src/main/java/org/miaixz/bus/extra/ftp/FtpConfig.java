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
package org.miaixz.bus.extra.ftp;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.miaixz.bus.extra.ssh.Connector;

/**
 * FTP配置项，提供FTP各种参数信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FtpConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852277795963L;

    private Connector connector;
    /**
     * 编码
     */
    private Charset charset;
    /**
     * Socket连接超时时长，单位毫秒
     */
    private long soTimeout;
    /**
     * 服务器语言
     */
    private String serverLanguageCode;
    /**
     * 服务器系统关键词
     */
    private String systemKey;

    /**
     * 构造
     */
    public FtpConfig() {
    }

    /**
     * 构造
     *
     * @param connector 连接信息，包括host、port、user、password等信息
     * @param charset   编码
     */
    public FtpConfig(final Connector connector, final Charset charset) {
        this(connector, charset, null, null);
    }

    /**
     * 构造
     *
     * @param connector          连接信息，包括host、port、user、password等信息
     * @param charset            编码
     * @param serverLanguageCode 服务器语言
     * @param systemKey          系统关键字
     */
    public FtpConfig(final Connector connector, final Charset charset, final String serverLanguageCode,
            final String systemKey) {
        this.connector = connector;
        this.charset = charset;
        this.serverLanguageCode = serverLanguageCode;
        this.systemKey = systemKey;
    }

    /**
     * 创建默认配置
     *
     * @return FtpConfig
     */
    public static FtpConfig of() {
        return new FtpConfig();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * 设置连接信息
     *
     * @param connector 连接信息
     * @return this
     */
    public FtpConfig setConnector(final Connector connector) {
        this.connector = connector;
        return this;
    }

    /**
     * 设置超时，注意此方法会调用{@link Connector#setTimeout(long)} 此方法需在{@link #setConnector(Connector)}后调用，否则会创建空的Connector
     *
     * @param timeout 链接超时
     * @return this
     */
    public FtpConfig setConnectionTimeout(final long timeout) {
        if (null == connector) {
            connector = Connector.of();
        }
        connector.setTimeout(timeout);
        return this;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     * @return this
     */
    public FtpConfig setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 获取读取数据超时时间
     *
     * @return 读取数据超时时间
     */
    public long getSoTimeout() {
        return soTimeout;
    }

    /**
     * 设置读取数据超时时间
     *
     * @param soTimeout 读取数据超时时间
     * @return this
     */
    public FtpConfig setSoTimeout(final long soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    /**
     * 获取服务器语言
     *
     * @return 服务器语言
     */
    public String getServerLanguageCode() {
        return serverLanguageCode;
    }

    /**
     * 设置服务器语言
     *
     * @param serverLanguageCode 服务器语言
     * @return this
     */
    public FtpConfig setServerLanguageCode(final String serverLanguageCode) {
        this.serverLanguageCode = serverLanguageCode;
        return this;
    }

    /**
     * 获取服务器系统关键词
     *
     * @return 服务器系统关键词
     */
    public String getSystemKey() {
        return systemKey;
    }

    /**
     * 设置服务器系统关键词
     *
     * @param systemKey 服务器系统关键词
     * @return this
     */
    public FtpConfig setSystemKey(final String systemKey) {
        this.systemKey = systemKey;
        return this;
    }

}
